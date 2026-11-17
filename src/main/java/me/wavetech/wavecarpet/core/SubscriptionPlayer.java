package me.wavetech.wavecarpet.core;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

public interface SubscriptionPlayer {
	default Set<Identifier> getSubscriptions$wavecarpet() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default List<Identifier> getAvailableSubscriptions$wavecarpet() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default boolean hasSubscription$wavecarpet(Identifier id) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default boolean hasAvailableSubscription$wavecarpet(Identifier id) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default boolean toggleSubscription$wavecarpet(Identifier id) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
