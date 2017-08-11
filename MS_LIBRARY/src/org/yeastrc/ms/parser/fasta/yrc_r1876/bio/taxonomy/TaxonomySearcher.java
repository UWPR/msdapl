package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.taxonomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import org.yeastrc.db.DBConnectionManager;

/**
 * Singleton class for looking up NCBI taxonomy names for id numbers.
 *
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Sep 23, 2004
 */

public class TaxonomySearcher {

    // The only instance of this class
    private static final TaxonomySearcher INSTANCE = new TaxonomySearcher();

    /**
     * Get the only instance of this class
     * @return
     */
    public static TaxonomySearcher getInstance() { return INSTANCE; }

    // Private constructor to keep it in the family
    private TaxonomySearcher() {
        this.taxMap = new TreeMap<Integer, String>();
    }

    // This is how we're caching the lookups.
    private Map<Integer, String> taxMap;

    /**
     * Returns the taxonomy name associated with this NCBI taxonomy ID.
     * This uses the NCBI taxonomy database located at:
     * http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/
     * @param id The NCBI taxonomy ID
     * @return The name of that ID, or null if the id could not be found
     */
    public String getName(int id) throws SQLException {
        Integer key = new Integer(id);

        if (!this.taxMap.containsKey(key)) {

            // Have to do a SQL lookup
            // Get our connection to the database.
            Connection conn = DBConnectionManager.getConnection("yrc");
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {

                // Our SQL statement
                String sqlStr = "SELECT name FROM NCBI_Taxonomy WHERE id = ?";
                stmt = conn.prepareStatement(sqlStr);
                stmt.setInt(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {

                    // Found it, add it to the map
                    String name = rs.getString(1);
                    this.taxMap.put(key, name);
                } else {

                    // Didn't find it, call it SPECIES UNKNOWN
                    this.taxMap.put(key, "SPECIES UNKNOWN");
                }

                rs.close(); rs = null;
                stmt.close(); stmt = null;
                conn.close(); conn = null;
            }
            finally {

                // Always make sure result sets and statements are closed,
                // and the connection is returned to the pool
                if (rs != null) {
                    try { rs.close(); } catch (SQLException e) { ; }
                    rs = null;
                }
                if (stmt != null) {
                    try { stmt.close(); } catch (SQLException e) { ; }
                    stmt = null;
                }
                if (conn != null) {
                    try { conn.close(); } catch (SQLException e) { ; }
                    conn = null;
                }
            }
        }

        return this.taxMap.get(key);
    }
}