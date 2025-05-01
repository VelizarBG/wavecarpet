package me.wavetech.wavecarpet;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;

@SuppressWarnings("unused")
public class WaveCarpetSettings {
	public static final String WAVETECH = "WaveTech";

	@Rule(categories = { WAVETECH })
	public static boolean stopLightRecalculationDataFix = false;

	@Rule(categories = { WAVETECH, SURVIVAL, COMMAND }, options = { "true", "false", "ops", "0", "1", "2", "3", "4" })
	public static String commandImportStatsToScoreboard = "ops";

	@Rule(categories = { WAVETECH, SURVIVAL, FEATURE })
	public static boolean obtainableInvisibleItemFrames = false;

	@Rule(categories = { WAVETECH, CREATIVE })
	public static boolean disableOpsBypassWhitelist = false;

	@Rule(categories = { WAVETECH, SURVIVAL, COMMAND }, options = { "true", "false", "ops", "0", "1", "2", "3", "4" })
	public static String commandPistonBolt = "false";

	@Rule(categories = { WAVETECH, SURVIVAL })
	public static boolean fixEarlyTicketExpiration = false;
}
