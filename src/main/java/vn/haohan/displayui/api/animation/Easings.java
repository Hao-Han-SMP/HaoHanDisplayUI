package vn.haohan.displayui.api.animation;

import vn.haohan.displayui.utils.MathUtils;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/** Shared easing functions and interpolation helpers. */
public enum Easings {
    Linear(t -> t), Curve(Easings::curve),
    InSine(t -> 1 - Math.cos(t * Math.PI / 2)), OutSine(t -> Math.sin(t * Math.PI / 2)),
    InOutSine(t -> -(Math.cos(Math.PI * t) - 1) / 2),
    InQuad(t -> t * t), OutQuad(t -> 1 - (1 - t) * (1 - t)),
    InOutQuad(t -> t < .5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2),
    InCubic(t -> t * t * t), OutCubic(t -> 1 - Math.pow(1 - t, 3)),
    InOutCubic(t -> t < .5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2),
    InQuart(t -> Math.pow(t, 4)), OutQuart(t -> 1 - Math.pow(1 - t, 4)),
    InOutQuart(t -> t < .5 ? 8 * Math.pow(t, 4) : 1 - Math.pow(-2 * t + 2, 4) / 2),
    InQuint(t -> Math.pow(t, 5)), OutQuint(t -> 1 - Math.pow(1 - t, 5)),
    InOutQuint(t -> t < .5 ? 16 * Math.pow(t, 5) : 1 - Math.pow(-2 * t + 2, 5) / 2),
    InExpo(t -> t == 0 ? 0 : Math.pow(2, 10 * t - 10)),
    OutExpo(t -> t == 1 ? 1 : 1 - Math.pow(2, -10 * t)),
    InOutExpo(t -> t == 0 || t == 1 ? t : t < .5
            ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2),
    InCircle(t -> 1 - Math.sqrt(1 - t * t)),
    OutCircle(t -> Math.sqrt(1 - (t - 1) * (t - 1))),
    InOutCircle(t -> t < .5
            ? (1 - Math.sqrt(1 - 4 * t * t)) / 2
            : (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2),
    InSin(Easings::sin2), OutSin(t -> 1 - sin2(1 - t)),
    BackOut(Easings::backOut), ElasticOut(Easings::elasticOut), BounceOut(Easings::bounceOut);

    private final DoubleUnaryOperator function;

    Easings(DoubleUnaryOperator function) { this.function = Objects.requireNonNull(function); }

    public double apply(double progress) { return function.applyAsDouble(MathUtils.clamp(progress, 0, 1)); }
    public double inc(Number n) { return apply(n.doubleValue()); }
    public double dec(Number n) { return 1 - inc(n); }
    public double dec(double n, double start, double end) { return MathUtils.lerp(start, end, n); }
    public double get(Number n, boolean invert, boolean flip) {
        double value = n.doubleValue();
        if (invert && flip) return 1 - inc(1 - value);
        if (invert) return inc(1 - value);
        if (flip) return 1 - inc(value);
        return inc(value);
    }

    private static double curve(double t) { return t < .5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2; }
    private static double sin2(double t) { return Math.sin(t * Math.PI / 2); }
    private static double backOut(double t) {
        double c1 = 1.70158;
        return 1 + (c1 + 1) * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }
    private static double elasticOut(double t) {
        if (t == 0 || t == 1) return t;
        return Math.pow(2, -10 * t) * Math.sin((t * 10 - .75) * (2 * Math.PI / 3)) + 1;
    }
    private static double bounceOut(double t) {
        double n = 7.5625, d = 2.75;
        if (t < 1 / d) return n * t * t;
        if (t < 2 / d) { double u = t - 1.5 / d; return n * u * u + .75; }
        if (t < 2.5 / d) { double u = t - 2.25 / d; return n * u * u + .9375; }
        double u = t - 2.625 / d;
        return n * u * u + .984375;
    }
}
