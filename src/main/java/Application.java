import com.google.gson.Gson;
import dto.StoredConfig;
import gui.MainPanel;
import util.DatExtractor;
import util.DataCompressor;
import util.XenFileWorker;

import java.io.*;
import java.nio.file.Files;

public class Application {

    private static final String extractText = "extract";
    private static final String compressText = "compress";
    private static final String extractXenText = "extractxen";
    private static final String compressXenText = "compressxen";

    public static void main(String[] args) throws Exception {
        File panelConfigfile = new File("last_paths.json");
        StoredConfig config = new StoredConfig();
        if (panelConfigfile.exists()) {
            String data = new String(Files.readAllBytes(panelConfigfile.toPath()));
            Gson gson = new Gson();
            config = gson.fromJson(data, StoredConfig.class);
        }
        MainPanel panel = new MainPanel(config);

/*

        if (args.length < 2) {
            System.out.println("Use for extract or compress texts from/to xeen.dat\nextractor.jar");
            System.out.println("extract [XEEN.DAT path] [xeen.dat.xls path]");
            System.out.println("compress [XEEN.DAT path] [xeen.dat.xls path] [out XEEN.DAT path]");
            System.out.println("extractxen [.XEN or .BIN file from CC] [.xls path]");
            System.out.println("compressxen [.xls path] [out path]");
            System.exit(0);
        }
        File dat = new File(args[1]);
        if (extractText.equals(args[0])) {
            System.out.println("Extracting");
            DatExtractor ec = new DatExtractor();
            ec.extractText(dat, args[2]);
        }
        if (compressText.equals(args[0])) {
            System.out.println("Compressing");
            DataCompressor dc = new DataCompressor();
            File xls = new File(args[2]);
            String outFilename = "." + File.separator + dat.getName() + ".translated.dat";
            if (args.length == 4) {
                outFilename = args[3];
            }
            dc.compressTexts(dat, xls, outFilename);
        }
        if (extractXenText.equals(args[0])) {
            System.out.println("Extracting XEN");
            XenFileWorker ec = new XenFileWorker();
            ec.extractTexts(dat, args[2]);
        }
        if (compressXenText.equals(args[0])) {
            System.out.println("Compressing XEN");
            XenFileWorker ec = new XenFileWorker();
            File xls = new File(args[1]);
            ec.compressTexts(xls, args[2]);
        }*/
    }
}
