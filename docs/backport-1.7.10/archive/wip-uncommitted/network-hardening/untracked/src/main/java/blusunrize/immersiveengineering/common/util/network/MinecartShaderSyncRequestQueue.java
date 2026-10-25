package blusunrize.immersiveengineering.common.util.network;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.entities.EntityPropertiesShaderCart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Coalesces client requests for minecart shader state and resolves them only
 * from the authoritative server tick. Requests are primitive ids only and
 * never force chunks or broadcast responses to unrelated players.
 */
public final class MinecartShaderSyncRequestQueue
{
	private static final int MINECART_TRACKING_RANGE = 80;
	private static final double MAX_DISTANCE_SQ = MINECART_TRACKING_RANGE*MINECART_TRACKING_RANGE;
	private static final int MAX_PENDING_GLOBAL = 512;
	private static final int MAX_PENDING_PER_PLAYER = 128;
	private static final int MAX_GLOBAL_PER_TICK = 64;
	private static final int MAX_PER_PLAYER_PER_TICK = 8;

	private static final ConcurrentHashMap<UUID, PlayerRequests> REQUESTS = new ConcurrentHashMap<UUID, PlayerRequests>();
	private static final AtomicInteger GLOBAL_PENDING = new AtomicInteger();

	private MinecartShaderSyncRequestQueue()
	{
	}

	public static boolean request(EntityPlayerMP player, int dimension, int entityId)
	{
		if(player==null || entityId<0)
			return false;
		UUID playerId = player.getUniqueID();
		long key = requestKey(dimension, entityId);
		while(true)
		{
			PlayerRequests requests = REQUESTS.get(playerId);
			if(requests==null)
			{
				PlayerRequests created = new PlayerRequests(player);
				PlayerRequests existing = REQUESTS.putIfAbsent(playerId, created);
				requests = existing!=null?existing:created;
			}
			synchronized(requests)
			{
				if(REQUESTS.get(playerId)!=requests)
					continue;
				if(requests.pendingKeys.contains(key))
					return true;
				if(requests.player!=player)
				{
					int stale = requests.queue.size();
					requests.queue.clear();
					requests.pendingKeys.clear();
					GLOBAL_PENDING.addAndGet(-stale);
					REQUESTS.remove(playerId, requests);
					continue;
				}
				if(requests.queue.size()>=MAX_PENDING_PER_PLAYER || !tryAcquireGlobal())
					return false;
				requests.queue.addLast(new Request(dimension, entityId, key));
				requests.pendingKeys.add(key);
				return true;
			}
		}
	}

	private static boolean tryAcquireGlobal()
	{
		while(true)
		{
			int current = GLOBAL_PENDING.get();
			if(current>=MAX_PENDING_GLOBAL)
				return false;
			if(GLOBAL_PENDING.compareAndSet(current, current+1))
				return true;
		}
	}

	private static long requestKey(int dimension, int entityId)
	{
		return ((long)dimension<<32)^(entityId&0xffffffffL);
	}

	public static int drain()
	{
		int processed = 0;
		for(Map.Entry<UUID, PlayerRequests> entry : REQUESTS.entrySet())
		{
			if(processed>=MAX_GLOBAL_PER_TICK)
				break;
			PlayerRequests requests = entry.getValue();
			int processedForPlayer = 0;
			while(processedForPlayer<MAX_PER_PLAYER_PER_TICK && processed<MAX_GLOBAL_PER_TICK)
			{
				Request request;
				synchronized(requests)
				{
					if(REQUESTS.get(entry.getKey())!=requests)
						break;
					request = requests.queue.pollFirst();
					if(request==null)
					{
						REQUESTS.remove(entry.getKey(), requests);
						break;
					}
					requests.pendingKeys.remove(request.key);
					GLOBAL_PENDING.decrementAndGet();
					if(requests.queue.isEmpty())
						REQUESTS.remove(entry.getKey(), requests);
				}
				process(requests.player, request);
				processedForPlayer++;
				processed++;
			}
		}
		return processed;
	}

	private static void process(EntityPlayerMP player, Request request)
	{
		if(player==null || player.isDead)
			return;
		World world = player.worldObj;
		if(world==null || world.provider.dimensionId!=request.dimension)
			return;
		Entity entity = world.getEntityByID(request.entityId);
		if(!(entity instanceof EntityMinecart))
			return;
		if(player.getDistanceSq(entity.posX, entity.posY, entity.posZ)>MAX_DISTANCE_SQ)
			return;
		EntityPropertiesShaderCart properties = (EntityPropertiesShaderCart)entity.getExtendedProperties(
				EntityPropertiesShaderCart.PROPERTY_NAME);
		if(properties!=null)
			ImmersiveEngineering.packetHandler.sendTo(new MessageMinecartShaderSync(entity, properties), player);
	}

	static int pendingRequestCount()
	{
		return GLOBAL_PENDING.get();
	}

	private static final class PlayerRequests
	{
		private final EntityPlayerMP player;
		private final ArrayDeque<Request> queue = new ArrayDeque<Request>();
		private final Set<Long> pendingKeys = new HashSet<Long>();

		private PlayerRequests(EntityPlayerMP player)
		{
			this.player = player;
		}
	}

	private static final class Request
	{
		private final int dimension;
		private final int entityId;
		private final long key;

		private Request(int dimension, int entityId, long key)
		{
			this.dimension = dimension;
			this.entityId = entityId;
			this.key = key;
		}
	}
}
