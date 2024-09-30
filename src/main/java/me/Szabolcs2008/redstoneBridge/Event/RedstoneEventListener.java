package me.Szabolcs2008.redstoneBridge.Event;

import me.Szabolcs2008.redstoneBridge.RedstoneBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;


public class RedstoneEventListener implements Listener {
    @EventHandler
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        FileConfiguration leverConfig = RedstoneBridge.getBridgeConfig();
        Location loc = event.getBlock().getLocation();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        Integer[] coords = {x, y, z};
        FileConfiguration config = Bukkit.getServer().getPluginManager().getPlugin("RedstoneBridge").getConfig();
        if (config.getBoolean("debug")) {
            Bukkit.getLogger().info("Block update: " + Arrays.toString(coords));
        }
        if (RedstoneBridge.validCoordinates.contains(Arrays.toString(coords))) {

            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskLater(Bukkit.getServer().getPluginManager().getPlugin("RedstoneBridge"), () -> {
                Block block = loc.getBlock();
                BlockData blockData = block.getBlockData();
                int powerLevel;
                boolean powered;

                if (blockData instanceof AnaloguePowerable powerable) {
                    powerLevel = powerable.getPower();
                    powered = powerLevel > 0;
                    if (config.getBoolean("debug")) {
                        Bukkit.getLogger().info("AnaloguePowerable at "+Arrays.toString(coords)+" Block power: "+powerLevel);
                    }
                } else {
                    powerLevel = block.getBlockPower();
                    powered = powerLevel > 0;
                    if (config.getBoolean("debug")) {
                        Bukkit.getLogger().info("Not AnaloguePowerable at " + Arrays.toString(coords) + " Block power: " + powerLevel);
                    }
                }


                for (String key : leverConfig.getKeys(false)) {
                    String json;
                    if (leverConfig.getInt(key+".block-x") == x && leverConfig.getInt(key+".block-y") == y && leverConfig.getInt(key+".block-z") == z) {
                        if (leverConfig.getString(key+".mode").equalsIgnoreCase("switch")) {
                            if (config.getBoolean("debug")) {
                                Bukkit.getLogger().info("Registered block updated: "+Arrays.toString(coords)+", Powered: "+powered);
                            }
                            json = "{\"name\": \""+key+"\", \"mode\": \"SWITCH\", \"powered\": "+powered+"}";

                        } else if (leverConfig.getString(key+".mode").equalsIgnoreCase("rgb")) {
                            String color = config.getString("colors."+powerLevel);
                            json = "{\"name\": \""+key+"\", \"mode\": \"RGB\", \"powered\": "+powered+", \"power-level\": "+powerLevel+", \"color\": \""+color+"\"}";
                            if (config.getBoolean("debug")) {
                                Bukkit.getLogger().info("Registered block updated: "+Arrays.toString(coords)+", State: "+powerLevel+", Color: "+config.getString("colors."+powerLevel));
                            }

                        } else if (leverConfig.getString(key+".mode").equalsIgnoreCase("analogue")) {
                            String color = config.getString("colors."+powerLevel);
                            json = "{\"name\": \""+key+"\", \"mode\": \"ANALOGUE\", \"powered\": "+powered+", \"power-level\": "+powerLevel+"}";
                            if (config.getBoolean("debug")) {
                                Bukkit.getLogger().info("Registered block updated: "+Arrays.toString(coords)+", State: "+powerLevel+", Color: "+config.getString("colors."+powerLevel));
                            }

                        } else {
                            json = "{}";
                        }

                        HttpClient client = HttpClient.newHttpClient();

                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(leverConfig.getString(key+".url")))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(json))
                                .build();

                        try {
                            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (resp.statusCode() != 200) {
                                Bukkit.getLogger().warning("Something went wrong. Response code: "+resp.statusCode());
                            }
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }
            }, 2);
        }
    }
}
