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

import net.thenextlvl.gopaint.api.brush.setting.BrushSettings;
import net.thenextlvl.gopaint.api.math.Sphere;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public class SprayBrush extends CraftBrush {

    private static final String DESCRIPTION = "Configurable random chance brush";
    private static final String HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4MGY3NjVlYTgwZGVlMzcwODJkY2RmZDk4MTJlZTM2ZmRhODg0ODY5MmE4NDFiZWMxYmJkOWVkNTFiYTIyIn19fQ==";
    private static final String NAME = "Spray Brush";

    public SprayBrush() {
        super(NAME, DESCRIPTION, HEAD);
    }

    @Override
    public void paint(Location location, Player player, BrushSettings brushSettings) {
        performEdit(player, session -> {
            Stream<Block> blocks = Sphere.getBlocksInRadius(location, brushSettings.getSize(), null, false);
            blocks.filter(block -> passesDefaultChecks(brushSettings, player, block))
                    .filter(block -> brushSettings.getRandom().nextInt(100) < brushSettings.getChance())
                    .forEach(block -> setBlock(session, block, brushSettings.getRandomBlock()));
        });
    }
}