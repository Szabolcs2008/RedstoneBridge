package me.Szabolcs2008.redstoneBridge;

import me.Szabolcs2008.redstoneBridge.Command.bridgeCommand;
import me.Szabolcs2008.redstoneBridge.Event.RedstoneEventListener;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public final class RedstoneBridge extends JavaPlugin {

    private static File leverConfigFile;
    public static FileConfiguration leverConfig;
    public static ArrayList<String> validCoordinates = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("bridge").setExecutor(new bridgeCommand());
        getCommand("bridge").setTabCompleter(new bridgeCommand());
        leverConfigFile = new File(getDataFolder(), "bridges.yml");
        if (!leverConfigFile.exists()) {
            leverConfigFile.getParentFile().mkdirs();
            saveResource("bridges.yml", false);
        }

        leverConfig = new YamlConfiguration();
        try {
            leverConfig.load(leverConfigFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new RedstoneEventListener(), this);

        for (String key : leverConfig.getKeys(false)) {
            int x = leverConfig.getInt(key+".block-x");
            int y = leverConfig.getInt(key+".block-y");
            int z = leverConfig.getInt(key+".block-z");
            Integer[] coords = {x, y, z};
            getLogger().info(Arrays.toString(coords));
            validCoordinates.add(Arrays.toString(coords));
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static FileConfiguration getBridgeConfig() {
        return leverConfig;
    }

    public static void saveBridges() throws IOException {
        leverConfig.save(leverConfigFile);
    }
}
