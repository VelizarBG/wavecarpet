package me.wavetech.wavecarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.wavetech.wavecarpet.core.Subscriptions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class SubscribeCommand {
	private SubscribeCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = Commands.literal("subscribe")
			.requires(_ -> Subscriptions.anyAvailable());
		root.then(Commands.literal("toggle").then(subscriptionArgument().executes(context -> {
			var player = context.getSource().getPlayerOrException();
			var id = IdentifierArgument.getId(context, "subscription");
			if (!Subscriptions.isAvailable(id)) {
				context.getSource().sendFailure(Component.literal("That subscription is unavailable."));
				return 0;
			}
			boolean subscribed = player.toggleSubscription$wavecarpet(id);
			String status = subscribed ? "SUBSCRIBED to " : "UNSUBSCRIBED from ";
			player.sendSystemMessage(Component.literal(status + id).withStyle(subscribed ? ChatFormatting.GOLD : ChatFormatting.RED), true);
			return 1;
		})));
		root.then(Commands.literal("query").then(subscriptionArgument().executes(context -> {
			var player = context.getSource().getPlayerOrException();
			var id = IdentifierArgument.getId(context, "subscription");
			if (!Subscriptions.isAvailable(id)) {
				context.getSource().sendFailure(Component.literal("That subscription is unavailable."));
				return 0;
			}
			boolean subscribed = player.hasSubscription$wavecarpet(id);
			String message = subscribed ? "You are subscribed to " : "You are not subscribed to ";
			player.sendSystemMessage(Component.literal(message + id).withStyle(subscribed ? ChatFormatting.GOLD : ChatFormatting.RED), true);
			return 1;
		})));
		dispatcher.register(root);
	}

	private static RequiredArgumentBuilder<CommandSourceStack, Identifier> subscriptionArgument() {
		return RequiredArgumentBuilder.<CommandSourceStack, Identifier>argument("subscription", IdentifierArgument.id())
			.suggests((_, builder) -> (
				SharedSuggestionProvider.suggest(Subscriptions.availableIds().stream().map(Identifier::toString), builder)
			));
	}
}
