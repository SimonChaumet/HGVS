package fr.scarex.HGVS.HGVSUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.config.ConfigCategory;

import com.google.common.base.Predicate;

import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSCommands.HGVSCommandGame;
import fr.scarex.HGVS.HGVSConfiguration.HGVSConfiguration;
import fr.scarex.HGVS.HGVSEvents.HGVSChunkGenTickHandler;
import fr.scarex.HGVS.HGVSEvents.HGVSChunkGenTickHandler.ChunkPos;
import fr.scarex.HGVS.HGVSEvents.HGVSTickHandler;

public final class HGVSScorboardUtils
{
	private static final Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
	private static Collection existingTeams;
	private static final String[] existingTeamsColors = new String[16];

	public static EnumChatFormatting getNextUnusedTeamColor() {
		HGVSScorboardUtils.existingTeams = HGVSScorboardUtils.scoreboard.getTeams();
		Iterator t = HGVSScorboardUtils.existingTeams.iterator();
		ScorePlayerTeam s;
		int i = 0;
		while (t.hasNext()) {
			s = (ScorePlayerTeam) t.next();
			if (s != null && s instanceof ScorePlayerTeam) {
				HGVSScorboardUtils.existingTeamsColors[i] = s.getColorPrefix();
			}
			i++;
		}

		boolean[] b = new boolean[16];
		for (int j = 0; j < EnumChatFormatting.values().length; j++) {
			for (int k = 0; k < existingTeamsColors.length; k++) {
				if (existingTeamsColors[k] != null && existingTeamsColors[k].equalsIgnoreCase(EnumChatFormatting.values()[j].toString())) {
					b[j] = true;
				}
			}
		}
		for (int l = 0; l < b.length; l++) {
			if (!b[l]) return EnumChatFormatting.values()[l];
		}
		return null;
	}

	public static ScorePlayerTeam getNextUnusedTeamByColor() {
		EnumChatFormatting ecf = HGVSScorboardUtils.getNextUnusedTeamColor();
		if (ecf != null) {
			ScorePlayerTeam spt = HGVSScorboardUtils.scoreboard.createTeam(ecf.getFriendlyName());
			spt.setNamePrefix(ecf.toString());
			spt.setNameSuffix(EnumChatFormatting.RESET.toString());
			return spt;
		}
		return null;
	}

	public static void removeUselessTeams() {
		Iterator ite = scoreboard.getTeams().iterator();
		ScorePlayerTeam st;
		while (ite.hasNext() && (st = (ScorePlayerTeam) ite.next()) != null) {
			Iterator ite1 = st.getMembershipCollection().iterator();
			boolean flag = true;
			while (ite1.hasNext() && flag) {
				if ((String) ite1.next() != null) {
					flag = false;
				}
			}
			if (flag) scoreboard.removeTeam(st);
		}
	}

	public static List getAllPlayersNotInSpectator() {
		List players = new ArrayList<EntityPlayerMP>();
		EntityPlayerMP p;
		Iterator ite = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
		while (ite.hasNext()) {
			if (!(p = (EntityPlayerMP) ite.next()).isSpectator()) players.add(p);
		}
		return players;
	}

	public static EntityPlayerMP getNearestPlayer(final EntityLivingBase e) {
		Predicate filter = new Predicate() {
			public boolean apply(Entity entityIn) {
				if (!(entityIn instanceof EntityPlayerMP)) return false;
				if (entityIn.getName().equals(e.getName())) return false;
				EntityPlayerMP p = (EntityPlayerMP) entityIn;
				if (p.isOnSameTeam(e)) return false;
				return true;
			}

			public boolean apply(Object o) {
				return this.apply((Entity) o);
			}
		};
		List list = e.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, e.getEntityBoundingBox().expand(200.0D, 200.0D, 200.0D), filter);
		if (list != null && list.size() > 0) {
			Iterator ite = list.iterator();
			EntityPlayerMP nearestP = (EntityPlayerMP) ite.next();
			while (ite.hasNext()) {
				EntityPlayerMP p = (EntityPlayerMP) ite.next();
				if (nearestP.equals(e)) {
					nearestP = p;
				} else if (nearestP.getDistanceSqToEntity(e) > (p).getDistanceSqToEntity(e)) {
					nearestP = p;
				}
			}
			return nearestP;
		} else {
			return null;
		}

	}

	public static void checkForStart() {
		if (HGVSConfiguration.feastsPerPlayer > 0) {
			int playerCount = getAllPlayersNotInSpectator().size();
			if (playerCount >= HGVSConfiguration.numberOfPlayerToStart && !HGVSTickHandler.canrun && HGVSTickHandler.episodes == 1 && HGVSTickHandler.minutes == HGVSConfiguration.episodeLength && HGVSTickHandler.seconds == 0) {
				ConfigCategory cat = HGVS.config.getCategory(HGVSConfiguration.CATEGORY);
				cat.get("minSpreadDistance").set((playerCount * 10) + 50);
				cat.get("maxSpreadDistance").set((playerCount * 100) + 200);
				HGVSConfiguration.setValue("minSpreadDistance", "" + (playerCount * 10 + 50));
				HGVSConfiguration.setValue("maxSpreadDistance", "" + (playerCount * 100 + 200));
				WorldBorder wb = MinecraftServer.getServer().getEntityWorld().getWorldBorder();
				wb.setCenter(0.0D, 0.0D);
				wb.setTransition((HGVSConfiguration.maxSpreadDistance + 50) * 2);
				int n = Math.round(HGVSConfiguration.feastsPerPlayer * playerCount);
				HGVSConfiguration.feastsNumber = n > 0 ? n : 0;
				HGVSCommandGame.start((short) 120, (short) 6, true);
				int chunksToLoad = (HGVSConfiguration.maxSpreadDistance / 16) - (HGVSConfiguration.maxSpreadDistance % 16);
				for (int i = -chunksToLoad; i < chunksToLoad; i++) {
					for (int i1 = -chunksToLoad; i1 < chunksToLoad; i1++) {
						HGVSChunkGenTickHandler.genList.add(new ChunkPos(i, i1, 0, MinecraftServer.getServer()));
					}
				}
			}
		}
	}

	public static void checkForStop() {
		if (HGVSConfiguration.feastsPerPlayer > 0) {
			int playerCount = getAllPlayersNotInSpectator().size();
			if (playerCount <= 1 && HGVSTickHandler.canrun) {
				Iterator ite = getAllPlayersNotInSpectator().iterator();
				while (ite.hasNext()) {
					EntityPlayerMP p = (EntityPlayerMP) ite.next();
					HGVSCommandGame.showStatsToSender(p, p.getName());
				}
				HGVSCommandGame.stop(true);
			}
		}
	}
}
