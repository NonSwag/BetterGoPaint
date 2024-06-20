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

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.TextComponent;
import net.thenextlvl.gopaint.GoPaintPlugin;
import net.thenextlvl.gopaint.api.brush.Brush;
import net.thenextlvl.gopaint.api.brush.BrushController;
import net.thenextlvl.gopaint.api.brush.setting.ItemBrushSettings;
import net.thenextlvl.gopaint.api.brush.setting.PlayerBrushSettings;
import net.thenextlvl.gopaint.brush.setting.CraftItemBrushSettings;
import net.thenextlvl.gopaint.brush.setting.CraftPlayerBrushSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CraftBrushController implements BrushController {
    private final Map<UUID, PlayerBrushSettings> playerBrushes = new HashMap<>();
    private final GoPaintPlugin plugin;

    @Override
    public PlayerBrushSettings getBrushSettings(Player player) {
        return playerBrushes.computeIfAbsent(player.getUniqueId(), ignored -> new CraftPlayerBrushSettings(plugin));
    }

    @Override
    public Optional<ItemBrushSettings> parseBrushSettings(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return Optional.empty();
        var meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasLore() || !meta.hasDisplayName()) return Optional.empty();
        if (!(meta.displayName() instanceof TextComponent name)) return Optional.empty();
        var key = NamespacedKey.fromString(name.content());
        if (key == null) return Optional.empty();
        var brush = plugin.brushRegistry().getBrush(key);
        return brush.map(current -> parseBrushSettings(current, meta));
    }

    @Override
    public ItemBrushSettings parseBrushSettings(Brush brush, ItemMeta itemMeta) {
        return CraftItemBrushSettings.parse(brush, itemMeta);
    }

    @Override
    public void removeBrushSettings(Player player) {
        playerBrushes.remove(player.getUniqueId());
    }
}
