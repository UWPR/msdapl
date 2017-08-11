/*
 * NR_SEQAccSearcher.java
 * Created on Aug 31, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Aug 31, 2006
 */

public class SequenceSearcher {

	private SequenceSearcher() { }
	
	public static SequenceSearcher getInstance() {
		return new SequenceSearcher();
	}
	
	/**
	 * Find and return the single species ID that corresponds to the supplied sequenceID
	 * in the YRC_NRSEQ database.  Returns 0 if no species were found or if multiple
	 * species were found
	 * @return
	 * @throws Exception
	 */
	public int getSpecies( int sequenceID ) throws Exception {
		
		int id = 0;
		
		Connection conn = DatabaseHelper.getConnection(false);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			String sql = "SELECT DISTINCT speciesID FROM tblProtein WHERE sequenceID = ?";
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, sequenceID );
			rs = stmt.executeQuery();
			
			List<Integer> ids = new ArrayList<Integer>();
			while (rs.next()) {
				ids.add( new Integer( rs.getInt( 1 ) ) );
			}
			
			if (ids.size() == 1)
				id = ((Integer)(ids.get( 0 ) )).intValue();
			
			ids.clear();
			ids = null;
			
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
		} finally {
			
			if (rs != null) {
				try {
					rs.close();
					rs = null;
				} catch (Exception e) { ; }
			}

			if (stmt != null) {
				try {
					stmt.close();
					stmt = null;
				} catch (Exception e) { ; }
			}
			
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (Exception e) { ; }
			}
		}
		
		return id;
	}
	
}
