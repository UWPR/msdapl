/*
 * NCBITaxonomySearcher.java
 * Created on Aug 31, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.ncbi.NetworkedProteinSearcher;
import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein.Protein;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Aug 31, 2006
 */

public class NCBITaxonomySearcher {

	private NCBITaxonomySearcher() { }
	
	public static NCBITaxonomySearcher getInstance() {
		return new NCBITaxonomySearcher();
	}

	public int getSpeciesByGi( int gi ) throws Exception {
		int id = -1; // initialize to -1 instead of 0 since 0 can be a valid taxID in the database.
		             // We will use -1 to flag the case where no entry was found 
		             // in the database for the given gi
		
		Connection conn = DatabaseHelper.getConnection(false);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			// TODO 07/07/11 Remove hard-coded database name (NCBI)
			// gi2taxonomy and NCBI_taxonomy tables should both be in the NCBI database
			String sql = "SELECT taxID FROM NCBI.gi2taxonomy WHERE gi = ? ORDER BY insertDate DESC";
			
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, gi );
			rs = stmt.executeQuery();
			
			// Return the most recent, non-zero taxonomy ID
			while (rs.next()) {
				id = rs.getInt( 1 );
				if(id != 0)
				    break;
			}

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
		
		
		// If this gi was not found in our database we will query NCBI's database directly.
        if(id == -1 || id == 0) {
            if(id == -1)
                System.out.print("NO matching entry found in NCBI NR.");
            if( id == 0)
                System.out.println("Matching entry in NCBI-NR has TaxID = 0.");
            System.out.print("Looking up NCBI directly for GI: "+gi+"... ");
            NetworkedProteinSearcher searcher = new NetworkedProteinSearcher();
            searcher.setGi(gi);
            
            Protein protein = null;
            try {protein = searcher.getProteinFromNCBI();}
            catch(Exception e) {
                e.printStackTrace();
                System.out.println("ERROR IN NetworkedProteinSearcher: "+e.getMessage());
            }
            if(protein != null) {
                id = protein.getSpecies().getId();
                System.out.println("Found TaxID from NCBI: "+id);
            }
            else {
                id = 0;
                System.out.println("NOT FOUND");
            }
        }       
		return id;	
	}
	
	public int getSpeciesByName( String name ) throws Exception {
		int id = 0;
		
		// !!! NOTE: for the YRC's setup this should return a connection to the "yrc" database
		//           since NCBI_Taxonomy is in the "yrc" database.
		Connection conn = DatabaseHelper.getNcbiTaxConnection(false);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "SELECT id FROM NCBI_Taxonomy WHERE name = ?";
		
			stmt = conn.prepareStatement( sql );
			stmt.setString( 1, name );
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				id = rs.getInt( 1 );
			}

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
