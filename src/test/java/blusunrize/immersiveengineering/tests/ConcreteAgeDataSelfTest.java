package blusunrize.immersiveengineering.tests;

import blusunrize.immersiveengineering.common.fluids.ConcreteAgeData;
import net.minecraft.nbt.NBTTagCompound;

public class ConcreteAgeDataSelfTest
{
	public static void main(String[] args)
	{
		testCoordinatePacking();
		testResizeRemovalAndClamp();
		testNbtRoundTrip();
		System.out.println("CONCRETE_AGE_DATA_SELF_TEST_PASS");
	}

	private static void testCoordinatePacking()
	{
		int[][] cases = {{0,0,0},{1,255,-1},{-30000000,64,30000000},{33554431,4095,-33554432}};
		for(int[] c : cases)
		{
			long packed = ConcreteAgeData.pack(c[0], c[1], c[2]);
			check(ConcreteAgeData.unpackX(packed)==c[0], "x pack mismatch");
			check(ConcreteAgeData.unpackY(packed)==c[1], "y pack mismatch");
			check(ConcreteAgeData.unpackZ(packed)==c[2], "z pack mismatch");
		}
	}

	private static void testResizeRemovalAndClamp()
	{
		ConcreteAgeData data = new ConcreteAgeData("test");
		for(int i=0; i<5000; i++)
			data.setAge(i-2500, i&255, 2500-i, i%32);
		check(data.size()==5000, "resize lost entries");
		for(int i=0; i<5000; i++)
			check(data.getAge(i-2500, i&255, 2500-i)==i%32, "age mismatch after resize");

		for(int i=0; i<5000; i+=3)
			data.removeAge(i-2500, i&255, 2500-i);
		for(int i=0; i<5000; i++)
		{
			int expected = i%3==0?0:i%32;
			check(data.getAge(i-2500, i&255, 2500-i)==expected, "cluster removal corruption");
		}

		data.setAge(9, 9, 9, 999);
		check(data.getAge(9, 9, 9)==31, "upper age clamp failed");
		data.setAge(9, 9, 9, -5);
		check(data.getAge(9, 9, 9)==0, "lower age clamp failed");
	}

	private static void testNbtRoundTrip()
	{
		ConcreteAgeData original = new ConcreteAgeData("test");
		for(int i=0; i<128; i++)
			original.setAge(-64+i, i, 64-i, i%32);
		NBTTagCompound nbt = new NBTTagCompound();
		original.writeToNBT(nbt);

		ConcreteAgeData restored = new ConcreteAgeData("test");
		restored.readFromNBT(nbt);
		check(restored.size()==original.size(), "NBT size mismatch");
		for(int i=0; i<128; i++)
			check(restored.getAge(-64+i, i, 64-i)==i%32, "NBT age mismatch");
	}

	private static void check(boolean condition, String message)
	{
		if(!condition)
			throw new AssertionError(message);
	}
}
