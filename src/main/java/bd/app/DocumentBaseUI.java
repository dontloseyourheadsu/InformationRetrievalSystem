package bd.app;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DocumentBaseUI extends JFrame {
    private Connection conn;
    private JTextField queryField;
    private JComboBox<String> metricBox;
    private JTextArea resultsArea;

    public DocumentBaseUI() {
        super("Document Base IR System");
        setSize(950, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Setup components
        JButton addDocs = new JButton("Add Documents");
        JButton search = new JButton("Search");
        JButton compareButton = new JButton("Compare Documents");
        queryField = new JTextField(40);
        metricBox = new JComboBox<>(new String[] {"cosine", "euclidean", "jaccard"});
        resultsArea = new JTextArea(20, 60);
        resultsArea.setEditable(false);

        // Layout
        JPanel top = new JPanel();
        top.add(addDocs);
        top.add(new JLabel("Query:"));
        top.add(queryField);
        top.add(metricBox);
        top.add(search);
        top.add(compareButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        // Connect to DB
        try {
            conn = DatabaseConnection.connect();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to DB: " + e.getMessage());
            System.exit(1);
        }

        // Listeners
        addDocs.addActionListener(e -> handleAddDocs());
        search.addActionListener(e -> handleSearch());
        compareButton.addActionListener(e -> {
            new CompareDocumentsUI(conn).setVisible(true);
        });
    }

    private void handleAddDocs() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            for (File file : files) {
                try {
                    int id = DocumentIngestor.insertDocument(file, conn);
                    String text = Files.readString(file.toPath());
                    TextPreprocessor.processAndIndex(id, text, conn);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed on " + file.getName() + ": " + ex.getMessage());
                }
            }
            JOptionPane.showMessageDialog(this, "Documents added.");
        }
    }

    private void handleSearch() {
        String query = queryField.getText();
        if (query.isBlank()) return;
        String metric = (String) metricBox.getSelectedItem();
        try {
            List<String> results = SimilarityEngine.runQuery(query, metric, conn);
            resultsArea.setText(String.join("\n", results));
        } catch (Exception ex) {
            resultsArea.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DocumentBaseUI().setVisible(true));
    }
}
