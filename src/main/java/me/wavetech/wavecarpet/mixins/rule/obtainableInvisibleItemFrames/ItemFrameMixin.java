package me.wavetech.wavecarpet.mixins.rule.obtainableInvisibleItemFrames;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ ItemFrame.class, GlowItemFrame.class })
public abstract class ItemFrameMixin extends Entity {
	public ItemFrameMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyExpressionValue(method = "getFrameItemStack", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack makeDroppedFrameInvisible(ItemStack stack) {
		Item item = stack.getItem();
		if (WaveCarpetSettings.obtainableInvisibleItemFrames
			&& item instanceof ItemFrameItem && this.isInvisible()) {
			var data = new CompoundTag();
			data.put("id", StringTag.valueOf(
				BuiltInRegistries.ENTITY_TYPE
					.wrapAsHolder(((HangingEntityItemAccessor) item).getType())
					.unwrapKey().orElseThrow().location().toString()
			));
			data.put("Invisible", ByteTag.ONE);
			stack.set(DataComponents.ENTITY_DATA, CustomData.of(data));
			stack.set(
				DataComponents.CUSTOM_NAME,
				Component.literal("Invisible " + item.getName().getString())
					.setStyle(Style.EMPTY.withItalic(false))
			);
		}
		return stack;
	}
}
