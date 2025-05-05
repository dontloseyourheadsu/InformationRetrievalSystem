package bd.app;

import org.tartarus.snowball.ext.PorterStemmer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextPreprocessor {
    private static final Set<String> stopWords = Set.of("the", "is", "at", "of", "on", "and", "a");

    public static void processAndIndex(int docId, String content, Connection conn) throws Exception {
        Map<String, Integer> termFreq = new HashMap<>();
        PorterStemmer stemmer = new PorterStemmer();

        String[] tokens = content.toLowerCase().split("[^a-z0-9]+");
        for (String token : tokens) {
            if (stopWords.contains(token) || token.isBlank()) continue;

            stemmer.setCurrent(token);
            stemmer.stem();
            String term = stemmer.getCurrent();

            termFreq.put(term, termFreq.getOrDefault(term, 0) + 1);

            // Insert Term, Word, Represent
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO Term (name) VALUES ('" + term + "') ON CONFLICT DO NOTHING");
            stmt.executeUpdate("INSERT INTO Word (word) VALUES ('" + token + "') ON CONFLICT DO NOTHING");
            stmt.executeUpdate("INSERT INTO Represent (word, term) VALUES ('" + token + "', '" + term + "') ON CONFLICT DO NOTHING");
            stmt.close();
        }

        // Insert frequencies into HAS
        PreparedStatement ps = conn.prepareStatement("INSERT INTO HAS (id, name, frequency) VALUES (?, ?, ?)");
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            ps.setInt(1, docId);
            ps.setString(2, entry.getKey());
            ps.setDouble(3, entry.getValue());
            ps.addBatch();
        }
        ps.executeBatch();
        ps.close();
    }
}
