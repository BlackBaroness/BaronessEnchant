package ru.baronessdev.enchant;

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

import java.util.HashMap;

/**
 * @author Black_Baroness
 */
public final class Core extends JavaPlugin implements Listener {

    private final HashMap<Player, ItemStack> memory = new HashMap<>();

    @Override
    public void onEnable() {
        // пишу немного пьяный, не бейте если будут косяки. Поправлю когда нибудь
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (getConfig().getBoolean("use-permission")) {
            if (!p.hasPermission(getConfig().getString("permission"))) return;
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

            // майнкрафт творит удивительные вещи, поэтому выполняем замену на следующий тик
            Bukkit.getScheduler().runTask(this, () -> p.getInventory().setItem(e.getSlot(), current));

            sendMessage(p, "success");
        } catch (IllegalArgumentException ex) {
            sendMessage(p, (current.getType().equals(Material.AIR)
                    ? "empty"
                    : "fail"));
        }
        memory.remove(p);
    }

    @SuppressWarnings("all")
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
}
