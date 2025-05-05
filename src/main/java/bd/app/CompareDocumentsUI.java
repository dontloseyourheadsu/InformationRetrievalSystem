package bd.app;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CompareDocumentsUI extends JFrame {
    private JComboBox<String> doc1Box, doc2Box;
    private JComboBox<String> metricBox;
    private JTextArea resultArea;
    private Connection conn;
    private Map<String, Integer> titleToId = new LinkedHashMap<>();

    public CompareDocumentsUI(Connection conn) {
        super("Compare Documents");
        this.conn = conn;

        setSize(500, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadDocumentTitles();

        JPanel inputPanel = new JPanel();
        doc1Box = new JComboBox<>(titleToId.keySet().toArray(new String[0]));
        doc2Box = new JComboBox<>(titleToId.keySet().toArray(new String[0]));
        metricBox = new JComboBox<>(new String[]{"cosine", "euclidean"});

        inputPanel.add(new JLabel("Document 1:"));
        inputPanel.add(doc1Box);
        inputPanel.add(new JLabel("Document 2:"));
        inputPanel.add(doc2Box);
        inputPanel.add(new JLabel("Metric:"));
        inputPanel.add(metricBox);

        JButton compareButton = new JButton("Compare");
        inputPanel.add(compareButton);

        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        compareButton.addActionListener(e -> handleCompare());
    }

    private void loadDocumentTitles() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, title FROM Text ORDER BY title")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                titleToId.put(title, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load documents: " + e.getMessage());
        }
    }

    private void handleCompare() {
        try {
            String title1 = (String) doc1Box.getSelectedItem();
            String title2 = (String) doc2Box.getSelectedItem();
            int id1 = titleToId.get(title1);
            int id2 = titleToId.get(title2);
            String metric = (String) metricBox.getSelectedItem();

            double score = SimilarityEngine.compareDocuments(id1, id2, metric, conn);
            resultArea.setText("Similarity Score (" + metric + "): " + String.format("%.4f", score));
        } catch (Exception ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }
}
