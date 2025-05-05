package bd.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;

public class CompareDocumentsUI extends JFrame {
    private JTextField doc1Field, doc2Field;
    private JComboBox<String> metricBox;
    private JTextArea resultArea;
    private Connection conn;

    public CompareDocumentsUI(Connection conn) {
        super("Compare Documents");
        this.conn = conn;

        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Top input panel
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Doc ID 1:"));
        doc1Field = new JTextField(5);
        inputPanel.add(doc1Field);
        inputPanel.add(new JLabel("Doc ID 2:"));
        doc2Field = new JTextField(5);
        inputPanel.add(doc2Field);

        metricBox = new JComboBox<>(new String[] { "cosine", "euclidean" });
        inputPanel.add(new JLabel("Metric:"));
        inputPanel.add(metricBox);

        JButton compareButton = new JButton("Compare");
        inputPanel.add(compareButton);

        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        compareButton.addActionListener(e -> handleCompare());
    }

    private void handleCompare() {
        try {
            int doc1 = Integer.parseInt(doc1Field.getText().trim());
            int doc2 = Integer.parseInt(doc2Field.getText().trim());
            String metric = (String) metricBox.getSelectedItem();
            double score = SimilarityEngine.compareDocuments(doc1, doc2, metric, conn);
            resultArea.setText("Similarity Score: " + String.format("%.4f", score));
        } catch (Exception ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }
}
