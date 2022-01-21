package bonn2.regionseller;

import bonn2.regionseller.listener.SignListener;
import bonn2.regionseller.util.DataUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class RegionSeller extends JavaPlugin {

    public static RegionSeller plugin;
    public static Logger logger;

    @Override
    public void onEnable() {
        plugin = this;
        logger = plugin.getLogger();
        DataUtil.load();
        getServer().getPluginManager().registerEvents(new SignListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
