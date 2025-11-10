package gui;

import lombok.Data;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Data
public class LogPanel extends JScrollPane implements ActionListener {
    private JFrame frame;
    JTextArea textArea;

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public LogPanel(int x, int y) {
        textArea = new JTextArea(20, 50);

        // Настраиваем перенос строк и слов
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Помещаем JTextArea в JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame = new JFrame("Might and Magic 5 file woorker logs");
        frame.setLocation(x, y);
        frame.add(scrollPane);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }
}
