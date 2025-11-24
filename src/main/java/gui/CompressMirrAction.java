package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.MirrFileCompressor;
import util.XenFileWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.swing.JOptionPane.showMessageDialog;

public class CompressMirrAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public CompressMirrAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;
        if (config.getValues().containsKey(Operations.COMPRESS_MIRR)) {
            cfg = config.getValues().get(Operations.COMPRESS_MIRR);
        }

        JFileChooser chooser = new JFileChooser(".txt");
        chooser.setSelectedFile(new File(cfg == null ? "darkmirr.txt" : cfg.getData().get(ConfigLineType.ORIGINAL_PATH)));
        chooser.setDialogTitle("Выбери оригинальный файл MIRR (darkmirr.txt или xeenmirr.txt)");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xeDat = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери путь к Xls файлу");
        chooser.setSelectedFile(new File(cfg == null || cfg.getData().get(ConfigLineType.XLS_PATH) == null ?
                "stored_texts.xls" : cfg.getData().get(ConfigLineType.XLS_PATH)));
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xlsFile = chooser.getSelectedFile();

        chooser = new JFileChooser(".txt");
        chooser.setSelectedFile(new File(cfg == null || cfg.getData().get(ConfigLineType.NEW_PATH) == null ?
                "darkmirr.txt" : cfg.getData().get(ConfigLineType.NEW_PATH)));
        chooser.setDialogTitle("Выбери новый файл MIRR (darkmirr.txt или xeenmirr.txt)");
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File newDat = chooser.getSelectedFile();


        System.out.println("Compressing MIRR");
        MirrFileCompressor.compressMirr(xeDat, xlsFile, newDat);

        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, xeDat.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XLS_PATH, xlsFile.getAbsolutePath());
        cfg.getData().put(ConfigLineType.NEW_PATH, newDat.getAbsolutePath());
        config.getValues().put(Operations.COMPRESS_MIRR, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
