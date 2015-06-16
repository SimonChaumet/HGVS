package fr.scarex.HGVS.HGVSEvents;

import java.util.LinkedList;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import fr.scarex.HGVS.HGVS;

public class HGVSChunkGenTickHandler
{
	public static LinkedList<HGVSChunkGenTickHandler.ChunkPos> genList = new LinkedList<HGVSChunkGenTickHandler.ChunkPos>();
	public static long time = 0L;
	public static int chunkGenPerTick = 4;

	@SubscribeEvent
	public void onServerTickEvent(ServerTickEvent event) {
		if (!genList.isEmpty()) {
			for (int i = 0; i < chunkGenPerTick; i++) {
				ChunkPos pos = genList.poll();
				if (pos != null) {
					ChunkProviderServer provider = MinecraftServer.getServer().worldServerForDimension(pos.dimID).theChunkProviderServer;
					if (!provider.chunkExists(pos.x, pos.z)) provider.loadChunk(pos.x, pos.z);
					if (genList.peek() == null) {
						pos.sender.addChatMessage(new ChatComponentTranslation("HGVS.game.preload.success", MinecraftServer.getCurrentTimeMillis() - time));
						HGVS.info(String.format("Successfully preloaded chunk(s) (%s ms)", MinecraftServer.getCurrentTimeMillis() - time));
					}
					provider.unloadQueuedChunks();
				}
			}
		}
	}

	public static class ChunkPos
	{
		public int x;
		public int z;
		public int dimID;
		public ICommandSender sender;

		public ChunkPos(int x, int z, int dimID, ICommandSender sender) {
			this.x = x;
			this.z = z;
			this.dimID = dimID;
			this.sender = sender;
		}
	}
}
