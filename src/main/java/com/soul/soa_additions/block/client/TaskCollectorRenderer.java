package com.soul.soa_additions.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.TaskCollectorBlock;
import com.soul.soa_additions.block.entity.ModBlockEntities;
import com.soul.soa_additions.block.entity.TaskCollectorBlockEntity;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.net.ClientQuestState;
import com.soul.soa_additions.quest.progress.QuestStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Draws the selected task on the collector's display face: quest title, the
 * task description (wrapped), a progress bar, and the live count. Screen-size
 * variants (3×3, 5×5) render the same layout scaled up — the display extends
 * {@code size} blocks up and {@code (size-1)/2} blocks to each side of the
 * anchor block, so a single placed block acts as a wall-sized screen.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TaskCollectorRenderer implements BlockEntityRenderer<TaskCollectorBlockEntity> {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TASK_COLLECTOR.get(),
                ctx -> new TaskCollectorRenderer());
    }

    private static final int BG = 0xC0101820;      // screen background
    private static final int BAR_BG = 0xFF303840;
    private static final int BAR_FG = 0xFF3FC46A;
    private static final int TITLE = 0xFFFFD867;
    private static final int BODY = 0xFFE0E6EA;
    private static final int DIM = 0xFF8A949C;

    @Override
    public void render(TaskCollectorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(be.getBlockState().getBlock() instanceof TaskCollectorBlock)) return;
        Direction facing = be.getBlockState().getValue(TaskCollectorBlock.FACING);
        var level = be.getLevel();
        if (level == null) return;
        // NxN detection: only the anchor block of a complete square draws;
        // covered panels stay blank; ragged extras draw their own 1×1.
        var screen = com.soul.soa_additions.block.ScreenGeometry.screenFor(level, be.getBlockPos(), facing);
        if (screen == null) return;
        int size = screen.size();

        pose.pushPose();
        // Move to the center of the whole screen (world-space, sign-safe),
        // then rotate to the face and step just proud of the surface.
        Direction right = com.soul.soa_additions.block.ScreenGeometry.rightOf(facing);
        double half = (size - 1) / 2.0;
        pose.translate(0.5 + right.getStepX() * half, 0.5 + half, 0.5 + right.getStepZ() * half);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0, 0, 0.501 + (size > 1 ? 0.01 : 0));
        // Screen plane: size×size blocks. Work in a 128×128-per-block pixel space.
        float px = size * 128f;
        pose.scale(size / px, -size / px, size / px);
        pose.translate(-px / 2, -px / 2, 0);

        Font font = Minecraft.getInstance().font;
        Matrix4f mat = pose.last().pose();
        VertexConsumer quads = buffers.getBuffer(RenderType.gui());

        fill(quads, mat, 2, 2, px - 2, px - 2, BG);

        String title = "Task Collector";
        String body = null;
        int count = 0, target = 1;
        boolean hasTask = false;

        String questId = be.selectedQuest();
        if (questId != null) {
            Quest quest = QuestRegistry.quest(questId).orElse(null);
            if (quest != null && be.selectedTask() < quest.tasks().size()) {
                QuestTask task = quest.tasks().get(be.selectedTask());
                title = quest.title();
                body = task.describe();
                target = Math.max(1, task.target());
                count = Math.min(target, ClientQuestState.taskCount(questId, be.selectedTask()));
                if (ClientQuestState.statusOf(questId) == QuestStatus.CLAIMED) count = target;
                hasTask = true;
            }
        }

        float scale = 6f;                 // ~21 chars per line at 128px
        pose.pushPose();
        pose.scale(scale, scale, 1);
        float w = px / scale;
        int y = 2;

        drawCentered(font, pose, buffers, trim(font, title, w - 4), w / 2, y, TITLE);
        y += 11;
        // The face is only px/scale units tall and the progress bar claims the bottom of it, so a
        // 1x1 screen has room for far fewer than three lines. Overflow used to render in mid-air
        // below the block and across the bar; now it is simply dropped.
        float textBottom = (hasTask ? px - 30f - 12f : px) / scale;
        if (body != null) {
            for (String line : wrap(font, body, (int) w - 4, 3)) {
                if (y + 9 > textBottom) break;
                drawCentered(font, pose, buffers, line, w / 2, y, BODY);
                y += 9;
            }
        } else if (y + 9 <= textBottom) {
            drawCentered(font, pose, buffers, "sneak-click to select a task", w / 2, y, DIM);
            y += 9;
        }
        pose.popPose();

        if (hasTask) {
            float pct = count / (float) target;
            float barY = px - 30, barH = 14, barX = 12, barW = px - 24;
            fill(quads, mat, barX, barY, barX + barW, barY + barH, BAR_BG);
            fill(quads, mat, barX + 1, barY + 1, barX + 1 + (barW - 2) * pct, barY + barH - 1, BAR_FG);
            pose.pushPose();
            pose.scale(scale, scale, 1);
            String pctText = String.format("%,d / %,d  (%d%%)", count, target, (int) (pct * 100));
            drawCentered(font, pose, buffers, pctText, px / scale / 2, (barY - 12) / scale, BODY);
            pose.popPose();
        }

        pose.popPose();
    }

    private static void fill(VertexConsumer vc, Matrix4f mat, float x0, float y0, float x1, float y1, int argb) {
        float a = (argb >>> 24) / 255f, r = ((argb >> 16) & 0xFF) / 255f,
              g = ((argb >> 8) & 0xFF) / 255f, b = (argb & 0xFF) / 255f;
        vc.vertex(mat, x0, y1, 0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, 0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, 0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y0, 0).color(r, g, b, a).endVertex();
    }

    private static void drawCentered(Font font, PoseStack pose, MultiBufferSource buffers, String text, float cx, float y, int color) {
        float x = cx - font.width(text) / 2f;
        font.drawInBatch(text, x, y, color, false, pose.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    private static String trim(Font font, String s, float maxWidth) {
        if (font.width(s) <= maxWidth) return s;
        while (!s.isEmpty() && font.width(s + "…") > maxWidth) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    private static List<String> wrap(Font font, String s, int maxWidth, int maxLines) {
        List<String> out = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : s.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.width(candidate) > maxWidth && !line.isEmpty()) {
                out.add(line.toString());
                line = new StringBuilder(word);
                if (out.size() == maxLines - 1) break;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty() && out.size() < maxLines) out.add(trim(font, line.toString(), maxWidth));
        return out;
    }
}
