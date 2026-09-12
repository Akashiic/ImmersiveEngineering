package blusunrize.immersiveengineering.common.fluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent, allocation-light age storage for wet concrete cells. */
public class ConcreteAgeData extends WorldSavedData
{
	public static final String DATA_NAME = "IEConcreteAge_v1";
	private static final float LOAD_FACTOR = .625f;

	private long[] keys = new long[64];
	private byte[] ages = new byte[64];
	private boolean[] used = new boolean[64];
	private int size;
	private int threshold = (int)(64*LOAD_FACTOR);

	public ConcreteAgeData()
	{
		this(DATA_NAME);
	}

	public ConcreteAgeData(String name)
	{
		super(name);
	}

	public static ConcreteAgeData get(World world)
	{
		if(world==null||world.isRemote)
			return null;
		ConcreteAgeData data = (ConcreteAgeData)world.perWorldStorage.loadData(ConcreteAgeData.class, DATA_NAME);
		if(data==null)
		{
			data = new ConcreteAgeData(DATA_NAME);
			world.perWorldStorage.setData(DATA_NAME, data);
		}
		return data;
	}

	public int getAge(int x, int y, int z)
	{
		int slot = findSlot(pack(x, y, z));
		return slot>=0?ages[slot]&255:0;
	}

	public void setAge(int x, int y, int z, int age)
	{
		put(pack(x, y, z), (byte)Math.max(0, Math.min(31, age)));
		markDirty();
	}

	public void removeAge(int x, int y, int z)
	{
		if(remove(pack(x, y, z)))
			markDirty();
	}

	public int size()
	{
		return size;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		clear();
		NBTTagList entries = nbt.getTagList("entries", 10);
		for(int i=0; i<entries.tagCount(); i++)
		{
			NBTTagCompound entry = entries.getCompoundTagAt(i);
			long key = entry.getLong("pos");
			int age = entry.getByte("age")&255;
			if(age<=31)
				put(key, (byte)age);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList entries = new NBTTagList();
		for(int i=0; i<used.length; i++)
			if(used[i])
			{
				NBTTagCompound entry = new NBTTagCompound();
				entry.setLong("pos", keys[i]);
				entry.setByte("age", ages[i]);
				entries.appendTag(entry);
			}
		nbt.setTag("entries", entries);
	}

	private int findSlot(long key)
	{
		int mask = keys.length-1;
		int slot = hash(key)&mask;
		while(used[slot])
		{
			if(keys[slot]==key)
				return slot;
			slot = (slot+1)&mask;
		}
		return -1;
	}

	private void put(long key, byte age)
	{
		if(size+1>threshold)
			resize(keys.length<<1);
		putRaw(key, age);
	}

	private void putRaw(long key, byte age)
	{
		int mask = keys.length-1;
		int slot = hash(key)&mask;
		while(used[slot])
		{
			if(keys[slot]==key)
			{
				ages[slot] = age;
				return;
			}
			slot = (slot+1)&mask;
		}

		used[slot] = true;
		keys[slot] = key;
		ages[slot] = age;
		size++;
	}

	private boolean remove(long key)
	{
		int slot = findSlot(key);
		if(slot<0)
			return false;
		used[slot] = false;
		size--;

		int mask = keys.length-1;
		int next = (slot+1)&mask;
		while(used[next])
		{
			long rekey = keys[next];
			byte reage = ages[next];
			used[next] = false;
			size--;
			putRaw(rekey, reage);
			next = (next+1)&mask;
		}
		return true;
	}

	private void resize(int newCapacity)
	{
		long[] oldKeys = keys;
		byte[] oldAges = ages;
		boolean[] oldUsed = used;

		keys = new long[newCapacity];
		ages = new byte[newCapacity];
		used = new boolean[newCapacity];
		size = 0;
		threshold = (int)(newCapacity*LOAD_FACTOR);
		for(int i=0; i<oldUsed.length; i++)
			if(oldUsed[i])
				putRaw(oldKeys[i], oldAges[i]);
	}

	private void clear()
	{
		keys = new long[64];
		ages = new byte[64];
		used = new boolean[64];
		size = 0;
		threshold = (int)(64*LOAD_FACTOR);
	}

	private static int hash(long value)
	{
		value ^= value>>>33;
		value *= 0xff51afd7ed558ccdl;
		value ^= value>>>33;
		value *= 0xc4ceb9fe1a85ec53l;
		value ^= value>>>33;
		return (int)value;
	}

	public static long pack(int x, int y, int z)
	{
		return ((long)x&0x3ffffffL)<<38 | ((long)z&0x3ffffffL)<<12 | (y&0xfffL);
	}

	public static int unpackX(long packed)
	{
		return (int)(packed>>38);
	}

	public static int unpackY(long packed)
	{
		return (int)(packed&0xfffL);
	}

	public static int unpackZ(long packed)
	{
		return (int)(packed<<26>>38);
	}
}
