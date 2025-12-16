package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.RawExtractor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.swing.JOptionPane.showMessageDialog;

public class CompressRawAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public CompressRawAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;

        if (config.getValues().containsKey(Operations.COMPRESS_BMP)) {
            cfg = config.getValues().get(Operations.COMPRESS_BMP);
        }

        JFileChooser chooser = new JFileChooser(".");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP File", "bmp");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "title2b.bmp" : cfg.getData().get(ConfigLineType.ORIGINAL_PATH)));
        chooser.setDialogTitle("Выбери графический BMP файл");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File raw = chooser.getSelectedFile();


        chooser.setDialogTitle("Выбери палитру");
        filter = new FileNameExtensionFilter("PAL File", "pal");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "dark.pal" : cfg.getData().get(ConfigLineType.XLS_PATH)));
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File pal = chooser.getSelectedFile();


        chooser.setDialogTitle("Выбери путь к RAW файлу");
        filter = new FileNameExtensionFilter("RAW File", "raw");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "title2b.raw" : cfg.getData().get(ConfigLineType.NEW_PATH)));
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File bmp = chooser.getSelectedFile();


        System.out.println("Compressing");
        RawExtractor dc = new RawExtractor();
        try {
            dc.compress(raw, bmp, pal);
            showMessageDialog(null, "Всё корректно записалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getMessage());
        }

        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, raw.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XLS_PATH, pal.getAbsolutePath());
        cfg.getData().put(ConfigLineType.NEW_PATH, bmp.getAbsolutePath());
        config.getValues().put(Operations.COMPRESS_BMP, cfg);


        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
