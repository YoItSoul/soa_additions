package com.soul.soa_additions.compat.jade;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.mining.MiningLevels;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows the mining tier a block needs, and whether what you're holding clears it.
 *
 * <p>Jade's own harvest-tool line walks a fixed list of vanilla tools (wood → netherite) and
 * shows the first that can harvest the block. Souls of Avarice gates a lot of blocks behind the
 * eleven tiers {@link com.soul.soa_additions.registry.SoaTiers} registers above netherite, and
 * for those the list runs out: {@code SimpleToolHandler#test} returns an empty stack and Jade
 * simply prints nothing. This provider fills that gap by naming the required tier instead of
 * trying to name a tool that may not exist as an item — most high-tier picks in this pack are
 * Smithery composed tools, built at runtime from materials rather than registered per tier.</p>
 *
 * <p>The verdict comes from {@code ItemStack#isCorrectToolForDrops}, the same call Forge's
 * {@code canHarvestBlock} makes, so it stays truthful for Smithery tools (which override it) and
 * for the level-3 collision between vanilla diamond and the pack's obsidian tier, where two
 * tiers share a number but not a rank.</p>
 */
public enum MiningLevelProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(SoaAdditions.MODID, "mining_level");

    private static final Component CAN_MINE = Component.literal(" ✔").withStyle(ChatFormatting.GREEN);
    private static final Component CANNOT_MINE = Component.literal(" ✖").withStyle(ChatFormatting.RED);

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        Tier required = MiningLevels.requiredTier(state);
        if (required == null) return;

        MutableComponent line = Component.translatable("soa_additions.jade.mining_level",
                MiningLevels.describe(required)).withStyle(ChatFormatting.GRAY);

        Player player = accessor.getPlayer();
        ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        line.append(held.isCorrectToolForDrops(state) ? CAN_MINE : CANNOT_MINE);

        tooltip.add(line);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    /** Just below Jade's own harvest-tool line, which sits at -8000. */
    @Override
    public int getDefaultPriority() {
        return TooltipPosition.HEAD + 2001;
    }
}
