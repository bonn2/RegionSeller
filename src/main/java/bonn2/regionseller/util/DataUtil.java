package bonn2.regionseller.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static bonn2.regionseller.RegionSeller.logger;
import static bonn2.regionseller.RegionSeller.plugin;

public class DataUtil {

    static JsonObject jsonObject = new JsonObject();
    static File file;

    public static void load() {
        logger.info("Loading data from disk...");

        file = new File(plugin.getDataFolder() + File.separator + "owned-plots.json");
        try {
            plugin.getDataFolder().mkdirs();
            if (file.createNewFile()) {
                new FileOutputStream(file).write("{}".getBytes(StandardCharsets.UTF_8));
            } else {
                jsonObject = new Gson().fromJson(new String(new FileInputStream(file).readAllBytes()), JsonObject.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        logger.info("Saving sold regions to file...");
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            new FileOutputStream(file).write(
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(jsonObject)
                            .getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPlot(UUID uuid, String type, String region, World world) {
        // Check if user already has a plot of this type
        if (hasPlot(uuid, type)) return;
        // Get JsonObject of all owned plots
        JsonObject ownedPlots = new JsonObject();
        if (jsonObject.has(uuid.toString())) {
            ownedPlots = jsonObject.remove(uuid.toString()).getAsJsonObject();
        }
        // Create new json object for plot
        JsonObject plot = new JsonObject();
        plot.addProperty("region", region);
        plot.addProperty("world", world.getName());
        // Add new json object to owned plots
        ownedPlots.add(type, plot);
        // Update owned plots on file
        jsonObject.add(uuid.toString(), ownedPlots);
        save();
    }

    public static void removePlot(UUID uuid, String type) {

    }

    public static boolean hasPlot(UUID uuid, String type) {
        if (jsonObject.has(uuid.toString()))
            return jsonObject.getAsJsonObject(uuid.toString()).has(type);
        return false;
    }
}
