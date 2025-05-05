package bd.app;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SVDComputer {
    public static void computeSVD(Connection conn) throws SQLException {
        List<Integer> docIds = new ArrayList<>();
        List<String> terms = new ArrayList<>();
        Map<Integer, Integer> docIndex = new HashMap<>();
        Map<String, Integer> termIndex = new HashMap<>();

        Statement st = conn.createStatement();

        // Get all documents
        ResultSet rs = st.executeQuery("SELECT id FROM Text");
        while (rs.next()) {
            int id = rs.getInt("id");
            docIndex.put(id, docIds.size());
            docIds.add(id);
        }
        rs.close();

        // Get all terms
        rs = st.executeQuery("SELECT name FROM Term");
        while (rs.next()) {
            String term = rs.getString("name");
            termIndex.put(term, terms.size());
            terms.add(term);
        }
        rs.close();

        // Create and fill the matrix
        double[][] matrix = new double[docIds.size()][terms.size()];
        rs = st.executeQuery("SELECT id, name, frequency FROM HAS");
        while (rs.next()) {
            int docId = rs.getInt("id");
            String term = rs.getString("name");
            double freq = rs.getDouble("frequency");

            int i = docIndex.get(docId);
            int j = termIndex.get(term);
            matrix[i][j] = freq;
        }
        rs.close();
        st.close();

        // Compute SVD
        RealMatrix A = MatrixUtils.createRealMatrix(matrix);
        SingularValueDecomposition svd = new SingularValueDecomposition(A);
        double[] singularValues = svd.getSingularValues();

        System.out.println("Singular values:");
        for (double val : singularValues) {
            System.out.printf("%.3f ", val);
        }
        System.out.println();
    }
}
