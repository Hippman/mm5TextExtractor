package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DatExtractor;
import util.XenFileWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.swing.JOptionPane.showMessageDialog;

public class ExtractMobsAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public ExtractMobsAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;
        if (config.getValues().containsKey(Operations.EXTRACT_MOB)) {
            cfg = config.getValues().get(Operations.EXTRACT_MOB);
        }

        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new File(cfg == null ? "xeen.mon" : cfg.getData().get(ConfigLineType.ORIGINAL_PATH)));
        chooser.setDialogTitle("Выбери оригинальный файл xeen.mon");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xeDat = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери путь к Xls файлу");
        chooser.setSelectedFile(new File(cfg == null ? "mob_names.xls" : cfg.getData().get(ConfigLineType.XLS_PATH)));
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File xlsFile = chooser.getSelectedFile();


        System.out.println("Extracting MOB");
        DatExtractor ec = new DatExtractor();
        try {
            ec.extractMobs(xeDat, xlsFile.getAbsolutePath());
            showMessageDialog(null, "Всё корректно считалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getMessage());
        }

        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, xeDat.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XLS_PATH, xlsFile.getAbsolutePath());
        config.getValues().put(Operations.EXTRACT_MOB, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
