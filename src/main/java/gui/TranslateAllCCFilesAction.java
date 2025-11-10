package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DataCompressor;
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

public class TranslateAllCCFilesAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public TranslateAllCCFilesAction(JFrame fram, StoredConfig config) {
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

        JFileChooser chooser = new JFileChooser(".");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV File", "csv");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "aaze.csv" : cfg.getData().get(ConfigLineType.AAZE_PATH)));
        chooser.setDialogTitle("Выбери файл перевода AAZE");
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File aaze = chooser.getSelectedFile();

        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "awards.csv" : cfg.getData().get(ConfigLineType.AWARDS_PATH)));
        chooser.setDialogTitle("Выбери файл перевода AWARDS");
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File awards = chooser.getSelectedFile();


        filter = new FileNameExtensionFilter("XLS File", "xls");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(cfg == null ? "mae.xen.xls" : cfg.getData().get(ConfigLineType.MAE_XEN_PATH)));
        chooser.setDialogTitle("Выбери файл перевода MAE.XEN");
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File maexen = chooser.getSelectedFile();

        chooser.setSelectedFile(new File(cfg == null ? "spells.xen.xls" : cfg.getData().get(ConfigLineType.SPELLS_XEN_PATH)));
        chooser.setDialogTitle("Выбери файл перевода SPELLS.XEN");
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File spellsxen = chooser.getSelectedFile();


        chooser.setSelectedFile(new File(cfg == null ? "dark.mon" : cfg.getData().get(ConfigLineType.XEN_MON_PATH)));
        filter = new FileNameExtensionFilter("MON File", "mon");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Выбери оригинальный файл dark.mon");
        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xeenMon = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери путь к Xls файлу c переводом dark.mon");
        filter = new FileNameExtensionFilter("XLS File", "xls");
        chooser.setFileFilter(filter);
        String filename = "mob_names.xls";
        if (cfg != null && cfg.getData().get(ConfigLineType.MOB_NAMES_PATH) != null) {
            filename = cfg.getData().get(ConfigLineType.MOB_NAMES_PATH);
        }

        chooser.setSelectedFile(new File(cfg == null ? "mob_names.xls" : filename));

        retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File mobNames = chooser.getSelectedFile();

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
            //AAZE
            TextFixesUtil.processForumTranslate2(aaze.getAbsolutePath(), outDir.getAbsolutePath(), 0);
            //AWARDS
            TextFixesUtil.processForumTranslate2(awards.getAbsolutePath(), outDir.getAbsolutePath(), 1);
            //mae.xen
            XenFileWorker ec = new XenFileWorker();
            ec.compressTexts(maexen, outDir.getAbsolutePath() + "/mae.xen");
            //spells.xen
            ec.compressTexts(spellsxen, outDir.getAbsolutePath() + "/spells.xen");
            //xen.mon
            DataCompressor dc = new DataCompressor();
            dc.compressMobs(xeenMon, mobNames.getAbsoluteFile(), outDir.getAbsolutePath() + "/dark.mon");


            showMessageDialog(null, "Всё корректно записалось");
        } catch (Exception ex) {
            showMessageDialog(null, ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.AAZE_PATH, aaze.getAbsolutePath());
        cfg.getData().put(ConfigLineType.AWARDS_PATH, awards.getAbsolutePath());
        cfg.getData().put(ConfigLineType.MAE_XEN_PATH, maexen.getAbsolutePath());
        cfg.getData().put(ConfigLineType.SPELLS_XEN_PATH, spellsxen.getAbsolutePath());
        cfg.getData().put(ConfigLineType.XEN_MON_PATH, xeenMon.getAbsolutePath());
        cfg.getData().put(ConfigLineType.EN_PATH, outDir.getAbsolutePath());
        cfg.getData().put(ConfigLineType.MOB_NAMES_PATH, mobNames.getAbsolutePath());
        config.getValues().put(Operations.TRANSLATE_ALL, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
