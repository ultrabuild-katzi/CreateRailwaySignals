package de.jannik.createrailwaysignal.track;

import net.minecraft.util.math.Vec3d;

public final class TrackEmbankmentMesh {
    public static final double SHOULDER_WIDTH = 1.6;
    public static final double BASE_WIDTH = 2.6;
    public static final double DEPTH = 0.9;

    private TrackEmbankmentMesh() {}

    /** Clockwise section: left shoulder, right shoulder, right toe, left toe. */
    public static Vec3d[] section(Vec3d position, Vec3d tangent, Vec3d up) {
        Vec3d across = up.crossProduct(tangent).normalize();
        Vec3d horizontal = new Vec3d(0, 1, 0).crossProduct(tangent).normalize();
        Vec3d centre = position.add(0, 0.04, 0);
        Vec3d left = centre.subtract(across.multiply(SHOULDER_WIDTH));
        Vec3d right = centre.add(across.multiply(SHOULDER_WIDTH));
        double baseY = Math.min(left.y, right.y) - DEPTH;
        Vec3d toe = new Vec3d(centre.x, baseY, centre.z);
        return new Vec3d[] {left, right, toe.add(horizontal.multiply(BASE_WIDTH)),
                toe.subtract(horizontal.multiply(BASE_WIDTH))};
    }
}
