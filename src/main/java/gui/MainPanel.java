package gui;

import dto.StoredConfig;
import util.PatchWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class MainPanel extends JPanel implements ActionListener {
    private StoredConfig config;
    private JFrame frame;

    private final JButton extractDatButton;
    private final JButton extractDatButtonRepeat;
    private final JButton compressDatButton;
    private final JButton compressDatButtonRepeat;
    private final JButton extractXeButton;
    private final JButton extractXeButtonRepeat;
    private final JButton compressXeButton;
    private final JButton compressXeButtonRepeat;

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

        extractXeButton = new JButton("Преобразовать в текст XE файл");
        extractXeButton.addActionListener(new ExtractXEAction(frame, config));
        extractXeButtonRepeat = new JButton("Повторить");

        compressXeButton = new JButton("Собрать обратно XE файл");
        compressXeButton.addActionListener(new CompressXEAction(frame, config));
        compressXeButtonRepeat = new JButton("Повторить");


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, 1));
        panel.add(extractDatButton);
        panel.add(extractDatButtonRepeat);
        panel.add(compressDatButton);
        panel.add(compressDatButtonRepeat);
        panel.add(extractXeButton);
        //panel.add(extractXeButtonRepeat);
        panel.add(compressXeButton);
        //panel.add(compressXeButtonRepeat);
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

        File fil1=new File("C:\\projects\\mm5TextExtractor\\XEEN2.DAT");
        File fil2=new File("C:\\projects\\mm5TextExtractor\\xeen_modified.dat");
        File fil4=new File("C:\\projects\\mm5TextExtractor\\xeen_modified_new.dat");
        File fil3=new File("C:\\projects\\mm5TextExtractor\\patch.xls");
        PatchWorker pw=new PatchWorker();
        try {
            //pw.makePatch(fil1,fil2,106608, 269664);
            pw.applyPatch(fil1,fil4,fil3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(extractDatButton)) {
            System.out.println("asdas");
        }
    }
}
