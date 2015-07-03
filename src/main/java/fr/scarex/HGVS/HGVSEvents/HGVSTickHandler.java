package fr.scarex.HGVS.HGVSEvents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.command.CommandException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreDummyCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSConfiguration.HGVSConfiguration;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils.HGVSEnumSoundUtils;
import fr.scarex.HGVS.HGVSUtils.HGVSScorboardUtils;

public class HGVSTickHandler
{
	private static ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
	public static Scoreboard sb;
	private long ticks;
	public static int seconds;
	public static int minutes;
	public static byte episodes;
	private static byte index = 0;
	public static boolean canrun = false;
	public static boolean isStarting = false;
	public static short startingSeconds = 5;
	public static short postBufferStarter = 2;
	public static Position[] posFeasts;
	public static Random rand = new Random();
	private boolean isScoreboardInitialized = false;
	public static String[] playersGameList = new String[64];
	public static boolean isStopped = false;
	public static boolean isStopping = false;
	public static int stoppingSeconds = 120;

	@SubscribeEvent
	public void onServerTickEvent(ServerTickEvent event) {
		if (event.phase.equals(Phase.END)) {
			this.ticks++;
			if (this.ticks % 20 == 0) second();
			if (this.ticks >= 1200) this.ticks = 0;
		}
	}

	@SubscribeEvent
	public void onWorldTickEvent(WorldTickEvent event) {
		if (HGVSConfiguration.disableWeather && event.phase.equals(Phase.START)) {
			WorldInfo wi = event.world.getWorldInfo();
			if (wi.isRaining() || wi.isThundering()) {
				wi.setCleanWeatherTime(0);
				wi.setRainTime(0);
				wi.setThunderTime(0);
				wi.setRaining(false);
				wi.setThundering(false);
			}
		}
	}

