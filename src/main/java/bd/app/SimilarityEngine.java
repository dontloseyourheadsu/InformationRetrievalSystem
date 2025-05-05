package bd.app;

import java.sql.*;
import java.util.*;

public class SimilarityEngine {

    public static double compareDocuments(int doc1, int doc2, String metric, Connection conn) throws SQLException {
        String sql = "";

        switch (metric.toLowerCase()) {
            case "cosine":
                sql = "SELECT SUM(c1.frequency * c2.frequency) / " +
                        "(SQRT(SUM(c1.frequency^2)) * SQRT(SUM(c2.frequency^2))) AS score " +
                        "FROM Complete c1 " +
                        "JOIN Complete c2 ON c1.name = c2.name " +
                        "WHERE c1.id = ? AND c2.id = ?";
                break;
            case "euclidean":
                sql = "SELECT SQRT(SUM(POWER(c1.frequency - c2.frequency, 2))) AS score " +
                        "FROM Complete c1 " +
                        "JOIN Complete c2 ON c1.name = c2.name " +
                        "WHERE c1.id = ? AND c2.id = ?";
                break;
            default:
                throw new IllegalArgumentException("Unsupported metric: " + metric);
        }

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, doc1);
        ps.setInt(2, doc2);
        ResultSet rs = ps.executeQuery();

        double result = rs.next() ? rs.getDouble(1) : 0.0;
        rs.close();
        ps.close();
        return result;
    }

    public static List<String> runQuery(String queryText, String metric, Connection conn) throws Exception {
        // Insert query as DOCUMENT + QUERY
        int queryId;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO Document DEFAULT VALUES", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            queryId = rs.getInt(1);
            rs.close();
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Query (id, label) VALUES (?, ?)")) {
            ps.setInt(1, queryId);
            ps.setString(2, "Qtemp");
            ps.executeUpdate();
        }

        // Preprocess and populate HAS
        TextPreprocessor.processAndIndex(queryId, queryText, conn);

        String sql = "";
        switch (metric.toLowerCase()) {
            case "cosine":
                sql = "SELECT t.title, " +
                        "SUM(cQ.frequency * cD.frequency) / " +
                        "(SQRT(SUM(cQ.frequency^2)) * SQRT(SUM(cD.frequency^2))) AS score " +
                        "FROM Complete cQ " +
                        "JOIN Complete cD ON cQ.name = cD.name " +
                        "JOIN Text t ON cD.id = t.id " +
                        "WHERE cQ.id = ? AND cD.id <> ? " +
                        "GROUP BY t.title " +
                        "ORDER BY score DESC LIMIT 10";
                break;
            case "euclidean":
                sql = "SELECT t.title, " +
                        "SQRT(SUM(POWER(cQ.frequency - cD.frequency, 2))) AS score " +
                        "FROM Complete cQ " +
                        "JOIN Complete cD ON cQ.name = cD.name " +
                        "JOIN Text t ON cD.id = t.id " +
                        "WHERE cQ.id = ? AND cD.id <> ? " +
                        "GROUP BY t.title " +
                        "ORDER BY score ASC LIMIT 10";
                break;
            case "jaccard":
                sql = "WITH " +
                        "terms_q AS (SELECT name FROM HAS WHERE id = ?), " +
                        "terms_d AS (SELECT id, name FROM HAS WHERE id <> ?), " +
                        "intersection AS (SELECT d.id, COUNT(*) AS inter " +
                        "FROM terms_q q JOIN terms_d d ON q.name = d.name GROUP BY d.id), " +
                        "union_counts AS (SELECT d.id, " +
                        "(SELECT COUNT(*) FROM terms_q) + COUNT(*) - " +
                        "(SELECT inter FROM intersection i WHERE i.id = d.id) AS union_size " +
                        "FROM terms_d d GROUP BY d.id), " +
                        "scores AS (SELECT i.id, 1.0 * i.inter / u.union_size AS score " +
                        "FROM intersection i JOIN union_counts u ON i.id = u.id) " +
                        "SELECT t.title, s.score FROM scores s " +
                        "JOIN Text t ON s.id = t.id ORDER BY s.score DESC LIMIT 10";
                break;
            default:
                throw new IllegalArgumentException("Unsupported metric: " + metric);
        }

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, queryId);
        ps.setInt(2, queryId);
        ResultSet rs = ps.executeQuery();

        List<String> results = new ArrayList<>();
        while (rs.next()) {
            String title = rs.getString(1);
            double score = rs.getDouble(2);
            results.add(title + " (Score: " + String.format("%.3f", score) + ")");
        }

        rs.close();
        ps.close();

        // Clean up query
        conn.createStatement().executeUpdate("DELETE FROM Document WHERE id = " + queryId);

        return results;
    }
}
