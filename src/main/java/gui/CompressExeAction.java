package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DataCompressor;
import util.ExeDataCompressor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.swing.JOptionPane.showMessageDialog;

public class CompressExeAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public CompressExeAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;

        if (config.getValues().containsKey(Operations.COMPRESS_EXE)) {
            cfg = config.getValues().get(Operations.COMPRESS_EXE);
        }

        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new File(cfg == null ? "Xeen.exe" : cfg.getData().get(ConfigLineType.ORIGINAL_PATH)));
        chooser.setDialogTitle("Выбери оригинальный файл Xeen.exe");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xeenDat = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери путь к Xls файлу");
        chooser.setSelectedFile(new File(cfg == null ? "xeen.exe.xls" : cfg.getData().get(ConfigLineType.XLS_PATH)));
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xlsFile = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери путь к новому xeen.exe файлу");
        chooser.setSelectedFile(new File(cfg == null ? "new.xeen.exe" : cfg.getData().get(ConfigLineType.NEW_PATH)));
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File newXeenExe = chooser.getSelectedFile();


        System.out.println("Compressing");
        ExeDataCompressor dc = new ExeDataCompressor();
        try {
            dc.compressTexts(xeenDat, xlsFile.getAbsoluteFile(), newXeenExe.getAbsolutePath());
            showMessageDialog(null, "Всё корректно записалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getMessage());
        }

        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, xeenDat.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XLS_PATH, xlsFile.getAbsolutePath());
        cfg.getData().put(ConfigLineType.NEW_PATH, newXeenExe.getAbsolutePath());
        config.getValues().put(Operations.COMPRESS_EXE, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
