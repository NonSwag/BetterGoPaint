package net.thenextlvl.gopaint.brush.pattern;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import net.thenextlvl.gopaint.api.brush.pattern.BuildPattern;
import net.thenextlvl.gopaint.api.brush.setting.BrushSettings;

public record SplatterPattern(
        EditSession session,
        BlockVector3 position,
        Player player,
        BrushSettings settings
) implements BuildPattern {

    @Override
    public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
        if (settings().getRandom().nextDouble() <= getRate(set)) return false;
        return set.setBlock(extent, getRandomBlockState());
    }

    private double getRate(BlockVector3 position) {
        var size = (double) settings().getBrushSize();
        var falloff = (100.0 - (double) settings().getFalloffStrength()) / 100.0;
        return (position.distance(position()) - size * falloff) / (size - size * falloff);
    }
}
