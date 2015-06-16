package fr.scarex.HGVS.HGVSEvents;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fr.scarex.HGVS.HGVSUtils.HGVSScorboardUtils;

public class HGVSPlayerInteractEventHandler
{
	@SubscribeEvent
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
			EntityPlayerMP p = (EntityPlayerMP) event.entityPlayer;
			if (p.getCurrentEquippedItem() != null && p.getCurrentEquippedItem().getItem() == Items.compass) {
				if (p.isSneaking()) {
					BlockPos pos = HGVSTickHandler.getNearestFeast(event.entityPlayer);
					if (((EntityPlayerMP) event.entityPlayer).theItemInWorldManager.isCreative()) {
						ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.compass.teleport", pos.getX(), pos.getZ());
						comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tp %s %s %s", pos.getX(), pos.getY() + 6, pos.getZ())));
						p.playerNetServerHandler.sendPacket(new S02PacketChat(comp));
					}
					p.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(pos));
					p.playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation("HGVS.compass.showPosF", pos.getX(), pos.getZ(), String.format("%1$.1f", StrictMath.sqrt(p.getDistanceSq(pos)))), (byte) 2));
				} else {
					EntityPlayerMP np = HGVSScorboardUtils.getNearestPlayer(p);
					if (np != null) {
						p.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(np.getPosition()));
						p.playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation("HGVS.compass.showPosP", np.getDisplayName(), String.format("%1$.1f", p.getDistanceToEntity(np))), (byte) 2));
					} else {
						p.playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation("HGVS.compass.noPlayerFound"), (byte) 2));
					}
				}
			}
		}
	}
}
