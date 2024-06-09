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
package net.onelitefeather.bettergopaint.objects.brush;

import net.onelitefeather.bettergopaint.brush.BrushSettings;
import net.onelitefeather.bettergopaint.objects.other.Settings;
import net.onelitefeather.bettergopaint.utils.Height;
import net.onelitefeather.bettergopaint.utils.Sphere;
import net.onelitefeather.bettergopaint.utils.curve.BezierSpline;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PaintBrush extends Brush {

    private static final HashMap<UUID, List<Location>> selectedPoints = new HashMap<>();

    @Override
    public void paint(final Location target, final Player player, final BrushSettings brushSettings) {
        String prefix = Settings.settings().GENERIC.PREFIX;

        List<Location> locations = selectedPoints.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>());
        locations.add(target);

        if (!player.isSneaking()) {
            player.sendRichMessage(prefix + " Paint brush point #" + locations.size() + " set.");
            return;
        }

        selectedPoints.remove(player.getUniqueId());

        performEdit(player, session -> {
            List<Block> blocks = Sphere.getBlocksInRadiusWithAir(locations.getFirst(), brushSettings.getSize());
            for (Block block : blocks) {
                if (!passesDefaultChecks(brushSettings, player, block)) {
                    continue;
                }

                if (Height.getAverageHeightDiffAngle(block.getLocation(), 1) >= 0.1
                        && Height.getAverageHeightDiffAngle(block.getLocation(), brushSettings.getAngleDistance())
                        >= Math.tan(Math.toRadians(brushSettings.getAngleHeightDifference()))) {
                    continue;
                }

                double rate = (block.getLocation().distance(locations.getFirst()) - (brushSettings.getSize() / 2.0)
                        * ((100.0 - brushSettings.getFalloffStrength()) / 100.0)) / ((brushSettings.getSize() / 2.0)
                        - (brushSettings.getSize() / 2.0) * ((100.0 - brushSettings.getFalloffStrength()) / 100.0));

                if (brushSettings.getRandom().nextDouble() <= rate) {
                    continue;
                }

                LinkedList<Location> newCurve = new LinkedList<>();
                newCurve.add(block.getLocation());
                for (Location location : locations) {
                    newCurve.add(block.getLocation().clone().add(
                            location.getX() - locations.getFirst().getX(),
                            location.getY() - locations.getFirst().getY(),
                            location.getZ() - locations.getFirst().getZ()
                    ));
                }
                BezierSpline spline = new BezierSpline(newCurve);
                double maxCount = (spline.getCurveLength() * 2.5) + 1;

                for (int y = 0; y <= maxCount; y++) {
                    Location location = spline
                            .getPoint(((double) y / maxCount) * (locations.size() - 1))
                            .getBlock().getLocation();

                    if (!location.getChunk().isLoaded() || location.getBlock().isEmpty()) {
                        continue;
                    }

                    if (!passesDefaultChecks(brushSettings, player, block)) {
                        continue;
                    }

                    setBlock(session, location.getBlock(), brushSettings.getRandomBlock());
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Paint Brush";
    }

}
