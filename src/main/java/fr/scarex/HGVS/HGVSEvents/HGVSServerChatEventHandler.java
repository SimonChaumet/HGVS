package fr.scarex.HGVS.HGVSEvents;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils;

public class HGVSServerChatEventHandler
{
	@SubscribeEvent
	public void onServerChatEvent(ServerChatEvent event) {
		String s = event.message.toLowerCase();
		if (s.startsWith("gg")) HGVSBroadcastSoundUtils.broadcastSoundToAll("hgvs:gg", 10.0F, 1.0F);
	}
}
