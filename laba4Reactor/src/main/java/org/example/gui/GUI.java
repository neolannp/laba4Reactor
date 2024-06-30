package org.example.gui;

import org.example.BurnupMaker;
import org.example.handlers.DBReader;
import org.example.Manager;
import org.example.Regions;
import org.example.reactor.Reactor;
import org.example.reactor.ReactorDB;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class GUI extends JFrame {
    private JButton chooseFileButton;

    private JButton importButton;
    private JButton goCalculateButton;
    private JPanel mainPanel;
    private JTree reactorsTree;
    private Regions regions;
    private Map<String, List<ReactorDB>> reactors;
    private HashMap<String, Reactor> reactorsType;
    private HashMap<String, Double> reactorsTypeMap;

    public GUI() throws URISyntaxException {
        setLookAndFeel();
        initializeComponents();
        setupFrame();
        createUIComponents();
        addListeners();
        setVisible(true);
    }

    public static void main(String[] args) throws URISyntaxException {
        new GUI();
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        chooseFileButton= new JButton("choose file");
        importButton = new JButton("choose DB");
        goCalculateButton = new JButton("count consumption");
        mainPanel = new JPanel(new BorderLayout());
        reactorsTree = new JTree(new DefaultMutableTreeNode("Reactors"));
        importButton.setEnabled(false);
        reactorsTree.setEnabled(false);
        goCalculateButton.setEnabled(false);
    }

    private void setupFrame() {
        setTitle("laba4");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(mainPanel);
    }

    private void createUIComponents() {
        JScrollPane scrollPane = new JScrollPane(reactorsTree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        buttonPanel.add(chooseFileButton,gbc);
        buttonPanel.add(importButton, gbc);
        buttonPanel.add(goCalculateButton, gbc);

        return buttonPanel;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        return gbc;
    }

    private void addListeners() {
        chooseFileButton.addActionListener(e -> handleChooseFileButtonClick());
        importButton.addActionListener(e -> showFileChooser());
        goCalculateButton.addActionListener(e -> showCalculatorDialog());
    }
    private void handleChooseFileButtonClick() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("./"));
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Manager manager = new Manager();
                reactorsType = manager.readCommonClass(selectedFile.getAbsolutePath());
                displayReactorData(reactorsType);
                System.out.print(reactorsType.keySet());
                reactorsTypeMap=manager.getReactorTypeMap(reactorsType);
                System.out.print(reactorsTypeMap);
            } catch (Exception ex) {
                showErrorDialog("Choose correct file");
            }
        }
    }
        private void displayReactorData(HashMap<String, Reactor> reactorsType) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

        for (Map.Entry<String, Reactor> entry : reactorsType.entrySet()) {
            DefaultMutableTreeNode reactorNode = new DefaultMutableTreeNode(entry.getKey());

            reactorNode.add(new DefaultMutableTreeNode("Burnup: " + entry.getValue().burnup));
            reactorNode.add(new DefaultMutableTreeNode("Class: " + entry.getValue().reactorClass));
            reactorNode.add(new DefaultMutableTreeNode("Electrical Capacity: " + entry.getValue().electricalCapacity));
            reactorNode.add(new DefaultMutableTreeNode("First Load: " + entry.getValue().firstLoad));
            reactorNode.add(new DefaultMutableTreeNode("KPD: " + entry.getValue().kpd));
            reactorNode.add(new DefaultMutableTreeNode("Life Time: " + entry.getValue().lifeTime));
            reactorNode.add(new DefaultMutableTreeNode("Terminal Capacity: " + entry.getValue().terminalCapacity));
            reactorNode.add(new DefaultMutableTreeNode("File Type: " + entry.getValue().fileType));

            treeModel.insertNodeInto(reactorNode, rootNode, treeModel.getChildCount(rootNode));
        }

        JTree tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tree);

        JFrame treeFrame = new JFrame();
        treeFrame.add(scrollPane);
        treeFrame.setSize(400, 400);
        treeFrame.setLocationRelativeTo(null);
        treeFrame.setVisible(true);

        importButton.setEnabled(true);
    }

    private void showFileChooser() {

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DB files", "db");
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File("./"));
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.getName().toLowerCase().endsWith(".db")) {
                fillTree(file);
            } else {
                showErrorDialog("choose file format .db");
            }
        }
    }

    private void showCalculatorDialog() {
        ConsumptionCalculationsGUI dialog = new ConsumptionCalculationsGUI(regions, reactors);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void fillTree(File file) {
        try {
            reactors = new TreeMap<>(DBReader.importReactors(file));
            regions = DBReader.importRegions(file);
            populateTree();
            reactorsTree.setEnabled(true);
            goCalculateButton.setEnabled(true);
        } catch (SQLException e) {
            showErrorDialog("Error");
        }
    }

    private void populateTree() {
        DefaultTreeModel treeModel = (DefaultTreeModel) reactorsTree.getModel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Reactors");

        Map<String, DefaultMutableTreeNode> countryNodes = new HashMap<>();
        BurnupMaker burnupMaker=new BurnupMaker(reactorsTypeMap,reactors);
        burnupMaker.match();

        for (Map.Entry<String, List<ReactorDB>> entry : reactors.entrySet()) {
            String country = entry.getValue().getFirst().getCountry();

            DefaultMutableTreeNode countryNode = countryNodes.get(country);

            if (countryNode == null) {
                countryNode = new DefaultMutableTreeNode(country);
                countryNodes.put(country, countryNode);
                root.add(countryNode);
            }

            for (ReactorDB reactor : entry.getValue()) {
                DefaultMutableTreeNode reactorNode = new DefaultMutableTreeNode(reactor);
                countryNode.add(reactorNode);
            }
        }

        List<DefaultMutableTreeNode> sortedCountryNodes = new ArrayList<>(countryNodes.values());
        sortedCountryNodes.sort(Comparator.comparing(DefaultMutableTreeNode::toString));

        sortedCountryNodes.forEach(root::add);

        treeModel.setRoot(root);
    }


    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
