package blusunrize.immersiveengineering.common.util.network;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Marshals C2S work from FML's Netty callback onto the server tick thread.
 * Bounds deliberately favor safety over throughput: ordinary GUI traffic
 * should never need more than a handful of pending actions per player.
 */
public final class ServerNetworkTaskQueue
{
	private static final int MAX_QUEUED_TASKS = 64;
	private static final int MAX_PENDING_PER_PLAYER = 4;
	private static final int MAX_TASKS_PER_TICK = 16;
	private static final int DROP_LOG_INTERVAL_TICKS = 100;

	private static final ConcurrentLinkedQueue<QueuedTask> TASKS = new ConcurrentLinkedQueue<QueuedTask>();
	private static final ConcurrentHashMap<UUID, PlayerQuota> PLAYER_QUOTAS = new ConcurrentHashMap<UUID, PlayerQuota>();
	private static final AtomicInteger QUEUED_TASKS = new AtomicInteger();
	private static final AtomicInteger DROPPED_SINCE_LOG = new AtomicInteger();
	private static int dropLogCooldown;

	private ServerNetworkTaskQueue()
	{
	}

	public static boolean enqueue(EntityPlayerMP player, Runnable task)
	{
		if(player==null || task==null)
			return false;
		UUID playerId = player.getUniqueID();
		PlayerQuota quota = acquirePlayerQuota(playerId);
		if(quota==null)
		{
			DROPPED_SINCE_LOG.incrementAndGet();
			return false;
		}
		if(!tryAcquire(QUEUED_TASKS, MAX_QUEUED_TASKS))
		{
			releasePlayerQuota(playerId, quota);
			DROPPED_SINCE_LOG.incrementAndGet();
			return false;
		}
		TASKS.offer(new QueuedTask(playerId, quota, task));
		return true;
	}

	private static boolean tryAcquire(AtomicInteger counter, int limit)
	{
		while(true)
		{
			int current = counter.get();
			if(current>=limit)
				return false;
			if(counter.compareAndSet(current, current+1))
				return true;
		}
	}

	private static PlayerQuota acquirePlayerQuota(UUID playerId)
	{
		while(true)
		{
			PlayerQuota quota = PLAYER_QUOTAS.get(playerId);
			if(quota==null)
			{
				PlayerQuota created = new PlayerQuota();
				PlayerQuota existing = PLAYER_QUOTAS.putIfAbsent(playerId, created);
				quota = existing!=null?existing:created;
			}
			synchronized(quota)
			{
				if(PLAYER_QUOTAS.get(playerId)!=quota)
					continue;
				if(quota.pending>=MAX_PENDING_PER_PLAYER)
					return null;
				quota.pending++;
				return quota;
			}
		}
	}

	private static void releasePlayerQuota(UUID playerId, PlayerQuota quota)
	{
		synchronized(quota)
		{
			quota.pending--;
			if(quota.pending==0)
				PLAYER_QUOTAS.remove(playerId, quota);
		}
	}

	public static int drain()
	{
		int processed = 0;
		while(processed<MAX_TASKS_PER_TICK)
		{
			QueuedTask queued = TASKS.poll();
			if(queued==null)
				break;
			QUEUED_TASKS.decrementAndGet();
			try
			{
				queued.task.run();
			}
			catch(RuntimeException ex)
			{
				IELogger.error("Network task failed safely: "+ex.getClass().getName()+": "+ex.getMessage());
			}
			finally
			{
				releasePlayerQuota(queued.playerId, queued.quota);
			}
			processed++;
		}
		updateDropTelemetry();
		return processed;
	}

	private static void updateDropTelemetry()
	{
		if(dropLogCooldown>0)
		{
			dropLogCooldown--;
			return;
		}
		int dropped = DROPPED_SINCE_LOG.getAndSet(0);
		if(dropped>0)
		{
			IELogger.warn("Dropped "+dropped+" excessive client GUI/network actions in the last interval");
			dropLogCooldown = DROP_LOG_INTERVAL_TICKS;
		}
	}

	static int queuedTaskCount()
	{
		return QUEUED_TASKS.get();
	}

	private static final class PlayerQuota
	{
		private int pending;
	}

	private static final class QueuedTask
	{
		private final UUID playerId;
		private final PlayerQuota quota;
		private final Runnable task;

		private QueuedTask(UUID playerId, PlayerQuota quota, Runnable task)
		{
			this.playerId = playerId;
			this.quota = quota;
			this.task = task;
		}
	}
}
