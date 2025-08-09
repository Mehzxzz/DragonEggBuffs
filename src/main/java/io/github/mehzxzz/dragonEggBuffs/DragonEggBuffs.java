package io.github.mehzxzz.dragonEggBuffs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DragonEggBuffs extends JavaPlugin {

    private int extraHearts;
    private List<String> potionEffectsConfig;

    private boolean glowHolder;

    private final String teamName = "DragonEgg";
    private final ChatColor teamColor = ChatColor.DARK_PURPLE;
    private final String teamPrefix = "";

    private Team dragonEggTeam;
    private Scoreboard scoreboard;

    private final Set<Player> playersWithEgg = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        setupTeam();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean hasEgg = hasDragonEgg(player);
                    if (hasEgg) {
                        applyEffects(player);
                        playersWithEgg.add(player);
                        addPlayerToTeam(player);
                        if (glowHolder) player.setGlowing(true);
                    } else {
                        if (playersWithEgg.contains(player)) {
                            resetPlayerHealth(player);
                            playersWithEgg.remove(player);
                            removePlayerFromTeam(player);
                            if (glowHolder) player.setGlowing(false);
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
        glowHolder = config.getBoolean("glow-holder", true);
    }

    private void setupTeam() {
        dragonEggTeam = scoreboard.getTeam(teamName);
        if (dragonEggTeam == null) {
            dragonEggTeam = scoreboard.registerNewTeam(teamName);
        }
        dragonEggTeam.setPrefix(teamPrefix);
        dragonEggTeam.setColor(teamColor);
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

    private void resetPlayerHealth(Player player) {
        player.setMaxHealth(20.0);
        if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
        }
    }

    private void addPlayerToTeam(Player player) {
        if (!dragonEggTeam.hasEntry(player.getName())) {
            dragonEggTeam.addEntry(player.getName());
        }
    }

    private void removePlayerFromTeam(Player player) {
        if (dragonEggTeam.hasEntry(player.getName())) {
            dragonEggTeam.removeEntry(player.getName());
        }
    }
}