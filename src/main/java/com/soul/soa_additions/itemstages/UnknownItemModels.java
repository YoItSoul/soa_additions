package com.soul.soa_additions.itemstages;

import com.soul.soa_additions.SoaAdditions;
import net.darkhax.gamestages.event.StagesSyncedEvent;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * GreedyCraft's "Unknown Item" rendering, reimplemented for 1.20.
 *
 * <p>GreedyCraft ran HardcoreItemStages alongside ItemStages with
 * {@code hideUnknownItems=true}, which wrapped every item's override list and
 * swapped in a question-mark model whenever the client player lacked the item's
 * stage. Combined with ItemStages' {@code hideRestrictionsInJEI=false}, staged
 * content stayed listed in JEI as anonymous question marks rather than
 * disappearing — you could see that something existed without learning what.
 * HardcoreItemStages has no 1.20 port, so that behaviour lives here.</p>
 *
 * <p>One deliberate improvement on the original: GreedyCraft rendered a single
 * flat sprite for everything. Here a {@link BlockItem} resolves to the
 * question-mark <em>cube</em> ({@code soa_additions:unknown_block}) and anything
 * else to the flat question-mark <em>sprite</em>
 * ({@code soa_additions:item/unknown_item}), so a hidden block still reads as a
 * block in the inventory.</p>
 *
 * <p>Caching: {@link net.minecraft.client.renderer.entity.ItemRenderer#getModel}
 * runs for every item drawn every frame, while
 * {@link RestrictionManager#getRestriction} walks every stage the player lacks
 * and every restriction filed under it — roughly 1,500 predicates in this pack.
 * Results are therefore memoised per {@link Item} and dropped whenever stages
 * change or restrictions reload. Keying on the item (not the stack) is only
 * sound while no restriction inspects NBT; the pack's restrictions are all
 * item-identity or namespace based. If an NBT-sensitive restriction is ever
 * added — an enchantment restriction, say — this cache must key on the stack.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class UnknownItemModels {

    /** Flat question-mark sprite, for non-block items. */
    public static final ModelResourceLocation UNKNOWN_ITEM =
            new ModelResourceLocation(new ResourceLocation(SoaAdditions.MODID, "unknown_item"), "inventory");

    /** Question-mark cube, for block items. Baked already as the block's own item model. */
    public static final ModelResourceLocation UNKNOWN_BLOCK =
            new ModelResourceLocation(new ResourceLocation(SoaAdditions.MODID, "unknown_block"), "inventory");

    private static final Map<Item, Boolean> HIDDEN_CACHE = new IdentityHashMap<>();

    private UnknownItemModels() {}

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        // The cube form is baked automatically as the item model of the
        // registered unknown_block; only the flat sprite needs requesting.
        event.register(UNKNOWN_ITEM);
        MinecraftForge.EVENT_BUS.addListener(UnknownItemModels::onStagesSynced);
        MinecraftForge.EVENT_BUS.addListener(UnknownItemModels::onRecipesUpdated);
    }

    private static void onStagesSynced(StagesSyncedEvent event) {
        HIDDEN_CACHE.clear();
    }

    private static void onRecipesUpdated(RecipesUpdatedEvent event) {
        HIDDEN_CACHE.clear();
    }

    /** True when this stack is staged away from the local player right now. */
    public static boolean isHidden(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        final Minecraft mc = Minecraft.getInstance();
        final LocalPlayer player = mc.player;
        final ClientLevel level = mc.level;
        if (player == null || level == null) {
            return false;
        }
        final Boolean cached = HIDDEN_CACHE.get(stack.getItem());
        if (cached != null) {
            return cached;
        }
        boolean hidden;
        try {
            hidden = RestrictionManager.INSTANCE.getRestriction(player, stack) != null;
        } catch (RuntimeException e) {
            // Restrictions not built yet (early world load) - treat as visible
            // rather than blanking the whole inventory.
            return false;
        }
        HIDDEN_CACHE.put(stack.getItem(), hidden);
        return hidden;
    }

    /** The stand-in model for a hidden stack, or null when it should render normally. */
    public static BakedModel hiddenModel(ItemStack stack) {
        if (!isHidden(stack)) {
            return null;
        }
        final var models = Minecraft.getInstance().getModelManager();
        if (stack.getItem() instanceof BlockItem) {
            return models.getModel(UNKNOWN_BLOCK);
        }
        // UNKNOWN_ITEM is a standalone model requested via ModelEvent.RegisterAdditional.
        // That overload is deprecated-for-removal in Forge 47 and could re-key the model
        // under a different variant, in which case the lookup yields the missing-model
        // cube. Falling back to the block form keeps a hidden item reading as a question
        // mark rather than as a purple-and-black error box.
        final BakedModel flat = models.getModel(UNKNOWN_ITEM);
        return flat == models.getMissingModel() ? models.getModel(UNKNOWN_BLOCK) : flat;
    }
}
