package me.wavetech.wavecarpet.mixins.rule.obtainableInvisibleItemFrames;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	@Shadow private @Nullable String itemName;

	@SuppressWarnings("unchecked")
	@WrapOperation(
		method = "createResult",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/util/StringUtil;isBlank(Ljava/lang/String;)Z",
				args = "")
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
			ordinal = 0
		)
	)
	private <T> T tryConvertRenamedFrame(ItemStack stack, DataComponentType<? super T> component, @Nullable T value, Operation<T> original) {
		Item item = stack.getItem();
		if (WaveCarpetSettings.obtainableInvisibleItemFrames
			&& item instanceof ItemFrameItem && this.itemName.toLowerCase().equals("invisible")) {
			var data = new CompoundTag();
			data.put("id", StringTag.valueOf(
				BuiltInRegistries.ENTITY_TYPE
					.wrapAsHolder(((HangingEntityItemAccessor) item).getType())
					.unwrapKey().orElseThrow().location().toString()
			));
			data.put("Invisible", ByteTag.ONE);
			stack.set(DataComponents.ENTITY_DATA, CustomData.of(data));
			value = (T) Component.literal("Invisible " + item.getDescription().getString())
					.setStyle(Style.EMPTY.withItalic(false));
		}
		//noinspection MixinExtrasOperationParameters
		return original.call(stack, component, value);
	}
}
