package fr.scarex.HGVS.HGVSCommands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.border.WorldBorder;
import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSConfiguration.HGVSConfiguration;
import fr.scarex.HGVS.HGVSEvents.HGVSChunkGenTickHandler;
import fr.scarex.HGVS.HGVSEvents.HGVSChunkGenTickHandler.ChunkPos;
import fr.scarex.HGVS.HGVSEvents.HGVSStatisticsCollectorEventsHandler;
import fr.scarex.HGVS.HGVSEvents.HGVSStatisticsCollectorEventsHandler.StatisticsCollectorPlayer;
import fr.scarex.HGVS.HGVSEvents.HGVSTickHandler;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils.HGVSEnumSoundUtils;

public class HGVSCommandGame extends CommandBase
{
	public static ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();

	@Override
	public String getName() {
		return "game";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "HGVS.command.game.usage";
	}

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("start")) {
				byte s = (byte) (args.length > 1 ? parseInt(args[1], 0, 127) : 10);
				byte buffer = (byte) (args.length > 2 ? parseInt(args[2], 0, 127) : 0);
				boolean doFormat = args.length > 3 ? parseBoolean(args[3]) : (HGVSTickHandler.episodes == 1 && HGVSTickHandler.minutes == HGVSConfiguration.episodeLength && HGVSTickHandler.seconds == 0 ? true : false);
				start(s, buffer, doFormat);
			} else if (args[0].equalsIgnoreCase("pause")) {
				if (pause()) sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.pause.success"));
			} else if (args[0].equalsIgnoreCase("stop")) {
				stop(args.length > 1 ? parseBoolean(args[1]) : !HGVSTickHandler.isStopped);
			} else if (args[0].equalsIgnoreCase("shift")) {
				byte ep = (byte) (args.length > 1 ? parseInt(args[1], 0, 127) : HGVSTickHandler.episodes + 1);
				int m = args.length > 2 ? parseInt(args[2], 0) : HGVSConfiguration.episodeLength;
				int s = args.length > 3 ? parseInt(args[3], 0, 60) : 0;
				shift(ep, m, s);
				sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.shift.success", HGVSTickHandler.episodes, HGVSTickHandler.minutes, HGVSTickHandler.seconds));
			} else if (args[0].equalsIgnoreCase("update")) {
				HGVSTickHandler.recreateScoreboard();
			} else if (args[0].equalsIgnoreCase("stats")) {
				if (args.length > 2) {
					boolean flag1 = false;
					if (args[1].equalsIgnoreCase("show")) {
						flag1 = showStatsToSender(sender, args[2]);
					} else if (args[1].equalsIgnoreCase("reset")) {
						if (HGVSStatisticsCollectorEventsHandler.statPlayers != null && HGVSStatisticsCollectorEventsHandler.statPlayers.length > 0) {
							for (int i = 0; i < HGVSStatisticsCollectorEventsHandler.statPlayers.length; i++) {
								if (HGVSStatisticsCollectorEventsHandler.statPlayers[i] != null && HGVSStatisticsCollectorEventsHandler.statPlayers[i].playerName.equalsIgnoreCase(args[2])) {
									HGVSStatisticsCollectorEventsHandler.statPlayers[i] = null;
									flag1 = true;
								}
							}
						}
					} else {
						sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.usage"));
					}
					if (!flag1) sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.playerNF", args[2]));
				} else if (args.length > 1 && args[1].equalsIgnoreCase("show")) {
					for (StatisticsCollectorPlayer sp : HGVSStatisticsCollectorEventsHandler.statPlayers) {
						if (sp != null) sender.addChatMessage(new ChatComponentText(sp.playerName));
					}
				} else {
					sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.usage"));
				}
			} else if (args[0].equalsIgnoreCase("list")) {
				if (args.length > 1) {
					if (args.length > 2 && args[1].equalsIgnoreCase("add")) {
						boolean flag = true;
						for (int i = 0; i < HGVSTickHandler.playersGameList.length && flag; i++) {
							if (HGVSTickHandler.playersGameList[i] != null && HGVSTickHandler.playersGameList[i].equalsIgnoreCase(args[2])) flag = false;
							if (HGVSTickHandler.playersGameList[i] == null) {
								HGVSTickHandler.playersGameList[i] = args[2];
								flag = false;
							}
						}
						if (!flag)
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.list.add.success", args[2]));
						else
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.list.add.failure", args[2]));
					} else if (args.length > 2 && args[1].equalsIgnoreCase("remove")) {
						boolean kickAfter = args.length > 3 ? parseBoolean(args[3]) : true;
						boolean flag = true;
						for (int i = 0; i < HGVSTickHandler.playersGameList.length && flag; i++) {
							if (HGVSTickHandler.playersGameList[i] != null && HGVSTickHandler.playersGameList[i].equals(args[2])) {
								HGVSTickHandler.playersGameList[i] = null;
								if (kickAfter) getPlayer(sender, args[2]).playerNetServerHandler.kickPlayerFromServer(EnumChatFormatting.DARK_RED + "You have been removed from the game list");
								flag = false;
							}
						}
						if (!flag)
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.list.remove.success", args[2]));
						else
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.list.remove.failure", args[2]));
					} else if (args[1].equalsIgnoreCase("show")) {
						if (args.length > 2) {
							sender.addChatMessage(new ChatComponentTranslation(HGVSTickHandler.isPlayerInGameList(args[2]) ? "HGVS.general.true" : "HGVS.general.false"));
						} else {
							for (int i = 0; i < HGVSTickHandler.playersGameList.length; i++) {
								if (HGVSTickHandler.playersGameList[i] != null) sender.addChatMessage(new ChatComponentText(HGVSTickHandler.playersGameList[i]));
							}
						}
					} else {
						sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.list.usage"));
					}
				} else {
					sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.list.usage"));
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				HGVSConfiguration.syncConfig();
				HGVSTickHandler.minutes = HGVSConfiguration.episodeLength;
				HGVSTickHandler.episodes = 1;
				HGVSTickHandler.seconds = 0;
				HGVSTickHandler.recreateScoreboard();
			} else if (args[0].equalsIgnoreCase("border")) {
				WorldBorder wb = getWorldBorder();
				if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
					wb.setCenter(0.0D, 0.0D);
					wb.setDamageBuffer(4.0D);
					wb.setDamageAmount(0.2D);
					wb.setWarningDistance(40);
					wb.setWarningTime(15);
				}
				if (args.length > 2 && args[1].equalsIgnoreCase("set")) {
					if (args.length > 3) {
						double size = parseDouble(args[2], 1.0D, 6.0E7D);
						int episodeTarget = parseInt(args[3]);

						if (episodeTarget > HGVSTickHandler.episodes)
							wb.setTransition(wb.getDiameter(), size, (((episodeTarget - HGVSTickHandler.episodes - 1) * HGVSConfiguration.episodeLength + HGVSTickHandler.minutes) * 60 + HGVSTickHandler.seconds) * 1000L);
						else
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.border.set.moreEpisodes", episodeTarget, HGVSTickHandler.episodes));
					} else {
						double size = parseDouble(args[2], 1.0D, 6.0E7D);
						wb.setTransition(size);
					}
				} else {
					sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.border.usage"));
				}
			} else if (args[0].equalsIgnoreCase("heal")) {
				EntityPlayerMP entityplayerMP = args.length > 1 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
				boolean regenFood = false;
				if (args.length > 2) regenFood = parseBoolean(args[2]);
				entityplayerMP.setHealth(20);
				if (regenFood) entityplayerMP.addPotionEffect(new PotionEffect(23, 10, 200));
				sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.heal.success", entityplayerMP.getName()));
			} else if (args[0].equalsIgnoreCase("config")) {
				if (args.length > 2) {
					if (args[1].equalsIgnoreCase("get"))
						sender.addChatMessage(new ChatComponentText(HGVSConfiguration.getType(args[2]) + " : " + HGVSConfiguration.getValue(args[2])));
					else if (args.length > 3 && args[1].equalsIgnoreCase("set"))
						HGVSConfiguration.setValue(args[2], args[3]);
					else
						sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.config.usage"));
				} else {
					sender.addChatMessage(new ChatComponentTranslation("HGVS.command.game.config.usage"));
				}
			} else if (args[0].equalsIgnoreCase("rf")) {
				HGVSTickHandler.newPositions();
			} else if (args[0].equalsIgnoreCase("preload")) {
				HGVSChunkGenTickHandler.time = MinecraftServer.getCurrentTimeMillis();
				int size = args.length > 1 ? parseInt(args[1], 1) : 20;
				for (int i = -size; i < size; i++) {
					for (int i1 = -size; i1 < size; i1++) {
						HGVSChunkGenTickHandler.genList.add(new ChunkPos(i, i1, 0, sender));
					}
				}
			} else {
				sender.addChatMessage(new ChatComponentTranslation(getCommandUsage(sender)));
			}
		} else {
			sender.addChatMessage(new ChatComponentTranslation(getCommandUsage(sender)));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 3;
	}

	@Override
	public List getAliases() {
		return Arrays.asList(new String[] { "HGVSGame" });
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) return getListOfStringsMatchingLastWord(args, "start", "pause", "stop", "shift", "update", "stats", "list", "reload", "border", "heal", "config", "rf");
		if (args.length == 4 && args[0].equalsIgnoreCase("start")) return getListOfStringsMatchingLastWord(args, "true", "false");
		if (args.length == 2 && args[0].equalsIgnoreCase("stop")) return getListOfStringsMatchingLastWord(args, "true", "false");
		if (args.length == 2 && args[0].equalsIgnoreCase("stats")) return getListOfStringsMatchingLastWord(args, "show", "reset");
		if (args.length == 3 && args[0].equalsIgnoreCase("stats")) return getListOfStringsMatchingLastWord(args, getListOfPlayerUsernames());
		if (args.length == 2 && args[0].equalsIgnoreCase("list")) return getListOfStringsMatchingLastWord(args, "add", "remove", "show");
		if (args.length == 3 && args[0].equalsIgnoreCase("list")) return getListOfStringsMatchingLastWord(args, getListOfPlayerUsernames());
		if (args.length == 2 && args[0].equalsIgnoreCase("border")) return getListOfStringsMatchingLastWord(args, "reload", "set");
		if (args.length == 2 && args[0].equalsIgnoreCase("heal")) return getListOfStringsMatchingLastWord(args, getListOfPlayerUsernames());
		if (args.length == 3 && args[0].equalsIgnoreCase("heal")) return getListOfStringsMatchingLastWord(args, "true", "false");
		if (args.length == 2 && args[0].equalsIgnoreCase("config")) return getListOfStringsMatchingLastWord(args, "get", "set");
		if (args.length == 3 && args[0].equalsIgnoreCase("config")) return func_175762_a(args, HGVSConfiguration.getValidKeys());
		if (args.length == 4 && args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("set") && HGVSConfiguration.getType(args[2]).equals("boolean")) return getListOfStringsMatchingLastWord(args, "true", "false");
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 2 && args[0].equalsIgnoreCase("stats") ? true : (index == 2 && args[0].equalsIgnoreCase("list"));
	}

	public String[] getListOfPlayerUsernames() {
		return MinecraftServer.getServer().getAllUsernames();
	}

	protected static WorldBorder getWorldBorder() {
		return MinecraftServer.getServer().worldServers[0].getWorldBorder();
	}

	public static void stop(boolean flag) {
		if (flag) {
			pause();
			HGVSTickHandler.isStopping = true;
			HGVSTickHandler.isStopped = true;
		} else {
			HGVSTickHandler.isStarting = false;
			HGVSTickHandler.isStopped = false;
			HGVSTickHandler.isStopping = false;
			HGVSTickHandler.startingSeconds = 5;
			HGVSTickHandler.postBufferStarter = 2;
			HGVSTickHandler.stoppingSeconds = 120;
		}
	}

	public static boolean pause() {
		if (HGVSTickHandler.canrun || HGVSTickHandler.isStarting) {
			HGVSTickHandler.canrun = false;
			HGVSTickHandler.isStarting = false;
			HGVSStatisticsCollectorEventsHandler.canrun = false;
			serverConfigManager.getServerInstance().setDifficultyForAllWorlds(EnumDifficulty.EASY);
			MinecraftServer.getServer().getEntityWorld().getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
			HGVSBroadcastSoundUtils.broadcastSoundToAll(HGVSEnumSoundUtils.RANDOM_ANVIL_LAND, 10.0F, 1.0F);
			WorldBorder wb = MinecraftServer.getServer().getEntityWorld().getWorldBorder();
			wb.setTransition(wb.getDiameter());
			HGVSTickHandler.updateMOTD(EnumChatFormatting.DARK_RED + "Game paused");
			Iterator ite = serverConfigManager.playerEntityList.iterator();
			while (ite.hasNext()) {
				EntityPlayerMP epMP = (EntityPlayerMP) ite.next();
				if (epMP != null) {
					if (serverConfigManager.getOppedPlayers().getEntry(epMP.getGameProfile()) != null && !epMP.isSpectator()) {
						epMP.setGameType(GameType.CREATIVE);
					} else {
						if (!epMP.isSpectator()) {
							epMP.setGameType(GameType.ADVENTURE);
							epMP.capabilities.disableDamage = true;
							epMP.capabilities.allowEdit = false;
							epMP.sendPlayerAbilities();
							epMP.addPotionEffect(new PotionEffect(2, Integer.MAX_VALUE, 20, false, false));
							epMP.addPotionEffect(new PotionEffect(8, Integer.MAX_VALUE, 250, false, false));
							if (HGVSConfiguration.kickPlayerOnPause) epMP.playerNetServerHandler.kickPlayerFromServer("Pause, prend un café et va pisser !");
						}
					}
				}
			}
			for (WorldServer worldS : MinecraftServer.getServer().worldServers) {
				Iterator ite1 = worldS.loadedEntityList.iterator();
				while (ite1.hasNext()) {
					Entity entity = (Entity) ite1.next();
					if (entity instanceof EntityLiving) {
						EntityLiving el = (EntityLiving) entity;
						NBTTagCompound compound = new NBTTagCompound();
						el.writeEntityToNBT(compound);
						compound.setBoolean("NoAI", true);
						el.readEntityFromNBT(compound);
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public static void shift(byte episode, int minutes, int seconds) {
		if (episode != HGVSTickHandler.episodes) HGVSTickHandler.newEpisode(episode);
		HGVSTickHandler.episodes = episode;
		HGVSTickHandler.minutes = minutes;
		HGVSTickHandler.seconds = seconds;
		HGVSTickHandler.recreateScoreboard();
	}

	public static boolean start(short startingSeconds, short buffer, boolean format) {
		if (!HGVSTickHandler.canrun && !HGVSTickHandler.isStarting) {
			WorldBorder wb = getWorldBorder();
			if (HGVSTickHandler.episodes == 1 && HGVSTickHandler.minutes == HGVSConfiguration.episodeLength && HGVSTickHandler.seconds == 0) {
				if (format) {
					wb.setCenter(0.0D, 0.0D);
					wb.setDamageBuffer(4.0D);
					wb.setDamageAmount(0.2D);
					wb.setWarningDistance(40);
					wb.setWarningTime(15);
					MinecraftServer.getServer().getEntityWorld().setSpawnPoint(new BlockPos(0, 70, 0));
				}
				wb.setTransition(HGVSConfiguration.maxSpreadDistance * 2 + 40.0D);
				GameRules gr = MinecraftServer.getServer().getEntityWorld().getGameRules();
				gr.setOrCreateGameRule("doFireTick", "true");
				gr.setOrCreateGameRule("mobGriefing", "true");
				gr.setOrCreateGameRule("keepInventory", "false");
				gr.setOrCreateGameRule("doMobSpawning", "true");
				gr.setOrCreateGameRule("doMobLoot", "true");
				gr.setOrCreateGameRule("doTileDrops", "true");
				gr.setOrCreateGameRule("commandBlockOutput", "false");
				gr.setOrCreateGameRule("naturalRegeneration", "false");
				gr.setOrCreateGameRule("logAdminCommands", "true");
				gr.setOrCreateGameRule("showDeathMessages", "true");
				gr.setOrCreateGameRule("randomTickSpeed", "3");
				gr.setOrCreateGameRule("sendCommandFeedback", "true");
				gr.setOrCreateGameRule("reducedDebugInfo", "false");
				HGVS.info("Gamerules set");
				serverConfigManager.getServerInstance().setDifficultyForAllWorlds(EnumDifficulty.HARD);
			}
			serverConfigManager.getServerInstance().setDifficultyForAllWorlds(EnumDifficulty.HARD);
			HGVSTickHandler.startingSeconds = startingSeconds;
			HGVSTickHandler.postBufferStarter = buffer;
			HGVSStatisticsCollectorEventsHandler.canrun = true;
			HGVSTickHandler.isStarting = true;
			return true;
		}
		return false;
	}

	public static boolean showStatsToSender(ICommandSender receiver, String playerName) {
		for (StatisticsCollectorPlayer sp : HGVSStatisticsCollectorEventsHandler.statPlayers) {
			if (sp != null && sp.playerName.equalsIgnoreCase(playerName)) {
				receiver.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.remainingDeaths", sp.getRemainingDeaths()));
				receiver.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.healthRegenerate", sp.getHealthRegenerate()));
				Iterator ite = sp.getAllDeaths().iterator();
				while (ite.hasNext()) {
					Entry<Integer, String> entry = (Entry<Integer, String>) ite.next();
					receiver.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.death", entry.getKey(), sp.playerName, entry.getValue()));
				}
				receiver.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "--------" + EnumChatFormatting.RESET));
				Iterator ite1 = sp.getAllDamages().iterator();
				while (ite1.hasNext()) {
					Entry<String, Float> entry1 = (Entry<String, Float>) ite1.next();
					receiver.addChatMessage(new ChatComponentTranslation("HGVS.command.game.stats.damage", entry1.getKey(), entry1.getValue()));
				}
				return true;
			}
		}
		return false;
	}
}
