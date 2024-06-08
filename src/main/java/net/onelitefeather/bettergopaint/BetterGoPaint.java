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
package net.onelitefeather.bettergopaint;

import com.fastasyncworldedit.core.Fawe;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.bettergopaint.command.GoPaintCommand;
import net.onelitefeather.bettergopaint.command.ReloadCommand;
import net.onelitefeather.bettergopaint.listeners.ConnectListener;
import net.onelitefeather.bettergopaint.listeners.InteractListener;
import net.onelitefeather.bettergopaint.listeners.InventoryListener;
import net.onelitefeather.bettergopaint.objects.other.Settings;
import net.onelitefeather.bettergopaint.objects.player.PlayerBrushManager;
import net.onelitefeather.bettergopaint.utils.Constants;
import net.onelitefeather.bettergopaint.utils.DisabledBlocks;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.serverlib.ServerLib;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;


public class BetterGoPaint extends JavaPlugin implements Listener {

    public static boolean plotSquaredEnabled;
    private static PlayerBrushManager manager;
    private static BetterGoPaint betterGoPaint;

    public static BetterGoPaint getGoPaintPlugin() {
        return betterGoPaint;
    }

    public static PlayerBrushManager getBrushManager() {
        return manager;
    }

    public static boolean isPlotSquaredEnabled() {
        return plotSquaredEnabled;
    }

    public void reload() {
        BetterGoPaint.getGoPaintPlugin().reloadConfig();
        manager = new PlayerBrushManager();
        Settings.settings().reload(new File(getDataFolder(), "config.yml"));
    }

    public void onEnable() {
        // Check if we are in a safe environment
        ServerLib.checkUnsafeForks();
        PaperLib.suggestPaper(this);

        // disable if goPaint and BetterGoPaint are installed simultaneously
        if (hasOriginalGoPaint()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //noinspection UnnecessaryUnicodeEscape
        getComponentLogger().info(MiniMessage.miniMessage().deserialize(
                "<white>Made with <red>\u2665</red> <white>in <gradient:black:red:gold>Germany</gradient>"
        ));

        betterGoPaint = this;
        if (!Files.exists(getDataFolder().toPath())) {
            try {
                Files.createDirectories(getDataFolder().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Settings.settings().reload(new File(getDataFolder(), "config.yml"));
        enableBStats();

        manager = new PlayerBrushManager();

        registerListeners();
        registerCommands();
        DisabledBlocks.addBlocks();
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
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ConnectListener(this), this);
        pm.registerEvents(new InteractListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
    }

    private boolean hasOriginalGoPaint() {
        if (getServer().getPluginManager().getPlugin("goPaint") == this) {
            return false;
        }
        getComponentLogger().error(MiniMessage.miniMessage().deserialize(
                "<red>BetterGoPaint is a replacement for goPaint. Please use one instead of both"
        ));
        getComponentLogger().error(MiniMessage.miniMessage().deserialize(
                "<red>This plugin is now disabling to prevent future errors"
        ));

        return true;
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

    private void enableBStats() {
        Metrics metrics = new Metrics(this, Constants.BSTATS_ID);

        metrics.addCustomChart(new SimplePie(
                "faweVersion",
                () -> Fawe.instance().getVersion().toString()
        ));
    }

}