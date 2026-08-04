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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * <p>The swap is installed at bake time by replacing every {@code inventory}
 * variant in the baked model registry with an {@link UnknownItemModelWrapper}.
 * An earlier attempt hooked {@code ItemRenderer#getModel} with a mixin instead;
 * that could never work in this project, because the build deliberately ships no
 * refmap (mixins here otherwise target only deobfuscated mod classes) and so a
 * Mojang-named vanilla method reference never resolves against the SRG-named
 * runtime.</p>
 *
 * <p>One deliberate improvement on the original: GreedyCraft rendered a single
 * flat sprite for everything. Here a {@link BlockItem} resolves to the
 * question-mark <em>cube</em> ({@code soa_additions:unknown_block}) and anything
 * else to the flat question-mark <em>sprite</em>
 * ({@code soa_additions:item/unknown_item}), so a hidden block still reads as a
 * block in the inventory.</p>
 *
 * <p>Caching: the override runs for every item drawn every frame, while
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

    private static final Logger LOG = LogManager.getLogger("SOA-UnknownItems");

    private static final Map<Item, Boolean> HIDDEN_CACHE = new IdentityHashMap<>();

    /** Set once the flat sprite has been reported missing, so the warning is not per-frame. */
    private static boolean warnedMissingFlat;

    private UnknownItemModels() {}

    /**
     * Verifies that both stand-in models actually baked.
     *
     * <p>Both are the item models of registered entries ({@code ModItems.UNKNOWN_ITEM},
     * {@code ModBlocks.UNKNOWN_BLOCK}), so they bake like any other item and this
     * should never fire. It exists because the failure mode is silent — a missing
     * stand-in just renders as the wrong question mark, which is easy to miss and
     * was in fact missed once already.</p>
     */
    @SubscribeEvent
    public static void verifyModels(ModelEvent.BakingCompleted event) {
        final var models = event.getModelManager();
        if (models.getModel(UNKNOWN_ITEM) == models.getMissingModel()) {
            LOG.warn("{} did not bake; hidden items will render as the question-mark cube.", UNKNOWN_ITEM);
        }
        if (models.getModel(UNKNOWN_BLOCK) == models.getMissingModel()) {
            LOG.warn("{} did not bake; hidden blocks will render as the missing model.", UNKNOWN_BLOCK);
        }
    }

    /**
     * Wraps every baked item model so a staged stack resolves to a question mark.
     *
     * <p>Only {@code inventory} variants are touched, so block models in the world
     * are left exactly as their owning mod baked them. The two question-mark
     * stand-ins are skipped so they can never hide themselves.</p>
     *
     * <p>Fired on a worker thread, but the map is ours alone for the duration and
     * {@link Map.Entry#setValue} is not a structural modification, so replacing
     * values while iterating is safe.</p>
     *
     * <p>Runs at {@link EventPriority#LOWEST} so that mods which <em>replace</em>
     * entries in this map — CTM's connected-texture pass, MysticalAgriculture's
     * crop models — have already had their turn. Wrapping last makes this the
     * outermost model, which is the only position where the swap cannot be
     * discarded by a later listener.</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void wrapItemModels(ModelEvent.ModifyBakingResult event) {
        int wrapped = 0;
        for (Map.Entry<ResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            final ResourceLocation key = entry.getKey();
            if (!(key instanceof ModelResourceLocation model) || !"inventory".equals(model.getVariant())) {
                continue;
            }
            if (UNKNOWN_ITEM.equals(key) || UNKNOWN_BLOCK.equals(key)) {
                continue;
            }
            final BakedModel baked = entry.getValue();
            if (baked == null || baked instanceof UnknownItemModelWrapper) {
                continue;
            }
            entry.setValue(new UnknownItemModelWrapper(baked));
            wrapped++;
        }
        HIDDEN_CACHE.clear();
        LOG.debug("Wrapped {} item models for unknown-item rendering.", wrapped);
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
            // rather than blanking the whole inventory. Deliberately not cached.
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
        final BakedModel flat = models.getModel(UNKNOWN_ITEM);
        if (flat != models.getMissingModel()) {
            return flat;
        }
        // Should be unreachable now that unknown_item is a registered item. Falling back
        // to the cube keeps a hidden item reading as a question mark rather than as a
        // purple-and-black error box, but say so once - silently degrading here is what
        // made every hidden item render as a block for a build.
        if (!warnedMissingFlat) {
            warnedMissingFlat = true;
            LOG.warn("{} is unbaked; falling back to the cube for non-block items.", UNKNOWN_ITEM);
        }
        return models.getModel(UNKNOWN_BLOCK);
    }

    /**
     * Drops the memoised hidden/visible verdicts when the inputs behind them change.
     *
     * <p>Separate from the outer class because these ride the Forge bus while the
     * model events ride the mod bus.</p>
     */
    @Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class CacheInvalidation {

        private CacheInvalidation() {}

        /** Stage grants and revokes arrive here on the client. */
        @SubscribeEvent
        public static void onStagesSynced(StagesSyncedEvent event) {
            HIDDEN_CACHE.clear();
        }

        /** Stands in for datapack sync, which is when restrictions are rebuilt. */
        @SubscribeEvent
        public static void onRecipesUpdated(RecipesUpdatedEvent event) {
            HIDDEN_CACHE.clear();
        }
    }
}
