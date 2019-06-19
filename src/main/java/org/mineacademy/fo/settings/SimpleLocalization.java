package org.mineacademy.fo.settings;

import org.mineacademy.fo.Valid;
import org.mineacademy.fo.plugin.SimplePlugin;

/**
 * A simple implementation of a basic localization file.
 * We create the localization/messages_LOCALEPREFIX.yml file
 * automatically and fill it with values from your localization/messages_LOCALEPREFIX.yml
 * file placed within in your plugins jar file.
 */
@SuppressWarnings("unused")
public abstract class SimpleLocalization extends YamlStaticConfig {

	/**
	 * A flag indicating that this class has been loaded
	 *
	 * You can place this class to {@link SimplePlugin#getSettingsClasses()} to make
	 * it load automatically
	 */
	private static boolean localizationClassCalled;

	// --------------------------------------------------------------------
	// Loading
	// --------------------------------------------------------------------

	/**
	 * Create and load the localization/messages_LOCALEPREFIX.yml file.
	 *
	 * See {@link SimpleSettings#LOCALE_PREFIX} for the locale prefix.
	 *
	 * The locale file is extracted from your plugins jar to the localization/ folder
	 * if it does not exists, or updated if it is out of date.
	 */
	@Override
	protected final void load() throws Exception {
		createLocalizationFile(SimpleSettings.LOCALE_PREFIX);
	}

	// --------------------------------------------------------------------
	// Version
	// --------------------------------------------------------------------

	/**
	 * The configuration version number, found in the "Version" key in the file.,
	 */
	protected static Integer VERSION;

	/**
	 * Set and update the config version automatically, however the {@link #VERSION} will
	 * contain the older version used in the file on the disk so you can use
	 * it for comparing in the init() methods
	 *
	 * Please call this as a super method when overloading this!
	 */
	@Override
	protected void beforeLoad() {
		// Load version first so we can use it later
		pathPrefix(null);

		if ((VERSION = getInteger("Version")) != getConfigVersion())
			set("Version", getConfigVersion());
	}

	/**
	 * Return the very latest config version
	 *
	 * Any changes here must also be made to the "Version" key in your settings file.
	 *
	 * @return
	 */
	protected abstract int getConfigVersion();

	// --------------------------------------------------------------------
	// Shared values
	// --------------------------------------------------------------------

	// --------------------------------------------------------------------
	// Sections - you must write them into your locale file
	// --------------------------------------------------------------------

	/**
	 * Locale keys related to your plugin commands
	 * */
	public static class Commands {

		/**
		 * The message at "No_Console" key shown when console is denied executing a command, typically:
		 *
		 * "&cYou may only use this command as a player"
		 *
		 */
		public static String NO_CONSOLE;

		/**
		 * The message at "Reload_Success" key shown when the plugin has been reloaded successfully, typically:
		 *
		 * "{plugin_name} {plugin_version} has been reloaded."
		 */
		public static String RELOAD_SUCCESS;

		/**
		 * The message at "Reload_Fail" key shown when the plugin has failed to reload, typically:
		 *
		 * "&4Oups, &creloading failed! See the console for more information. Error: {error}"
		 */
		public static String RELOAD_FAIL;

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			pathPrefix("Commands");

			NO_CONSOLE = getString("No_Console");
			RELOAD_SUCCESS = getString("Reload_Success");
			RELOAD_FAIL = getString("Reload_Fail");
		}
	}

	// --------------------------------------------------------------------
	// Main localized keys, you must write them into your locale file
	// --------------------------------------------------------------------

	/**
	 * The "Update_Available" key you need to put in your locale file, typically can look like this:
	 *
	 * Update_Available: |-
	 *   &2A new version of &3ChatControl&2 is available.
	 *   &2Current version: &f{current}&2; New version: &f{new}
	 *   &2URL: &7https://www.spigotmc.org/resources/10258/.
	 */
	public static String UPDATE_AVAILABLE;

	/**
	 * The message for player if they lack a permission, typical example:
	 *
	 * "&cInsufficient permission ({permission})."
	 */
	public static String NO_PERMISSION;

	// --------------------------------------------------------------------
	// Optional localized keys
	// --------------------------------------------------------------------

	/**
	 * The message when a section is missing from data.db file (typically we use
	 * this file to store serialized values such as arenas from minigame plugins).
	 *
	 * Default: "&c{name} lacks database information! Please only create {type} in-game! Skipping.."
	 */
	public static String DATA_MISSING;

	/**
	 * The message when the console attempts to start a server conversation which is prevented.
	 *
	 * Default: "Only players may enter this conversation."
	 *
	 */
	public static String CONVERSATION_REQUIRES_PLAYER;

	/**
	 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
	 */
	private static void init() {
		pathPrefix(null);
		Valid.checkBoolean(!localizationClassCalled, "Localization class already loaded!");

		UPDATE_AVAILABLE = getString("Update_Available");
		NO_PERMISSION = getString("No_Permission");
		DATA_MISSING = isSet("Data_Missing") ? getString("Data_Missing") : "&c{name} lacks database information! Please only create {type} in-game! Skipping..";
		CONVERSATION_REQUIRES_PLAYER = isSet("Conversation_Requires_Player") ? getString("Conversation_Requires_Player") : "Only players may enter this conversation.";

		localizationClassCalled = true;
	}

	/**
	 * Was this class loaded?
	 *
	 * @return
	 */
	public static final Boolean isLocalizationCalled() {
		return localizationClassCalled;
	}

	/**
	 * Reset the flag indicating that the class has been loaded,
	 * used in reloading.
	 */
	public static final void resetLocalizationCall() {
		localizationClassCalled = false;
	}
}
