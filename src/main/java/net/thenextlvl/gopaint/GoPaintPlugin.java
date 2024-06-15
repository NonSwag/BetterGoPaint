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
package net.thenextlvl.gopaint;

import core.i18n.file.ComponentBundle;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.gopaint.brush.PlayerBrushManager;
import net.thenextlvl.gopaint.command.GoPaintCommand;
import net.thenextlvl.gopaint.command.ReloadCommand;
import net.thenextlvl.gopaint.listeners.ConnectListener;
import net.thenextlvl.gopaint.listeners.InteractListener;
import net.thenextlvl.gopaint.listeners.InventoryListener;
import net.thenextlvl.gopaint.objects.other.Settings;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

@Getter
@Accessors(fluent = true)
public class GoPaintPlugin extends JavaPlugin implements Listener {

    public static final String PAPER_DOCS = "https://jd.papermc.io/paper/1.20.6/org/bukkit/Material.html#enum-constant-summary";

    public static final String USE_PERMISSION = "gopaint.use";
    public static final String ADMIN_PERMISSION = "gopaint.admin";
    public static final String RELOAD_PERMISSION = "gopaint.command.admin.reload";
    public static final String WORLD_BYPASS_PERMISSION = "gopaint.world.bypass";

    private final File translations = new File(getDataFolder(), "translations");
    private final ComponentBundle bundle = new ComponentBundle(translations, audience ->
            audience instanceof Player player ? player.locale() : Locale.US)
            .register("messages", Locale.US)
            .register("messages_german", Locale.GERMANY)
            .miniMessage(bundle -> MiniMessage.builder().tags(TagResolver.resolver(
                    TagResolver.standard(),
                    Placeholder.component("prefix", bundle.component(Locale.US, "prefix"))
            )).build());

    private final PlayerBrushManager brushManager = new PlayerBrushManager(bundle);
    private final Metrics metrics = new Metrics(this, 22279);

    @Override
    public void onEnable() {
        // disable if goPaint and goPaintAdvanced are installed simultaneously
        if (hasOriginalGoPaint()) {
            getComponentLogger().error("goPaintAdvanced is a replacement for goPaint. Please use one instead of both");
            getComponentLogger().error("This plugin is now disabling to prevent future errors");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();

        Material brush = Settings.settings().GENERIC.DEFAULT_BRUSH;
        if (!brush.isItem()) {
            getComponentLogger().error("{} is not a valid default brush, it has to be an item", brush.name());
            getComponentLogger().error("For more information visit {}", PAPER_DOCS);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
    }

    public void reloadConfig() {
        Settings.settings().reload(this, new File(getDataFolder(), "config.yml"));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        Bukkit.getCommandMap().register("gopaint", getPluginMeta().getName(), new GoPaintCommand(this));

        var annotationParser = enableCommandSystem();
        if (annotationParser != null) {
            annotationParser.parse(new ReloadCommand(this));
            annotationParser.parse(new GoPaintCommand(this));
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new InventoryListener(brushManager()), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectListener(brushManager()), this);
    }

    private boolean hasOriginalGoPaint() {
        return Bukkit.getPluginManager().getPlugin("goPaint") != this;
    }

    private @Nullable AnnotationParser<CommandSender> enableCommandSystem() {
        try {
            LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                    this,
                    ExecutionCoordinator.simpleCoordinator()
            );
            if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                commandManager.registerBrigadier();
                getLogger().info("Brigadier support enabled");
            }
            return new AnnotationParser<>(commandManager, CommandSender.class);

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Cannot init command manager");
            return null;
        }
    }

}
