package de.jannik.createrailwaysignal.util;

import net.minecraft.util.math.Vec3d;
import de.jannik.createrailwaysignal.track.TrackEmbankmentMesh;

public final class TrackCantMathTest {

    public static void main(String[] args) {
        for (double turn : new double[] {-135, -90, -30, 0, 30, 90, 135}) {
            for (double slope : new double[] {-0.3, 0, 0.3}) {
                Vec3d axis1 = new Vec3d(1, slope, 0).normalize();
                Vec3d axis2 = new Vec3d(-Math.cos(Math.toRadians(turn)), -slope,
                        -Math.sin(Math.toRadians(turn))).normalize();
                for (double cant : new double[] {-600, -160, 0, 160, 600}) {
                    checkMirroredCurve(axis1, axis2, cant, cant);
                    checkMirroredCurve(axis1, axis2, cant, cant / 2);
                    checkPositionNormal(axis1, cant);
                    checkEmbankment(axis1, cant);
                }
            }
        }
        System.out.println("Track cant, position offset, and embankment geometry regression checks passed");
    }

    private static void checkEmbankment(Vec3d tangent, double cant) {
        Vec3d position = new Vec3d(10, 20, 30);
        Vec3d slopeNormal = TrackCantMath.WORLD_UP.subtract(tangent.multiply(tangent.y)).normalize();
        Vec3d up = TrackCantMath.rotateAroundAxis(slopeNormal, tangent,
                TrackCantMath.angleRadiansFromCantMm(cant));
        Vec3d[] section = TrackEmbankmentMesh.section(position, tangent, up);
        Vec3d[] reversed = TrackEmbankmentMesh.section(position, tangent.multiply(-1), up);
        assertVectorClose(section[0], reversed[1], "Reversing curve moved shoulder");
        assertVectorClose(section[2], reversed[3], "Reversing curve moved toe");
        assertVectorClose(position.add(0, 0.04, 0), section[0].add(section[1]).multiply(0.5),
                "Embankment top is not centred on rails");
        if (Math.abs(section[0].distanceTo(section[1]) - 2 * TrackEmbankmentMesh.SHOULDER_WIDTH) > 1.0e-7)
            throw new AssertionError("Incorrect embankment shoulder width");
        if (section[2].y >= Math.min(section[0].y, section[1].y) || section[2].y != section[3].y)
            throw new AssertionError("Embankment must slope down to a level base");
        for (Vec3d vertex : section)
            if (!Double.isFinite(vertex.x) || !Double.isFinite(vertex.y) || !Double.isFinite(vertex.z))
                throw new AssertionError("Invalid embankment vertex");
    }

    private static void checkPositionNormal(Vec3d tangent, double cant) {
        Vec3d slopeNormal = TrackCantMath.WORLD_UP.subtract(tangent.multiply(tangent.y)).normalize();
        Vec3d banked = TrackCantMath.rotateAroundAxis(slopeNormal, tangent,
                TrackCantMath.angleRadiansFromCantMm(cant));
        Vec3d positionNormal = TrackCantMath.positionNormal(banked, tangent);
        assertVectorClose(slopeNormal, positionNormal, "Cant displaced the train anchor");
        assertVectorClose(positionNormal, TrackCantMath.positionNormal(banked, tangent.multiply(-1)),
                "Reversing the curve changed the anchor");
        assertVectorClose(slopeNormal, TrackCantMath.positionNormal(slopeNormal, tangent),
                "Unbanked slope changed");
        if (cant != 0 && banked.distanceTo(slopeNormal) < 1.0e-3)
            throw new AssertionError("Visual banking normal was lost");
    }

    private static void checkMirroredCurve(Vec3d axis1, Vec3d axis2, double cant1, double cant2) {
        Vec3d normal1 = TrackCantMath.rotateAroundAxis(TrackCantMath.WORLD_UP, axis1,
                TrackCantMath.angleRadiansFromCantMm(cant1));
        Vec3d normal2 = TrackCantMath.rotateAroundAxis(TrackCantMath.WORLD_UP, axis2,
                TrackCantMath.angleRadiansFromCantMm(cant2));
        for (int sample = 0; sample <= 100; sample++) {
            double t = sample / 100.0;
            Vec3d tangent = axis1.multiply(1 - t).subtract(axis2.multiply(t)).normalize();
            Vec3d primary = TrackCantMath.hump(t, normal1, normal2, axis1, axis2, tangent, true);
            Vec3d secondary = TrackCantMath.hump(1 - t, normal2, normal1, axis2, axis1,
                    tangent.multiply(-1), false);
            assertVectorClose(primary, secondary, "Opposite normals at t=" + t);
            if (sample == 0 || sample == 100)
                assertVectorClose(TrackCantMath.WORLD_UP, primary, "Endpoint is not flat");
            double forwardRoll = TrackCantMath.rollDegrees(primary, tangent);
            double reverseRoll = TrackCantMath.rollDegrees(secondary, tangent.multiply(-1));
            if (Math.abs(forwardRoll + reverseRoll) > 1.0e-7)
                throw new AssertionError("Roll must reverse with the render axis at t=" + t);
            if (sample == 50 && cant1 == cant2 && axis1.y == 0) {
                Vec3d expected = TrackCantMath.rotateAroundAxis(TrackCantMath.WORLD_UP, tangent,
                        TrackCantMath.angleRadiansFromCantMm(cant1));
                assertVectorClose(expected, primary, "Primary banking direction changed");
            }
        }
    }

    private static void assertVectorClose(Vec3d expected, Vec3d actual, String message) {
        if (expected.distanceTo(actual) > 1.0e-7)
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }
}
