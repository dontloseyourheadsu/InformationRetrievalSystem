package bd.app;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;

public class DocumentIngestor {
    public static int insertDocument(File file, Connection conn) throws Exception {
        // Step 1: Insert into Document table
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Document DEFAULT VALUES", Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        int docId = rs.getInt(1);
        rs.close();

        // Step 2: Insert into Text table
        String content = new String(Files.readAllBytes(file.toPath()));
        String url = file.getName();
        String title = file.getName();
        String author = "unknown";
        LocalDate date = LocalDate.now();

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Text (id, url, title, author, date) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, docId);
        ps.setString(2, url);
        ps.setString(3, title);
        ps.setString(4, author);
        ps.setDate(5, Date.valueOf(date));
        ps.executeUpdate();
        ps.close();

        return docId;
    }
}
