/*
 * goPaint is designed to simplify painting inside of Minecraft.
 * Copyright (C) Arcaniax-Development
 * Copyright (C) Arcaniax team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.onelitefeather.bettergopaint.brush;

import net.onelitefeather.bettergopaint.objects.brush.Brush;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record ExportedPlayerBrush(
        Brush brush,
        @Nullable Material mask,
        List<Material> blocks,
        Axis axis,
        boolean surfaceMode,
        int size,
        int chance,
        int thickness,
        int angleDistance,
        int fractureDistance,
        int falloffStrength,
        int mixingStrength,
        double angleHeightDifference
) implements BrushSettings {

    private static final Random RANDOM = new Random();

    public ExportedPlayerBrush(Builder builder) {
        this(
                builder.brush,
                builder.mask,
                builder.blocks,
                builder.axis,
                builder.surfaceMode,
                builder.size,
                builder.chance,
                builder.thickness,
                builder.angleDistance,
                builder.fractureDistance,
                builder.falloffStrength,
                builder.mixingStrength,
                builder.angleHeightDifference
        );
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public boolean maskEnabled() {
        return mask() != null;
    }

    @Override
    public @NotNull Material randomBlock() {
        return blocks().get(random().nextInt(blocks().size()));
    }

    @Override
    public @NotNull Random random() {
        return RANDOM;
    }

    public static Builder builder(Brush brush) {
        return new Builder(brush);
    }

    public static final class Builder {

        private final @NotNull Brush brush;

        private @Nullable Material mask;
        private @NotNull List<Material> blocks = new ArrayList<>();

        private Axis axis;

        private boolean surfaceMode;
        private int size;
        private int chance;
        private int thickness;
        private int angleDistance;
        private int fractureDistance;
        private int falloffStrength;
        private int mixingStrength;
        private double angleHeightDifference;

        private Builder(@NotNull Brush brush) {
            this.brush = brush;
        }

        public Builder surfaceMode(boolean surfaceMode) {
            this.surfaceMode = surfaceMode;
            return this;
        }

        public Builder blocks(@NotNull List<Material> blocks) {
            this.blocks = blocks;
            return this;
        }

        public Builder mask(@Nullable Material mask) {
            this.mask = mask;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder chance(int chance) {
            this.chance = chance;
            return this;
        }

        public Builder thickness(int thickness) {
            this.thickness = thickness;
            return this;
        }

        public Builder angleDistance(int angleDistance) {
            this.angleDistance = angleDistance;
            return this;
        }

        public Builder fractureDistance(int fractureDistance) {
            this.fractureDistance = fractureDistance;
            return this;
        }

        public Builder falloffStrength(int falloffStrength) {
            this.falloffStrength = falloffStrength;
            return this;
        }

        public Builder mixingStrength(int mixingStrength) {
            this.mixingStrength = mixingStrength;
            return this;
        }

        public Builder angleHeightDifference(double angleHeightDifference) {
            this.angleHeightDifference = angleHeightDifference;
            return this;
        }

        public Builder axis(@NotNull Axis axis) {
            this.axis = axis;
            return this;
        }

        public ExportedPlayerBrush build() {
            return new ExportedPlayerBrush(this);
        }

    }

}
