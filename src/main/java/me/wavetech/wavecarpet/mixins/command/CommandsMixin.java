package me.wavetech.wavecarpet.mixins.command;

import com.mojang.brigadier.CommandDispatcher;
import me.wavetech.wavecarpet.commands.ImportStatsToScoreboardCommand;
import me.wavetech.wavecarpet.commands.PistonBoltCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
	@Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false))
	private void addCommands(Commands.CommandSelection selection, CommandBuildContext buildContext, CallbackInfo ci) {
		ImportStatsToScoreboardCommand.register(this.dispatcher);
		PistonBoltCommand.register(this.dispatcher);
	}
}
