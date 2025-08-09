package io.github.mehzxzz.dragonEggBuffs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DragonEggBuffs extends JavaPlugin {

    private int extraHearts;
    private List<String> potionEffectsConfig;
    private final Set<Player> playersWithEgg = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean hasEgg = hasDragonEgg(player);
                    if (hasEgg) {
                        applyEffects(player);
                        playersWithEgg.add(player);
                    } else {
                        if (playersWithEgg.contains(player)) {
                            resetPlayerHealth(player);
                            playersWithEgg.remove(player);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        extraHearts = config.getInt("extra-hearts", 4);
        potionEffectsConfig = config.getStringList("potion-effects");
    }

    private boolean hasDragonEgg(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        return false;
    }

    private void applyEffects(Player player) {
        double maxHealth = 20.0 + (extraHearts * 2);
        player.setMaxHealth(maxHealth);
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }

        for (String effectString : potionEffectsConfig) {
            String[] parts = effectString.split(",");
            if (parts.length == 2) {
                try {
                    PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                    int level = Integer.parseInt(parts[1]) - 1;
                    if (type != null) {
                        PotionEffect effect = new PotionEffect(type, 40, level, false, false, true);
                        player.addPotionEffect(effect, true);
                    }
                } catch (Exception e) {
                    getLogger().warning("Invalid config potion effect: " + effectString);
                }
            } else {
                getLogger().warning("Potion effect config format incorrect: " + effectString);
            }
        }
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dragoneggbuffs")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("dragoneggbuffs.reload")) {
                    sender.sendMessage("§cYou do not have permission to reload the config.");
                    return true;
                }
                reloadConfig();
                loadConfig();
                sender.sendMessage("§5DragonEggBuffs config reloaded.");
                return true;
            }
            sender.sendMessage("§cUsage: /dragoneggbuffs reload");
            return true;
        }
        return false;
    }

    private void resetPlayerHealth(Player player) {
        player.setMaxHealth(20.0);
        if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
        }
    }
}