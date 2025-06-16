package io.github.dashuxiadezhya.shortcutmenu;
import org.LinXun.GeFileDetect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
public final class ShortcutMenu extends JavaPlugin {

    GeFileDetect geFileDetect;
    FileConfiguration config;
    @Override
    public void onEnable() {
        geFileDetect = new GeFileDetect(this);
        config = geFileDetect.CreateFileVe();
        getCommand("Linxun_menu").setExecutor(new MenuDirectives(this,config));
        getServer().getPluginManager().registerEvents(new MenuListener(this,config),this);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"自定义菜单   "+ "当前版本：" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"禁用自定义菜单   "+ "当前版本：" + getDescription().getVersion());
    }

}