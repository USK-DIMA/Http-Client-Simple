package ru.uskov.dmitry;

import javax.swing.*;

/**
 * Created by Dmitry on 30.11.2016.
 */
public class MainForm {
    private JButton button1;
    private JTextField textField1;
    private JTextArea textArea1;
    private JPanel mainPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
