package ru.uskov.dmitry;

import http.client.HttpClient;
import http.client.HttpHeader;
import http.client.HttpResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Dmitry on 02.12.2016.
 */
public class MainForm {
    private JPanel mainPanel;
    private JButton sendButton;
    private JTextField textField1;
    private JTextPane textPane1;
    private JPanel topPanel;
    private JPanel buttonPanel;
    private JLabel httpLabel;
    private JLabel infoLable;
    private JList headerList;
    private JTextField statusTextField;
    private JComboBox methodComboBox;
    private JComboBox encodingComboBox;


    public MainForm() {
        methodComboBox.setModel(new DefaultComboBoxModel(new String[]{"GET", "POST"}));
        encodingComboBox.setModel(new DefaultComboBoxModel(new String[]{"UTF-8", "windows-1251"}));


        printInfo("Ready");
        statusTextField.setEnabled(false);
        sendButton.addActionListener(e -> {
            sendButton.setEnabled(false);
            new Thread(() -> {
                String url = textField1.getText();
                try {
                    printInfo("Process");
                    HttpResponse response = HttpClient.get(url, getEncoding());
                    textPane1.setText(response.getBody());
                    showHeaders(response.getHeaders());
                    statusTextField.setText(response.getStatusLine());
                    sendButton.setEnabled(true);
                    printInfo("Ready");
                } catch (Exception exep) {
                    printInfo("Error");
                    sendButton.setEnabled(true);
                }
            }).start();
        });
    }

    private void showHeaders(List<HttpHeader> headers) {
        DefaultListModel model = new DefaultListModel();
        headers.stream().forEach(h -> model.addElement(h.toString()));
        headerList.setModel(model);
    }

    private String getEncoding() {
        return encodingComboBox.getSelectedItem().toString();
    }

    private void printInfo(String message) {
        infoLable.setText(message);
    }

    private static void setLookAndFeelClassName(String systemLookAndFeelClassName) {
        try {
            UIManager.setLookAndFeel(systemLookAndFeelClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void fixSize(JComponent panel, int size) {
        panel.setMinimumSize(new Dimension(-1, size));
        panel.setPreferredSize(new Dimension(-1, size));
        panel.setMaximumSize(new Dimension(-1, size));
    }

    public static void main(String[] args) {
        setLookAndFeelClassName(UIManager.getSystemLookAndFeelClassName());
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Http Client");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
