/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.runtime;

/** Monotonic clock that exposes elapsed time in Minecraft ticks. */
final class UiAnimationClock {
    private static final double NANOS_PER_TICK = 50_000_000.0;
    private long lastNanos;

    void reset() {
        lastNanos = System.nanoTime();
    }

    void clear() {
        lastNanos = 0L;
    }

    double advance() {
        long now = System.nanoTime();
        if (lastNanos == 0L) {
            lastNanos = now;
            return 0.0;
        }
        double ticks = Math.max(0.0, (now - lastNanos) / NANOS_PER_TICK);
        lastNanos = now;
        return ticks;
    }
}
