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
package net.thenextlvl.gopaint.brush;

import net.thenextlvl.gopaint.objects.brush.Brush;
import net.thenextlvl.gopaint.objects.other.SurfaceMode;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.*;

public record ExportedPlayerBrush(
        Brush brush,
        @Nullable Material mask,
        List<Material> blocks,
        Axis axis,
        SurfaceMode surfaceMode,
        int size,
        int chance,
        int thickness,
        int angleDistance,
        int fractureDistance,
        int falloffStrength,
        int mixingStrength,
        double angleHeightDifference
) implements BrushSettings {

    private static final Random RANDOM = new SecureRandom();

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
    public Material randomBlock() {
        return blocks().get(random().nextInt(blocks().size()));
    }

    @Override
    public Random random() {
        return RANDOM;
    }

    public static Builder builder(Brush brush) {
        return new Builder(brush);
    }

    public static final class Builder {

        private final Brush brush;

        private List<Material> blocks = Collections.emptyList();
        private Axis axis = Axis.Y; // todo: plugin.config().GENERIC.DEFAULT_AXIS;
        private SurfaceMode surfaceMode = SurfaceMode.DISABLED;

        private @Nullable Material mask;

        private int size;
        private int chance;
        private int thickness;
        private int angleDistance;
        private int fractureDistance;
        private int falloffStrength;
        private int mixingStrength;
        private double angleHeightDifference;

        private Builder(Brush brush) {
            this.brush = brush;
        }

        public Builder surfaceMode(SurfaceMode surfaceMode) {
            this.surfaceMode = surfaceMode;
            return this;
        }

        public Builder blocks(List<Material> blocks) {
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

        public Builder axis(Axis axis) {
            this.axis = axis;
            return this;
        }

        public ExportedPlayerBrush build() {
            return new ExportedPlayerBrush(this);
        }

    }

    @Deprecated(forRemoval = true)
    public static ExportedPlayerBrush parse(Brush brush, ItemMeta itemMeta) {
        ExportedPlayerBrush.Builder builder = ExportedPlayerBrush.builder(brush);
        Optional.ofNullable(itemMeta.getLore()).ifPresent(lore -> lore.stream()
                .map(line -> line.replace("§8", ""))
                .forEach(line -> {
                    if (line.startsWith("Size: ")) {
                        builder.size(Integer.parseInt(line.substring(6)));
                    } else if (line.startsWith("Chance: ")) {
                        builder.chance(Integer.parseInt(line.substring(8, line.length() - 1)));
                    } else if (line.startsWith("Thickness: ")) {
                        builder.thickness(Integer.parseInt(line.substring(11)));
                    } else if (line.startsWith("Axis: ")) {
                        builder.axis(Axis.valueOf(line.substring(6).toUpperCase()));
                    } else if (line.startsWith("FractureDistance: ")) {
                        builder.fractureDistance(Integer.parseInt(line.substring(18)));
                    } else if (line.startsWith("AngleDistance: ")) {
                        builder.angleDistance(Integer.parseInt(line.substring(15)));
                    } else if (line.startsWith("AngleHeightDifference: ")) {
                        builder.angleHeightDifference(Double.parseDouble(line.substring(23)));
                    } else if (line.startsWith("Mixing: ")) {
                        builder.mixingStrength(Integer.parseInt(line.substring(8)));
                    } else if (line.startsWith("Falloff: ")) {
                        builder.falloffStrength(Integer.parseInt(line.substring(9)));
                    } else if (line.startsWith("Blocks: ")) {
                        builder.blocks(Arrays.stream(line.substring(8).split(", "))
                                .map(Material::matchMaterial)
                                .filter(Objects::nonNull)
                                .toList());
                    } else if (line.startsWith("Mask: ")) {
                        builder.mask(Material.matchMaterial(line.substring(6)));
                    } else if (line.startsWith("Surface Mode: ")) {
                        SurfaceMode.byName(line.substring(14)).ifPresent(builder::surfaceMode);
                    }
                }));
        return builder.build();
    }

}
