package io.github.dashuxiadezhya.sweepinghime;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.GeFileDetect;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Autoclean extends JavaPlugin {
    private boolean isCleanRunning = true;
    private boolean isConfigValid = true;
    private boolean isConfirmPending = false;
    private int CleanSchedulerID = -1;
    private int CleanCountDownSchedulerID = -1;
    private int currentCountdown;
    private BukkitTask confirmTask;
    private FileConfiguration config;
    private GeFileDetect geFileDetect;
    public static final String SYSTEM_MESSAGE = ChatColor.RED + "[凌寻] " + ChatColor.YELLOW;

    private enum eCleanCommand {
        START, STOP, CLEAN, REVISE, RELOAD, STATUS, PRINT
    }
    @Override
    public void onEnable() {
        geFileDetect = new GeFileDetect(this);
        config = geFileDetect.CreateFileVe();
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"加载扫地姬   "+ "当前版本：" + getDescription().getVersion());
        this.getCommand("linxun_cleanItem").setExecutor(this);
        this.getCommand("linxun_cleanItem").setTabCompleter(this);
        currentCountdown = config.getInt("AutoTime.CountDownTime");
        OpenClean();
    }

    //操作指令
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //检查指令
        if (!sender.isOp()){sender.sendMessage(SYSTEM_MESSAGE +"权限不足,无法使用该指令");return true;}
        if (!cmd.getName().equalsIgnoreCase("linxun_cleanItem")) {return false;}

        //缺少权限检查 待补充

        //无参数显示帮助
        if (args.length == 0) {
            sender.sendMessage(SYSTEM_MESSAGE+"扫地姬使用帮助:\n" +
                    ChatColor.RED +
                    "/linxun_cleanItem start - 开启定期清理\n" +
                    "/linxun_cleanItem stop - 停止定期清理\n" +
                    "/linxun_cleanItem clean - 立即清理掉落物\n" +
                    "/linxun_cleanItem revise - 修改配置信息\n" +
                    "/linxun_cleanItem status - 查看当前状态\n" +
                    "/linxun_cleanItem print - 打印配置信息");
            return true;
        }

        try {
            eCleanCommand cleanCmd = eCleanCommand.valueOf(args[0].toUpperCase());
            switch (cleanCmd) {
                case START:
                    handleStartCommand(sender);
                    break;
                case STOP:
                    handleStopCommand(sender);
                    break;
                case CLEAN:
                    handleCleanCommand(sender, args);
                    break;
                case REVISE:
                    handleReviseCommand(sender, args);
                    break;
                case RELOAD:
                    handleReloadCommand(sender);
                    break;
                case STATUS:
                    handleStatusCommand(sender);
                    break;
                case PRINT:
                    handlePrintCommand(sender);
                    break;
                default:
                    sender.sendMessage(SYSTEM_MESSAGE +"未知指令，请输入/linxun_cleanItem来查看帮助");
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(SYSTEM_MESSAGE +"未知指令，请输入/linxun_cleanItem来查看帮助");
        }
        return true;

    }
    // 指令补全提示
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.isOp()){return completions;}
        if (!command.getName().equalsIgnoreCase("linxun_cleanItem")) {
            return completions;
        }
        //第一个输入的参数
        if (args.length == 1){
            completions = Arrays.stream(eCleanCommand.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .filter(s -> s.isEmpty() || s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            //第二个输入的参数
        } else if (args.length == 2 && args[0].equalsIgnoreCase("revise")) {
            //获取相关配置节点下的键值 用来补全参数
            ConfigurationSection autoTimeSection = config.getConfigurationSection("AutoTime"); // ConfigurationSection 指config配置的节点
            if (autoTimeSection != null) {
                completions.addAll(autoTimeSection.getKeys(false));  // getkeys （false） 表示 只获取节点下的 键值 （true）表示 获取节点的完整路径
                if (!args[1].isEmpty()) {
                    completions.removeIf(s -> !s.startsWith(args[1]));
                }
            }
        }
        return completions;
    }
    //打印配置信息
    private void handlePrintCommand(CommandSender sender) {
        sender.sendMessage("调试 - 完整配置: \n" + config.saveToString());
    }

    // 操作状态
    private void handleStatusCommand(CommandSender sender) {
        sender.sendMessage(isCleanRunning ?ChatColor.GREEN + "已开启" :ChatColor.RED +"已关闭");
    }
    // 重载配置
    private void handleReloadCommand(CommandSender sender) {
        if (isCleanRunning){
            sender.sendMessage(SYSTEM_MESSAGE +"请先使用/linxun_cleanItem stop来暂停清理");
            return;
        }
        //游戏修改/文件修改
        if (!isConfigValid){
            try {
                geFileDetect.memorySave();
            }catch (IOException e){
                geFileDetect.errorPrint("配置文件修改失败");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED +"异常："+e);
                isConfigValid = false;
                return;
            }
            isConfigValid = true;
        }else{
            config = geFileDetect.diskSave();
        }
        sender.sendMessage(SYSTEM_MESSAGE +"已重载配置");
        sender.sendMessage(SYSTEM_MESSAGE +"tips：指令修改优先级比手动修改高，指令修改会覆盖手动修改的配置");
    }
    // 修改配置
    private void handleReviseCommand(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("revise")){
            if (args.length != 3) {
                sender.sendMessage(SYSTEM_MESSAGE +"用法：/linxun_cleanItem revise [InstantlyTime|DelayTime|CountDownTime] 参数/秒 ");
                return;
            }
            if (isCleanRunning) {
                sender.sendMessage(SYSTEM_MESSAGE +"请先使用/linxun_cleanItem stop来暂停清理");
                return;
            }
            String AutoTimeConfig = "AutoTime."+args[1];
            if (!args[2].matches("\\d+")) {
                sender.sendMessage(SYSTEM_MESSAGE +"必须输入整数数字");
                return;
            }
            if (args[1].equals("CountDownTime")){
                config.set(AutoTimeConfig, Integer.parseInt(args[2]));
                isConfigValid = false;
                sender.sendMessage(SYSTEM_MESSAGE +"配置信息AutoTime." +ChatColor.RED + args[1] + "修改为："+ChatColor.RED + args[2]);
                sender.sendMessage(SYSTEM_MESSAGE +"请输入/linxun_cleanItem reload重载配置");
                return;
            }
            config.set(AutoTimeConfig, Long.parseLong(args[2]));
            isConfigValid = false;
            sender.sendMessage(SYSTEM_MESSAGE +"配置信息AutoTime." +ChatColor.RED + args[1] + "修改为："+ChatColor.RED + args[2]);
            sender.sendMessage(SYSTEM_MESSAGE +"请输入/linxun_cleanItem reload重载配置");
        }
    }

    // 立即执行
    private void handleCleanCommand(CommandSender sender, String[] args) {
        //首次输入
        if (args.length == 1){
            if (isConfirmPending){
                sender.sendMessage(SYSTEM_MESSAGE +"重复清理给你服崩了");
                return;
            }
            sender.sendMessage(SYSTEM_MESSAGE +"请在10秒内输入/linxun_cleanItem clean confirm来确认清理");
            isConfirmPending = true;
            confirmTask = Bukkit.getScheduler().runTaskLater(this,()->{
                try {
                    isConfirmPending = false;
                    sender.sendMessage(SYSTEM_MESSAGE +"已超时，重新输入/linxun_cleanItem clean");
                }catch (Exception e){
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"二次确认异常");
                }},210L);
            return;
        }
        //二次确认
        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            if (isConfirmPending){
                confirmTask.cancel();
                confirmTask = null;
                isConfirmPending = false;
                InstantClean();
            }else {
                sender.sendMessage(SYSTEM_MESSAGE +"请先输入/linxun_cleanItem clean来进行操作");
            }
        }else {
            isConfirmPending = false;
            confirmTask.cancel();
            confirmTask = null;
            sender.sendMessage(SYSTEM_MESSAGE +" 输入有误，请重新输入 /linxun_cleanItem 来进行清理操作");
        }
    }

    // 手动开启（默认已开启）
    private void handleStartCommand (CommandSender sender) {
        if (isCleanRunning) {
            sender.sendMessage(SYSTEM_MESSAGE +"扫地姬开启状态，请勿重复开启！！！");
            return;
        }
        if (!isConfigValid){
            sender.sendMessage(SYSTEM_MESSAGE +"请输入/linxun_cleanItem reload重载配置，在进行开启操作");
            return;
        }
        OpenClean();
        isCleanRunning = true;
        currentCountdown = config.getInt("AutoTime.CountDownTime");
        sender.sendMessage(SYSTEM_MESSAGE +"扫地姬已开启");
    }
    // 初次执行清理指令
    private void OpenClean () {
        CleanSchedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this, this::CountDown,config.getLong("AutoTime.InstantlyTime")*20, config.getLong("AutoTime.DelayTime")*20);
    }

    //手动关闭
    private void handleStopCommand (CommandSender sender) {
        if (!isCleanRunning) {
            sender.sendMessage(SYSTEM_MESSAGE +"扫地姬关闭状态，请勿重复关闭！！！");
            return;
        }
        Bukkit.getScheduler().cancelTask(CleanSchedulerID);
        Bukkit.getScheduler().cancelTask(CleanCountDownSchedulerID);
        CleanSchedulerID = -1;
        CleanCountDownSchedulerID = -1;
        isCleanRunning = false;
        currentCountdown = config.getInt("AutoTime.CountDownTime");
        sender.sendMessage(SYSTEM_MESSAGE +"扫地姬已关闭");
    }
    //立即清理掉落物
    private void InstantClean () {
        int itemCount = 0;
        //遍历所有世界
        for (World World : Bukkit.getWorlds()){
            //遍历所有世界下的掉落物
            for (Item item : World.getEntitiesByClass(Item.class)) {
                item.remove();
                itemCount++;
            }
        }
        Bukkit.broadcastMessage(SYSTEM_MESSAGE +"扫地姬: "+ChatColor.AQUA+"已清理"+ChatColor.RED+itemCount + ChatColor.AQUA+"个掉落物");
    }
    //倒计时结束 清理掉落物
    private void CountDown () {
        if (CleanCountDownSchedulerID !=-1){Bukkit.getScheduler().cancelTask(CleanCountDownSchedulerID);}

        CleanCountDownSchedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this,()->{
            if (currentCountdown > 0) {
                Bukkit.broadcastMessage(SYSTEM_MESSAGE +"扫地姬: "+ChatColor.AQUA+"倒计时"+ChatColor.RED+currentCountdown + ChatColor.AQUA+"秒清理掉落物");
                currentCountdown--;
            }else {
                InstantClean();
                currentCountdown = config.getInt("AutoTime.CountDownTime");
                Bukkit.getScheduler().cancelTask(CleanCountDownSchedulerID);
                CleanCountDownSchedulerID = -1;
            }
        },0L,20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTask(CleanSchedulerID);
        Bukkit.getScheduler().cancelTask(CleanCountDownSchedulerID);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"卸载扫地姬   "+ "当前版本：" + getDescription().getVersion());
    }
}
