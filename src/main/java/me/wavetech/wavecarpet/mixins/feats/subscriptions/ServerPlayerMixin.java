package me.wavetech.wavecarpet.mixins.feats.subscriptions;

import me.wavetech.wavecarpet.core.SubscriptionPlayer;
import me.wavetech.wavecarpet.core.Subscriptions;
import me.wavetech.wavecarpet.network.SubscriptionsSync;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements SubscriptionPlayer {
	@Unique
	private static final String WAVECARPET_SUBSCRIPTIONS_KEY = "wavecarpet_subscriptions";
	@Unique
	private final Set<Identifier> subscriptions = new HashSet<>();

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void saveSubscriptions(ValueOutput output, CallbackInfo ci) {
		output.store(WAVECARPET_SUBSCRIPTIONS_KEY, Subscriptions.CODEC, List.copyOf(subscriptions));
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void loadSubscriptions(ValueInput input, CallbackInfo ci) {
		input.read(WAVECARPET_SUBSCRIPTIONS_KEY, Subscriptions.CODEC).ifPresent(subscriptions::addAll);
		SubscriptionsSync.sync((ServerPlayer) (Object) this);
	}

	@Override
	public Set<Identifier> getSubscriptions$wavecarpet() {
		return this.subscriptions;
	}

	@Override
	public List<Identifier> getAvailableSubscriptions$wavecarpet() {
		return this.subscriptions.stream().filter(Subscriptions::isAvailable).toList();
	}

	@Override
	public boolean hasSubscription$wavecarpet(Identifier id) {
		return this.subscriptions.contains(id);
	}

	@Override
	public boolean hasAvailableSubscription$wavecarpet(Identifier id) {
		return Subscriptions.isAvailable(id) && this.subscriptions.contains(id);
	}

	@Override
	public boolean toggleSubscription$wavecarpet(Identifier id) {
		boolean subscribed;
		if (!this.subscriptions.remove(id)) {
			this.subscriptions.add(id);
			subscribed = true;
		} else {
			subscribed = false;
		}
		Subscriptions.onSubscriptionChange((ServerPlayer) (Object) this, id, subscribed);
		return subscribed;
	}
}