	private void second() {
		if (serverConfigManager == null) serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
		if (isStopping) {
			if (stoppingSeconds <= 0) {
				MinecraftServer.getServer().initiateShutdown();
			} else {
				if (stoppingSeconds >= 60 && (stoppingSeconds % 60 == 30 || stoppingSeconds % 60 == 0)) {
					serverConfigManager.sendChatMsg(new ChatComponentText(String.format("%3$s------------[Stopping : %3$s%1$sm%2$02d]%3$s------------", stoppingSeconds / 60, stoppingSeconds % 60, EnumChatFormatting.DARK_RED)));
					updateMOTD(String.format("%3$sGame stopping in : %4$s%1$sm%2$02d", stoppingSeconds / 60, stoppingSeconds % 60, EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_BLUE));
				} else if (stoppingSeconds < 20 || stoppingSeconds == 30) {
					serverConfigManager.sendChatMsg(new ChatComponentText(String.format("%2$s------------[Stopping : %1$02d]------------", stoppingSeconds, EnumChatFormatting.DARK_RED)));
					updateMOTD(String.format("%2$sGame stopping in : %3$s%1$02d", stoppingSeconds, EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_BLUE));
				}
				stoppingSeconds--;
			}
		}
		if (isStarting) {
			if (startingSeconds == 0) {
				serverConfigManager.sendChatMsg(new ChatComponentText(EnumChatFormatting.DARK_AQUA + "-------------------- [GO!] --------------------" + EnumChatFormatting.RESET));
				updateMOTD(EnumChatFormatting.DARK_GREEN + "Game running");
				MinecraftServer.getServer().getEntityWorld().getGameRules().setOrCreateGameRule("doDaylightCycle", "" + HGVSConfiguration.doDayLightCycle);
				Iterator ite = serverConfigManager.playerEntityList.iterator();
				while (ite.hasNext()) {
					EntityPlayerMP epMP = (EntityPlayerMP) ite.next();
					if (epMP != null && !epMP.isSpectator()) {
						epMP.setGameType(GameType.SURVIVAL);
						epMP.capabilities.disableDamage = false;
						epMP.capabilities.allowFlying = false;
						epMP.capabilities.allowEdit = true;
						epMP.sendPlayerAbilities();
						if (epMP.isPotionActive(11)) epMP.removePotionEffect(11);
						if (epMP.isPotionActive(2)) epMP.removePotionEffect(2);
						if (epMP.isPotionActive(8)) epMP.removePotionEffect(8);
						epMP.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(epMP.getHealth(), epMP.getFoodStats().getFoodLevel(), epMP.getFoodStats().getSaturationLevel()));
					}
				}

				if (episodes == 1 && minutes == HGVSConfiguration.episodeLength && seconds == 0) {
					Iterator ite2 = serverConfigManager.playerEntityList.iterator();
					while (ite2.hasNext()) {
						EntityPlayerMP epMP = (EntityPlayerMP) ite2.next();
						if (!epMP.isSpectator()) {
							epMP.setGameType(GameType.SURVIVAL);
							epMP.inventory.clear();
							epMP.inventoryContainer.detectAndSendChanges();
							epMP.capabilities.allowFlying = false;
							epMP.capabilities.disableDamage = false;
							epMP.sendPlayerAbilities();
							epMP.clearActivePotions();
							epMP.addExperienceLevel(-100);
							epMP.setHealth(20);
							epMP.addPotionEffect(new PotionEffect(11, 30 * 20, 200, false, false));
							epMP.getFoodStats().setFoodLevel(40);
							epMP.getFoodStats().addExhaustion(40.0F);
							epMP.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(epMP.getHealth(), epMP.getFoodStats().getFoodLevel(), epMP.getFoodStats().getSaturationLevel()));
						}
					}
					if (HGVSConfiguration.doDayLightCycle)
						MinecraftServer.getServer().getEntityWorld().setWorldTime(0);
					else
						MinecraftServer.getServer().getEntityWorld().setWorldTime(6000);

					HGVSTickHandler.newPositions();
					try {
						spread(HGVSScorboardUtils.getAllPlayersNotInSpectator(), new Position(MinecraftServer.getServer().getEntityWorld().getSpawnPoint().getX(), MinecraftServer.getServer().getEntityWorld().getSpawnPoint().getZ()), HGVSConfiguration.minSpreadDistance, HGVSConfiguration.maxSpreadDistance, ((Entity) HGVSScorboardUtils.getAllPlayersNotInSpectator().get(0)).worldObj, false);
					} catch (Exception e) {
						HGVS.error("Can not spread players");
					}
				} else if (HGVSConfiguration.feastsPerPlayer > 0) {
					WorldBorder wb = MinecraftServer.getServer().getEntityWorld().getWorldBorder();
					wb.setTransition(wb.getDiameter(), wb.getDiameter() * HGVSConfiguration.feastsPerPlayer, ((HGVSTickHandler.minutes * 60) + HGVSTickHandler.seconds) * 1000);
				}
				for (WorldServer worldS : MinecraftServer.getServer().worldServers) {
					Iterator ite1 = worldS.loadedEntityList.iterator();
					while (ite1.hasNext()) {
						Entity entity = (Entity) ite1.next();
						if (entity instanceof EntityLiving) {
							EntityLiving el = (EntityLiving) entity;
							NBTTagCompound compound = new NBTTagCompound();
							el.writeEntityToNBT(compound);
							compound.setBoolean("NoAI", false);
							el.readEntityFromNBT(compound);
						}
					}
				}
				HGVSBroadcastSoundUtils.broadcastSoundToAll(HGVSEnumSoundUtils.WITHER_DEATH, 1.0F, 0.9F);
				startingSeconds = -1;
			}
			if (startingSeconds <= 0) {
				if (postBufferStarter > 0) postBufferStarter--;
				if (postBufferStarter <= 0) {
					isStarting = false;
					canrun = true;
					isStopped = false;
				}
			}
			if (startingSeconds > 0) {
				updateMOTD(EnumChatFormatting.GOLD + "Game starting in : " + EnumChatFormatting.AQUA + startingSeconds);
				if (startingSeconds >= 60 && (startingSeconds % 60 == 0 || startingSeconds % 60 == 30)) {
					serverConfigManager.sendChatMsg(new ChatComponentText(String.format("%4$s-------------------- [%1$sm%2$02d] --------------------%3$s", Math.floor(startingSeconds / 60), startingSeconds % 60, EnumChatFormatting.RESET, EnumChatFormatting.GOLD)));
				} else if (startingSeconds <= 20 || startingSeconds == 30) {
					serverConfigManager.sendChatMsg(new ChatComponentText(String.format("%3$s-------------------- [%1$02d] --------------------%2$s", startingSeconds, EnumChatFormatting.RESET, EnumChatFormatting.GOLD)));
					HGVSBroadcastSoundUtils.broadcastSoundToAll(HGVSEnumSoundUtils.RANDOM_SUCCESSFUL_HIT, 0.8F, 1.0F);
				}
				startingSeconds--;
			}
		}
		if (canrun) {
			this.seconds--;
			if (this.seconds <= 0) {
				this.seconds = 59;
				this.minute();
			}
		}
		if (canrun) this.recreateScoreboard();
	}

	private void minute() {
		if (!isScoreboardInitialized) {
			try {
				deleteScoreboards();
				isScoreboardInitialized = true;
			} catch (Exception e) {}
		}
		if (canrun) {
			this.minutes--;
			if (this.minutes < 0) newEpisode((byte) (this.episodes + 1));
		}
	}

	public static void updateMOTD(String s) {
		if (serverConfigManager == null) serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
		serverConfigManager.getServerInstance().setMOTD(s);
		serverConfigManager.getServerInstance().getServerStatusResponse().setServerDescription(new ChatComponentText(serverConfigManager.getServerInstance().getMOTD()));
	}

	public static void newEpisode(byte ep) {
		minutes = HGVSConfiguration.episodeLength - 1;
		episodes = (byte) ep;
		serverConfigManager.sendChatMsg(new ChatComponentText(String.format("%2$s-------------------- [Episode: %1$s] --------------------%3$s", episodes, EnumChatFormatting.DARK_GREEN, EnumChatFormatting.RESET)));
		HGVSBroadcastSoundUtils.broadcastSoundToAll(HGVSEnumSoundUtils.RANDOM_LEVELUP, 10.0F, 1.0F);
		if (HGVSConfiguration.feastsPerPlayer > 0) {
			int n = Math.round((float) HGVSConfiguration.feastsPerPlayer * (HGVSScorboardUtils.getAllPlayersNotInSpectator().size() - (episodes - 1) * HGVSConfiguration.feastsPerPlayer));
			HGVSConfiguration.feastsNumber = n > 0 ? n : 0;
			WorldBorder wb = MinecraftServer.getServer().getEntityWorld().getWorldBorder();
			wb.setTransition(wb.getDiameter(), wb.getDiameter() * HGVSConfiguration.feastsPerPlayer, (HGVSConfiguration.episodeLength * 60) * 1000);
		}
		newPositions();
	}

	public static void newPositions() {
		if (serverConfigManager == null) serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
		World world = MinecraftServer.getServer().getEntityWorld();
		WorldBorder border = world.getWorldBorder();
		posFeasts = func_110670_a(rand, HGVSConfiguration.feastsNumber, border.minX(), border.minZ(), border.maxX(), border.maxZ());
		int l = 0;
		for (int i = 0; i < posFeasts.length; i++) {
			Position position = posFeasts[i];
			if (position != null) {
				l++;
				serverConfigManager.sendChatMsg(new ChatComponentTranslation("HGVS.command.game.feasts", String.format("(%.0f, %.0f)", position.posX, position.posY)));
				BlockPos pos1 = new BlockPos(position.posX, position.func_111092_a(world), position.posY);
				BlockPos pos2 = new BlockPos(position.posX, pos1.getY() + 1, position.posY);
				world.setBlockState(pos1, Blocks.stained_glass.getDefaultState());
				world.setBlockState(pos2, Blocks.chest.getDefaultState());
				TileEntityChest chestTI = (TileEntityChest) (world.getTileEntity(pos2));

				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("CustomName", "Feast " + l);
				compound.setString("id", "Chest");
				compound.setString("Lock", "");
				compound.setInteger("x", pos2.getX());
				compound.setInteger("y", pos2.getY());
				compound.setInteger("z", pos2.getZ());
				NBTTagList nbttaglist = new NBTTagList();
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) 13);
				EnumRandomItemStack.getRandomItemStack().writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
				compound.setTag("Items", nbttaglist);

				chestTI.readFromNBT(compound);
			}
		}
	}

	public static void recreateScoreboard() {
		if (sb == null && MinecraftServer.getServer().getEntityWorld() != null) HGVSTickHandler.sb = MinecraftServer.getServer().getEntityWorld().getScoreboard();
		if (sb != null) {
			HGVSTickHandler.index++;
			if (HGVSTickHandler.index > 3) HGVSTickHandler.index = 0;
			sb.addScoreObjective("HGVS" + index, ScoreDummyCriteria.DUMMY);
			ScoreObjective so = sb.getObjective("HGVS" + index);
			so.setDisplayName(HGVSTickHandler.getScoreboardDisplayName());
			sb.getValueFromObjective(String.format("%3$sPlayers : %4$s%2$s%1$s", HGVSScorboardUtils.getAllPlayersNotInSpectator().size(), EnumChatFormatting.WHITE, EnumChatFormatting.GRAY, EnumChatFormatting.RESET), so).setScorePoints(4);
			sb.getValueFromObjective(String.format("%3$sTeams : %4$s%2$s%1$s", sb.getTeams().size(), EnumChatFormatting.WHITE, EnumChatFormatting.GRAY, EnumChatFormatting.RESET), so).setScorePoints(3);
			sb.getValueFromObjective(String.format("%3$sEpisode : %2$s%1$02d", HGVSTickHandler.episodes, EnumChatFormatting.WHITE, EnumChatFormatting.GRAY), so).setScorePoints(2);
			sb.getValueFromObjective(String.format("%1$02d%4$s:%3$s%2$02d", HGVSTickHandler.minutes, HGVSTickHandler.seconds, EnumChatFormatting.WHITE, EnumChatFormatting.GRAY), so).setScorePoints(1);
			sb.getValueFromObjective(EnumChatFormatting.GOLD + "------", so).setScorePoints(0);
			if (posFeasts == null || posFeasts.length <= 0) newPositions();
			for (int i = 0; i < posFeasts.length; i++) {
				if (posFeasts[i] != null) sb.getValueFromObjective(String.format("Feast%1$01d(%2$d, %3$d)", i + 1, (int) posFeasts[i].posX, (int) posFeasts[i].posY), so).setScorePoints(-(i + 1));
			}
			sb.setObjectiveInDisplaySlot(sb.getObjectiveDisplaySlotNumber("sidebar"), so);
			if (sb.getObjective("HGVS" + (index > 0 ? index - 1 : 3)) != null) sb.func_96519_k(sb.getObjective("HGVS" + (index > 0 ? index - 1 : 3)));
		}
	}

	public static void addScoreboardPositions() {
		if (sb == null && MinecraftServer.getServer().getEntityWorld() != null) HGVSTickHandler.sb = MinecraftServer.getServer().getEntityWorld().getScoreboard();
		if (sb != null) {
			if (sb.getObjective("Health") == null) sb.addScoreObjective("Health", ScoreDummyCriteria.health);
			sb.setObjectiveInDisplaySlot(sb.getObjectiveDisplaySlotNumber("list"), sb.getObjective("Health"));
		}
	}

	private static String getScoreboardDisplayName() {
		String[] s = new String[] {
				"%3$sH%2$sG%2$sV%2$sS %2$s1 %2$sScoreboard%1$s",
				"%2$sH%3$sG%2$sV%2$sS %2$s1 %3$sScoreboard%1$s",
				"%2$sH%2$sG%3$sV%2$sS %2$s1 %4$sScoreboard%1$s",
				"%2$sH%2$sG%2$sV%3$sS %2$s1 %5$sScoreboard%1$s" };
		return String.format(s[HGVSTickHandler.index], new Object[] {
				EnumChatFormatting.RESET,
				EnumChatFormatting.DARK_BLUE,
				EnumChatFormatting.DARK_RED,
				EnumChatFormatting.DARK_GREEN,
				EnumChatFormatting.GOLD });
	}

	public static void load(File f) {
		try {
			if (f.getParentFile() != null) f.getParentFile().mkdirs();
			if (!f.exists() && !f.createNewFile()) HGVS.error("Couldn't create the timer and episodes file");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String s;
			HGVSTickHandler.episodes = Byte.parseByte((s = br.readLine()) != null ? s : "1");
			HGVS.info("Successfuly loaded " + HGVSTickHandler.episodes + " episodes");
			HGVSTickHandler.minutes = Integer.parseInt((s = br.readLine()) != null ? s : "" + HGVSConfiguration.episodeLength);
			HGVS.info("Successfuly loaded " + HGVSTickHandler.minutes + " minutes");
			HGVSTickHandler.seconds = Integer.parseInt((s = br.readLine()) != null ? s : "0");
			HGVS.info("Successfuly loaded " + HGVSTickHandler.seconds + " seconds");
			br.close();
		} catch (IOException e) {
			HGVS.error("Timer and episodes :" + e);
		}
	}

	public static void saveTimer(File f) {
		deleteScoreboards();
		try {
			if (f.getParentFile() != null) f.getParentFile().mkdirs();
			if (!f.exists() && !f.createNewFile()) HGVS.error("Couldn't create timer and episodes file");

			PrintWriter writer = new PrintWriter(f);
			writer.print("");
			writer.println(HGVSTickHandler.episodes);
			writer.println(HGVSTickHandler.minutes);
			writer.println(HGVSTickHandler.seconds);
			writer.close();
			HGVS.info("Timer saved");
		} catch (IOException e) {
			HGVS.error("Timer and episodes :" + e);
		}
	}

	public static void loadFeastsFile(File f) {
		try {
			if (f.getParentFile() != null) f.getParentFile().mkdirs();
			if (!f.exists() && !f.createNewFile()) HGVS.error("Couldn't create the feasts positions file");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String l;
			String s[] = new String[2];
			int i = 0;
			posFeasts = new Position[20];
			while ((l = br.readLine()) != null) {
				s = l.split(" ");
				posFeasts[i] = new Position(Double.parseDouble(s[0] != null ? s[0] : "0"), Double.parseDouble(s[1] != null ? s[1] : "0"));
				i++;
			}
			br.close();
		} catch (IOException e) {
			HGVS.error("Feasts positions :" + e);
		}
	}

	public static void saveFeastPositions(File f) {
		try {
			if (f.getParentFile() != null) f.getParentFile().mkdirs();
			if (!f.exists() && !f.createNewFile()) HGVS.error("Couldn't create feasts positions file");

			PrintWriter writer = new PrintWriter(f);
			writer.print("");
			for (Position p : posFeasts) {
				if (p != null) writer.println(p.posX + " " + p.posY);
			}
			writer.close();
			HGVS.info("Feasts positions saved");
		} catch (IOException e) {
			HGVS.error("Feasts positions :" + e);
		}
	}

	public static void deleteScoreboards() {
		if (sb == null && MinecraftServer.getServer().getEntityWorld() != null) HGVSTickHandler.sb = MinecraftServer.getServer().getEntityWorld().getScoreboard();
		if (sb != null) {
			if (sb.getObjective("HGVS0") != null) sb.func_96519_k(sb.getObjective("HGVS0"));
			if (sb.getObjective("HGVS1") != null) sb.func_96519_k(sb.getObjective("HGVS1"));
			if (sb.getObjective("HGVS2") != null) sb.func_96519_k(sb.getObjective("HGVS2"));
			if (sb.getObjective("HGVS3") != null) sb.func_96519_k(sb.getObjective("HGVS3"));
		}
	}

	public static boolean isPlayerInGameList(String player) {
		for (String player1 : playersGameList) {
			if (player1 != null && player1.equalsIgnoreCase(player)) return true;
		}
		return false;
	}

	public static void addPlayerToGameList(String player) {
		if (player.length() > 0) {
			boolean flag = true;
			for (int i = 0; i < playersGameList.length && flag; i++) {
				if (playersGameList[i] == null) {
					playersGameList[i] = player;
					flag = false;
				}
			}
		}
	}

	public static BlockPos getNearestFeast(EntityPlayer p) {
		if (posFeasts.length < 1 || posFeasts[0] == null) return p.getEntityWorld().getSpawnPoint();
		BlockPos nearestP = posFeasts[0].getBlockPos();
		for (int i = 0; i < posFeasts.length; i++) {
			if (posFeasts[i] != null && p.getDistanceSq(posFeasts[i].getBlockPos()) < p.getDistanceSq(nearestP)) {
				nearestP = posFeasts[i].getBlockPos();
			}
		}
		return nearestP;
	}

	private void spread(List p_110669_2_, Position p_110669_3_, double p_110669_4_, double p_110669_6_, World worldIn, boolean p_110669_9_) throws CommandException {
		Random random = new Random();
		double d2 = p_110669_3_.posX - p_110669_6_;
		double d3 = p_110669_3_.posY - p_110669_6_;
		double d4 = p_110669_3_.posX + p_110669_6_;
		double d5 = p_110669_3_.posY + p_110669_6_;
		Position[] aposition = this.func_110670_a(random, p_110669_9_ ? this.func_110667_a(p_110669_2_) : p_110669_2_.size(), d2, d3, d4, d5);
		int i = this.func_110668_a(p_110669_3_, p_110669_4_, worldIn, random, d2, d3, d4, d5, aposition, p_110669_9_);
		double d6 = this.func_110671_a(p_110669_2_, worldIn, aposition, p_110669_9_);
		serverConfigManager.sendChatMsg(new ChatComponentTranslation("HGVS.command.game.spreading.success"));
	}

	private int func_110667_a(List p_110667_1_) {
		HashSet hashset = Sets.newHashSet();
		Iterator iterator = p_110667_1_.iterator();

		while (iterator.hasNext()) {
			Entity entity = (Entity) iterator.next();

			if (entity instanceof EntityPlayer) {
				hashset.add(((EntityPlayer) entity).getTeam());
			} else {
				hashset.add((Object) null);
			}
		}

		return hashset.size();
	}

	private int func_110668_a(Position p_110668_1_, double p_110668_2_, World worldIn, Random p_110668_5_, double p_110668_6_, double p_110668_8_, double p_110668_10_, double p_110668_12_, Position[] p_110668_14_, boolean p_110668_15_) throws CommandException {
		boolean flag1 = true;
		double d5 = 3.4028234663852886E38D;
		int i;

		for (i = 0; i < 10000 && flag1; ++i) {
			flag1 = false;
			d5 = 3.4028234663852886E38D;
			int k;
			Position position1;

			for (int j = 0; j < p_110668_14_.length; ++j) {
				Position position = p_110668_14_[j];
				k = 0;
				position1 = new Position();

				for (int l = 0; l < p_110668_14_.length; ++l) {
					if (j != l) {
						Position position2 = p_110668_14_[l];
						double d6 = position.func_111099_a(position2);
						d5 = Math.min(d6, d5);

						if (d6 < p_110668_2_) {
							++k;
							position1.posX += position2.posX - position.posX;
							position1.posY += position2.posY - position.posY;
						}
					}
				}

				if (k > 0) {
					position1.posX /= (double) k;
					position1.posY /= (double) k;
					double d7 = (double) position1.func_111096_b();

					if (d7 > 0.0D) {
						position1.func_111095_a();
						position.func_111094_b(position1);
					} else {
						position.func_111097_a(p_110668_5_, p_110668_6_, p_110668_8_, p_110668_10_, p_110668_12_);
					}

					flag1 = true;
				}

				if (position.func_111093_a(p_110668_6_, p_110668_8_, p_110668_10_, p_110668_12_)) {
					flag1 = true;
				}
			}

			if (!flag1) {
				Position[] aposition = p_110668_14_;
				int i1 = p_110668_14_.length;

				for (k = 0; k < i1; ++k) {
					position1 = aposition[k];

					if (!position1.func_111098_b(worldIn)) {
						position1.func_111097_a(p_110668_5_, p_110668_6_, p_110668_8_, p_110668_10_, p_110668_12_);
						flag1 = true;
					}
				}
			}
		}

		if (i >= 10000) {
			throw new CommandException("commands.spreadplayers.failure." + (p_110668_15_ ? "teams" : "players"), new Object[] {
					Integer.valueOf(p_110668_14_.length),
					Double.valueOf(p_110668_1_.posX),
					Double.valueOf(p_110668_1_.posY),
					String.format("%.2f", new Object[] { Double.valueOf(d5) }) });
		} else {
			return i;
		}
	}

	private double func_110671_a(List p_110671_1_, World worldIn, Position[] p_110671_3_, boolean p_110671_4_) {
		double d0 = 0.0D;
		int i = 0;
		HashMap hashmap = Maps.newHashMap();

		for (int j = 0; j < p_110671_1_.size(); ++j) {
			Entity entity = (Entity) p_110671_1_.get(j);
			Position position;

			if (p_110671_4_) {
				Team team = entity instanceof EntityPlayer ? ((EntityPlayer) entity).getTeam() : null;

				if (!hashmap.containsKey(team)) {
					hashmap.put(team, p_110671_3_[i++]);
				}

				position = (Position) hashmap.get(team);
			} else {
				position = p_110671_3_[i++];
			}

			entity.setPositionAndUpdate((double) ((float) MathHelper.floor_double(position.posX) + 0.5F), (double) position.func_111092_a(worldIn), (double) MathHelper.floor_double(position.posY) + 0.5D);
			double d2 = Double.MAX_VALUE;

			for (int k = 0; k < p_110671_3_.length; ++k) {
				if (position != p_110671_3_[k]) {
					double d1 = position.func_111099_a(p_110671_3_[k]);
					d2 = Math.min(d1, d2);
				}
			}

			d0 += d2;
		}

		d0 /= (double) p_110671_1_.size();
		return d0;
	}

	private static Position[] func_110670_a(Random p_110670_1_, int p_110670_2_, double p_110670_3_, double p_110670_5_, double p_110670_7_, double p_110670_9_) {
		Position[] aposition = new Position[p_110670_2_];

		for (int j = 0; j < aposition.length; ++j) {
			Position position = new Position();
			position.func_111097_a(p_110670_1_, p_110670_3_, p_110670_5_, p_110670_7_, p_110670_9_);
			aposition[j] = position;
		}

		return aposition;
	}

	public static class Position
	{
		public double posX;
		public double posY;

		Position() {}

		Position(double p_i1358_1_, double p_i1358_3_) {
			this.posX = p_i1358_1_;
			this.posY = p_i1358_3_;
		}

		public double func_111099_a(Position p_111099_1_) {
			double d0 = this.posX - p_111099_1_.posX;
			double d1 = this.posY - p_111099_1_.posY;
			return Math.sqrt(d0 * d0 + d1 * d1);
		}

		public void func_111095_a() {
			double d0 = (double) this.func_111096_b();
			this.posX /= d0;
			this.posY /= d0;
		}

		public float func_111096_b() {
			return MathHelper.sqrt_double(this.posX * this.posX + this.posY * this.posY);
		}

		public void func_111094_b(Position p_111094_1_) {
			this.posX -= p_111094_1_.posX;
			this.posY -= p_111094_1_.posY;
		}

		public boolean func_111093_a(double p_111093_1_, double p_111093_3_, double p_111093_5_, double p_111093_7_) {
			boolean flag = false;

			if (this.posX < p_111093_1_) {
				this.posX = p_111093_1_;
				flag = true;
			} else if (this.posX > p_111093_5_) {
				this.posX = p_111093_5_;
				flag = true;
			}

			if (this.posY < p_111093_3_) {
				this.posY = p_111093_3_;
				flag = true;
			} else if (this.posY > p_111093_7_) {
				this.posY = p_111093_7_;
				flag = true;
			}

			return flag;
		}

		public int func_111092_a(World worldIn) {
			BlockPos blockpos = new BlockPos(this.posX, 256.0D, this.posY);

			do {
				if (blockpos.getY() <= 0) { return 257; }

				blockpos = blockpos.down();
			} while (worldIn.getBlockState(blockpos).getBlock().getMaterial() == Material.air);

			return blockpos.getY() + 1;
		}

		public boolean func_111098_b(World worldIn) {
			BlockPos blockpos = new BlockPos(this.posX, 256.0D, this.posY);
			Material material;

			do {
				if (blockpos.getY() <= 0) { return false; }

				blockpos = blockpos.down();
				material = worldIn.getBlockState(blockpos).getBlock().getMaterial();
			} while (material == Material.air);

			return !material.isLiquid() && material != Material.fire;
		}

		public void func_111097_a(Random p_111097_1_, double p_111097_2_, double p_111097_4_, double p_111097_6_, double p_111097_8_) {
			this.posX = MathHelper.getRandomDoubleInRange(p_111097_1_, p_111097_2_, p_111097_6_);
			this.posY = MathHelper.getRandomDoubleInRange(p_111097_1_, p_111097_4_, p_111097_8_);
		}

		public BlockPos getBlockPos() {
			return new BlockPos(posX, this.func_111092_a(MinecraftServer.getServer().getEntityWorld()), posY);
		}
	}

	public enum EnumRandomItemStack {
		EMERALD(new ItemStack(Items.emerald), 7),
		DIAMOND_SWORD(new ItemStack(Items.diamond_sword), 0, Items.diamond_sword.getItemEnchantability() - 2),
		GOLDEN_SWORD(new ItemStack(Items.golden_sword), 0, Items.golden_sword.getItemEnchantability()),
		GOLDEN_CARROT(new ItemStack(Items.golden_carrot), 3),
		GHAST_TEAR(new ItemStack(Items.ghast_tear), 2),
		ARROW(new ItemStack(Items.arrow), 47),
		COOKED_BEEF(new ItemStack(Items.cooked_beef), 47),
		BOOK(new ItemStack(Items.book), 0, 20),
		GOLD_INGOT(new ItemStack(Items.gold_ingot), 3),
		IRON_INGOT(new ItemStack(Items.iron_ingot), 8),
		NETHER_WART(new ItemStack(Items.nether_wart), 15),
		FLINT(new ItemStack(Items.flint), 20),
		STRING(new ItemStack(Items.string), 16),
		MELON(new ItemStack(Items.melon), 5),
		DIAMOND(new ItemStack(Items.diamond), 4),
		FISHING_ROD(new ItemStack(Items.fishing_rod)),
		DIAMOND_PICKAXE(new ItemStack(Items.diamond_pickaxe), 0, Items.diamond_pickaxe.getItemEnchantability() - 2),
		IRON_PICKAXE(new ItemStack(Items.iron_pickaxe), 0, Items.iron_pickaxe.getItemEnchantability()),
		RABBIT_STEW(new ItemStack(Items.rabbit_stew)),
		LAVA_BUCKET(new ItemStack(Items.lava_bucket)),
		TNT(new ItemStack(Blocks.tnt), 20),
		SLIMEBALL(new ItemStack(Items.slime_ball), 8),
		ENDER_CHEST(new ItemStack(Blocks.ender_chest), 2),
		SUGAR_CANE(new ItemStack(Items.reeds), 32),
		BREAD(new ItemStack(Items.bread), 64),
		BREWING_STAND(new ItemStack(Items.brewing_stand), 2),
		POTATO(new ItemStack(Items.potato), 15),
		RECORD_CAT(new ItemStack(Items.record_cat)),
		RECORD_FAR(new ItemStack(Items.record_far)),
		JUKEBOX(new ItemStack(Blocks.jukebox)),
		EMPTY_MAP(new ItemStack(Items.map), 4),
		FERMENTED_SPIDER_EYE(new ItemStack(Items.fermented_spider_eye), 4),
		MAGMA_CREAM(new ItemStack(Items.magma_cream), 5);

		public ItemStack stack;
		public int isRandomStack;
		public int isRandomEnchantment;
		private final static Random rand = new Random();

		private EnumRandomItemStack(ItemStack stack, int randomSRange, int randomERange) {
			this.stack = stack;
			this.isRandomStack = randomSRange;
			this.isRandomEnchantment = randomERange;
		}

		private EnumRandomItemStack(ItemStack stack) {
			this(stack, 0, 0);
		}

		private EnumRandomItemStack(ItemStack stack, int r1) {
			this(stack, r1, 0);
		}

		public static ItemStack getRandomItemStack() {
			int i = rand.nextInt(EnumRandomItemStack.values().length);
			ItemStack stack1 = EnumRandomItemStack.values()[i].stack.copy();
			int a = EnumRandomItemStack.values()[i].isRandomStack;
			int b = EnumRandomItemStack.values()[i].isRandomEnchantment;
			if (a > 0) stack1.stackSize = rand.nextInt(a) + 1;
			if (b > 0) EnchantmentHelper.addRandomEnchantment(rand, stack1, b);
			return stack1;
		}
	}
}
