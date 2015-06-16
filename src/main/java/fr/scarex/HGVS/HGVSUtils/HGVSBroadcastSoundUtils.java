package fr.scarex.HGVS.HGVSUtils;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class HGVSBroadcastSoundUtils
{
	/**
	 * Play a sound for all players in the array
	 * 
	 * @param s
	 *            Sound (e.g : mob.wither.death)
	 * @param epmpA
	 *            array of players
	 * @param volume
	 *            volume of the sound
	 * @param pitch
	 *            pitch of the sound
	 * 
	 * @author SCAREX
	 */
	public static void broadcastSound(String s, EntityPlayerMP[] epmpA, float volume, float pitch) {
		for (EntityPlayerMP playerMP : epmpA) {
			playerMP.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(s, playerMP.posX, playerMP.posY, playerMP.posZ, volume, pitch));
		}
	}

	/**
	 * Play a sound for all players connected
	 * 
	 * @param s
	 *            Sound (e.g : mob.wither.death)
	 * @param volume
	 *            volume of the sound
	 * @param pitch
	 *            pitch of the sound
	 */
	public static void broadcastSoundToAll(String s, float volume, float pitch) {
		for (WorldServer worldObj : MinecraftServer.getServer().worldServers) {
			Iterator ite = worldObj.playerEntities.iterator();
			while (ite.hasNext()) {
				EntityPlayerMP playerMP = (EntityPlayerMP) ite.next();
				if (playerMP != null) {
					playerMP.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(s, playerMP.posX, playerMP.posY, playerMP.posZ, volume, pitch));
				}
			}
		}
	}

	/**
	 * Play a sound for all players connected
	 * 
	 * @param s
	 *            Sound (e.g : mob.wither.death)
	 * @param volume
	 *            volume of the sound
	 * @param pitch
	 *            pitch of the sound
	 */
	public static void broadcastSoundToAll(HGVSEnumSoundUtils es, float volume, float pitch) {
		broadcastSoundToAll(es.toString(), volume, pitch);
	}

	public enum HGVSEnumSoundUtils {
		BAT_DEATH("mob.bat.death"),
		BAT_HURT("mob.bat.hurt"),
		BAT_LOOP("mob.bat.loop"),
		BAT_TAKEOFF("mob.bat.takeoff"),

		BLAZE_BREATHE("mob.blaze.breathe"),
		BLAZE_DEATH("mob.blaze.death"),
		BLAZE_HIT("mob.blaze.hit"),

		CAT_HISS("mob.cat.hiss"),
		CAT_HITT("mob.cat.hitt"),
		CAT_MEOW("mob.cat.meow"),
		CAT_PURR("mob.cat.purr"),
		CAT_PURREOW("mob.cat.purreow"),

		CHICKEN_HURT("mob.chicken.hurt"),
		CHICKEN_PLOP("mob.chicken.plop"),
		CHICKEN_SAY("mob.chicken.say"),
		CHICKEN_step("mob.chicken.step"),

		WITHER_DEATH("mob.wither.death"),
		WITHER_HURT("mob.wither.hurt"),
		WITHER_SHOOT("mob.wither.idle"),
		WITHER_SPAWN("mob.wither.spawn"),

		RANDOM_ANVIL_BREAK("random.anvil_break"),
		RANDOM_ANVIL_LAND("random.anvil_land"),
		RANDOM_ANVIL_USE("random.anvil_use"),
		RANDOM_LEVELUP("random.levelup"),
		RANDOM_SUCCESSFUL_HIT("random.successful_hit");

		private final String location;

		private HGVSEnumSoundUtils(String location) {
			this.location = location;
		}

		public String toString() {
			return this.location;
		}
	}
}
