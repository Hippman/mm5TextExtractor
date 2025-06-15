package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DatExtractor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExtractDatAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public ExtractDatAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(".");

        chooser.setSelectedFile((File) null);
        chooser.setDialogTitle("Выбери файл Xeen.dat");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xeenDat = chooser.getSelectedFile();
        chooser.setDialogTitle("Выбери путь к Xls файлу");
        chooser.setSelectedFile(new File("stored_texts.xls"));
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File xlsFile = chooser.getSelectedFile();
        System.out.println("Extracting");
        DatExtractor ec = new DatExtractor();
        ec.extractText(xeenDat, xlsFile.getAbsolutePath());

        ConfigLine cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.ORIGINAL_PATH, xeenDat.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XLS_PATH, xlsFile.getAbsolutePath());
        config.getValues().put(Operations.EXTRACT_DAT, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
