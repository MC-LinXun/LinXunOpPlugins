package io.github.dashuxiadezhya.shortcutmenu;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

import static org.LinXun.GeFileDetect.SYSTEM_MESSAGE;
import static org.bukkit.Bukkit.getLogger;

public class MenuDirectives implements CommandExecutor {

    private final ShortcutMenu plugin;
    private final FileConfiguration config;

    public MenuDirectives(ShortcutMenu plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    // 创建菜单页面
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("Linxun_menu")) {return false;}
        if (!(sender instanceof Player)) {
            sender.sendMessage(SYSTEM_MESSAGE + "请在游戏内输入该指令，打开菜单");
            return true;
        }
        // 获取配置节点
        ConfigurationSection menuConfig = config.getConfigurationSection("menus.menu_main");
        ConfigurationSection buttons = config.getConfigurationSection("menus.menu_main.buttons");
        if (buttons == null || buttons.getKeys(false).isEmpty()) {
            sender.sendMessage(SYSTEM_MESSAGE + "配置为空,或不存在");
            return true;
        }
        // 创建菜单，添加按钮
        Player player = (Player) sender;
        String title = config.getString("menus.menu_main.title");
        Inventory menu = Bukkit.createInventory(null, config.getInt("menus.menu_main.size"),ChatColor.translateAlternateColorCodes('&',(title)));
        for (String configkey : buttons.getKeys(false)) {
            try {
                int key = Integer.parseInt(configkey)-1;
                ConfigurationSection buttonkey = buttons.getConfigurationSection(configkey);
                ItemStack buttonItem = createButtonItem(buttonkey,key);
                menu.setItem(key, buttonItem);
            }catch (NumberFormatException e){
                getLogger().warning( "配置格式错误");
                sender.sendMessage(SYSTEM_MESSAGE + "配置格式错误");
            }
        }
        if (menuConfig.getString("back") != null) {
            createCoverItemItem(menu,menuConfig);
        }
        player.openInventory(menu);
        return true;
    }
    // 覆盖空白位置
    private void createCoverItemItem(Inventory menu,ConfigurationSection menuConfig) {
        Material material = Material.getMaterial(menuConfig.getString("back"));
        ItemStack itemStack;

        if (menuConfig.contains("short") &&
            menuConfig.isInt("short") &&
            menuConfig.getInt("short") >= 0 &&
            menuConfig.getInt("short") <= 15) {
                itemStack = new ItemStack(material,1,(short) menuConfig.getInt("short"));

        }else {
                itemStack = new ItemStack(material);
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("§c禁止点击"); // 黑色文字（在黑色背景中不可见）
        itemStack.setItemMeta(meta);
        for (int i=0;i<menu.getSize();i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i,itemStack.clone());
            }
        }
    }
    // 创建菜单按钮
    private ItemStack createButtonItem (ConfigurationSection itemconfig,Integer key) {
        Material material = Material.matchMaterial(itemconfig.getString("material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (itemconfig.contains("name")){
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemconfig.getString("name")));
            meta.setLocalizedName("menu_main_button_" + (key+1) );
        }
        if (itemconfig.contains("lore")){
            List<String> lore = itemconfig.getStringList("lore").stream()
                    .map(line ->ChatColor.translateAlternateColorCodes('&',line))
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
