package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;


import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionEvent;

public class SelectUser extends JFrame {

    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Integer> ids;
    private int idfile;
    private String path;

    public SelectUser(String path) throws IOException, ClassNotFoundException {

        this.path = path;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 316, 241);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // load user
        loadUser();
        table = new JTable(model) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                return switch (column) {
                    case 0 -> Boolean.class;
                    case 1 -> String.class;
                    case 2 -> String.class;
                    default -> Boolean.class;
                };
            }
        };
        TableColumnModel tcm = table.getColumnModel();
        tcm.removeColumn(tcm.getColumn(1));
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setBounds(10, 11, 105, 185);
        JScrollPane scrollPaneTable = new JScrollPane(table);
        scrollPaneTable.setBounds(10, 11, 274, 136);
        contentPane.add(scrollPaneTable);

        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            Socket soc = null;
            try {
                soc = new Socket("localhost", 1234);
                DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                dos.writeInt(13);
                dos.writeUTF(path);
                dos.writeInt(table.getRowCount());
                for (int i = 0; i < table.getRowCount(); i++) {
                    dos.writeBoolean((boolean) table.getModel().getValueAt(i, 0));
                    dos.flush();
                    if ((boolean) table.getModel().getValueAt(i, 0)) {
                        dos.writeInt(Integer.parseInt(table.getModel().getValueAt(i, 1).toString()));
                        dos.flush();
                    }
                }
                dispose();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnOK.setBounds(20, 168, 89, 23);
        contentPane.add(btnOK);

    }

    private void loadUser() throws IOException, ClassNotFoundException {
        Socket soc = new Socket("localhost", 1234);
        DataInputStream dis = new DataInputStream(soc.getInputStream());
        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
        dos.writeInt(12);
        dos.writeUTF(path);
        if (dis.readUTF().equals("Ok!")) {

//                DataInputStream dis = new DataInputStream(soc.getInputStream());
            ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());


            String[] header = {"Select", "ID", "Username", "Role"};
            ArrayList<ArrayList<Object>> data = (ArrayList<ArrayList<Object>>) ois.readObject();

            Object[][] value = data.stream().map(u -> u.toArray(new Object[0])).toArray(Object[][]::new);
            model = new DefaultTableModel(value, header);
        }
        else {
            System.out.println("Choose file");
        }
    }
}
