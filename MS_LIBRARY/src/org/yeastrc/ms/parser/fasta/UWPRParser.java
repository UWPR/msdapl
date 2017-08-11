/**
 * UWPRParser.java
 * @author Vagisha Sharma
 * Jan 6, 2010
 * @version 1.0
 */
package org.yeastrc.ms.parser.fasta;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class UWPRParser extends MSParser {

    /**
     * Attempts to get the database ID associated with this UWPR FASTA file from the
     * YRC_NRSEQ database.  If it can not be found, a new entry is created, and the value
     * for that new entry is returned
     */
    private int databaseID = 0;
    
    private static final Pattern yeastSGDPattern = Pattern.compile("^.*SGDID:S\\d{9}.*$");
    
    private static final Pattern swissProtPattern = Pattern.compile("^\\w+\\s+sp\\|[A-Z]\\w+\\|(\\w+)\\|.*$");

    private static final Logger log = Logger.getLogger("FastaParserLog");

    public int getDatabaseID() throws Exception {

        // returned the cached database ID if we have it
        if (databaseID != 0)
            return databaseID;
        
        String filename = getFilename();
        
        // get our database connection
        Connection conn = DatabaseHelper.getConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT id FROM tblDatabase WHERE name = ?";
            stmt = conn.prepareStatement( sql );
            stmt.setString( 1, filename );
            rs = stmt.executeQuery();
            if (rs.next()) {
                this.databaseID = rs.getInt( 1 );
                
                rs.close(); rs = null;
                stmt.close(); stmt = null;
                conn.close(); conn = null;
                
                return this.databaseID;
            }

            rs.close(); rs = null;
            stmt.close(); stmt = null;
            log.info(" \nGenerating new NR_SEQ database entry for: " + filename );
            
            sql = "INSERT INTO tblDatabase (name, description) VALUES (?, ?)";
            stmt = conn.prepareStatement( sql );
            stmt.setString( 1, filename );
            stmt.setString( 2, "UWPR generated FASTA file for MS/MS analysis." );
            stmt.executeUpdate();
            
            stmt.close(); stmt = null;
            
            sql = "SELECT id FROM tblDatabase WHERE name = ?";
            stmt = conn.prepareStatement( sql );
            stmt.setString( 1, filename );
            rs = stmt.executeQuery();
            if (rs.next()) {
                this.databaseID = rs.getInt( 1 );
                
                rs.close(); rs = null;
                stmt.close(); stmt = null;
                conn.close(); conn = null;
                
                return this.databaseID;
            } else
                throw new Exception( "Creation of new database for " + filename + " failed..." );
            
            
        } finally {
            
            if (rs != null) {
                try {
                    rs.close(); rs = null;
                } catch (Exception e) { ; }
            }

            if (stmt != null) {
                try {
                    stmt.close(); stmt = null;
                } catch (Exception e) { ; }
            }
            
            if (conn != null) {
                try {
                    conn.close(); conn = null;
                } catch (Exception e) { ; }
            }
            
        }
    }
    
    /**
     * UWPR-specific determination of species ID from FASTA header
     */
    public int getSpecies(String header, int sequenceID ) throws Exception {
        
        // SGDID pattern in the header
        if(getFilename().equals("SGDyeast.fasta.20090807.for-rev")) {
            Matcher dm = yeastSGDPattern.matcher(header);
            if(dm.matches() && !header.startsWith("rev_"))  return 4932;
        }
        if(getFilename().equals("LM_ddb.22.fasta")) { // Lars' file
            
            if(header.startsWith("rev"))
                return 0;

            Matcher dm = swissProtPattern.matcher(header);
            if(dm.matches()) {
                int id = SwissProtSearcher.getInstance().getSpeciesByAcc(dm.group(1));
                if(id != 0)
                    return id;
            }

            if(header.contains("Streptococcus pyogenes M1") || header.contains("Streptococus pyogenes M1") ||
               header.contains("Streptococcus pyogenes M") || header.contains("Streptococus pyogenes M"))
                return 160490;

            else if(header.contains("pdb|1YXY|") ||
                        header.contains("pdb|2FLI|") ||
                        header.contains("pdb|1SU0|")) {
                        return 1314;
            }
            else if(header.contains("pdb|1S7O|")) // Streptococcus pyogenes serotype M3
                        return 301448;
            else if(header.contains("pdb|1Z0P|"))
                        return 160490;


            else if(header.contains("NP_268867.1") ||
                        header.contains("NP_268868.1") ||
                        header.contains("NP_268880.1") ||
                        header.contains("NP_269004.1") ||
                        header.contains("NP_269005.1") ||
                        header.contains("NP_269006.1") ||
                        header.contains("NP_269007.1") ||
                        header.contains("NP_269008.1") ||
                        header.contains("NP_269009.1") ||
                        header.contains("NP_269262.1") ||
                        header.contains("NP_269403.1") ||
                        header.contains("NP_269579.1") ||
                        header.contains("NP_269895.1")) {

                        return 160490;
            }
            else if(header.contains("isb|IPI"))
                return 9606;
            else if(header.contains("AAC41771.1") ||
                        header.contains("NP_000217.2") ||
                        header.contains("CAD91891.1") ||
                        header.contains("Q8TC04_HUMAN"))
                return 9606;

        }
        return super.getSpecies( header, sequenceID );
    }
}
