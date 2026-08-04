package com.soul.soa_additions.mixin.itemstages;

import net.darkhax.itemstages.Restriction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/**
 * Restores GreedyCraft's ItemStages configuration.
 *
 * <p>ItemStages 2.x (1.12) was configured through {@code config/itemstages.cfg};
 * 8.0.3 (1.20) dropped the config file and hardcodes every protection to
 * {@code true} in the {@link Restriction} constructor, so a plain
 * {@code ItemStages.restrict(...)} behaves far more harshly than GreedyCraft
 * ever did. Rather than chain setters onto ~1,550 script call sites, this
 * restores GreedyCraft's values once, where the defaults are set.</p>
 *
 * <p>GreedyCraft's settings and their 1.20 equivalents:</p>
 * <ul>
 *   <li>{@code hideRestrictionsInJEI=false} &rarr; {@code hideInJEI=false}. Staged
 *       items stay listed in JEI as question marks (see
 *       {@code UnknownItemModels}) instead of vanishing, which is what makes the
 *       progression legible: you can see something exists without learning what.</li>
 *   <li>{@code dropUnknownItems=false} (HardcoreItemStages) &rarr;
 *       {@code preventInventory=false}. GreedyCraft let players carry staged
 *       items; 1.20's default would spit them out of the inventory, destroying
 *       quest rewards and loot handed out ahead of their stage.</li>
 *   <li>No pickup restriction existed in 1.12 &rarr; {@code preventPickup=false}.</li>
 *   <li>{@code allowEquipRestricted=false} &rarr; {@code preventEquipment=true} (kept).</li>
 *   <li>{@code allowInteractRestricted=false} &rarr; {@code preventUsing=true} and
 *       {@code preventAttacking=true} (both kept).</li>
 *   <li>{@code changeRestrictionTooltip=true} &rarr; the hidden name below.</li>
 * </ul>
 */
@Mixin(value = Restriction.class, remap = false)
public abstract class RestrictionDefaultsMixin {

    @Shadow private boolean preventInventory;
    @Shadow private boolean preventPickup;
    @Shadow private boolean hideInJEI;
    @Shadow private Function<ItemStack, Component> hiddenName;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void soa$greedycraftDefaults(String[] stages, CallbackInfo ci) {
        this.preventInventory = false;
        this.preventPickup = false;
        this.hideInJEI = false;
        this.hiddenName = RestrictionDefaultsMixin::soa$unknownName;
    }

    /**
     * GreedyCraft renamed every staged item through
     * {@code ItemStages.setUnfamiliarName} in unfamiliar.zs, with one exception:
     * the wither skeleton skull got its own name so the wither fight read as a
     * deliberate gate rather than a generic unknown.
     */
    private static Component soa$unknownName(ItemStack stack) {
        if (stack.is(Items.WITHER_SKELETON_SKULL)) {
            return Component.translatable("tooltip.soa_additions.cursed_skull");
        }
        return Component.translatable("tooltip.soa_additions.unknown_item");
    }
}
