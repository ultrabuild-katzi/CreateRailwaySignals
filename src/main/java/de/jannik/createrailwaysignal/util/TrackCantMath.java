package de.jannik.createrailwaysignal.util;

import com.simibubi.create.content.trains.track.BezierConnection;
import net.minecraft.util.math.Vec3d;

/**
 * Real-world track cant (Überhöhung) math, applied to Create's curved track connections.
 * <p>
 * Create stores one "face normal" vector per end of a {@link BezierConnection} (the
 * {@code normals} field). Those get interpolated along the curve and crossed with the track's
 * tangent to produce the actual "up" vector used for rendering. By rotating that normal around
 * the tangent (the track's own axis at that end) we can bank a curve exactly like a real railway.
 */
public final class TrackCantMath {

    /** Standard rail gauge in millimeters, used to convert cant (mm) to a tilt angle. */
    public static final double STANDARD_GAUGE_MM = 1435.0;

    /** Practical engineering ceiling for track cant; players can go well beyond real-world limits. */
    public static final double MAX_CANT_MM = 10000.0;

    /** Standard design limit for ballasted track (Schotteroberbau) without special approval. */
    public static final double STANDARD_CANT_MM = 160.0;

    /** The flat/untilted face normal used by ordinary, un-canted track. */
    public static final Vec3d WORLD_UP = new Vec3d(0, 1, 0);

    /** Fraction of a connection's own length used to ramp cant in/out at each end (see {@link #hump}). */
    public static final double TRANSITION_FRACTION = 0.2;

    private TrackCantMath() {
    }

    /**
     * Spherical linear interpolation between two (near-)unit vectors, independent of Create's own
     * {@code VecHelper.slerp} so it can be called directly from a mixin that replaces it.
     */
    public static Vec3d slerp(double t, Vec3d a, Vec3d b) {
        Vec3d an = a.normalize();
        Vec3d bn = b.normalize();
        double dot = clamp(an.dotProduct(bn), -1, 1);
        double theta = Math.acos(dot);
        if (theta < 1.0e-6)
            return a;
        double sinTheta = Math.sin(theta);
        double weightA = Math.sin((1 - t) * theta) / sinTheta;
        double weightB = Math.sin(t * theta) / sinTheta;
        return a.multiply(weightA).add(b.multiply(weightB));
    }

    /**
     * Builds a "hump" up-vector profile for a banked connection: flat (matching whatever plain
     * track touches it) at the literal endpoints {@code t=0}/{@code t=1}, ramping up to the
     * connection's own target bank angle over the first/last {@link #TRANSITION_FRACTION} of its
     * length, and holding that angle in between. This is what removes the visible seam where a
     * canted curve meets ordinary, un-tilted track, without needing to touch the neighboring
     * piece.
     * <p>
     * Unlike Create's own plain {@code slerp(t, fn1, fn2)} (which blends two world-space
     * vectors directly and can swing through an unrelated orientation - including the opposite
     * lean - for anything but a short, simple curve), this interpolates the bank angle as a
     * single signed number relative to each end's own tangent, then re-applies that number
     * around the curve's actual local tangent {@code localTangent} at {@code t}. A plain number
     * can only ramp smoothly from one value to another; it can never flip sides the way two
     * raw vectors can when slerped across a wide turn.
     *
     * @param tangentStart the connection's tangent/axis at {@code t=0} ({@code fn1} was rotated around this)
     * @param tangentEnd   the connection's tangent/axis at {@code t=1} ({@code fn2} was rotated around this)
     * @param localTangent the connection's actual tangent at {@code t} (handles slopes/pitch correctly)
     * @param primary whether this is the primary connection; the secondary swaps endpoint axes
     *                without negating them, but reverses the actual curve tangent
     */
    public static Vec3d hump(double t, Vec3d fn1, Vec3d fn2, Vec3d tangentStart, Vec3d tangentEnd, Vec3d localTangent, boolean primary) {
        double angle1 = signedAngleAroundAxis(WORLD_UP, fn1, tangentStart);
        double angle2 = fn1.equals(fn2) ? angle1 : signedAngleAroundAxis(WORLD_UP, fn2, tangentEnd);
        double naturalAngle = angle1 + (angle2 - angle1) * t;

        double r = TRANSITION_FRACTION;
        double rampFactor = t < r ? t / r : (t > 1 - r ? (1 - t) / r : 1.0);
        double appliedAngle = naturalAngle * rampFactor * (primary ? 1 : -1);

        if (Math.abs(appliedAngle) < 1.0e-9)
            return WORLD_UP;
        return rotateAroundAxis(WORLD_UP, localTangent, appliedAngle);
    }

    /**
     * Estimates the (tightest) radius of a curved connection in meters, by sampling several
     * points along it and computing the circumradius of consecutive triples. Create treats
     * 1 block as 1 meter for train speed math, so block units can be used directly.
     */
    public static double estimateRadiusMeters(BezierConnection bc) {
        double minRadius = Double.POSITIVE_INFINITY;
        int samples = 16;
        for (int i = 1; i < samples; i++) {
            double t0 = (i - 1) / (double) samples;
            double t1 = i / (double) samples;
            double t2 = (i + 1) / (double) samples;
            if (t2 > 1)
                break;

            Vec3d p0 = bc.getPosition(t0);
            Vec3d p1 = bc.getPosition(t1);
            Vec3d p2 = bc.getPosition(t2);
            double radius = circumRadius(p0, p1, p2);
            if (!Double.isNaN(radius) && radius > 0 && radius < minRadius)
                minRadius = radius;
        }
        return minRadius;
    }

