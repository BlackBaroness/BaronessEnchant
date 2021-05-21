package ru.baronessdev.free.enchant;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.free.enchant.logging.LogType;
import ru.baronessdev.free.enchant.logging.Logger;
import ru.baronessdev.free.enchant.util.UpdateCheckerUtil;

import java.util.HashMap;

public final class Enchant extends JavaPlugin implements Listener {

    private final HashMap<Player, ItemStack> memory = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        checkUpdates();
        new Metrics(this, 11435);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (getConfig().getBoolean("use-permission") && !p.hasPermission(getConfig().getString("permission"))) {
            return;
        }

        ItemStack current = e.getCurrentItem();

        // если это первый клик
        if (!memory.containsKey(p)) {
            if (current.getType().equals(Material.ENCHANTED_BOOK)) {
                memory.put(p, current);
                sendMessage(p, "tip");
            }
            return;
        }

        // если это второй клик
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) memory.get(p).getItemMeta();
        try {
            if (current == null) return;
            current.addEnchantments(meta.getStoredEnchants());
            e.setCancelled(true);

            Bukkit.getScheduler().runTask(this, () -> p.getInventory().setItem(e.getSlot(), current));

            sendMessage(p, "success");
        } catch (IllegalArgumentException ex) {
            sendMessage(p, (current.getType().equals(Material.AIR)
                    ? "empty"
                    : "fail"));
        }
        memory.remove(p);
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) memory.remove((Player) e.getPlayer());
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent e) {
        memory.remove(e.getPlayer());
    }

    private void sendMessage(Player p, String path) {
        String s = getConfig().getString(path);
        if (s.equals("")) return;
        p.sendMessage(s.replace('&', ChatColor.COLOR_CHAR));
    }

    private void checkUpdates() {
        try {
            int i = UpdateCheckerUtil.check(this);
            if (i != -1) {
                Logger.log(LogType.INFO, "New version found: v" + ChatColor.YELLOW + i + ChatColor.GRAY + " (Current: v" + getDescription().getVersion() + ")");
                Logger.log(LogType.INFO, "Update now: " + ChatColor.AQUA + "market.baronessdev.ru/shop/licenses/");
            }
        } catch (UpdateCheckerUtil.UpdateCheckException e) {
            Logger.log(LogType.ERROR, "Could not check for updates: " + e.getRootCause());
            Logger.log(LogType.ERROR, "Please contact Baroness's Dev if this isn't your mistake.");
        }
    }
}
