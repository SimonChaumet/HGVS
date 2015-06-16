package fr.scarex.HGVS;

import java.io.File;
import java.lang.reflect.Method;

import net.minecraft.block.BlockFarmland;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import fr.scarex.HGVS.HGVSCommands.HGVSCommandFriend;
import fr.scarex.HGVS.HGVSCommands.HGVSCommands;
import fr.scarex.HGVS.HGVSConfiguration.HGVSConfiguration;
import fr.scarex.HGVS.HGVSEvents.HGVSEventHandler;
import fr.scarex.HGVS.HGVSEvents.HGVSStatisticsCollectorEventsHandler;
import fr.scarex.HGVS.HGVSEvents.HGVSTickHandler;

/**
 * @author SCAREX
 * 
 */
@Mod(modid = HGVS.MODID, name = HGVS.name, version = HGVS.version, acceptableRemoteVersions = HGVS.acceptableRemoteVersions)
public class HGVS
{
	/* ====================Basic setup beginning==================== */

	protected static final String MODID = "HGVS";
	protected static final String name = "Hunger Gamers SCAREX version";
	protected static final String version = "1.8R1.0.0";
	protected static final String acceptableRemoteVersions = "*";

	@Instance("HGVS")
	public static HGVS instance;
	private static Logger logger;
	private static File hgvsFolderBase;
	public File friendRequestsFile;
	public File timerAEpisodesFile;
	public File posFeastsFile;
	private static File hgvsStatsFile;
	public static Configuration config;

	public static final String getModid() {
		return MODID;
	}

	public static final String getName() {
		return name;
	}

	public static final String getVersion() {
		return version;
	}

	public static final String getAcceptableremoteversions() {
		return acceptableRemoteVersions;
	}

	public static final File getModDirectory() {
		return hgvsFolderBase;
	}

	public static final File getHGVSStatsFile() {
		return hgvsStatsFile;
	}

	public static final void info(String s) {
		if (HGVSConfiguration.showLogsInfo) logger.info(s);
	}

	public static final void info(Object e) {
		if (HGVSConfiguration.showLogsInfo) logger.info(e);
	}

	public static final void warn(String s) {
		if (HGVSConfiguration.showLogsWarn) logger.warn(s);
	}

	public static final void warn(Object e) {
		if (HGVSConfiguration.showLogsWarn) logger.warn(e);
	}

	public static final void warn(Object e, Throwable t) {
		if (HGVSConfiguration.showLogsWarn) logger.warn(e, t);
	}

	public static final void error(String s) {
		if (HGVSConfiguration.showLogsError) logger.error(s);
	}

	public static final void error(Object o) {
		if (HGVSConfiguration.showLogsError) logger.error(o);
	}

	public static final void error(Object e, Throwable t) {
		if (HGVSConfiguration.showLogsError) logger.error(e, t);
	}

	/* ====================Basic setup end==================== */

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		HGVSConfiguration.syncConfig();

		this.logger = event.getModLog();
		this.info("HGVS initializing");
		this.hgvsFolderBase = new File(event.getModConfigurationDirectory(), "HGVS");
		if (!hgvsFolderBase.exists() && !hgvsFolderBase.mkdirs()) this.error("Couldn't create the HGVS folder !");
		this.friendRequestsFile = new File(getModDirectory(), "friendRequests.hgvs");
		this.timerAEpisodesFile = new File(getModDirectory(), "TimerAEpisodes.hgvs");
		this.posFeastsFile = new File(getModDirectory(), "posFeasts.hgvs");

		HGVSCommandFriend.getFriendRequests(this.friendRequestsFile);
		HGVSTickHandler.load(this.timerAEpisodesFile);
		HGVSTickHandler.loadFeastsFile(this.posFeastsFile);

		this.hgvsStatsFile = new File(getModDirectory(), "Stats");
		if (!hgvsStatsFile.exists() && !hgvsStatsFile.mkdirs()) this.error("Couldn't create the HGVS Stats folder !");
		HGVSStatisticsCollectorEventsHandler.loadStatisticsFiles(getHGVSStatsFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		HGVSEventHandler.registerEvents();
	}

	@EventHandler
	public void onSeverStarting(FMLServerStartingEvent event) {
		HGVSCommands.registerCommands();
		HGVSEventHandler.registerTickEvents();
	}

	@EventHandler
	public void onServerStarted(FMLServerStartedEvent event) {
		HGVSTickHandler.deleteScoreboards();
		HGVSTickHandler.recreateScoreboard();
		HGVSTickHandler.addScoreboardPositions();
		HGVSTickHandler.updateMOTD(EnumChatFormatting.DARK_GREEN + "Game will start soon");
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		if (!hgvsFolderBase.exists() && !hgvsFolderBase.mkdirs()) this.error("Couldn't create the HGVS folder");
		HGVSCommandFriend.registerFriendRequests(this.friendRequestsFile);
		HGVSTickHandler.saveTimer(this.timerAEpisodesFile);
		HGVSTickHandler.saveFeastPositions(this.posFeastsFile);

		if (!hgvsStatsFile.exists() && !hgvsStatsFile.mkdirs()) this.error("Couldn't create the HGVS Stats folder !");
		HGVSStatisticsCollectorEventsHandler.saveStatisticsFiles(getHGVSStatsFile());
	}

	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if (HGVSConfiguration.refreshWorldAndStatistics && HGVSTickHandler.isStopped) {
			HGVS.info("deleting files");
			File f1 = new File(getModDirectory().getParentFile().getParentFile(), MinecraftServer.getServer().getFolderName());
			File f2 = getModDirectory();

			try {
				FileUtils.deleteDirectory(f1);
				FileUtils.deleteDirectory(f2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
