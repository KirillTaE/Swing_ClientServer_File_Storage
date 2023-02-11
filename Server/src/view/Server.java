package view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JTable;

import model.bean.FileInfo;
import model.bean.User;
import model.bo.FileBO;
import model.bo.UserBO;

public class Server extends JFrame {
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private JTree tree;
    static String value = "";
    private JPanel contentPane;
    private static JTable table;
    private JButton btnRefresh;

    static ArrayList<Socket> listSK = new ArrayList<Socket>();
    static ArrayList<FileInfo> myFiles = new ArrayList<FileInfo>();

    private static Logger logger = Logger.getLogger(Server.class.getName());


    static {
        FileHandler fh;
        try {
//            LogManager.getLogManager().readConfiguration(
//                    Server.class.getResourceAsStream("/logging.properties"));
            logger.setUseParentHandlers(false);
            fh = new FileHandler("ServerReq.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.info("Logger initialized");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception: " + e);
        }
    }

    public static void main(String[] args) {
        Server frame = new Server();
        frame.setVisible(true);
        frame.open();
    }

    public String path_to_All_Files = "Server_Files\\";

    public Server() {
        UserBO userBO = new UserBO();
        FileBO fileBO = new FileBO();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(500, 50, 750, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JTabbedPane tabMain = new JTabbedPane(JTabbedPane.TOP);
        tabMain.setBounds(0, 0, 730, 561);
        contentPane.add(tabMain);

        // Server Files Tab
        JPanel tabMyFile = new JPanel();
        tabMyFile.setLayout(null);
        tabMain.addTab("My file", null, tabMyFile, null);

        loadFile();

//        File fileRoot = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\Client_Files");
        File fileRoot = new File(path_to_All_Files + "test_temp\\All_Files");
        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel = new DefaultTreeModel(root);

        tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBounds(0, 0, 437, 533);

        createChildren(fileRoot, root);
        tabMyFile.add(scrollPane);

//        deleteDir(new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp"));
        deleteDir(new File(path_to_All_Files + "\\test_temp"));

        JButton btnDelFile = new JButton("Delete");
        btnDelFile.addActionListener(e -> {
            TreePath[] paths = tree.getSelectionPaths();
            for (int i = 0; paths != null && i < paths.length; i++) {
                value = "";
                Object[] elements = paths[i].getPath();
                for (Object element : elements) {
                    value += element + "\\";
                }
                String path = value.substring(0, value.length() - 1);
                if (path.contains(".")) {
                    try {
                        fileBO.deleteFile(path);
                    } catch (ClassNotFoundException | SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            btnRefresh.doClick();
        });
        btnDelFile.setBounds(475, 30, 89, 23);
        tabMyFile.add(btnDelFile);

        JButton btnAccess = new JButton("Access...");
        btnAccess.addActionListener(e -> {
            TreePath[] paths = tree.getSelectionPaths();
            if (Objects.requireNonNull(paths).length == 1) {
                // select access user
                value = "";
                Object[] elements = paths[0].getPath();
                for (Object element : elements) {
                    value += element + "\\";
                }
                String path = value.substring(0, value.length() - 1);
                try {
                    ArrayList<Integer> ids = fileBO.getListUserID(fileBO.getFile(path).getId());
                    SelectUser frame = new SelectUser(ids, fileBO.getFile(path).getId());
                    frame.setVisible(true);
                } catch (ClassNotFoundException | SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnAccess.setBounds(475, 123, 89, 23);
        tabMyFile.add(btnAccess);

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            // refresh tree 1: server
            loadFile();
            root.removeAllChildren();
//                File fileRoot = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\Client_Files");
            File fileRoot1 = new File(path_to_All_Files + "\\test_temp\\All_Files");
            createChildren(fileRoot1, root);
//                deleteDir(new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp"));
            deleteDir(new File(path_to_All_Files + "\\test_temp"));
            treeModel.reload();
        });
        btnRefresh.setBounds(475, 213, 89, 23);
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
                EditUser frame = new EditUser(id);
                frame.setVisible(true);
            }
        });
        btnEdit.setBounds(218, 174, 89, 23);
        tabUserManager.add(btnEdit);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> {
            int[] index = table.getSelectedRows();
            for (int j : index) {
                try {
                    userBO.deleteUser(Integer.parseInt(table.getModel().getValueAt(j, 0).toString()));
                } catch (ClassNotFoundException | SQLException e1) {
                    e1.printStackTrace();
                }
            }
            reloadUser();
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
    }

    private void loadFile() {
        FileBO fileBO = new FileBO();
        try {
            ArrayList<FileInfo> files = fileBO.getAllFile();
            ArrayList<String> paths = new ArrayList<String>();
//            System.out.println(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\" );
            for (FileInfo fileInfo : files) {
                paths.add(fileInfo.getPath());
//                File file = new File(System.getProperty("user.home") + "\\OneDrive\\Desktop\\test_temp\\" + fileInfo.getPath());
                File file = new File(path_to_All_Files + "\\test_temp\\" + fileInfo.getPath());
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (ClassNotFoundException | SQLException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private void loadUser() {
        UserBO userBO = new UserBO();
        ArrayList<User> listUser = new ArrayList<User>();
        try {
            listUser = userBO.getAllUser();
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        String[] header = {"ID", "Username", "Name", "Role"};
        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (User user : listUser) {
            ArrayList<String> row = new ArrayList<String>(Arrays.asList(Integer.toString(user.getId()),
                    user.getUsername(), user.getName(), user.getRole() == 1 ? "admin" : "user"));
            data.add(row);
        }
        String[][] value = data.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
        table = new JTable(value, header);
        AbstractTableModel tableModel = (AbstractTableModel) table.getModel();
        TableColumnModel tcm = table.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));
        tableModel.fireTableDataChanged();
    }

    protected static void reloadUser() {
        UserBO userBO = new UserBO();
        ArrayList<User> listUser = new ArrayList<User>();
        try {
            listUser = userBO.getAllUser();
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        String[] header = {"ID", "Username", "Name", "Role"};
        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (User user : listUser) {
            ArrayList<String> row = new ArrayList<String>(Arrays.asList(Integer.toString(user.getId()),
                    user.getUsername(), user.getName(), user.getRole() == 1 ? "admin" : "user"));
            data.add(row);
        }
        String[][] value = data.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
        DefaultTableModel tableModel = new DefaultTableModel(value, header);
        table.setModel(tableModel);
        TableColumnModel tcm = table.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));
    }

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

    private void open() {
        try {
            ServerSocket server = new ServerSocket(1234);
            System.out.println("Server is listening...");
            logger.info("Server is listening...");
            while (true) {
                Socket socket = server.accept();
//                System.out.println("Connected with " + socket);
                logger.info("Connected with " + socket);
                Server.listSK.add(socket);
//                System.out.println(Arrays.toString(new ArrayList[]{listSK}));
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread extends Thread {
        private Socket soc = null;
        private DataInputStream dis = null;
        private DataOutputStream dos = null;

        public ServerThread(Socket s) {
            this.soc = s;
        }

        public void run() {
            try {
                FileBO fileBO = new FileBO();
                UserBO userBO = new UserBO();
                dis = new DataInputStream(soc.getInputStream());
                int cmd = dis.readInt();
                System.out.println(cmd);

                switch (cmd) {
                    // receive request file path
                    case 1 -> {
                        try {
                            logger.info("<1> - (Receive Request) - <Receive File Path>");
                            int n = dis.readInt();
                            DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                            dos.writeInt(1);
                            dos.writeInt(n);
                            for (int i = 0; i < n; ++i) {
                                String path = dis.readUTF();
                                try {
                                    System.out.println(path);
                                    FileInfo file = fileBO.getFile(path);
                                    // send to client
                                    dos.writeUTF("Ok!");
                                    dos.writeUTF(file.getName());
                                    dos.writeUTF(file.getPath());
                                    dos.writeInt(file.getData().length);
                                    dos.write(file.getData());
                                } catch (Exception e) {
                                    System.out.println("Choose file");
                                    dos.writeUTF("No!");
                                }

                            }
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Exception: " + e);
                            throw new RuntimeException(e);
                        }
//                        catch (ClassNotFoundException | SQLException e) {
//                            e.printStackTrace();
//                        }

                    }

                    // receive file
                    case 2 -> {
                        try {
                            logger.info("<2> - (Receive Request) - <Receive File>");
                            int n = dis.readInt();
                            for (int i = 0; i < n; ++i) {
                                String name = dis.readUTF();
                                System.out.println(name);
                                String path = dis.readUTF();
                                System.out.println(path);
                                String created = dis.readUTF();
                                System.out.println(created);
                                String modified = dis.readUTF();
                                System.out.println(modified);
                                int size = dis.readInt();
//							System.out.println(size);
                                byte[] data = dis.readNBytes(size);
//							System.out.println(new String(data));
                                // if file already exist
                                if (fileBO.isExistFile(path)) {
                                    FileInfo file = fileBO.getFile(path);
                                    // if file has been modified
                                    if (cmpDate(file.getCreated(), created) || cmpDate(file.getModified(), modified)
                                            || !cmpMD5(file.getData(), data)) {
                                        // edit file
                                        fileBO.editFile(name, path, created, modified, data);
                                    }
                                } else {
                                    // add new file
                                    fileBO.addFile(name, path, created, modified, data);
                                }
                            }
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    // validate login
                    case 3 -> {
                        try {
                            logger.info("<3> - (Receive Request) - <Validate Login>");
                            dis = new DataInputStream(soc.getInputStream());
                            String username = dis.readUTF();
                            String password = dis.readUTF();
                            boolean valid = false;
                            valid = userBO.isValidUser(username, password);
                            dos = new DataOutputStream(soc.getOutputStream());
                            dos.writeBoolean(valid);
                            if (valid) {
                                int id = userBO.getUser(username).getId();
                                int role = userBO.getUser(username).getRole();
                                dos.writeInt(id);
                                dos.writeInt(role);
                            }
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    // edit user
                    case 4 -> {
                        logger.info("<4> - (Receive Request) - <Edit User>");
                        dis = new DataInputStream(soc.getInputStream());
                        int id = dis.readInt();
                        String password = dis.readUTF();
                        String name = dis.readUTF();
                        try {
                            userBO.editUser(id, password, name);
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    // get all file path from server to client tree view
                    case 5 -> {
                        try {
                            logger.info("<5> - (Receive Request) - <Get All File Path From Server to Client Tree View>");
                            dis = new DataInputStream(soc.getInputStream());
                            int id = dis.readInt();
                            dos = new DataOutputStream(soc.getOutputStream());
                            ArrayList<FileInfo> data = fileBO.getFileByUser(id);
                            dos.writeInt(data.size());
                            for (FileInfo datum : data) {
                                dos.writeUTF(datum.getPath());
                            }
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    // get user detail
                    case 6 -> {
                        try {

                            dis = new DataInputStream(soc.getInputStream());
                            int id = dis.readInt();
                            User user = userBO.getUser(id);
                            dos = new DataOutputStream(soc.getOutputStream());
                            dos.writeUTF(user.getUsername());
                            dos.writeUTF(user.getPassword());
                            dos.writeUTF(user.getName());
                            dos.writeInt(user.getRole());

                            logger.info("<6> - (Receive Request) - <Get User Detail>" + id);
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    // admin get All User
                    case 7 -> {
                        logger.info("<7> - (Receive Request) - <Admin get All User>");
                        //                            UserBO userBO = new UserBO();
                        ArrayList<User> listUser = new ArrayList<User>();
                        try {
                            listUser = userBO.getAllUser();
                        } catch (ClassNotFoundException | SQLException e1) {
                            e1.printStackTrace();
                        }

                        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
                        for (User user : listUser) {
                            ArrayList<String> row = new ArrayList<String>(Arrays.asList(Integer.toString(user.getId()),
                                    user.getUsername(), user.getName(), user.getRole() == 1 ? "admin" : "user"));
                            data.add(row);
                        }

                        ObjectOutputStream oos = new ObjectOutputStream(soc.getOutputStream());
                        oos.writeObject(data);
                    }

                    // admin Edit User
                    case 8 -> {

                        dis = new DataInputStream(soc.getInputStream());
                        int id = dis.readInt();
//                        User user = userBO.getUser(id);
//                        dos = new DataOutputStream(soc.getOutputStream());
//                        dos.writeUTF(user.getUsername());
//                        dos.writeUTF(user.getPassword());
//                        dos.writeUTF(user.getName());
//                        dos.writeInt(user.getRole());


                        String txtPassword = dis.readUTF();
                        String txtName = dis.readUTF();
                        int role = dis.readInt();
                        userBO.editUser(id, txtPassword, txtName, role);
                        Server.reloadUser();

                        logger.info("<8> - (Receive Request) - <Admin Edit User>" + id + "," + txtName + "," + role);
                    }

                    // admin Delete User
                    case 9 -> {
                        logger.info("<9> - (Receive Request) - <Admin Delete User>");

                        dis = new DataInputStream(soc.getInputStream());
                        int index_len = dis.readInt();
                        int index;
                        for (int i = 0; i < index_len; i++) {
                            index = dis.readInt();
                            userBO.deleteUser(Integer.parseInt(table.getModel().getValueAt(index, 0).toString()));
                        }

                        Server.reloadUser();

                    }

                    // admin Add User
                    case 10 -> {

                        dis = new DataInputStream(soc.getInputStream());

                        String txtUsername = dis.readUTF();
                        String txtPassword = dis.readUTF();
                        String txtName = dis.readUTF();
                        int role = dis.readInt();

                        userBO.addUser(txtUsername, txtPassword, txtName, role);

                        Server.reloadUser();

                        logger.info("<10> - (Receive Request) - <Admin Add User>: " + txtUsername + "," + txtName + "," + role);

                    }

                    // admin Delete file
                    case 11 -> {
                        logger.info("<11> - (Receive Request) - <Admin Delete file>");

                        ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());

                        TreePath[] paths = (TreePath[]) ois.readObject();

                        for (int i = 0; paths != null && i < paths.length; i++) {
                            value = "";
                            Object[] elements = paths[i].getPath();
                            for (Object element : elements) {
                                value += element + "\\";
                            }
                            String path = value.substring(0, value.length() - 1);
                            if (path.contains(".")) {
                                fileBO.deleteFile(path);
                            }
                        }

                        Server.reloadUser();
                    }

                    // admin get All User (Select)
                    case 12 -> {
                        logger.info("<12> - (Receive Request) - <Admin Get All User (Select share)>");

                        dis = new DataInputStream(soc.getInputStream());
                        dos = new DataOutputStream(soc.getOutputStream());

                        String path = dis.readUTF();
                        try {
                            ArrayList<Integer> ids = fileBO.getListUserID(fileBO.getFile(path).getId());
                            dos.writeUTF("Ok!");

                            //                            UserBO userBO = new UserBO();
                            ArrayList<User> listUser = new ArrayList<User>();
                            try {
                                listUser = userBO.getAllUser();
                            } catch (ClassNotFoundException | SQLException e1) {
                                e1.printStackTrace();
                            }

                            ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();

                            for (User user : listUser) {
                                ArrayList<Object> row = new ArrayList<Object>(Arrays.asList(ids.contains(user.getId()) || ids.size() == 0,
                                        Integer.toString(user.getId()), user.getUsername(), user.getRole() == 1 ? "admin" : "user"));
                                data.add(row);
                            }

                            ObjectOutputStream oos = new ObjectOutputStream(dos);
                            oos.writeObject(data);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Exception: Choose file not folder! " + e);
                            System.out.println("Choose file");
                            dos.writeUTF("No!");
                        }
                    }

                    // Share File
                    case 13 -> {
                        logger.info("<13> - (Receive Request) - <Share File>");

                        dis = new DataInputStream(soc.getInputStream());

                        String path = dis.readUTF();

                        int tableRowCount = dis.readInt();

                        int idfile = fileBO.getFile(path).getId();

                        try {
                            fileBO.deleteShareFile(idfile);
                            for (int i = 0; i < tableRowCount; i++) {
                                if (dis.readBoolean()) {
                                    fileBO.addShareFile(idfile, dis.readInt());
                                }
                            }
                        } catch (ClassNotFoundException | SQLException e1) {
                            logger.log(Level.WARNING, "Exception: " + e1);
                            e1.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Exception: " + e);
                e.printStackTrace();
            } catch (SQLException | ClassNotFoundException e) {
                logger.log(Level.WARNING, "Exception: " + e);
                throw new RuntimeException(e);
            }
        }

        // Compare MD5 checksum to check if file has changed
        private boolean cmpMD5(byte[] data1, byte[] data2) {
            try {
                MessageDigest md1 = MessageDigest.getInstance("MD5");
                md1.update(data1);
                byte[] digest1 = md1.digest();
                MessageDigest md2 = MessageDigest.getInstance("MD5");
                md2.update(data2);
                byte[] digest2 = md2.digest();
                return Arrays.equals(digest1, digest2);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return false;
        }

        // Compare date time. Return if first parameter is before second parameter
        private boolean cmpDate(String date1, String date2) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            try {
                return sdf.parse(date1).before(sdf.parse(date2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
