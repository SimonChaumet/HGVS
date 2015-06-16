package fr.scarex.HGVS.HGVSConfiguration;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraftforge.common.config.Property;
import fr.scarex.HGVS.HGVS;

public class HGVSConfiguration
{
	public static String CATEGORY;

	public static boolean showLogsInfo = true;
	public static boolean showLogsWarn = true;
	public static boolean showLogsError = true;
	public static int episodeLength = 20;
	public static int minSpreadDistance = 100;
	public static int maxSpreadDistance = 600;
	public static int deathsBeforeKick = 2;
	public static int feastsNumber = 3;
	public static boolean kickPlayerOnPause = true;
	public static boolean kickPlayerOnDeath = false;
	public static boolean doDayLightCycle = true;
	public static boolean disableWeather = false;
	public static boolean enableStatistics = true;
	public static boolean refreshWorldAndStatistics = false;
	public static float feastsPerPlayer = 0;
	public static boolean kickPlayerOnConnection = false;
	public static int numberOfPlayerToStart = 10;
	public static String header = "Hunger Games Version SCAREX";
	public static String footer = "hgvs.verygames.net";

	public static void syncConfig() {
		CATEGORY = HGVS.config.CATEGORY_GENERAL;

		HGVS.config.load();

		List<String> propOrder = new ArrayList<String>();
		Property prop;

		prop = HGVS.config.get(CATEGORY, "showLogsInfo", true);
		prop.comment = "Set this to false to disable logs of type \"info\" from the HGVS mod";
		showLogsInfo = prop.getBoolean(true);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "showLogsWarn", true);
		prop.comment = "Set this to false to disable logs of type \"warn\" from the HGVS mod";
		showLogsWarn = prop.getBoolean(true);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "showLogsError", true);
		prop.comment = "Set this to false to disable logs of type \"error\" from the HGVS mod";
		showLogsError = prop.getBoolean(true);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "episodeLength", 20);
		prop.comment = "This is the number of minutes in an episode";
		episodeLength = prop.getInt(20);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "minSpreadDistance", 100).setMaxValue(29999998);
		prop.comment = "This is the minimum distance to spread players in the map";
		minSpreadDistance = prop.getInt(100);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "maxSpreadDistance", 600).setMaxValue(29999998);
		prop.comment = "This is the maximum distance to spread players in the map";
		maxSpreadDistance = prop.getInt(600);
		propOrder.add(prop.getName());

		if (minSpreadDistance > maxSpreadDistance || minSpreadDistance <= 0) minSpreadDistance = 100;
		if (maxSpreadDistance <= 0) maxSpreadDistance = 600;

		prop = HGVS.config.get(CATEGORY, "deathsBeforeKick", 2);
		prop.comment = "This is the number of deaths accepted (1 means that once dead the player is set in spectator mod)";
		deathsBeforeKick = prop.getInt(2);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "feastsNumber", 3);
		prop.comment = "This is the number of feasts that will be created per episode";
		feastsNumber = prop.getInt(3);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "kickPlayerOnPause", true);
		prop.comment = "Set this to false if you don't want to kick players when the game is paused";
		kickPlayerOnPause = prop.getBoolean(true);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "kickPlayerOnDeath", false);
		prop.comment = "Set this to true if you don't want to set the player in spectator mode once he died";
		kickPlayerOnDeath = prop.getBoolean(false);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "doDayLightCycle", true);
		prop.comment = "Set this to false if you don't want to have the day/night cycle";
		doDayLightCycle = prop.getBoolean(true);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "disableWeather", false);
		prop.comment = "Set this to true if you want to disable rain and thunder";
		disableWeather = prop.getBoolean(false);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "enableStatistics", true);
		prop.comment = "Set this to false to disable the statistics";
		enableStatistics = prop.getBoolean(true);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "refreshWorldAndStatistics", false);
		prop.comment = "Set this to true if you want to reset the world when the game is finished";
		refreshWorldAndStatistics = prop.getBoolean(false);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "feastsPerPlayer", 0).setMinValue(0);
		prop.comment = "Set this to 0 to change it manually OR set a percentage (e.g with 80, first episode : 8, second episode : 7, third episode : 7 etc. The calculation for this example is : \"0.80 * (NUMBER_OF_PLAYER - (NUMBER_OF_EPISODE - 1) * 0.80)\")";
		feastsPerPlayer = (float) prop.getInt(0) / 100;
		if (feastsPerPlayer < 0) feastsPerPlayer = 0;
		propOrder.add(prop.getName());
		
		prop = HGVS.config.get(CATEGORY, "kickPlayerOnConnection", false);
		prop.comment = "Set this to true to kick a player which is not in the game list when he log in";
		kickPlayerOnConnection = prop.getBoolean(false);
		propOrder.add(prop.getName());
		
		prop = HGVS.config.get(CATEGORY, "numberOfPlayerToStart", 10);
		prop.comment = "Change this to wait a number of player before starting";
		numberOfPlayerToStart = prop.getInt(10);
		propOrder.add(prop.getName());

		prop = HGVS.config.get(CATEGORY, "header", "Hunger Games Version SCAREX");
		prop.comment = "This text will appear at the top of the tab overlay";
		header = prop.getString();
		propOrder.add(prop.getName());
		
		prop = HGVS.config.get(CATEGORY, "footer", "hgvs.verygames.net");
		prop.comment = "This text will appear at the bottom of the tab overlay";
		footer = prop.getString();
		propOrder.add(prop.getName());
		
		HGVS.config.setCategoryPropertyOrder(CATEGORY_GENERAL, propOrder);

		if (HGVS.config.hasChanged()) HGVS.config.save();
	}

	public static String getType(String value) {
		if (HGVS.config.getCategory(CATEGORY).containsKey(value)) {
			Property prop = HGVS.config.getCategory(CATEGORY).get(value);
			return prop.isBooleanValue() ? "boolean" : (prop.isIntValue() ? "int" : (prop.isDoubleValue() ? "double" : "string"));
		} else {
			return "null";
		}
	}

	public static String getValue(String value) {
		return HGVS.config.getCategory(CATEGORY).containsKey(value) ? HGVS.config.getCategory(CATEGORY).get(value).getString() : "null";
	}

	public static void setValue(String key, String value) {
		Property prop = HGVS.config.getCategory(CATEGORY).get(key).setValue(value);
		HGVS.config.save();
		syncConfig();
	}

	public static Collection getValidKeys() {
		return HGVS.config.getCategory(CATEGORY).keySet();
	}
}
