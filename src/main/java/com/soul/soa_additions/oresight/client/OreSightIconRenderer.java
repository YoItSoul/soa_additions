package com.soul.soa_additions.oresight.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soul.soa_additions.oresight.OreSight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom render for the Ore Sight {@link MobEffectInstance} icon — shows the
 * block being tracked instead of a static texture. With multiple ore-sights
 * stacked, cycles through the tracked blocks every 60 ticks (~3s) so the
 * player can see what's currently being highlighted at a glance.
 *
 * <p>Wired in {@link com.soul.soa_additions.oresight.OreSightEffect#initializeClient}.</p>
 */
public final class OreSightIconRenderer implements IClientMobEffectExtensions {

    private static final int CYCLE_TICKS = 60;

    /** Lazy cache of every {@link OreSight#isOreBlock} in the registry,
     *  used for the master ore-sight icon cycle. Built on first access on
     *  the client thread so registries are guaranteed populated. */
    private static List<Block> ALL_ORE_BLOCKS = null;

    private static List<Block> allOreBlocks() {
        List<Block> cached = ALL_ORE_BLOCKS;
        if (cached != null) return cached;
        List<Block> built = new ArrayList<>();
        for (Block b : ForgeRegistries.BLOCKS.getValues()) {
            if (OreSight.isOreBlock(b) && b.asItem() != net.minecraft.world.item.Items.AIR) {
                built.add(b);
            }
        }
        if (built.isEmpty()) built.add(Blocks.IRON_ORE);
        ALL_ORE_BLOCKS = built;
        return built;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance,
                                       EffectRenderingInventoryScreen<?> screen,
                                       GuiGraphics gfx, int x, int y, int blitOffset) {
        return drawIcon(gfx, x + 6, y + 7);
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui,
                                 GuiGraphics gfx, int x, int y, float z, float alpha) {
        return drawIcon(gfx, x + 3, y + 3);
    }

    private boolean drawIcon(GuiGraphics gfx, int x, int y) {
        Block block = pickBlock();
        if (block == null) return false;             // fall through to default icon
        ItemStack stack = new ItemStack(block.asItem());
        if (stack.isEmpty()) return false;

        PoseStack pose = gfx.pose();
        pose.pushPose();
        // Item icons render at 16x16; vanilla effect icon slot is 18x18 inv / 16x16 hud.
        // No scale required — overlays the slot 1:1.
        gfx.renderItem(stack, x, y);
        pose.popPose();
        return true;
    }

    /** Pick which block to render as the effect icon. Cycles every
     *  {@link #CYCLE_TICKS} ticks. With master ore-sight active we cycle
     *  the FULL registry of ore blocks (not just nearby ones) so the HUD
     *  visually conveys "all ores" — that also avoids ever falling through
     *  to the missing {@code master_ore_sight.png} texture. */
    private static Block pickBlock() {
        List<Block> source;
        if (OreSightClient.isMasterActive()) {
            source = allOreBlocks();
        } else {
            source = OreSightClient.trackedSortedByExpiry();
        }
        if (source.isEmpty()) return null;
        if (source.size() == 1) return source.get(0);
        long t = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        int idx = Math.floorMod(t / CYCLE_TICKS, source.size());
        return source.get(idx);
    }
}
