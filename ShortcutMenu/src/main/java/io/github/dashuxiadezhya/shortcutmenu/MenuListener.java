package io.github.dashuxiadezhya.shortcutmenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Collections;
import java.util.List;

import static org.LinXun.GeFileDetect.SYSTEM_MESSAGE;


public class MenuListener implements Listener{
    private final ShortcutMenu plugin;
    private final FileConfiguration config;
    public MenuListener(ShortcutMenu plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }
    // 菜单按钮事件
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
    //合法检测
        if(!Verification.ListenerMenuVerify(event,config))return;
        event.setCancelled(true);

        if(!Verification.ListenerPlayerVerify(event.getWhoClicked()))return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if(!Verification.ListenerLegalButtonVerify(item,"")) return;

        String customname = item.getItemMeta().getLocalizedName();
        if(!Verification.ListenerLegalButtonVerify(item,customname))return;

        String buttonKey = customname.substring("menu_main_button_".length());
        ConfigurationSection buttonConfig = config.getConfigurationSection("menus.menu_main.buttons."+buttonKey);

        if (!Verification.UniversalNull(buttonConfig))return;
        List<String> commandsConfig = buttonConfig.getStringList("commands");
    //执行逻辑
        for (String command : commandsConfig) {
            if (command.startsWith("^")){
                String players = command.substring(1).replace("%player%",player.getName());
                Bukkit.dispatchCommand(player,players);
            }else if (command.startsWith("~")){
                String consoles = command.substring(1).replace("%player%",player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),consoles);
            }else {
                player.sendMessage(SYSTEM_MESSAGE + command);
            }
        }
    }
    //玩家首次进入检测
    @EventHandler
    public void onPlayerFirstJoin(PlayerJoinEvent joinevent) {
        if (!joinevent.getPlayer().hasPlayedBefore()){
            ItemStack itemStack = new ItemStack(Material.WATCH);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',config.getString("menus.menu_main.title")));
            itemMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&a右键打开菜单")));
            itemStack.setItemMeta(itemMeta);
            joinevent.getPlayer().getInventory().addItem(itemStack);
        }
    }
    //手持菜单检测
    @EventHandler
    public void onPlayerItemClick(PlayerInteractEvent interactEvent) {
        if (!Verification.ListenerItemClickVerify(interactEvent,config))return;
        interactEvent.getPlayer().performCommand("Linxun_menu");
    }
}
