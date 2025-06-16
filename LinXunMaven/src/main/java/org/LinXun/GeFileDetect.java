package org.LinXun;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;


public class GeFileDetect {
    public static final String SYSTEM_MESSAGE = ChatColor.RED + "[凌寻] " + ChatColor.YELLOW;
    private FileConfiguration config;
    private final JavaPlugin plugin;
    private File configUrl;

    public GeFileDetect(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration CreateFileVe () {
        //配置默认路径
        Path filePathUrl = Paths.get(
                plugin.getDataFolder().getParentFile().toString(),
                "LinXun",
                    plugin.getDescription().getName()
        );
        File filesUrl = filePathUrl.toFile();
        // 目标文件路径
        configUrl = new File(filesUrl,"config.yml");

        //检查是否创建成功,无法创建 则读取默认配置
        if (!filesUrl.exists() && !filesUrl.mkdirs()) {
            defaultConfig();
            return config;
        }else if (!configUrl.exists()){
            try {
                InputStream in = plugin.getResource("config.yml");
                Files.copy(in,configUrl.toPath());
                config = YamlConfiguration.loadConfiguration(configUrl);
                return config;
            }catch (IOException e) {
                defaultConfig();
                return config;
            }
        }
        config = YamlConfiguration.loadConfiguration(configUrl);
        return config;
    }
    public void defaultConfig (){
        InputStream defaultIn = plugin.getResource("config.yml");
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultIn, StandardCharsets.UTF_8));
        errorPrint(plugin.getDescription().getName()+"配置文件创建异常,自动读取默认配置(默认配置无法被改写)");
    }

    //磁盘内重载配置
    public FileConfiguration diskSave () {
        config = YamlConfiguration.loadConfiguration(configUrl);
        return config;
    }

    //内存重载配置到磁盘
    public void memorySave  () throws IOException {
        config.save(configUrl);
    }

    public void errorPrint(String message) {
        // 去除颜色代码
        String strippedMessage = ChatColor.stripColor(message);

        // 计算有效显示长度（中文算2个字符，英文算1个）
        int displayWidth = calculateDisplayWidth(strippedMessage);

        // 生成边框（两侧各留3个字符空间）
        String border = ChatColor.YELLOW + repeatString(displayWidth + 10);
        // 发送到控制台
        Bukkit.getConsoleSender().sendMessage(border);
        Bukkit.getConsoleSender().sendMessage(SYSTEM_MESSAGE + ChatColor.GREEN + message);
        Bukkit.getConsoleSender().sendMessage(border);
    }

    /**
     * 计算字符串的显示宽度（中文2字符，英文1字符）
     */
    private static int calculateDisplayWidth(String str) {
        int width = 0;
        for (char c : str.toCharArray()) {
            width += (isChinese(c) ? 2 : 1);
        }
        return width;
    }

    /**
     * 判断是否为中文字符
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private static String repeatString(int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append("=");
        }
        return sb.toString();
    }

}
