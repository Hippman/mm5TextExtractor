package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.TextFixesUtil;
import util.XenFileWorker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;

public class TranslateSmallAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public TranslateSmallAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;
        if (config.getValues().containsKey(Operations.TRANSLATE_SMALL)) {
            cfg = config.getValues().get(Operations.TRANSLATE_SMALL);
        }

        JFileChooser chooser = new JFileChooser(".");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV File", "csv");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "translate.csv" : cfg.getData().get(ConfigLineType.XLS_PATH)));
        chooser.setDialogTitle("Выбери afqk перевода");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File trText = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери директорию с непереведенными файлами");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (cfg != null && cfg.getData().get(ConfigLineType.EN_PATH) != null) {
            chooser.setSelectedFile(new File(cfg.getData().get(ConfigLineType.EN_PATH)));
        }

        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File enDir = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери директорию с переведенными файлами");
        if (cfg != null && cfg.getData().get(ConfigLineType.RU_PATH) != null) {
            chooser.setSelectedFile(new File(cfg.getData().get(ConfigLineType.RU_PATH)));
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File ruDir = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери директорию куда сохранять новые файлы");
        if (cfg != null && cfg.getData().get(ConfigLineType.ORIGINAL_PATH) != null) {
            chooser.setSelectedFile(new File(cfg.getData().get(ConfigLineType.ORIGINAL_PATH)));
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File outDir = chooser.getSelectedFile();

        try {
            TextFixesUtil.processForumTranslate(trText.getAbsolutePath(),
                    enDir.getAbsolutePath(),
                    ruDir.getAbsolutePath(),
                    outDir.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.XLS_PATH, trText.getAbsolutePath());
        cfg.getData().put(ConfigLineType.EN_PATH, enDir.getAbsolutePath());
        cfg.getData().put(ConfigLineType.RU_PATH, ruDir.getAbsolutePath());
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, outDir.getAbsolutePath());
        config.getValues().put(Operations.TRANSLATE_SMALL, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
