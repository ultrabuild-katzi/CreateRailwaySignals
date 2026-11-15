package de.jannik.createrailwaysignal.client;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import de.jannik.createrailwaysignal.track.EmbankmentTrack;
import de.jannik.createrailwaysignal.track.TrackEmbankmentMesh;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class TrackEmbankmentRenderer {
    private static final Map<BezierConnection, List<Vec3d[]>> SECTIONS = new WeakHashMap<>();

    private TrackEmbankmentRenderer() {}

    public static void clearCache() {
        SECTIONS.clear();
    }

    public static void render(TrackBlockEntity be, MatrixStack matrices, VertexConsumerProvider buffers) {
        var world = be.getWorld();
        if (world == null)
            return;
        for (BezierConnection curve : be.getConnections().values()) {
            BlockState material = ((EmbankmentTrack) curve).crs$getEmbankment();
            if (material == null || material.isAir())
                continue;
            if (!curve.isPrimary() && world.isChunkLoaded(curve.getKey()))
                continue;
            List<Vec3d[]> sections = SECTIONS.computeIfAbsent(curve, TrackEmbankmentRenderer::buildSections);
            var client = MinecraftClient.getInstance();
            BakedModel model = client.getBlockRenderManager().getModel(material);
            VertexConsumer vertices = buffers.getBuffer(material.isOpaque()
                    ? RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                    : RenderLayer.getEntityTranslucent(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
            Vec3d origin = Vec3d.of(be.getPos());
            Sprite[] sprites = new Sprite[4];
            int[] colours = new int[4];
            Direction[] faces = {Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST};
            for (int face = 0; face < 4; face++) {
                List<BakedQuad> quads = model.getQuads(material, faces[face], Random.create(42));
                if (quads.isEmpty())
                    quads = model.getQuads(material, null, Random.create(42));
                sprites[face] = quads.isEmpty() ? model.getParticleSprite() : quads.get(0).getSprite();
                colours[face] = !quads.isEmpty() && quads.get(0).hasColor()
                        ? client.getBlockColors().getColor(material, world, be.getPos(), quads.get(0).getColorIndex())
                        : 0xFFFFFF;
            }
            for (int segment = 1; segment < sections.size(); segment++) {
                Vec3d[] a = sections.get(segment - 1);
                Vec3d[] b = sections.get(segment);
                int light = WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(a[0].add(a[1]).multiply(0.5)));
                for (int face = 0; face < 4; face++) {
                    int next = (face + 1) % 4;
                    int strips = Math.max(1, (int) Math.ceil(a[face].distanceTo(a[next])));
                    for (int strip = 0; strip < strips; strip++) {
                        double from = strip / (double) strips;
                        double to = (strip + 1) / (double) strips;
                        quad(matrices.peek(), vertices, origin,
                                a[face].lerp(a[next], from), b[face].lerp(b[next], from),
                                b[face].lerp(b[next], to), a[face].lerp(a[next], to),
                                sprites[face], colours[face], light, segment % 2 * 0.5f);
                    }
                }
            }
            cap(matrices.peek(), vertices, origin, sections.get(0), sprites[1], colours[1],
                    WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(curve.getPosition(0))), true);
            cap(matrices.peek(), vertices, origin, sections.get(sections.size() - 1), sprites[1], colours[1],
                    WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(curve.getPosition(1))), false);
        }
    }

    private static List<Vec3d[]> buildSections(BezierConnection curve) {
        List<Vec3d[]> result = new ArrayList<>();
        Vec3d start = curve.starts.getFirst();
        Vec3d end = curve.starts.getSecond();
        Vec3d handle1 = start.add(curve.axes.getFirst().multiply(curve.getHandleLength()));
        Vec3d handle2 = end.add(curve.axes.getSecond().multiply(curve.getHandleLength()));
        for (int i = 0; i <= curve.getSegmentCount(); i++) {
            float t = curve.getSegmentT(i);
            Vec3d tangent = VecHelper.bezierDerivative(start, end, handle1, handle2, t).normalize();
            Vec3d up = RailwaySignalConfigs.renderedTrackNormal(curve.getNormal(t), tangent);
            result.add(TrackEmbankmentMesh.section(curve.getPosition(t), tangent, up));
        }
        return result;
    }

    private static void cap(MatrixStack.Entry entry, VertexConsumer vertices, Vec3d origin, Vec3d[] section,
                            Sprite sprite, int colour, int light, boolean reverse) {
        int strips = (int) Math.ceil(TrackEmbankmentMesh.BASE_WIDTH * 2);
        for (int strip = 0; strip < strips; strip++) {
            double from = strip / (double) strips;
            double to = (strip + 1) / (double) strips;
            Vec3d a = section[0].lerp(section[1], from);
            Vec3d b = section[0].lerp(section[1], to);
            Vec3d c = section[3].lerp(section[2], to);
            Vec3d d = section[3].lerp(section[2], from);
            if (reverse)
                quad(entry, vertices, origin, d, c, b, a, sprite, colour, light, 0);
            else
                quad(entry, vertices, origin, a, b, c, d, sprite, colour, light, 0);
        }
    }

    private static void quad(MatrixStack.Entry entry, VertexConsumer vertices, Vec3d origin,
                             Vec3d a, Vec3d b, Vec3d c, Vec3d d, Sprite sprite, int colour, int light, float v) {
        Vec3d normal = b.subtract(a).crossProduct(d.subtract(a)).normalize();
        float shade = (float) (0.6 + 0.4 * Math.max(0, normal.y));
        vertex(entry, vertices, a.subtract(origin), normal, sprite, colour, light, 0, v, shade);
        vertex(entry, vertices, b.subtract(origin), normal, sprite, colour, light, 0, v + 0.5f, shade);
        vertex(entry, vertices, c.subtract(origin), normal, sprite, colour, light, 1, v + 0.5f, shade);
        vertex(entry, vertices, d.subtract(origin), normal, sprite, colour, light, 1, v, shade);
    }

    private static void vertex(MatrixStack.Entry entry, VertexConsumer vertices, Vec3d position, Vec3d normal,
                               Sprite sprite, int colour, int light, float u, float v, float shade) {
        vertices.vertex(entry.getPositionMatrix(), (float) position.x, (float) position.y, (float) position.z)
                .color((int) (((colour >> 16) & 255) * shade), (int) (((colour >> 8) & 255) * shade),
                        (int) ((colour & 255) * shade), 255)
                .texture(sprite.getFrameU(u * 16), sprite.getFrameV(v * 16))
                .overlay(OverlayTexture.DEFAULT_UV).light(light)
                .normal(entry.getNormalMatrix(), (float) normal.x, (float) normal.y, (float) normal.z).next();
    }
}
