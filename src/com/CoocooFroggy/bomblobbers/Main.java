package com.CoocooFroggy.bomblobbers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {
    static boolean mainSwitch;
    static boolean debug;
    static boolean stopSwitch;
    static Plugin plugin;
    BukkitTask gameTask;

    @Override
    public void onEnable() {
        //Set plugin
        plugin = this;

        //config.yml
        plugin.saveDefaultConfig();

        //Enable the plugin if it was previously enabled
        boolean previouslyEnabled = plugin.getConfig().getBoolean("plugin.enabled");
        if (previouslyEnabled) {
            mainSwitch = true;
        } else {
            mainSwitch = false;
        }

        getServer().getPluginManager().registerEvents(new RightClickListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new WaterListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getCommand("bomblobbers").setTabCompleter(new TabCompleter());

        //Set default values for teamsAndAliveCount
        DeathListener.teamsAndAliveCount.put("blue", 0);
        DeathListener.teamsAndAliveCount.put("red", 0);
        DeathListener.teamsAndAliveCount.put("green", 0);

        //Set default values for teamsAndPlayers
        DeathListener.teamsAndPlayers.put("blue", new ArrayList<>());
        DeathListener.teamsAndPlayers.put("red", new ArrayList<>());
        DeathListener.teamsAndPlayers.put("green", new ArrayList<>());

        //Set default values for teamsAndAlive
        DeathListener.teamsAndAlive.put("blue", new ArrayList<>());
        DeathListener.teamsAndAlive.put("red", new ArrayList<>());
        DeathListener.teamsAndAlive.put("green", new ArrayList<>());

        super.onEnable();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("bomblobbers") || label.equalsIgnoreCase("bl")) {
            if (!(args.length < 1)) {
                if (args[0].equalsIgnoreCase("enable")) {
                    //Run enable
                    return enableCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("disable")) {
                    //Run disable
                    return disableCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("start")) {
                    //Run start
                    return startCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("stop")) {
                    //Run stop
                    return stopCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("changeteam")) {
                    //Run changeteam
                    return changeTeamCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("debug")) {
                    //Run debug command
                    return debugCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("config")) {
                    //Run config command
                    return configCommand(sender, command, label, args);
                } else {
                    //Sender didn't type a valid arg
                    sender.sendMessage(ChatColor.RED + "Syntax: /bomblobbers [enable/disable, start/stop, changeteam, config]");
                    return true;
                }
            } else {
                //if they just typed /bomblobbers or /bl
                if (sender.hasPermission("bomblobbers.quicktoggle")) {
                    if (StartGame.gameStarted) {
                        sender.sendMessage(ChatColor.RED + "Game in progress! Stop the game first by using " + ChatColor.GOLD + "/bomblobbers stop");
                        return false;
                    }
                    mainSwitch = !mainSwitch;
                    if (mainSwitch) {
                        plugin.getConfig().set("plugin.enabled", true);
                        plugin.saveConfig();
                        sender.sendMessage(ChatColor.GREEN + "Enabled Bomb Lobbers plugin!");
                        return true;
                    } else {
                        plugin.getConfig().set("plugin.enabled", false);
                        plugin.saveConfig();
                        sender.sendMessage("Disabled Bomb Lobbers plugin!");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
                    return false;
                }
            }
        }
        return false;
    }

    public boolean enableCommand(CommandSender sender, Command command, String label, String[] args) {
        // /bomblobbers enable
        if (sender.hasPermission("bomblobbers.enable")) {
            mainSwitch = true;
            plugin.getConfig().set("plugin.enabled", true);
            plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Enabled Bomb Lobbers plugin!");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
            return false;
        }
    }

    public boolean disableCommand(CommandSender sender, Command command, String label, String[] args) {
        // /bomblobbers disable
        if (sender.hasPermission("bomblobbers.disable")) {
            if (StartGame.gameStarted) {
                sender.sendMessage(ChatColor.RED + "Game in progress! Stop the game first by using " + ChatColor.GOLD + "/bomblobbers stop");
                return false;
            }
            mainSwitch = false;
            plugin.getConfig().set("plugin.enabled", false);
            plugin.saveConfig();
            sender.sendMessage("Disabled Bomb Lobbers plugin!");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
            return false;
        }
    }

    public boolean startCommand(CommandSender sender, Command command, String label, String[] args) {
        //Command only runs for players
        if (sender instanceof Player) {
            // /bomblobbers start
            if (sender.hasPermission("bomblobbers.start")) {

                //Gamemode adventure
                List<Player> playerList = ((Player) sender).getWorld().getPlayers();
                for (int i = 0; i < playerList.size(); i++) {
                    playerList.get(i).setGameMode(GameMode.SURVIVAL);
                }

                //Runs startGame asynchronously (bad idea?)
                StartGame nonStaticStartGame = new StartGame();
                gameTask = Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        nonStaticStartGame.startGame((Player) sender);
                    }
                });
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Sorry, you must be a player to run this command");
            return false;
        }
    }

    public boolean stopCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("bomblobbers.stop")) {
            stopSwitch = true;
            sender.sendMessage("Stopped the game.");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
            return false;
        }
    }

    public boolean changeTeamCommand(CommandSender sender, Command command, String label, String[] args) {
        // /bomblobbers changeteam
        if (sender.hasPermission("bomblobbers.changeteam")) {
            if (!mainSwitch) {
                sender.sendMessage(ChatColor.RED + "You need to enable the plugin first! " + ChatColor.GOLD + "/bomblobbers enable");
                return true;
            }
            if (StartGame.gameStarted) {
                sender.sendMessage(ChatColor.RED + "Game in progress! Stop the game first by using " + ChatColor.GOLD + "/bomblobbers stop");
                return false;
            }
            if (sender instanceof Player) {
                ChangeTeam.changeTeam((Player) sender);
                return true;
            } else {
                sender.sendMessage("Sorry, you have to be a player to change teams.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
            return false;
        }
    }

    public boolean debugCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("bomblobbers.debug")) {
            debug = true;
            mainSwitch = true;
            sender.sendMessage(ChatColor.GREEN + "Enabled Bomb Lobbers plugin in debug mode!");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
            return false;
        }
    }

    public boolean configCommand(CommandSender sender, Command command, String label, String[] args) {
        //MARK: Config
        if (sender.hasPermission("bomblobbers.config")) {
            if (args.length < 2) {
                //If "/bl config"
                sender.sendMessage(ChatColor.RED + "Specify what to config. Options are: " + ChatColor.GOLD + "velocity, givetime, cooldown, directhitvelocity, directhitdamage, waterdamage, countdown, items, deathmessages, reset");
                return false;
            }
            if (args[1].equalsIgnoreCase("velocity")) {
                if (args.length < 3) {
                    //If "/bl config velocity"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as a decimal.");
                    return false;
                }
                //If "/bl config velocity _"
                double newVelocity;
                try {
                    //If "/bl config velocity [double]"
                    newVelocity = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    //If "/bl config velocity [Not a double]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the velocity.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config velocity [double]"
                plugin.getConfig().set("throw-velocity.current", newVelocity);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new velocity to " + ChatColor.GOLD + newVelocity + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("givetime")) {
                if (args.length < 3) {
                    //If "/bl config givetime"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as an integer.");
                    return false;
                }
                //If "/bl config givetime _"
                int newGiveTime;
                try {
                    //If "/bl config givetime [int]"
                    newGiveTime = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    //If "/bl config givetime [Not an int]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the give time.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config velocity [int]"
                plugin.getConfig().set("give-time.current", newGiveTime);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new give time to " + ChatColor.GOLD + newGiveTime + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("cooldown")) {
                if (args.length < 3) {
                    //If "/bl config cooldown"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as an integer.");
                    return false;
                }
                //If "/bl config cooldown _"
                int newCooldown;
                try {
                    //If "/bl config cooldown [int]"
                    newCooldown = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    //If "/bl config cooldown [Not an int]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the cooldown.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config cooldown [int]"
                plugin.getConfig().set("cooldown.current", newCooldown);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new cooldown to " + ChatColor.GOLD + newCooldown + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("directhitvelocity")) {
                if (args.length < 3) {
                    //If "/bl config directhitvelocity"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as a decimal.");
                    return false;
                }
                //If "/bl config directhitvelocity _"
                double newDirectHitVelocity;
                try {
                    //If "/bl config directhitvelocity [double]"
                    newDirectHitVelocity = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    //If "/bl config directhitvelocity [Not a double]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the direct hit velocity.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config directhitvelocity [double]"
                plugin.getConfig().set("direct-hit-velocity.current", newDirectHitVelocity);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new direct hit velocity to " + ChatColor.GOLD + newDirectHitVelocity + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("directhitdamage")) {
                if (args.length < 3) {
                    //If "/bl config directhitdamage"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as an integer.");
                    return false;
                }
                //If "/bl config directhitdamage _"
                int newDirectHitDamage;
                try {
                    //If "/bl config directhitdamage [int]"
                    newDirectHitDamage = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    //If "/bl config directhitdamage [Not an int]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the direct hit damage.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config directhitdamage [int]"
                plugin.getConfig().set("direct-hit-damage.current", newDirectHitDamage);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new direct hit damage to " + ChatColor.GOLD + newDirectHitDamage + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("waterdamage")) {
                if (args.length < 3) {
                    //If "/bl config waterdamage"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as an integer.");
                    return false;
                }
                //If "/bl config waterdamage _"
                int newWaterDamage;
                try {
                    //If "/bl config waterdamage [int]"
                    newWaterDamage = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    //If "/bl config waterdamage [Not an int]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the water damage.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config directhitdamage [int]"
                plugin.getConfig().set("water-damage.current", newWaterDamage);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new water damage to " + ChatColor.GOLD + newWaterDamage + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("countdown")) {
                if (args.length < 3) {
                    //If "/bl config countdown"
                    sender.sendMessage(ChatColor.RED + "Specify the amount as an integer.");
                    return false;
                }
                //If "/bl config countdown _"
                int newCountdown;
                try {
                    //If "/bl config countdown [int]"
                    newCountdown = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    //If "/bl config countdown [Not an int]"
                    sender.sendMessage(ChatColor.RED + "That is not a number. Please specify a number for the countdown.");
                    getLogger().info("[Bomb Lobbers] Error: " + e);
                    return false;
                }
                //If "/bl config countdown [int]"
                plugin.getConfig().set("countdown.current", newCountdown);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the new countdown to " + ChatColor.GOLD + newCountdown + ChatColor.GREEN + "!");
                return true;
            }
            else if (args[1].equalsIgnoreCase("items")) {
                //If "/bl config items"
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ManageItems.manageItems(player);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "You need to be a player to run this command.");
                    return false;
                }
            }
            else if (args[1].equalsIgnoreCase("deathmessages")) {
                //If "/bl config deathmessages"
                if (args.length > 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GOLD + "/bomblobbers config deathmessages");
                }
                boolean deathMessages = !plugin.getConfig().getBoolean("death-messages.current");
                plugin.getConfig().set("death-messages.current", deathMessages);
                if (deathMessages) {
                    sender.sendMessage(ChatColor.GREEN + "Enabled death messages");
                } else {
                    sender.sendMessage(ChatColor.RED + "Disabled death messages");
                }
                plugin.saveConfig();
                return true;
            }
            else if (args[1].equalsIgnoreCase("reset")) {
                //If "/bl config reset"
                //Delete the config and make a new one
                File configFile = new File(plugin.getDataFolder(), "config.yml");
                configFile.delete();
                saveDefaultConfig();
                reloadConfig();
                sender.sendMessage("All config values reset.");
                return true;
            }
            else {
                //If "/bl config ____"
                sender.sendMessage(ChatColor.RED + "That is not a valid config. Options are: " + ChatColor.GOLD + "velocity, givetime, cooldown, directhitvelocity, directhitdamage, waterdamage, countdown, items, deathmessages, reset");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
            return false;
        }
    }
}