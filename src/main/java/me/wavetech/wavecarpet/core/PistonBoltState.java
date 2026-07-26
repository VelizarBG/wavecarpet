package me.wavetech.wavecarpet.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PistonBoltState extends SavedData {
	public static final int VERSION = 1;
	public static final String STATE_VERSION_KEY = "state_version";
	public static final String LOCATION_ENCODINGS_KEY = "location_encodings";
	public static final Codec<Map<String, String>> BOLT_ENCODINGS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
		.xmap(HashMap::new, Function.identity());
	public static final Codec<PistonBoltState> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			BOLT_ENCODINGS_CODEC.fieldOf(LOCATION_ENCODINGS_KEY).forGetter(i -> i.locationEncodings),
			Codec.INT.fieldOf(STATE_VERSION_KEY).forGetter(_ -> VERSION)
		).apply(instance, PistonBoltState::new)
	);
	public static final SavedDataType<PistonBoltState> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("", "wavecarpet_pistonbolt"), PistonBoltState::new, CODEC, null);

	public final Map<String, String> locationEncodings;

	public PistonBoltState() {
		this.locationEncodings = new HashMap<>();
	}

	public PistonBoltState(Map<String, String> locationEncodings) {
		this.locationEncodings = locationEncodings;
	}

	public PistonBoltState(Map<String, String> locationEncodings, int version) {
		this(locationEncodings);
	}
}
