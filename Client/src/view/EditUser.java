package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;


import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;

public class EditUser extends JFrame {

    private JPanel contentPane;
    private JTextField txtID;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtName;
    private int id;


//    DataOutputStream dos;


    public EditUser(int id) throws IOException, ClassNotFoundException {
        Socket soc = new Socket("localhost", 1234);
        this.id = id;

        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
        dos.writeInt(6);
        dos.writeInt(id);
//        dos.writeInt(this.id);

        DataInputStream dis = new DataInputStream(soc.getInputStream());
        System.out.println("papa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 314);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JLabel lbID = new JLabel("ID");
        lbID.setBounds(24, 25, 101, 14);
        contentPane.add(lbID);

        JLabel lbUsername = new JLabel("Username");
        lbUsername.setBounds(24, 65, 101, 14);
        contentPane.add(lbUsername);

        JLabel lbPassword = new JLabel("Password");
        lbPassword.setBounds(24, 101, 101, 14);
        contentPane.add(lbPassword);

        JLabel lbName = new JLabel("Name");
        lbName.setBounds(24, 141, 101, 14);
        contentPane.add(lbName);

        JLabel lbRole = new JLabel("Role");
        lbRole.setBounds(24, 183, 101, 14);
        contentPane.add(lbRole);

        txtID = new JTextField();
        txtID.setEditable(false);
        txtID.setBounds(135, 22, 238, 20);
        contentPane.add(txtID);
        txtID.setColumns(10);
        txtID.setText(String.valueOf(id));

        txtUsername = new JTextField();
        txtUsername.setEditable(false);
        txtUsername.setBounds(135, 62, 238, 20);
        contentPane.add(txtUsername);
        txtUsername.setColumns(10);
        txtUsername.setText(dis.readUTF());

        System.out.println("papa222");
        txtPassword = new JPasswordField();
        txtPassword.setBounds(135, 98, 238, 20);
        contentPane.add(txtPassword);
        txtPassword.setColumns(10);
        txtPassword.setText(dis.readUTF());

        txtName = new JTextField();
        txtName.setBounds(135, 138, 238, 20);
        contentPane.add(txtName);
        txtName.setColumns(10);
        txtName.setText(dis.readUTF());

        JComboBox comboBox = new JComboBox(new String[]{"Admin", "User"});
        comboBox.setBounds(135, 178, 238, 24);
        comboBox.setSelectedIndex(dis.readInt() - 1);
        contentPane.add(comboBox);

        System.out.println("hfhrtghgtha");
        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {

            try {
                Socket soc1 = new Socket("localhost", 1234);

                DataOutputStream dos1 = new DataOutputStream(soc1.getOutputStream());
//                DataInputStream dis1 = new DataInputStream(soc1.getInputStream());
                dos1.writeInt(8);
                dos1.writeInt(id);
                dos1.writeUTF(new String(txtPassword.getPassword()));
                dos1.writeUTF(txtName.getText());
                dos1.writeInt(comboBox.getSelectedIndex() + 1);

                Admin.reloadUser();
                Admin.reloadUser();
                dispose();

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnOK.setBounds(67, 227, 89, 23);
        contentPane.add(btnOK);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        btnCancel.setBounds(266, 227, 89, 23);
        contentPane.add(btnCancel);
    }
}
