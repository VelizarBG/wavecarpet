package me.wavetech.wavecarpet.client;

import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientSubscriptions {
	private static final Set<Identifier> SUBSCRIPTIONS = new HashSet<>();

	private ClientSubscriptions() {}

	public static boolean has(Identifier subscription) {
		return SUBSCRIPTIONS.contains(subscription);
	}

	static void replace(List<Identifier> subscriptions) {
		SUBSCRIPTIONS.clear();
		SUBSCRIPTIONS.addAll(subscriptions);
	}

	static void clear() {
		SUBSCRIPTIONS.clear();
	}
}
