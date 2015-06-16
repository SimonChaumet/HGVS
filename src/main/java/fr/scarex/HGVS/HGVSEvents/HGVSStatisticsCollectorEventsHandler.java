package fr.scarex.HGVS.HGVSEvents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSConfiguration.HGVSConfiguration;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils.HGVSEnumSoundUtils;
import fr.scarex.HGVS.HGVSUtils.HGVSScorboardUtils;

public class HGVSStatisticsCollectorEventsHandler
{
	public static final Random rand = new Random();
	public static ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
	private static Scoreboard sb;
	private int position = -1;
	public static StatisticsCollectorPlayer[] statPlayers = new StatisticsCollectorPlayer[64];
	public static boolean canrun = false;

	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event) {
		if (event.entityLiving instanceof EntityPlayerMP) {
			EntityPlayerMP p = (EntityPlayerMP) event.entityLiving;
			HGVSBroadcastSoundUtils.broadcastSoundToAll(HGVSEnumSoundUtils.WITHER_SPAWN, 10.0F, 1.0F);
			p.worldObj.spawnEntityInWorld(new EntityXPOrb(p.worldObj, p.posX, p.posY, p.posZ, 12 + rand.nextInt(28)));

			if (MinecraftServer.getServer().isServerInOnlineMode()) {
				Item item = Items.skull;
				ItemStack stack = new ItemStack(item, 1, 3);
				NBTTagCompound compound = new NBTTagCompound();
				NBTTagCompound compound1 = new NBTTagCompound();
				stack.writeToNBT(compound);
				compound1.setString("SkullOwner", p.getName());
				((ItemSkull) item).updateItemStackNBT(compound1);
				compound.setTag("tag", compound1);
				stack.readFromNBT(compound);
				p.worldObj.spawnEntityInWorld(new EntityItem(p.worldObj, p.posX, p.posY, p.posZ, stack));
			}
			
			if (sb == null) sb = MinecraftServer.getServer().getEntityWorld().getScoreboard();
			ScorePlayerTeam st;
			if ((st = sb.getPlayersTeam(p.getName())) != null) sb.removePlayerFromTeam(p.getName(), st);
			boolean flag = true;
			if (HGVSConfiguration.enableStatistics) {
				for (int i = 0; i < statPlayers.length && flag; i++) {
					if (statPlayers[i] == null) statPlayers[i] = new StatisticsCollectorPlayer(p.getName(), HGVSConfiguration.deathsBeforeKick);
					if (statPlayers[i].playerName.equals(p.getName())) {
						if (!statPlayers[i].decreaseDeathCount(1, event.source)) {
							if (HGVSConfiguration.kickPlayerOnDeath)
								p.playerNetServerHandler.kickPlayerFromServer("GG !");
							else
								p.setGameType(GameType.SPECTATOR);
							if (statPlayers[i].getPosition() <= 0) statPlayers[i].setPosition(++position);
						}
						flag = false;
					}
				}
			}
			HGVSScorboardUtils.removeUselessTeams();
		}
	}

	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent event) {
		if (!canrun || HGVSTickHandler.isStarting) {
			event.setCanceled(true);
			event.ammount = 0.0F;
			return;
		}
		if (HGVSConfiguration.enableStatistics && canrun && event.entityLiving instanceof EntityPlayerMP && event.source != null && !((EntityPlayerMP) event.entityLiving).isPotionActive(11)) {
			EntityPlayerMP player = (EntityPlayerMP) event.entityLiving;
			boolean flag = true;
			for (int i = 0; i < statPlayers.length && flag; i++) {
				if (statPlayers[i] == null) statPlayers[i] = new StatisticsCollectorPlayer(player.getName(), HGVSConfiguration.deathsBeforeKick);
				if (statPlayers[i].playerName.equals(player.getName())) {
					statPlayers[i].setDamageBy(event.source, event.ammount > player.getHealth() ? player.getHealth() : event.ammount);
					flag = false;
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingHealEvent(LivingHealEvent event) {
		if (HGVSConfiguration.enableStatistics && event.entityLiving instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.entityLiving;
			boolean flag = true;
			for (int i = 0; i < statPlayers.length && flag; i++) {
				if (statPlayers[i] == null) statPlayers[i] = new StatisticsCollectorPlayer(player.getName(), HGVSConfiguration.deathsBeforeKick);
				if (statPlayers[i].playerName.equals(player.getName())) {
					statPlayers[i].addHealthRegenerate(event.amount);
					flag = false;
				}
			}
		}
	}

	public static void loadStatisticsFiles(File f) {
		File[] filesIn = f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File paramFile, String s) {
				return s.endsWith(".hgvsstats");
			}
		});
		for (int i = 0; i < filesIn.length; i++) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(filesIn[i]));
				String l;
				String[] s = new String[3];
				l = br.readLine();
				s = l.split(" ");

				int position = Integer.parseInt(s[0]);
				int remainingDeaths = Integer.parseInt(s[1]);
				float healthRegenerate = Float.parseFloat(s[2]);

				Map<Integer, String> deathsMap = new HashMap<Integer, String>();
				Map<String, Float> damagesMap = new HashMap<String, Float>();

				while ((l = br.readLine()) != null && !l.equalsIgnoreCase("stop")) {
					s = l.split(" ", 2);
					deathsMap.put(Integer.valueOf(s[0]), s[1]);
				}

				while ((l = br.readLine()) != null) {
					s = l.split(" ");
					String t = "";
					for (int j = 0; j < s.length - 1; j++) {
						t += s[j];
					}
					damagesMap.put(t, Float.valueOf(s[s.length - 1]));
				}
				br.close();

				StatisticsCollectorPlayer stat = new StatisticsCollectorPlayer(filesIn[i].getName().substring(0, filesIn[i].getName().length() - 10), position, remainingDeaths, healthRegenerate);
				stat.deathsMap.putAll(deathsMap);
				stat.damages.putAll(damagesMap);
				statPlayers[i] = stat;

				HGVS.info("The statistics for the player " + filesIn[i].getName().substring(0, filesIn[i].getName().length() - 10) + " has been successfully loaded");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveStatisticsFiles(File f) {
		for (int i = 0; i < statPlayers.length; i++) {
			if (statPlayers[i] != null) {
				StatisticsCollectorPlayer stat = statPlayers[i];
				try {
					if (stat != null) {
						File f1 = new File(f, stat.playerName + ".hgvsstats");
						if (!f1.exists()) f1.createNewFile();
						PrintWriter pw = new PrintWriter(f1);
						pw.print("");

						pw.println(stat.getPosition() + " " + stat.getRemainingDeaths() + " " + stat.getHealthRegenerate());
						Iterator ite = stat.getAllDeaths().iterator();
						while (ite.hasNext()) {
							Entry<Integer, String> e = (Entry<Integer, String>) ite.next();
							pw.println(e.getKey() + " " + e.getValue());
						}
						pw.println("stop");

						Iterator ite1 = stat.getAllDamages().iterator();
						while (ite1.hasNext()) {
							Entry<String, Float> e1 = (Entry<String, Float>) ite1.next();
							pw.println(e1.getKey() + " " + e1.getValue());
						}

						pw.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		HGVS.info("Statistics saved");
	}

	public static class StatisticsCollectorPlayer
	{
		public String playerName;
		private int position = -1;
		private int remainingDeaths;
		private float healthRegenerate = 0;
		private Map<Integer, String> deathsMap = new HashMap<Integer, String>();
		private Map<String, Float> damages = new HashMap<String, Float>();

		public StatisticsCollectorPlayer(String p) {
			this(p, HGVSConfiguration.deathsBeforeKick);
		}

		public StatisticsCollectorPlayer(String p, int deathsBeforeOut) {
			this.playerName = p;
			this.remainingDeaths = deathsBeforeOut;
		}

		public StatisticsCollectorPlayer(String p, int position, int remainingDeaths, float healthRegenerate) {
			this.playerName = p;
			this.position = position;
			this.remainingDeaths = remainingDeaths;
			this.healthRegenerate = healthRegenerate;
		}

		public boolean decreaseDeathCount(int i, DamageSource d) {
			if (remainingDeaths > 0) {
				deathsMap.put(remainingDeaths--, d.getEntity() != null && d.getEntity() instanceof EntityLivingBase ? d.getEntity().getName() : d.getDamageType());
			}
			if (remainingDeaths <= 0) return false;
			return true;
		}

		public void setPosition(int pos) {
			this.position = pos;
		}

		public int getPosition() {
			return this.position;
		}

		public void setDamageBy(DamageSource d, float damage) {
			boolean flag = false;
			Iterator ite = damages.entrySet().iterator();
			while (ite.hasNext() && !flag) {
				Entry<String, Float> entry = (Entry<String, Float>) ite.next();
				if (d.getEntity() != null && d.getEntity() instanceof EntityLivingBase && entry.getKey().equalsIgnoreCase(d.getEntity().getName())) {
					damages.put(entry.getKey(), entry.getValue() + damage);
					flag = true;
				} else if (entry.getKey().equalsIgnoreCase(d.getDamageType())) {
					damages.put(entry.getKey(), entry.getValue() + damage);
					flag = true;
				}
			}
			if (!flag) {
				if (d.getEntity() != null && d.getEntity() instanceof EntityLivingBase) {
					damages.put(d.getEntity().getName(), damage);
				} else {
					damages.put(d.getDamageType(), damage);
				}
			}
		}

		public void setKilledBy(int i, String s) {
			deathsMap.put(Integer.valueOf(i), s);
		}

		public Set<Entry<String, Float>> getAllDamages() {
			return damages.entrySet();
		}

		public Set<Entry<Integer, String>> getAllDeaths() {
			return deathsMap.entrySet();
		}

		public void resetDamages() {
			damages = new HashMap<String, Float>();
		}

		public void resetDeaths() {
			deathsMap = new HashMap<Integer, String>();
		}

		public int getRemainingDeaths() {
			return remainingDeaths;
		}

		public void addHealthRegenerate(float i) {
			healthRegenerate += i;
		}

		public void resetHealthRegenerate() {
			healthRegenerate = 0;
		}

		public float getHealthRegenerate() {
			return healthRegenerate;
		}
	}
}