    private static double circumRadius(Vec3d a, Vec3d b, Vec3d c) {
        double sideA = b.distanceTo(c);
        double sideB = a.distanceTo(c);
        double sideC = a.distanceTo(b);
        double s = (sideA + sideB + sideC) / 2.0;
        double areaSq = s * (s - sideA) * (s - sideB) * (s - sideC);
        if (areaSq <= 1.0e-9)
            return Double.POSITIVE_INFINITY; // (near) collinear points -> straight track
        double area = Math.sqrt(areaSq);
        return (sideA * sideB * sideC) / (4.0 * area);
    }

    /**
     * The balancing cant (Ausgleichsüberhöhung) u0 = 11.8 * V^2 / R, clamped to the legal
     * absolute ceiling. Returns 0 for straight track (infinite radius) or no speed limit.
     */
    public static double balancingCantMm(double speedKmh, double radiusMeters) {
        if (speedKmh <= 0 || Double.isInfinite(radiusMeters) || radiusMeters <= 0)
            return 0;
        double u0 = 11.8 * speedKmh * speedKmh / radiusMeters;
        return clamp(u0, 0, MAX_CANT_MM);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Converts a cant value in millimeters to the equivalent roll/tilt angle in radians. */
    public static double angleRadiansFromCantMm(double cantMm) {
        double ratio = clamp(cantMm / STANDARD_GAUGE_MM, -1, 1);
        return Math.asin(ratio);
    }

    /** Reads the cant (in millimeters) currently baked into a connection's stored normals. */
    public static double currentCantMm(BezierConnection bc) {
        double angle = signedAngleAroundAxis(new Vec3d(0, 1, 0), bc.normals.getFirst(), bc.axes.getFirst());
        return Math.sin(angle) * STANDARD_GAUGE_MM;
    }

    /**
     * Applies the given cant (mm) to both ends of a connection by rotating a world-up vector
     * around each end's tangent axis. {@code leanRight} is an explicit, unambiguous choice made
     * by the player, using a fixed sign relative to the primary connection rather than guessing
     * from the curve's shape. Applying from either endpoint must store the same physical normals.
     */
    public static void applyCantMm(BezierConnection bc, double cantMm, boolean leanRight) {
        double angle = angleRadiansFromCantMm(Math.abs(cantMm));
        Vec3d tangentStart = bc.axes.getFirst().normalize();
        Vec3d tangentEnd = bc.axes.getSecond().normalize();

        // Stored axes/normals are only swapped by secondary(), not negated. Keep their signed
        // angle unchanged when applying from either end; hump accounts for the reversed tangent.
        double sign = leanRight ? 1 : -1;

        bc.normals.setFirst(rotateAroundAxis(WORLD_UP, tangentStart, sign * angle));
        bc.normals.setSecond(rotateAroundAxis(WORLD_UP, tangentEnd, sign * angle));
    }

    /**
     * Roll/bank angle (degrees) a train bogey should tilt by at a point on the rails with the
     * given face normal and travel tangent, relative to flat/level track. Uses the same signed
     * convention as {@link #applyCantMm}, so a curve's actual cant translates directly into how
     * far a bogey riding it should lean.
     */
    public static double rollDegrees(Vec3d normal, Vec3d tangent) {
        return Math.toDegrees(signedAngleAroundAxis(WORLD_UP, normal, tangent));
    }

    /** Removes bank from the train's one-block anchor offset, retaining the curve's slope. */
    public static Vec3d positionNormal(Vec3d normal, Vec3d tangent) {
        double angle = signedAngleAroundAxis(WORLD_UP, normal, tangent);
        if (Math.abs(angle) < 1.0e-9)
            return normal;
        return rotateAroundAxis(normal, tangent, -angle);
    }

    /** Mirrors bank about the unbanked slope normal, without modifying the stored track data. */
    public static Vec3d renderedNormal(Vec3d normal, Vec3d tangent, boolean invert) {
        if (!invert)
            return normal;
        double angle = signedAngleAroundAxis(WORLD_UP, normal, tangent);
        return rotateAroundAxis(normal, tangent, -2 * angle);
    }

    /** Signed angle (radians) from {@code a} to {@code b}, measured around {@code axis}. */
    public static double signedAngleAroundAxis(Vec3d a, Vec3d b, Vec3d axis) {
        Vec3d k = axis.normalize();
        Vec3d aProj = a.subtract(k.multiply(a.dotProduct(k)));
        Vec3d bProj = b.subtract(k.multiply(b.dotProduct(k)));
        if (aProj.lengthSquared() < 1.0e-9 || bProj.lengthSquared() < 1.0e-9)
            return 0;
        aProj = aProj.normalize();
        bProj = bProj.normalize();
        double cos = aProj.dotProduct(bProj);
        double sin = k.dotProduct(aProj.crossProduct(bProj));
        return Math.atan2(sin, cos);
    }

    /** Rodrigues' rotation formula: rotates {@code v} around unit {@code axis} by {@code theta} radians. */
    public static Vec3d rotateAroundAxis(Vec3d v, Vec3d axis, double theta) {
        Vec3d k = axis.normalize();
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        Vec3d term1 = v.multiply(cos);
        Vec3d term2 = k.crossProduct(v).multiply(sin);
        Vec3d term3 = k.multiply(k.dotProduct(v) * (1 - cos));
        return term1.add(term2).add(term3);
    }
}
