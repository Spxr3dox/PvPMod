package com.legitmod.render;

import com.legitmod.feature.TargetStrafe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetMarker {

    private static float animAngle = 0f;

    public static void init() {
        WorldRenderEvents.LAST.register(TargetMarker::render);
    }

    private static void render(WorldRenderContext ctx) {
        if (!TargetStrafe.enabled) return;
        PlayerEntity target = TargetStrafe.getLockedTarget();
        if (target == null || !target.isAlive()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        animAngle += 3.5f;
        if (animAngle >= 360f) animAngle -= 360f;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack matrices = ctx.matrixStack();
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(10.0f);

        Tessellator tess = Tessellator.getInstance();

        double cx = target.getX();
        double cy = target.getY() + 0.9; // chest height
        double cz = target.getZ();

        float hw = 0.36f, hh = 0.9f;

        // ── Spinning diamond ring around target ──────────────────────
        float r  = 0.9f;  // ring radius
        float rad = (float) Math.toRadians(animAngle);

        try {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            int segments = 36;
            for (int i = 0; i < segments; i++) {
                float a1 = (float) Math.toRadians(i * 360f / segments + animAngle);
                float a2 = (float) Math.toRadians((i+1) * 360f / segments + animAngle);
                float pulse = 0.7f + 0.3f * (float)Math.sin(Math.toRadians(animAngle * 3 + i * 15));
                buf.vertex(mat,
                    (float)(cx + Math.sin(a1) * r),
                    (float) cy,
                    (float)(cz + Math.cos(a1) * r))
                    .color(0.2f, 0.85f * pulse, 1f, 0.95f);
                buf.vertex(mat,
                    (float)(cx + Math.sin(a2) * r),
                    (float) cy,
                    (float)(cz + Math.cos(a2) * r))
                    .color(0.2f, 0.85f * pulse, 1f, 0.95f);
            }
            BufferRenderer.drawWithGlobalProgram(buf.end());
        } catch (Exception ignored) {}

        // ── Corner brackets around player box ────────────────────────
        float bx1 = (float)(cx - hw), by1 = (float)(target.getY()),      bz1 = (float)(cz - hw);
        float bx2 = (float)(cx + hw), by2 = (float)(target.getY() + 1.8),bz2 = (float)(cz + hw);
        float cs   = 0.20f; // corner segment length

        try {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            float alpha = 0.75f + 0.25f * (float)Math.abs(Math.sin(Math.toRadians(animAngle * 2)));
            float[] c2 = {0.3f, 0.9f, 1f, alpha};

            // Bottom corners
            corner(buf, mat, bx1, by1, bz1, cs, c2, true,  true,  true);
            corner(buf, mat, bx2, by1, bz1, cs, c2, false, true,  true);
            corner(buf, mat, bx2, by1, bz2, cs, c2, false, true,  false);
            corner(buf, mat, bx1, by1, bz2, cs, c2, true,  true,  false);
            // Top corners
            corner(buf, mat, bx1, by2, bz1, cs, c2, true,  false, true);
            corner(buf, mat, bx2, by2, bz1, cs, c2, false, false, true);
            corner(buf, mat, bx2, by2, bz2, cs, c2, false, false, false);
            corner(buf, mat, bx1, by2, bz2, cs, c2, true,  false, false);

            BufferRenderer.drawWithGlobalProgram(buf.end());
        } catch (Exception ignored) {}

        // ── Vertical beam above head ──────────────────────────────────
        try {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            float beamAlpha = 0.6f + 0.3f * (float)Math.sin(Math.toRadians(animAngle * 4));
            buf.vertex(mat, (float)cx, (float)(target.getY() + 1.8f), (float)cz).color(0.4f, 0.8f, 1f, beamAlpha);
            buf.vertex(mat, (float)cx, (float)(target.getY() + 3.5f), (float)cz).color(0.4f, 0.8f, 1f, 0f);
            BufferRenderer.drawWithGlobalProgram(buf.end());
        } catch (Exception ignored) {}

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.pop();
    }

    private static void corner(BufferBuilder b, Matrix4f m,
            float x, float y, float z, float s, float[] c,
            boolean negX, boolean posY, boolean negZ) {
        float dx = negX ? s : -s;
        float dy = posY ? s : -s;
        float dz = negZ ? s : -s;
        // X edge
        b.vertex(m, x, y, z).color(c[0],c[1],c[2],c[3]);
        b.vertex(m, x+dx, y, z).color(c[0],c[1],c[2],c[3]);
        // Y edge
        b.vertex(m, x, y, z).color(c[0],c[1],c[2],c[3]);
        b.vertex(m, x, y+dy, z).color(c[0],c[1],c[2],c[3]);
        // Z edge
        b.vertex(m, x, y, z).color(c[0],c[1],c[2],c[3]);
        b.vertex(m, x, y, z+dz).color(c[0],c[1],c[2],c[3]);
    }
}
