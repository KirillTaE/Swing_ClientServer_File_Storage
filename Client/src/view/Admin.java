package view;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class Admin extends JFrame {
    private static JTable table;
    private JButton btnRefresh1;
    private DefaultMutableTreeNode root1;
    private DefaultMutableTreeNode root2;
    private DefaultMutableTreeNode root3;
    private DefaultTreeModel treeModel1;
    private DefaultTreeModel treeModel2;
    private DefaultTreeModel treeModel3;
    private JTree tree1;
    private JTree tree2;
    private JTree tree3;
    static String value = "";
    private JPanel contentPane;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtName;
    private JButton btnRefresh;
    private JScrollPane scrollPane2;
    static String value1 = "";
    static String value2 = "";
    final File[] fileToSend = new File[1];
    private int iduser;

//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    Login frame = new Login();
//                    frame.setVisible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
////		ArrayList<String> data = getListFiles(new File("C:\\Users\\Kirill\\OneDrive\\Desktop\\test"));
////		for (int i = 0; i < data.size(); ++i) {
////			System.out.println(data.get(i));
////		}
//    }

    public String path_to_All_Files = "";


    public Admin(int iduser) {

        this.iduser = iduser;

        init();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 900, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JTabbedPane tabMain = new JTabbedPane(JTabbedPane.TOP);
        tabMain.setBounds(0, 0, 874, 676);
        contentPane.add(tabMain);

        JPanel tabMyFile = new JPanel();
        tabMyFile.setLayout(null);
        tabMain.addTab("My file", null, tabMyFile, null);

        // tree 1: server folder

        // load folder from server
        loadFile();


//        File fileRoot1 = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\Client_Files");
        File fileRoot1 = new File(path_to_All_Files + "test_temp\\All_Files");
        root1 = new DefaultMutableTreeNode(new FileNode(fileRoot1));
        treeModel1 = new DefaultTreeModel(root1);

        tree1 = new JTree(treeModel1);
        tree1.setShowsRootHandles(true);

        JScrollPane scrollPane1 = new JScrollPane(tree1);
//        scrollPane1.setBounds(40, 61, 310, 570);

        scrollPane1.setBounds(526, 61, 310, 570);

        createChildren(fileRoot1, root1);
        tabMyFile.add(scrollPane1);


        // delete temporary folder
//        deleteDir(new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp"));
        deleteDir(new File(path_to_All_Files + "test_temp"));

        JButton btnSync = new JButton("Sync");
        btnSync.addActionListener(e -> {
            ArrayList<String> selection1 = new ArrayList<String>();
            TreePath[] paths1 = tree1.getSelectionPaths();
            for (int i = 0; paths1 != null && i < paths1.length; i++) {
                value1 = "";
                Object[] elements = paths1[i].getPath();
                for (Object element : elements) {
                    value1 += element + "\\";
                }
                String path = value1.substring(0, value1.length() - 1);
                selection1.add(path);
            }
            ArrayList<String> selection2 = new ArrayList<String>();
            TreePath[] paths2 = tree2.getSelectionPaths();
            for (int i = 0; paths2 != null && i < paths2.length; i++) {
                value2 = "";
                Object[] elements = paths2[i].getPath();
                for (Object element : elements) {
                    value2 += element + "\\";
                }
                String path = value2.substring(0, value2.length() - 1);
//                    File file = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\" + path);
                File file = new File(path_to_All_Files + path);
                if (file.isDirectory()) {
                    ArrayList<String> listFiles = getListFiles(file);
                    selection2.addAll(listFiles);
                } else {
                    selection2.add(path);
                }
            }

            // log for debugging
//				for (int i = 0; i < selection1.size(); ++i) {
//					System.out.println(selection1.get(i));
//				}
//				for (int i = 0; i < selection2.size(); ++i) {
//					System.out.println(selection2.get(i));
//				}

            try {
                Socket soc = new Socket("localhost", 1234);
                int n1 = selection1.size();
                int n2 = selection2.size();
                if (n1 > 0) {
                    DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                    dos.writeInt(1);
                    dos.writeInt(n1);
                    for (String s : selection1) {
                        dos.writeUTF(s);
                        dos.flush();
                    }
                    DataInputStream dis = new DataInputStream(soc.getInputStream());
                    int cmd = dis.readInt();
                    int n = dis.readInt();
                    for (int i = 0; i < n; ++i) {
                        String confirm = dis.readUTF();
                        if (confirm.equals("Ok!")) {
                            String name = dis.readUTF();
                            System.out.println(name);
                            String path = dis.readUTF();
                            System.out.println(path);
                            int size = dis.readInt();
                            byte[] data = dis.readNBytes(size);
//                            File file = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\" + path);
                            File file = new File(path_to_All_Files + path);
                            // if file exist, delete old file
                            if (file.exists()) {
                                file.delete();
                            }
                            file.getParentFile().mkdirs();
                            // create new file
                            file.createNewFile();
                            Files.write(file.toPath(), data, StandardOpenOption.SYNC);
                        } else {
                            System.out.println("Choose file");
                            break;
                        }
                    }
                }
                if (n2 > 0) {
                    DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                    dos.writeInt(2);
                    dos.writeInt(n2);
                    for (String s : selection2) {
//                            File file = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\" + s);
                        File file = new File(path_to_All_Files + s);
                        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        dos.writeUTF(file.getName());
                        dos.writeUTF(s);
                        dos.writeUTF(formatDateTime(attr.creationTime()));
                        dos.writeUTF(formatDateTime(attr.lastModifiedTime()));
                        byte[] data = Files.readAllBytes(file.toPath());
                        dos.writeInt(data.length);
                        dos.write(data);
                        dos.flush();
                    }
                }

                // clear selection
                tree1.clearSelection();
                tree2.clearSelection();

                // refresh view
                btnRefresh.doClick();

                new ReceiveClient(soc).start();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        btnSync.setBounds(385, 96, 89, 23);
        tabMyFile.add(btnSync);

        // tree 2: client folder
//        File fileRoot2 = new File("C:\\Users\\Kirill\\OneDrive\\Desktop\\Client_Files");
        File fileRoot2 = new File(path_to_All_Files + "All_Files");
        root2 = new DefaultMutableTreeNode(new FileNode(fileRoot2));
        treeModel2 = new DefaultTreeModel(root2);

        tree2 = new JTree(treeModel2);
        tree2.setShowsRootHandles(true);

        scrollPane2 = new JScrollPane(tree2);
//        scrollPane2.setBounds(526, 61, 310, 570);

        scrollPane2.setBounds(40, 61, 310, 570);

        createChildren(fileRoot2, root2);
        tabMyFile.add(scrollPane2);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(385, 305, 89, 23);
        btnRefresh.addActionListener(e -> {
            // refresh tree 1: server
            loadFile();
            root1.removeAllChildren();
//                File fileRoot1 = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\Client_Files");
            File fileRoot11 = new File(path_to_All_Files + "test_temp\\All_Files");
            createChildren(fileRoot11, root1);
            treeModel1.reload();
            // refresh tree 2: client
            root2.removeAllChildren();
//                File fileRoot2 = new File("C:\\Users\\Kirill\\OneDrive\\Desktop\\Client_Files");
            File fileRoot21 = new File(path_to_All_Files + "All_Files");
            createChildren(fileRoot21, root2);
            treeModel2.reload();
            // delete temporary folder
//                deleteDir(new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp"));
            deleteDir(new File(path_to_All_Files + "test_temp"));
        });
        tabMyFile.add(btnRefresh);

        // User management tab
        JPanel tabUserManager = new JPanel();
        tabUserManager.setLayout(null);
        tabMain.addTab("Number of users", null, tabUserManager, null);

        loadUser();
        table.setBounds(10, 11, 705, 185);
        JScrollPane scrollPaneTable = new JScrollPane(table);
        scrollPaneTable.setBounds(10, 11, 705, 136);
        tabUserManager.add(scrollPaneTable);

        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(e -> {
            int[] index = table.getSelectedRows();
            if (index.length == 1) {
                int id = Integer.parseInt(table.getModel().getValueAt(index[0], 0).toString());
                EditUser frame = null;
                try {
                    frame = new EditUser(id);
                    frame.setVisible(true);
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });
        btnEdit.setBounds(218, 174, 89, 23);
        tabUserManager.add(btnEdit);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> {

            int[] index = table.getSelectedRows();
            int index_len = index.length;

            Socket soc;
            DataOutputStream dos;
            try {
                soc = new Socket("localhost", 1234);
                dos = new DataOutputStream(soc.getOutputStream());
                dos.writeInt(9);
                dos.writeInt(index_len);
                for (int j : index) {
                    dos.writeInt(j);
                    dos.flush();
                }
                reloadUser();
                reloadUser();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });
        btnDelete.setBounds(393, 174, 89, 23);
        tabUserManager.add(btnDelete);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> {
            AddUser frame = new AddUser();
            frame.setVisible(true);
        });
        btnAdd.setBounds(62, 174, 89, 23);
        tabUserManager.add(btnAdd);

        JButton btnRefreshUser = new JButton("Refresh");
        btnRefreshUser.addActionListener(e -> reloadUser());
        btnRefreshUser.setBounds(546, 174, 89, 23);
        tabUserManager.add(btnRefreshUser);


        tabMyFile.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                tree1.clearSelection();
                tree2.clearSelection();
            }
        });

        JLabel lbTitle = new JLabel("<- Client|Synchronize Folder Application|Server ->");
        lbTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lbTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
        lbTitle.setBounds(210, 10, 450, 23);
        tabMyFile.add(lbTitle);

        // Access and Delete Files Tab
        JPanel tabAccDel = new JPanel();
        tabAccDel.setLayout(null);
        tabMain.addTab("Acc./ Del.", null, tabAccDel, null);

        loadFile();

//        File fileRoot = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\Client_Files");
        File fileRoot = new File(path_to_All_Files + "test_temp\\All_Files");
        root3 = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel3 = new DefaultTreeModel(root3);

        tree3 = new JTree(treeModel3);
        tree3.setShowsRootHandles(true);

        JScrollPane scrollPane = new JScrollPane(tree3);
        scrollPane.setBounds(0, 0, 437, 533);

        createChildren(fileRoot, root3);
        tabAccDel.add(scrollPane);

//        deleteDir(new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp"));
        deleteDir(new File(path_to_All_Files + "\\test_temp"));

        JButton btnDelFile = new JButton("Delete");
        btnDelFile.addActionListener(e -> {
            Socket soc = null;
            try {
                soc = new Socket("localhost", 1234);
                DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                dos.writeInt(11);

                ObjectOutputStream oos = new ObjectOutputStream(dos);


                TreePath[] paths = tree3.getSelectionPaths();
                oos.writeObject(paths);

                btnRefresh1.doClick();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });
        btnDelFile.setBounds(475, 30, 89, 23);
        tabAccDel.add(btnDelFile);

        JButton btnAccess = new JButton("Access...");
        btnAccess.addActionListener(e -> {
            TreePath[] paths = tree3.getSelectionPaths();
            if (Objects.requireNonNull(paths).length == 1) {
                // select access user
                value = "";
                Object[] elements = paths[0].getPath();
                for (Object element : elements) {
                    value += element + "\\";
                }
                String path = value.substring(0, value.length() - 1);
                try {
                    SelectUser frame = new SelectUser(path);
                    frame.setVisible(true);
                } catch (Exception e1) {
                    System.out.println("Choose file");
                }
            }
        });
        btnAccess.setBounds(475, 123, 89, 23);
        tabAccDel.add(btnAccess);


        btnRefresh1 = new JButton("Refresh");
        btnRefresh1.setBounds(475, 213, 89, 23);
        btnRefresh1.addActionListener(e -> {
            // refresh tree 1: server
            loadFile();
            root3.removeAllChildren();
//                File fileRoot1 = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\Client_Files");
            File fileRoot33 = new File(path_to_All_Files + "test_temp\\All_Files");
            createChildren(fileRoot33, root3);
            treeModel3.reload();

            deleteDir(new File(path_to_All_Files + "test_temp"));
        });
        tabAccDel.add(btnRefresh1);


        // User information tab
        JPanel tabUserInfo = new JPanel();
        tabUserInfo.setLayout(null);
        tabMain.addTab("User Information", null, tabUserInfo, null);

        JLabel lbUsername = new JLabel("Username");
        lbUsername.setBounds(32, 35, 101, 14);
        tabUserInfo.add(lbUsername);

        JLabel lbPassword = new JLabel("Password");
        lbPassword.setBounds(32, 75, 101, 14);
        tabUserInfo.add(lbPassword);

        JLabel lbName = new JLabel("Name");
        lbName.setBounds(32, 115, 101, 14);
        tabUserInfo.add(lbName);

        txtUsername = new JTextField();
        txtUsername.setEditable(false);
        txtUsername.setBounds(177, 32, 156, 20);
        tabUserInfo.add(txtUsername);
        txtUsername.setColumns(10);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(177, 72, 156, 20);
        tabUserInfo.add(txtPassword);
        txtPassword.setColumns(10);

        txtName = new JTextField();
        txtName.setBounds(177, 112, 156, 20);
        tabUserInfo.add(txtName);
        txtName.setColumns(10);

        try {
            Socket soc = new Socket("localhost", 1234);
            DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
            dos.writeInt(6);
            dos.writeInt(iduser);
            DataInputStream dis = new DataInputStream(soc.getInputStream());
            txtUsername.setText(dis.readUTF());
            txtPassword.setText(dis.readUTF());
            txtName.setText(dis.readUTF());
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            try {
                Socket soc = new Socket("localhost", 1234);
                DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                dos.writeInt(4);
                dos.writeInt(iduser);
                dos.writeUTF(new String(txtPassword.getPassword()));
                dos.writeUTF(txtName.getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        btnOK.setBounds(69, 182, 89, 23);
        tabUserInfo.add(btnOK);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            txtName.setText("");
            txtPassword.setText("");
        });
        btnCancel.setBounds(253, 182, 89, 23);
        tabUserInfo.add(btnCancel);

//		Socket soc;
//		try {
//			soc = new Socket("localhost", 1234);
//			new ReceiveClient(soc).start();
//		} catch (UnknownHostException e1) {
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
    }

    private void loadUser() {

        try {
            Socket soc = new Socket("localhost", 1234);
            DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
            dos.writeInt(7);
//                DataInputStream dis = new DataInputStream(soc.getInputStream());
            ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());


            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            data = (ArrayList<ArrayList<String>>) ois.readObject();


            String[] header = {"ID", "Username", "Name", "Role"};

            String[][] value = data.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
            table = new JTable(value, header);
            AbstractTableModel tableModel = (AbstractTableModel) table.getModel();
            TableColumnModel tcm = table.getColumnModel();
            tcm.removeColumn(tcm.getColumn(0));
            tableModel.fireTableDataChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected static void reloadUser() {
        try {

            Socket soc = new Socket("localhost", 1234);
            DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
            dos.writeInt(7);
//                DataInputStream dis = new DataInputStream(soc.getInputStream());
            ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());


            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            data = (ArrayList<ArrayList<String>>) ois.readObject();
            String[] header = {"ID", "Username", "Name", "Role"};

            String[][] value = data.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
            DefaultTableModel tableModel = new DefaultTableModel(value, header);
            table.setModel(tableModel);
            TableColumnModel tcm = table.getColumnModel();
            tcm.removeColumn(tcm.getColumn(0));

//        String[][] value = data.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
//        table = new JTable(value, header);
//        AbstractTableModel tableModel = (AbstractTableModel) table.getModel();
//        TableColumnModel tcm = table.getColumnModel();
//        tcm.removeColumn(tcm.getColumn(0));
//        tableModel.fireTableDataChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createChildren(File fileRoot, DefaultMutableTreeNode node) {
        File[] files = fileRoot.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
            node.add(childNode);
            if (file.isDirectory()) {
                createChildren(file, childNode);
            }
        }
    }

    class FileNode {

        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }

    // load file from server
    public void loadFile() {
        try {
            Socket soc = new Socket("localhost", 1234);
            DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
            dos.writeInt(5);
            dos.writeInt(iduser);
            DataInputStream dis = new DataInputStream(soc.getInputStream());
            int n = dis.readInt();
            System.out.println(n);
            for (int i = 0; i < n; ++i) {
                String path = dis.readUTF();
                System.out.println(path);
//                System.out.println(System.getProperty("user.home"));
//                File file = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\" + path);
                File file = new File(path_to_All_Files + "test_temp\\" + path);
                System.out.println(file.getParentFile().mkdirs());
                System.out.println(file.createNewFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // create temporary folder
//	public void createDir(ArrayList<String> paths) {
//		try {
//			for (int i = 0; i < paths.size(); ++i) {
//				File file = new File(System.getProperty("user.home") + "/OneDrive/Desktop/test_temp/test" + paths.get(i));
//				file.getParentFile().mkdirs();
//				file.createNewFile();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

    // delete temporary folder
    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (String child : Objects.requireNonNull(files)) {
                System.out.println(child);
                File childDir = new File(file, child);
                if (childDir.isDirectory()) {
                    deleteDir(childDir);
                } else {
                    childDir.delete();
                }
            }
            if (Objects.requireNonNull(file.list()).length == 0) {
                file.delete();
            }
        } else {
            file.delete();
        }
    }

    // format date time
    public static String formatDateTime(FileTime fileTime) {
        LocalDateTime localDateTime = fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
    }

    // make default share folder if not exist and load folder from server
    private void init() {
//        File share = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\Client_Files");
        File share = new File(path_to_All_Files + "All_Files");
        if (!share.exists()) {
            System.out.println(share.mkdir());
        }
    }

    // Get list paths of files from a directory
    private static ArrayList<String> getListFiles(File file) {
        ArrayList<String> listPaths = new ArrayList<String>();
        Stream<Path> paths = null;
        try {
            paths = Files.walk(Paths.get(file.getAbsolutePath()));
            paths.filter(Files::isRegularFile).map(path -> path.toString().substring(path.toString().indexOf("All_Files")))
                    .forEach(listPaths::add);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return listPaths;
    }

    class ReceiveClient extends Thread {
        private Socket client;

        public ReceiveClient(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(client.getInputStream());
                while (true) {
                    int cmd = dis.readInt();
                    // receive file
                    if (cmd == 1) {
                        String name = dis.readUTF();
                        System.out.println(name);
                        String path = dis.readUTF();
                        System.out.println(path);
                        int size = dis.readInt();
                        byte[] data = dis.readNBytes(size);
//                        File file = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\" + path);
                        File file = new File(path_to_All_Files + path);
                        // if file exist, delete old file
                        if (file.exists()) {
                            file.delete();
                        }
                        // create new file
                        file.createNewFile();
                        Files.write(file.toPath(), data, StandardOpenOption.SYNC);
                    }
                }
            } catch (Exception e) {
                try {
                    dis.close();
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
