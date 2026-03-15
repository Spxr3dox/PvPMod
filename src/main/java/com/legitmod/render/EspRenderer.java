package com.legitmod.render;

import com.legitmod.feature.Esp;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class EspRenderer {

    public static void init() {
        WorldRenderEvents.LAST.register(EspRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        if (!Esp.enabled || Esp.trackedPlayers.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack matrices = ctx.matrixStack();
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tess = Tessellator.getInstance();

        Esp.trackedPlayers.forEach((uuid, pos) -> {
            float extra = com.legitmod.feature.HitboxPlus.enabled
                    ? com.legitmod.feature.HitboxPlus.expand : 0f;
            float hw = 0.3f + extra;
            float h  = 1.8f;

            float x1=(float)(pos.x-hw), y1=(float)pos.y,     z1=(float)(pos.z-hw);
            float x2=(float)(pos.x+hw), y2=(float)(pos.y+h), z2=(float)(pos.z+hw);

            // Glow outer layer
            drawBox(tess, mat, x1-0.05f,y1-0.03f,z1-0.05f, x2+0.05f,y2+0.03f,z2+0.05f,
                    0.30f,0.55f,1f,0.10f);
            // Main fill
            drawBox(tess, mat, x1,y1,z1, x2,y2,z2, 0.25f,0.50f,1f,0.42f);
            // Wireframe
            drawWire(tess, mat, x1,y1,z1, x2,y2,z2, 0.5f,0.78f,1f,0.95f);
        });

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f,1f,1f,1f);
        matrices.pop();
    }

    private static void drawBox(Tessellator tess, Matrix4f m,
            float x1,float y1,float z1, float x2,float y2,float z2,
            float r,float g,float b,float a) {
        try {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            q(buf,m, x1,y1,z2, x2,y1,z2, x2,y2,z2, x1,y2,z2, r,g,b,a);
            q(buf,m, x2,y1,z1, x1,y1,z1, x1,y2,z1, x2,y2,z1, r,g,b,a);
            q(buf,m, x1,y1,z1, x1,y1,z2, x1,y2,z2, x1,y2,z1, r,g,b,a);
            q(buf,m, x2,y1,z2, x2,y1,z1, x2,y2,z1, x2,y2,z2, r,g,b,a);
            q(buf,m, x1,y2,z2, x2,y2,z2, x2,y2,z1, x1,y2,z1, r,g,b,a*1.4f);
            q(buf,m, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2, r,g,b,a*0.5f);
            BufferRenderer.drawWithGlobalProgram(buf.end());
        } catch (Exception ignored) {}
    }

    private static void drawWire(Tessellator tess, Matrix4f m,
            float x1,float y1,float z1, float x2,float y2,float z2,
            float r,float g,float b,float a) {
        try {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            ln(buf,m,x1,y1,z1,x2,y1,z1,r,g,b,a); ln(buf,m,x2,y1,z1,x2,y1,z2,r,g,b,a);
            ln(buf,m,x2,y1,z2,x1,y1,z2,r,g,b,a); ln(buf,m,x1,y1,z2,x1,y1,z1,r,g,b,a);
            ln(buf,m,x1,y2,z1,x2,y2,z1,r,g,b,a); ln(buf,m,x2,y2,z1,x2,y2,z2,r,g,b,a);
            ln(buf,m,x2,y2,z2,x1,y2,z2,r,g,b,a); ln(buf,m,x1,y2,z2,x1,y2,z1,r,g,b,a);
            ln(buf,m,x1,y1,z1,x1,y2,z1,r,g,b,a); ln(buf,m,x2,y1,z1,x2,y2,z1,r,g,b,a);
            ln(buf,m,x2,y1,z2,x2,y2,z2,r,g,b,a); ln(buf,m,x1,y1,z2,x1,y2,z2,r,g,b,a);
            BufferRenderer.drawWithGlobalProgram(buf.end());
        } catch (Exception ignored) {}
    }

    private static void q(BufferBuilder b, Matrix4f m,
            float x1,float y1,float z1, float x2,float y2,float z2,
            float x3,float y3,float z3, float x4,float y4,float z4,
            float r,float g,float bl,float a) {
        b.vertex(m,x1,y1,z1).color(r,g,bl,a);
        b.vertex(m,x2,y2,z2).color(r,g,bl,a);
        b.vertex(m,x3,y3,z3).color(r,g,bl,a);
        b.vertex(m,x4,y4,z4).color(r,g,bl,a);
    }

    private static void ln(BufferBuilder b, Matrix4f m,
            float x1,float y1,float z1, float x2,float y2,float z2,
            float r,float g,float bl,float a) {
        b.vertex(m,x1,y1,z1).color(r,g,bl,a);
        b.vertex(m,x2,y2,z2).color(r,g,bl,a);
    }
}