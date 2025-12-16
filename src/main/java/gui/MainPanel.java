package gui;

import dto.StoredConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainPanel extends JPanel implements ActionListener {
    private StoredConfig config;
    private JFrame frame;

    private final JButton extractDatButton;
    private final JButton extractDatButtonRepeat;
    private final JButton compressDatButton;
    private final JButton compressMirrButton;
    private final JButton compressDatButtonRepeat;
    private final JButton extractXeButton;
    private final JButton extractXeButtonRepeat;
    private final JButton compressXeButton;
    private final JButton translateFiles;
    private final JButton ExtractMobs;
    private final JButton CompressMobs;
    private final JButton extractXeenExe;
    private final JButton compressXeenExe;
    private final JButton extractRaw;
    private final JButton compressRaw;
    private final JSeparator separator;

    private final JButton compressAllTextFiles;
    private final JButton compressAllTextFilesRepeat;


    public MainPanel(StoredConfig config) {
        this.config = config;
        this.setLayout(new BoxLayout(this, 1));

        extractDatButton = new JButton("Распаковать Xeen.dat");
        extractDatButton.addActionListener(new ExtractDatAction(frame, config));

        extractDatButtonRepeat = new JButton("Повторить");
        extractDatButtonRepeat.addActionListener(new ExtractDatRepeatAction(frame, config));

        compressDatButton = new JButton("Запаковать Xeen.dat");
        compressDatButton.addActionListener(new CompressDatAction(frame, config));

        compressDatButtonRepeat = new JButton("Повторить");
        compressDatButtonRepeat.addActionListener(new CompressDatRepeatAction(frame, config));

        extractXeButton = new JButton("Преобразовать текстовый файл в XLS");
        extractXeButton.addActionListener(new ExtractXEAction(frame, config));
        extractXeButtonRepeat = new JButton("Повторить");

        compressXeButton = new JButton("Собрать из XLS текстовый файл");
        compressXeButton.addActionListener(new CompressXEAction(frame, config));
//-
        translateFiles = new JButton("Заполнить переводами малые файлы");
        translateFiles.addActionListener(new TranslateSmallAction(frame, config));

        ExtractMobs = new JButton("Экспортировать имена монстров из mon файла");
        ExtractMobs.addActionListener(new ExtractMobsAction(frame, config));
//-
        CompressMobs = new JButton("Записать имена монстров в mon файл");
        CompressMobs.addActionListener(new CompressMobAction(frame, config));

        extractXeenExe = new JButton("Извлечь тексты из Xeen.exe");
        extractXeenExe.addActionListener(new ExtractXeenExeAction(frame, config));

        compressXeenExe = new JButton("Записать тексты в Xeen.exe");
        compressXeenExe.addActionListener(new CompressExeAction(frame, config));

        compressMirrButton = new JButton("Собрать новый MIRR файл");
        compressMirrButton.addActionListener(new CompressMirrAction(frame, config));

        extractRaw = new JButton("Преобразовать RAW в BMP");
        extractRaw.addActionListener(new ExtractRawAction(frame, config));

        compressRaw = new JButton("Преобразовать  BMP в RAW");
        compressRaw.addActionListener(new CompressRawAction(frame, config));

        separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0, 10));

        compressAllTextFiles = new JButton("Собрать все текстовые файлы для CC");
        compressAllTextFiles.addActionListener(new TranslateAllCCFilesAction(frame, config));
        compressAllTextFilesRepeat = new JButton("Повторить");
        compressAllTextFilesRepeat.addActionListener(new TranslateAllCCFilesRepeatAction(frame, config));


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, 1));
        panel.add(extractDatButton);
        panel.add(extractDatButtonRepeat);
        panel.add(compressDatButton);
        panel.add(compressDatButtonRepeat);
        panel.add(extractXeButton);
        //panel.add(extractXeButtonRepeat);
        panel.add(compressXeButton);
        panel.add(translateFiles);
        panel.add(ExtractMobs);
        panel.add(CompressMobs);
        panel.add(extractXeenExe);
        panel.add(compressXeenExe);
        panel.add(compressMirrButton);
        panel.add(extractRaw);
        panel.add(compressRaw);
        panel.add(separator);
        panel.add(compressAllTextFiles);
        panel.add(compressAllTextFilesRepeat);
        add(panel);


        frame = new JFrame("Might and Magic 5 file woorker");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", panel);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(extractDatButton)) {
            System.out.println("asdas");
        }
    }
}
