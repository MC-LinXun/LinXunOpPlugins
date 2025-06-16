package io.github.dashuxiadezhya.shortcutmenu;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.configuration.ConfigurationSection;
public class Verification {
    // 配置指令验证
    public static boolean UniversalNull (ConfigurationSection section) {
        return (section != null && section.contains("commands"));
    }
    // 菜单验证
    public static boolean ListenerMenuVerify (InventoryClickEvent event, FileConfiguration config) {
        return config.getString("menus.menu_main.title").contains(event.getView().getTitle());
    }
    // 玩家验证
    public static boolean ListenerPlayerVerify (HumanEntity pla) {
        return pla instanceof Player;
    }
    // 合法按钮验证+按钮验证
    public static boolean ListenerLegalButtonVerify (ItemStack itemSta,String str) {
        return (itemSta != null && itemSta.hasItemMeta() && (str.isEmpty() || str.startsWith("menu_main_button_")));
    }

    public static boolean ListenerItemClickVerify (PlayerInteractEvent interactEvent,FileConfiguration config) {
        ItemStack itemSta = interactEvent.getPlayer().getInventory().getItemInMainHand();
        return interactEvent.getAction() == Action.RIGHT_CLICK_AIR &&
                itemSta.getType() != Material.AIR &&
                itemSta.hasItemMeta() &&
                itemSta.getItemMeta().getDisplayName().equals(config.getString("menus.menu_main.title"));

    }
}

