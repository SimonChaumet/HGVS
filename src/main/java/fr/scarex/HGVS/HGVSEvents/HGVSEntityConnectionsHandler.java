package fr.scarex.HGVS.HGVSEvents;

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSConfiguration.HGVSConfiguration;
import fr.scarex.HGVS.HGVSUtils.HGVSScorboardUtils;

public class HGVSEntityConnectionsHandler
{
	public ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();

	@SubscribeEvent
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityLiving) {
			EntityLiving el = (EntityLiving) event.entity;
			NBTTagCompound compound = new NBTTagCompound();
			el.writeEntityToNBT(compound);
			if (HGVSTickHandler.canrun) compound.setBoolean("NoAI", false);
			if (!HGVSTickHandler.canrun) compound.setBoolean("NoAI", true);
			el.readEntityFromNBT(compound);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		HGVSScorboardUtils.removeUselessTeams();
		HGVSScorboardUtils.checkForStop();
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (serverConfigManager == null) serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
		EntityPlayerMP epMP = (EntityPlayerMP) event.player;

		if (!HGVSTickHandler.canrun && HGVSTickHandler.episodes == 1 && HGVSTickHandler.minutes == HGVSConfiguration.episodeLength && HGVSTickHandler.seconds == 0 && !HGVSTickHandler.isPlayerInGameList(epMP.getName())) HGVSTickHandler.addPlayerToGameList(epMP.getName());
		if (serverConfigManager.getOppedPlayers().getEntry(epMP.getGameProfile()) != null && !HGVSTickHandler.isPlayerInGameList(epMP.getName())) HGVSTickHandler.addPlayerToGameList(epMP.getName());
		if (!HGVSTickHandler.isPlayerInGameList(epMP.getName()) && serverConfigManager.getOppedPlayers().getEntry(epMP.getGameProfile()) == null) {
			if (HGVSConfiguration.kickPlayerOnConnection) {
				epMP.playerNetServerHandler.kickPlayerFromServer(EnumChatFormatting.DARK_RED + "You are not in the game list");
				return;
			} else {
				HGVSTickHandler.addPlayerToGameList(epMP.getName());
				epMP.setGameType(GameType.SPECTATOR);
				epMP.clearActivePotions();
			}
		}
		if (HGVSTickHandler.canrun) {
			if (!epMP.isSpectator()) {
				epMP.setGameType(GameType.SURVIVAL);
				epMP.capabilities.allowFlying = false;
				epMP.capabilities.disableDamage = false;
				epMP.sendPlayerAbilities();
			}
			if (epMP.isPotionActive(11)) epMP.removePotionEffect(11);
			if (epMP.isPotionActive(2)) epMP.removePotionEffect(2);
			if (epMP.isPotionActive(8)) epMP.removePotionEffect(8);
		} else {
			if (serverConfigManager.getOppedPlayers().getEntry(epMP.getGameProfile()) == null) {
				if (!epMP.isSpectator()) {
					epMP.setGameType(GameType.ADVENTURE);
					epMP.capabilities.disableDamage = true;
					if ((HGVSTickHandler.episodes == 1 && HGVSTickHandler.minutes == HGVSConfiguration.episodeLength && HGVSTickHandler.seconds == 0)) {
						epMP.capabilities.allowFlying = true;
						epMP.capabilities.allowEdit = false;
					} else {
						epMP.addPotionEffect(new PotionEffect(11, Integer.MAX_VALUE, 20, false, false));
						epMP.addPotionEffect(new PotionEffect(2, Integer.MAX_VALUE, 20, false, false));
						epMP.addPotionEffect(new PotionEffect(8, Integer.MAX_VALUE, 250, false, false));
					}
					epMP.sendPlayerAbilities();
				}
			} else {
				if (!epMP.isSpectator()) epMP.setGameType(GameType.CREATIVE);
				if (epMP.isPotionActive(11)) epMP.removePotionEffect(11);
				if (epMP.isPotionActive(2)) epMP.removePotionEffect(2);
				if (epMP.isPotionActive(8)) epMP.removePotionEffect(8);
			}
		}
		try {
			Packet packet = new S47PacketPlayerListHeaderFooter();

			Field fi1 = packet.getClass().getDeclaredFields()[0];
			fi1.setAccessible(true);
			ChatComponentText compH = new ChatComponentText(HGVSConfiguration.header);
			fi1.set(packet, compH);
			fi1.setAccessible(false);

			Field fi2 = packet.getClass().getDeclaredFields()[1];
			fi2.setAccessible(true);
			ChatComponentText compF = new ChatComponentText(HGVSConfiguration.footer);
			fi2.set(packet, compF);
			fi2.setAccessible(false);

			epMP.playerNetServerHandler.sendPacket(packet);

		} catch (Exception e) {}
		epMP.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(HGVSTickHandler.getNearestFeast(epMP)));
		HGVSScorboardUtils.checkForStart();
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		HGVSScorboardUtils.checkForStop();
	}
}
