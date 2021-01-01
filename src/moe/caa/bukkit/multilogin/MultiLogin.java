package moe.caa.bukkit.multilogin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.caa.bukkit.multilogin.listener.BukkitListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MultiLogin extends JavaPlugin implements Runnable{
    public static MultiLogin INSTANCE;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        MultiLogin.INSTANCE = this;
        if(!getServer().getOnlineMode()){
            getLogger().severe("插件只能运行在“online-mode=true”的环境下");
            getLogger().severe("请打开服务端的正版验证！");
            setEnabled(false);
            return;
        }

        try {
            PluginData.reloadConfig();
            PluginData.readData();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("无法读取配置或数据文件，请检查！");
            setEnabled(false);
            return;
        }

        try {
            NMSUtil.initService(this);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("初始化修改失败，插件可能不兼容您的服务端！");
            setEnabled(false);
            return;
        }
        getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        WhitelistCommand command = new WhitelistCommand();
        getCommand("whitelist").setExecutor(command);
        getCommand("whitelist").setTabCompleter(command);

        MultiLoginCommand command1 = new MultiLoginCommand();
        getCommand("multilogin").setTabCompleter(command1);
        getCommand("multilogin").setExecutor(command1);

        getServer().getScheduler().runTaskTimerAsynchronously(this, this, 0, 20 * 60);

        getLogger().info("插件已加载");
    }


    @Override
    public void run() {
        save();
    }

    @Override
    public void onDisable() {
        save();
        getServer().shutdown();
    }

    private void save(){
        try {
            PluginData.saveData();
        } catch (IOException e) {
            getLogger().severe("无法保存数据文件");
            e.printStackTrace();
        }
    }

    private void update(){

    }

    public static void main(String[] args) {

    }
}
