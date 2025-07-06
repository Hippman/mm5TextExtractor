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

import static javax.swing.JOptionPane.showMessageDialog;

public class ExtractDatRepeatAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public ExtractDatRepeatAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!config.getValues().containsKey(Operations.EXTRACT_DAT)) {
            return;
        }
        ConfigLine line = config.getValues().get(Operations.EXTRACT_DAT);
        DatExtractor ec = new DatExtractor();
        try {
            ec.extractText(new File(line.getData().get(ConfigLineType.ORIGINAL_PATH)), line.getData().get(ConfigLineType.XLS_PATH));
            showMessageDialog(null, "Всё корректно считалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getMessage());
        }
    }
}
