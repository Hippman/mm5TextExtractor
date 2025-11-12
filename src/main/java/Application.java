import com.google.gson.Gson;
import dto.StoredConfig;
import gui.MainPanel;
import util.DatExtractor;
import util.DataCompressor;
import util.XenFileWorker;

import java.io.*;
import java.nio.file.Files;

public class Application {

    public static void main(String[] args) throws Exception {
        File panelConfigfile = new File("last_paths.json");
        StoredConfig config = new StoredConfig();
        if (panelConfigfile.exists()) {
            String data = new String(Files.readAllBytes(panelConfigfile.toPath()));
            Gson gson = new Gson();
            config = gson.fromJson(data, StoredConfig.class);
        }
        MainPanel panel = new MainPanel(config);
    }
}
