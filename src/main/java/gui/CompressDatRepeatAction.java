package gui;

import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DatExtractor;
import util.DataCompressor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static javax.swing.JOptionPane.showMessageDialog;

public class CompressDatRepeatAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public CompressDatRepeatAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!config.getValues().containsKey(Operations.COMPRESS_DAT)) {
            return;
        }
        ConfigLine line = config.getValues().get(Operations.COMPRESS_DAT);
        DataCompressor dc = new DataCompressor();
        try {
            dc.compressTexts(new File(line.getData().get(ConfigLineType.ORIGINAL_PATH)),
                    new File(line.getData().get(ConfigLineType.XLS_PATH)),
                    line.getData().get(ConfigLineType.NEW_PATH));
            showMessageDialog(null, "Всё корректно записалось");
        }catch (Exception ex){
            showMessageDialog(null, ex.getMessage());
        }

    }
}
