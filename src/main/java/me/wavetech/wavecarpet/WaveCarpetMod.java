package me.wavetech.wavecarpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.wavetech.wavecarpet.core.PistonBoltState;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class WaveCarpetMod implements ModInitializer, CarpetExtension {
	public static final Logger LOGGER = LoggerFactory.getLogger("wavecarpet");

	public static PistonBoltState pistonBoltState;

	@Override
	public void onInitialize() {
		CarpetServer.manageExtension(new WaveCarpetMod());
	}

	@Override
	public void onGameStarted() {
		CarpetServer.settingsManager.parseSettingsClass(WaveCarpetSettings.class);
	}

	@Override
	public void onServerLoadedWorlds(MinecraftServer server) {
		pistonBoltState = server.overworld().getDataStorage().computeIfAbsent(PistonBoltState.TYPE);
	}

	@Override
	public Map<String, String> canHasTranslations(String lang) {
		InputStream langFile = WaveCarpetMod.class.getClassLoader().getResourceAsStream("assets/wavecarpet/lang/%s.json".formatted(lang));
		if (langFile == null) {
			// we don't have that language
			return Collections.emptyMap();
		}
		String jsonData;
		try {
			jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		Gson gson = new GsonBuilder().setLenient().create(); // lenient allows for comments
		return gson.fromJson(jsonData, new TypeToken<Map<String, String>>() {}.getType());
	}
}
