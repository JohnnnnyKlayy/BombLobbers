package com.CoocooFroggy.bomblobbers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static com.CoocooFroggy.bomblobbers.Main.plugin;
import static org.bukkit.Bukkit.getLogger;

public class DirectHitChecker {
    Entity currentEntity;

    public void trackTNT(Entity tntEntity, Player thrower) {
        currentEntity = tntEntity;
        Object[] onlinePlayers = RightClickListener.onlinePlayers;
        whileLabel:
        while (!tntEntity.isDead()) {
            for (int i = 0; i < onlinePlayers.length; i++) {
                Player currentPlayer = null;
                if (onlinePlayers[i] instanceof Player) {
                    currentPlayer = (Player) onlinePlayers[i];
                } else {
                    getLogger().info("Not a player!");
                    return;
                }

                if (currentPlayer.equals(thrower))
                    continue;

                //Get location of player
                Block playerLocation = currentPlayer.getLocation().getBlock();
                Block playerHeadLocation = currentPlayer.getEyeLocation().getBlock();
                //Get location of TNT
                Block tntBlockLocation = tntEntity.getLocation().getBlock();
                Location tntLocation = tntEntity.getLocation();

//                //Make playerLocation + 1 in the Y
//                Block newPlayerLocation = currentPlayer.getLocation().add(0, 1, 0).getBlock();

                //If direct hit with player
                if (currentPlayer.getBoundingBox().overlaps(tntEntity.getBoundingBox())) {
                    //Get config velocity multiplier
                    double directHitVelocity = plugin.getConfig().getDouble("direct-hit-velocity.current");

                    //Make player go brrr
                    currentPlayer.setVelocity(tntEntity.getVelocity().multiply(directHitVelocity));

                    //Get config damage to player
                    int directHitDamage = plugin.getConfig().getInt("direct-hit-damage.current");

                    //Damage player in a new task, since it cannot be run asynchronously
                    Player finalCurrentPlayer = currentPlayer;
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            finalCurrentPlayer.damage(directHitDamage);
                        }
                    });
                    thrower.playSound(thrower.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
//                    getLogger().info("Played sound to " + thrower.getDisplayName());
                    break whileLabel;
                }

//                if (tntBlockLocation.equals(playerLocation) || tntBlockLocation.equals(playerHeadLocation)) {
////                    getLogger().info("Same block (success)!");
//                    currentPlayer.setVelocity(tntEntity.getVelocity().multiply(4));
//                    Player finalCurrentPlayer = currentPlayer;
//                    Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
//                        @Override
//                        public void run() {
//                            finalCurrentPlayer.damage(6);
//                        }
//                    });
//                    thrower.playSound(thrower.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
////                    getLogger().info("Played sound to " + thrower.getDisplayName());
//                    break whileLabel;
//                }
//                else {
//                    getLogger().info("not same, TNT loc is "  + tntLocation + ChatColor.RESET + " and " + currentPlayer.getDisplayName() + " player loc is " + ChatColor.GOLD + playerLocation);
//                }
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }
}
