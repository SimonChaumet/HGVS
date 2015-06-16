package fr.scarex.HGVS.HGVSEvents;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class HGVSEventHandler
{
	public static void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new HGVSStatisticsCollectorEventsHandler());
		MinecraftForge.EVENT_BUS.register(new HGVSEntityConnectionsHandler());
		MinecraftForge.EVENT_BUS.register(new HGVSPlayerInteractEventHandler());
		MinecraftForge.EVENT_BUS.register(new HGVSPotionBrewerEventHandler());
		MinecraftForge.EVENT_BUS.register(new HGVSServerChatEventHandler());
		FMLCommonHandler.instance().bus().register(new HGVSEntityConnectionsHandler());
	}

	public static void registerTickEvents() {
		FMLCommonHandler.instance().bus().register(new HGVSTickHandler());
		FMLCommonHandler.instance().bus().register(new HGVSChunkGenTickHandler());
	}
}
