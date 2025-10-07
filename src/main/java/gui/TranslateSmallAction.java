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

import static javax.swing.JOptionPane.showMessageDialog;

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
        chooser.setDialogTitle("Выбери файл перевода");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File trText = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери директорию куда сохранить файлы");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (cfg != null && cfg.getData().get(ConfigLineType.EN_PATH) != null) {
            chooser.setSelectedFile(new File(cfg.getData().get(ConfigLineType.EN_PATH)));
        }

        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File outDir = chooser.getSelectedFile();

        try {
            TextFixesUtil.processForumTranslate2(trText.getAbsolutePath(), outDir.getAbsolutePath());
            showMessageDialog(null, "Всё корректно записалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.XLS_PATH, trText.getAbsolutePath());
        cfg.getData().put(ConfigLineType.EN_PATH, outDir.getAbsolutePath());
        config.getValues().put(Operations.TRANSLATE_SMALL, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
