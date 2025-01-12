package me.wavetech.wavecarpet.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PistonBoltState extends SavedData {
	public static final int VERSION = 1;
	public static final String STATE_VERSION_KEY = "state_version";
	public static final String LOCATION_ENCODINGS_KEY = "location_encodings";
	private static final Codec<Map<String, String>> BOLT_ENCODINGS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
		.xmap(HashMap::new, Function.identity());

	public final Map<String, String> locationEncodings;

	public PistonBoltState() {
		this.locationEncodings = new HashMap<>();
	}

	public PistonBoltState(Map<String, String> locationEncodings) {
		this.locationEncodings = locationEncodings;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		var encodings = BOLT_ENCODINGS_CODEC.encodeStart(NbtOps.INSTANCE, locationEncodings).getOrThrow();
		tag.put(LOCATION_ENCODINGS_KEY, encodings);
		tag.putInt(STATE_VERSION_KEY, VERSION);
		return tag;
	}

	public static PistonBoltState readNbt(CompoundTag tag, MinecraftServer server) {
		int version = tag.getInt(STATE_VERSION_KEY);
		var encodings = BOLT_ENCODINGS_CODEC.parse(NbtOps.INSTANCE, tag.getCompound(LOCATION_ENCODINGS_KEY)).getOrThrow();
		var pistonBoltState = new PistonBoltState(encodings);
		return pistonBoltState;
	}

	public static PistonBoltState load(MinecraftServer server) {
		DimensionDataStorage stateManager = server.overworld().getDataStorage();
		return stateManager.computeIfAbsent(new SavedData.Factory<>(PistonBoltState::new, (compound, registries) -> readNbt(compound, server), null), "wavecarpet_pistonbolt");
	}
}
