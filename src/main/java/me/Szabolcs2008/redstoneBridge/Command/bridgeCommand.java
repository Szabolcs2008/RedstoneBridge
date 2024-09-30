package me.Szabolcs2008.redstoneBridge.Command;

import me.Szabolcs2008.redstoneBridge.RedstoneBridge;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class bridgeCommand implements CommandExecutor, TabCompleter {
    private void sendHelp(CommandSender commandSender) {
        commandSender.sendMessage("                     §c§lRedstoneBridge                      ");
        commandSender.sendMessage("");
        commandSender.sendMessage(" §f/bridge §chelp §r| §7shows this help");
        commandSender.sendMessage(" §f/bridge §clist §r| §7Lists all active bridges");
        commandSender.sendMessage(" §f/bridge §cadd §4<name> <mode> <x> <y> <z> §r| §7Maps a redstone component.");
        commandSender.sendMessage(" §f/bridge §cseturl §4<name> <url> §r| §7Sets the url of the redstone component.");
        commandSender.sendMessage(" §f/bridge §cmove §4<name> <x> <y> <z> §r| §7Moves the checked block.");
        commandSender.sendMessage(" §f/bridge §cremove §4<name> §r| §7Deletes a mapped redstone component");


    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sendHelp(commandSender);
        } else if (args[0].equalsIgnoreCase("help")) {
            sendHelp(commandSender);
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 6) {
                commandSender.sendMessage("Usage: /bridge add <name> <mode> <x> <y> <z>");
                return false;
            } else {
                String name = args[1];
                String mode = args[2];
                int x = Integer.parseInt(args[3]);
                int y = Integer.parseInt(args[4]);
                int z = Integer.parseInt(args[5]);

                FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
                bridgeConfig.createSection(name);
                bridgeConfig.set(name+".mode", mode);
                bridgeConfig.set(name+".block-x", x);
                bridgeConfig.set(name+".block-y", y);
                bridgeConfig.set(name+".block-z", z);
                bridgeConfig.set(name+".url", "");
                try {
                    RedstoneBridge.saveBridges();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                RedstoneBridge.validCoordinates.add(Arrays.toString(new Integer[]{x, y, z}));
                commandSender.sendMessage("§7Successfully added §c"+name+"§7 (pos: §4"+x+" §a"+y+" §9"+z+"§7, mode: §b"+mode+"§7)");

            }
        } else if (args[0].equalsIgnoreCase("seturl")) {
            if (args.length != 3) {
                commandSender.sendMessage("Usage: /bridge seturl <name> <url>");
                return false;
            }
            FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
            String name = args[1];
            String url = args[2];

            bridgeConfig.set(name+".url", url);

            try {
                RedstoneBridge.saveBridges();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            commandSender.sendMessage("§7Url updated for §c"+name);
        } else if (args[0].equalsIgnoreCase("move")) {
            if (args.length != 5) {
                commandSender.sendMessage("Usage: /bridge move <name> <x> <y> <z>");
                return false;
            }
            FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
            String name = args[1];
            int last_x = bridgeConfig.getInt(name+".block-x");
            int last_y = bridgeConfig.getInt(name+".block-y");
            int last_z = bridgeConfig.getInt(name+".block-z");

            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);

            bridgeConfig.set(name+".block-x", x);
            bridgeConfig.set(name+".block-y", y);
            bridgeConfig.set(name+".block-z", z);

            try {
                RedstoneBridge.saveBridges();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            commandSender.sendMessage("§7Moved §c"+name+"§7 to §4"+x+" §a"+y+" §9"+z);
            RedstoneBridge.validCoordinates.remove(Arrays.toString(new Integer[]{last_x, last_y, last_z}));
            RedstoneBridge.validCoordinates.add(Arrays.toString(new Integer[]{x, y, z}));


        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                commandSender.sendMessage("Usage: /bridge remove <name>");
                return false;
            }
            FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
            String name = args[1];
            int x = bridgeConfig.getInt(name+".block-x");
            int y = bridgeConfig.getInt(name+".block-y");
            int z = bridgeConfig.getInt(name+".block-z");

            bridgeConfig.set(name, null);

            RedstoneBridge.validCoordinates.remove(Arrays.toString(new Integer[]{x, y, z}));
            commandSender.sendMessage("§7Removed §c"+name);
        } else if (args[0].equalsIgnoreCase("list")) {
            commandSender.sendMessage("§7Active bridges:");
            FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
            for (String name : bridgeConfig.getKeys(false)) {
                ConfigurationSection bridge = bridgeConfig.getConfigurationSection(name);
                commandSender.sendMessage(" §c§n"+name);
                commandSender.sendMessage(" §8| §7Position: §4"+bridge.getInt("block-x")+" §a"+bridge.getInt("block-y")+" §9"+bridge.getInt("block-z"));
                commandSender.sendMessage(" §8| §7Mode: §b"+bridge.getString("mode"));
                commandSender.sendMessage(" §8| §7URL: §f§o"+bridge.getString("url"));

            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> tabCompletions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("bridge")) {
            if (args.length == 1) {
                String[] commands = {"help", "add", "remove", "setUrl", "move", "list"};
                StringUtil.copyPartialMatches(args[0], Arrays.asList(commands), tabCompletions);

            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String[] modes = {"SWITCH", "RGB", "ANALOGUE"};
                    StringUtil.copyPartialMatches(args[2], Arrays.asList(modes), tabCompletions);
                } else if (args.length == 4 || args.length == 5 || args.length == 6) {
                    if (commandSender instanceof Player player) {
                        Block targetedBlock = player.getTargetBlockExact(3);
                        if (targetedBlock != null) {
                            if (args.length == 4) {
                                String x = args[3];
                                if (x.isEmpty()) {
                                    x = String.valueOf(targetedBlock.getLocation().getBlockX());
                                }
                                tabCompletions.add(x + " " + targetedBlock.getLocation().getBlockY() + " " + targetedBlock.getLocation().getBlockZ());
                            } else if (args.length == 5) {
                                String y = args[4];
                                if (y.isEmpty()) {
                                    y = String.valueOf(targetedBlock.getLocation().getBlockY());
                                }
                                tabCompletions.add(y + " " + targetedBlock.getLocation().getBlockZ());
                            } else {
                                String z = args[5];
                                if (z.isEmpty()) {
                                    z = String.valueOf(targetedBlock.getLocation().getBlockZ());
                                }
                                tabCompletions.add(z);
                            }
                        }
                    }
                }

            } else if (args[0].equalsIgnoreCase("seturl") && args.length == 2){
                FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
                StringUtil.copyPartialMatches(args[1], bridgeConfig.getKeys(false), tabCompletions);

            } else if (args[0].equalsIgnoreCase("move")) {
                if (args.length == 2) {
                    FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
                    StringUtil.copyPartialMatches(args[1], bridgeConfig.getKeys(false), tabCompletions);
                } else if (args.length <= 5) {
                    if (commandSender instanceof Player player) {
                        Block targetedBlock = player.getTargetBlockExact(3);
                        if (targetedBlock != null) {
                            if (args.length == 3) {
                                String x = args[2];
                                if (x.isEmpty()) {
                                    x = String.valueOf(targetedBlock.getLocation().getBlockX());
                                }
                                tabCompletions.add(x + " " + targetedBlock.getLocation().getBlockY() + " " + targetedBlock.getLocation().getBlockZ());
                            } else if (args.length == 4) {
                                String y = args[3];
                                if (y.isEmpty()) {
                                    y = String.valueOf(targetedBlock.getLocation().getBlockY());
                                }
                                tabCompletions.add(y + " " + targetedBlock.getLocation().getBlockZ());
                            } else {
                                String z = args[4];
                                if (z.isEmpty()) {
                                    z = String.valueOf(targetedBlock.getLocation().getBlockZ());
                                }
                                tabCompletions.add(z);
                            }
                        }
                    }
                }

            } else if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
                FileConfiguration bridgeConfig = RedstoneBridge.getBridgeConfig();
                StringUtil.copyPartialMatches(args[1], bridgeConfig.getKeys(false), tabCompletions);
            }
        }
        return tabCompletions;
    }
}
