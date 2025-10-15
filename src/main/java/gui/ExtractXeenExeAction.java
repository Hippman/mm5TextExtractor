package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DatExtractor;
import util.ExeDatExtractor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.swing.JOptionPane.showMessageDialog;

public class ExtractXeenExeAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public ExtractXeenExeAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;
        if (config.getValues().containsKey(Operations.EXE_EXTRACT_DAT)) {
            cfg = config.getValues().get(Operations.EXE_EXTRACT_DAT);
        }
        JFileChooser chooser = new JFileChooser(".");

        chooser.setSelectedFile(new File(cfg == null ? "Xeen.exe" : cfg.getData().get(ConfigLineType.ORIGINAL_PATH)));
        chooser.setDialogTitle("Выбери файл Xeen.exe");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xeenDat = chooser.getSelectedFile();
        chooser.setDialogTitle("Выбери путь к Xls файлу");
        chooser.setSelectedFile(new File(cfg == null ? "xeen.exe.xls" : cfg.getData().get(ConfigLineType.XLS_PATH)));
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File xlsFile = chooser.getSelectedFile();
        System.out.println("Extracting");
        ExeDatExtractor ec = new ExeDatExtractor();
        try {
            ec.extractText(xeenDat, xlsFile.getAbsolutePath());
            showMessageDialog(null, "Всё корректно считалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getMessage());
        }

        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, xeenDat.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XLS_PATH, xlsFile.getAbsolutePath());
        config.getValues().put(Operations.EXE_EXTRACT_DAT, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
