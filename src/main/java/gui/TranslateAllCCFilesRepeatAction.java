package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DataCompressor;
import util.MirrFileCompressor;
import util.TextFixesUtil;
import util.XenFileWorker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.swing.JOptionPane.showMessageDialog;

public class TranslateAllCCFilesRepeatAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public TranslateAllCCFilesRepeatAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg = null;
        if (config.getValues().containsKey(Operations.TRANSLATE_ALL)) {
            cfg = config.getValues().get(Operations.TRANSLATE_ALL);
        }


        try {
            //AAZE
            TextFixesUtil.processForumTranslate2(cfg.getData().get(ConfigLineType.AAZE_PATH), cfg.getData().get(ConfigLineType.EN_PATH), 0);
            //AWARDS
            TextFixesUtil.processForumTranslate2(cfg.getData().get(ConfigLineType.AWARDS_PATH), cfg.getData().get(ConfigLineType.EN_PATH), 1);
            //mae.xen
            XenFileWorker ec = new XenFileWorker();
            ec.compressTexts(new File(cfg.getData().get(ConfigLineType.MAE_XEN_PATH)),cfg.getData().get(ConfigLineType.EN_PATH)+"/mae.xen");
            //spells.xen
            ec.compressTexts(new File(cfg.getData().get(ConfigLineType.SPELLS_XEN_PATH)),cfg.getData().get(ConfigLineType.EN_PATH)+"/spells.xen");
            //xen.mon
            DataCompressor dc = new DataCompressor();
            //mirr
            MirrFileCompressor.compressMirr(new File(cfg.getData().get(ConfigLineType.DARK_MIRR_PATH)),
                    new File(cfg.getData().get(ConfigLineType.DARK_MIRR_TRANSLATE_PATH)),
                    new File(cfg.getData().get(ConfigLineType.EN_PATH)+ "/darkmirr.txt"));

            dc.compressMobs(new File(cfg.getData().get(ConfigLineType.XEN_MON_PATH)),
                    new File(cfg.getData().get(ConfigLineType.MOB_NAMES_PATH)),
                    cfg.getData().get(ConfigLineType.EN_PATH)+"/xeen.mon");


            showMessageDialog(null, "Всё корректно записалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }

    }
}
