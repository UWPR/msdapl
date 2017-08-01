/*
 * SwissProtSearcher.java
 * Created on Aug 31, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Aug 31, 2006
 */

public class SwissProtSearcher {

	private SwissProtSearcher() { }
	
	public static SwissProtSearcher getInstance() {
		return new SwissProtSearcher();
	}
	
	/**
	 * Take an accession string and search the swiss-prot database for a match and return the NCBI taxonomy species ID for the matching organism
	 * @param acc
	 * @return
	 * @throws Exception
	 */
	public int getSpeciesByAcc(String acc) throws Exception {
		
		int id = 0;
		
		Connection conn = DatabaseHelper.getConnection(false);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			String sql = "SELECT speciesID FROM tblProtein AS a INNER JOIN tblProteinDatabase AS b ON a.id = b.proteinID WHERE b.accessionString = ? AND b.databaseID = ?";
			stmt = conn.prepareStatement( sql );
			stmt.setString( 1, acc );
			stmt.setInt( 2, ProteinDatabaseId.get(ProteinDatabaseName.SWISSPROT) );
			rs = stmt.executeQuery();
			
			if (rs.next())
				id = rs.getInt( 1 );
			
			
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
	
	/**
	 * Attempts to take the swiss-prot id tag for a species name and return the NCBI taxonomy ID number
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public int getSpeciesByName(String name) throws Exception {
		
		int id = 0;
		
		Connection conn = DatabaseHelper.getConnection(false);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {

			String sql = "SELECT speciesID FROM tblProtein AS a INNER JOIN tblProteinDatabase AS b ON a.id = b.proteinID WHERE b.databaseID = ? AND b.accessionString LIKE ? LIMIT 1";
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, ProteinDatabaseId.get(ProteinDatabaseName.SWISSPROT) );
			stmt.setString( 2, "%\\_" + name );
			rs = stmt.executeQuery();
			
			if (rs.next())
				id = rs.getInt( 1 );
			
			
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
