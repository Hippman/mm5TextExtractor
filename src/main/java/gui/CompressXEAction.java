package gui;

import com.google.gson.Gson;
import dto.ConfigLine;
import dto.StoredConfig;
import enums.ConfigLineType;
import enums.Operations;
import lombok.SneakyThrows;
import util.DataCompressor;
import util.XenFileWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompressXEAction implements ActionListener {


    private JFrame fram;
    private StoredConfig config;

    public CompressXEAction(JFrame fram, StoredConfig config) {
        this.fram = fram;
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigLine cfg=null;
        if(config.getValues().containsKey(Operations.COMPRESS_XE)){
            cfg=config.getValues().get(Operations.COMPRESS_XE);
        }

        JFileChooser chooser = new JFileChooser(".");

        chooser.setDialogTitle("Выбери путь к Xls файлу");
        chooser.setSelectedFile(new File(cfg==null?"stored_texts.xls":cfg.getData().get(ConfigLineType.XLS_PATH)));
        int retval = chooser.showOpenDialog(fram);
        if (retval != 0) {
            return;
        }
        File xlsFile = chooser.getSelectedFile();

        chooser.setDialogTitle("Выбери путь к новому XE/DAT файлу");
        chooser.setSelectedFile(new File(cfg==null?"new.dizl.xe":cfg.getData().get(ConfigLineType.NEW_PATH)));
        retval = chooser.showSaveDialog(fram);
        if (retval != 0) {
            return;
        }
        File newXeenDat = chooser.getSelectedFile();


        System.out.println("Compressing XEN");
        XenFileWorker ec = new XenFileWorker();
        ec.compressTexts(xlsFile,newXeenDat.getAbsolutePath());

        cfg = new ConfigLine();
        cfg.getData().put(ConfigLineType.XLS_PATH, xlsFile.getAbsolutePath());
        cfg.getData().put(ConfigLineType.NEW_PATH, newXeenDat.getAbsolutePath());
        config.getValues().put(Operations.COMPRESS_XE, cfg);

        Gson gson = new Gson();
        String data = gson.toJson(config);
        Files.write(Path.of("last_paths.json"), data.getBytes());
    }
}
