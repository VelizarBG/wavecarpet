package me.wavetech.wavecarpet.mixins.command.player.loadItems;

import carpet.commands.PlayerCommand;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.commands.Commands.literal;

@Mixin(value = PlayerCommand.class, remap = false)
public class PlayerCommandMixin {
	@Shadow private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) {
		throw new AssertionError();
	}

	@Shadow private static boolean cantManipulate(CommandContext<CommandSourceStack> context) {
		throw new AssertionError();
	}

	@Definition(id = "argument", method = "Lnet/minecraft/commands/Commands;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;", remap = true)
	@Definition(id = "word", method = "Lcom/mojang/brigadier/arguments/StringArgumentType;word()Lcom/mojang/brigadier/arguments/StringArgumentType;")
	@Expression("argument('player', word())")
	@ModifyExpressionValue(method = "register", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private static <T> RequiredArgumentBuilder<CommandSourceStack, T> insertLoadItemsParameter(RequiredArgumentBuilder<CommandSourceStack, T> original) {
		return original.then(literal("loadItems").executes(context -> {
			if (cantManipulate(context))
				return 0;

			ServerPlayer player = getPlayer(context);
			player.setLoadItems$wavecarpet(!player.getLoadItems$wavecarpet());

			context.getSource().sendSuccess(
				() -> Component.literal(
					(player.getLoadItems$wavecarpet() ? "Enabled" : "Disabled")
						+ " item loading for " + player.getDisplayName().getString()
				),
				false
			);
			return 1;
		}));
	}
}
