/*
 * IFASTAParser.java
 * Created on Aug 29, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Aug 29, 2006
 */

public abstract class NR_SEQ_FASTAParser {

	private static final Logger log = Logger.getLogger("FastaParserLog");

    private int noSpeciesFound = 0;

	/**
	 * Will attempt to parse the FASTA data present in the file supplied and place it in the database
	 * @param file The file to parse
	 * @param dryRun If true, data will not be written to the database.  Instead, text will be written
	 * to standard out where the data would have been written to the database (for testing purposes)
	 * @throws Exception If there is a problem
	 */

	public void parseFASTA(File file, boolean dryRun) throws Exception {
		
		log.info("Parsing: " + file.getPath() + "\n");
		this.filename = file.getName();
		
		FASTAReader reader = FASTAReader.getInstance( file );

		// mark current proteins associated w/ this database as obsolete (will be set back to
		// current if they are present in the new FASTA
		if (!dryRun)
			markProteinsObsolete();
		else
			log.info( "Would mark proteins obsolete here.\n" );
		
		Set headers = new HashSet();
		StringBuffer sequence = new StringBuffer();
		
		
		while (reader.readNext( headers, sequence )) {
			log.info( "\n" );
			
			String sequenceString = sequence.toString();
			
			// remove spaces from the sequence
			sequenceString = sequenceString.replaceAll( " ", "" );
			
			if (!FASTAValidator.validProteinSequence( sequenceString ))
				throw new Exception( "Invalid protein sequence: " + sequenceString );
			
			boolean newSequence = false;
			boolean newProtein = false;
			boolean newProteinDatabaseEntry = false;

			// remove a trailing * from the sequence, if present
			if (sequenceString.endsWith( "*" ))
				sequenceString = sequenceString.substring(0, sequenceString.length() - 1);
			
			int sequenceID = getSequenceID( sequenceString );
			if ( sequenceID == 0) {
				newSequence = true; newProtein = true; newProteinDatabaseEntry = true;
				if (!dryRun)
					sequenceID = insertSequence( sequenceString );
				else
					log.info( "\tWould add new sequence to database..." );
			} else
				log.info( "\tFound sequence ID: " + sequenceID );
			
			
			Iterator iter = headers.iterator();
			while (iter.hasNext()) {
				String header = (String)(iter.next());
				log.info( "\t\tGot header: " + header );
				
				String acc = getAccessionString( header );
				String description = getDescription( header );
				int species = getSpecies( header, sequenceID );
				if(species == 0)
				{
					noSpeciesFound++;
				}

				log.info( "\t\t\tAcc:\t" + acc );
				log.info( "\t\t\tDesc:\t" + description );
				log.info( "\t\t\tTaxy:\t" + species + "\n" );
				
				int proteinID = 0;
				if (!newProtein)
					proteinID = getProteinID( sequenceID, species );
				
				if (proteinID == 0) {
					newProtein = true; newProteinDatabaseEntry = true;
					if (!dryRun)
						proteinID = insertProtein( sequenceID, species );
					else
						log.info( "\t\t\tWould add new protein to database..." );
				} else
					log.info( "\t\t\tFound protein ID: " + proteinID );
				
				int proteinDatabaseID = 0;
				if (!newProteinDatabaseEntry)
					proteinDatabaseID = getProteinDatabaseID( proteinID, acc );

				String url = getURL();
				if (url != null)
					url = url.replaceFirst( "__ACCESSION_STRING__", acc );
				
				if (proteinDatabaseID == 0) {
					newProteinDatabaseEntry = true;
					
					if (!dryRun)
						addNewProteinDatabaseEntry( proteinID, acc, description, url );
					else
						log.info( "\t\t\t\tWould create new protein database entry..." );

				} else {

					// UPDATE THIS ENTRY
					if (!dryRun)
						updateProteinDatabaseEntry( proteinID, acc, description, url );
					else
						log.info( "\t\t\t\tWould update protein database entry..." );
				}
			}
			headers = new HashSet();
			sequence = new StringBuffer();
		}
	}

	public int getNoSpeciesFoundCount()
	{
		return noSpeciesFound;
	}

	/**
	 * Get the accession string for this FASTA header.  That is the string:
	 * >ACCESSION_STRING DESCRIPTION
	 * @param header
	 * @return
	 * @throws Exception
	 */
	public String getAccessionString( String header ) throws Exception {
		Pattern p = Pattern.compile("^(\\S+).*");
		Matcher m = p.matcher( header );
		
		if (!m.matches())
			throw new Exception( "Did not get a FASTA header passed to getAccessionString()" );		
		
		return m.group( 1 );
	}
	
	/**
	 * Get the description for this FASTA header.  That is the string:
	 * >ACCESSION_STRING DESCRIPTION
	 * @param header
	 * @return the header, null if not present
	 * @throws Exception
	 */	
	public String getDescription( String header ) throws Exception {
		Pattern p = Pattern.compile("^(\\S+)\\s+(.+)");
		Matcher m = p.matcher( header );
		
		if (!m.matches())
			return null;
		
		return m.group( 2 );
	}
	
	/**
	 * Get the URL for linking out to information about this protein reference
	 * @return The URL for a link out for this protein reference, where __ACCESSION_STRING__ will be replaced
	 * with the accession string for the protein  Returns null if not applicable
	 */
	public abstract String getURL();
	
	/**
	 * Get the NCBI Taxonomy id number for the supplied FASTA header
	 * @param header
	 * @param sequenceID The sequence ID associated with this FASTA header
	 * @return
	 * @throws Exception
	 */
	public abstract int getSpecies( String header, int sequenceID ) throws Exception;

	/**
	 * Get the YRC_NRSEQ database id for the given parser to associate data
	 * @return
	 * @throws Exception
	 */
	public abstract int getDatabaseID() throws Exception;
	

	// Mark all proteins associated with this database as not current in the protein reference table
	private void markProteinsObsolete() throws Exception {		
		String sql = "UPDATE tblProteinDatabase SET isCurrent = 'F' WHERE databaseID = " + getDatabaseID();
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.executeUpdate();
		
		stmt.close(); stmt = null;
	}

	private void updateProteinDatabaseEntry( int proteinID, String accessionString, String description, String url) throws Exception {
		String sql = "UPDATE tblProteinDatabase SET description = ?, url = ?, isCurrent = 'T' WHERE proteinID = ? AND databaseID = ? AND accessionString = ?";
		
		PreparedStatement stmt = conn.prepareStatement( sql );
		stmt.setString( 1, description );

		if (url == null)
			stmt.setNull( 2, Types.VARCHAR );
		else
			stmt.setString( 2, url );
		
		stmt.setInt( 3, proteinID );
		stmt.setInt( 4, getDatabaseID() );
		stmt.setString( 5, accessionString );
		
		stmt.executeUpdate();
		stmt.close(); stmt = null;

		log.info( "\t\t\t\tUpdated protein database entry." );
	}
	
	// Add a new ProteinDatabase entry based on the supplied parameters
	private void addNewProteinDatabaseEntry( int proteinID, String accessionString, String description, String url) throws Exception {
		
		// added IGNORE here to alleviate the situation where the same protein sequence is in the FASTA multiple times with huge accession strings that are almost identical
		// this change ensures we keep only the first instance of it
		String sql = "INSERT IGNORE INTO tblProteinDatabase (proteinID, databaseID, accessionString, description, URL, isCurrent) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		
		PreparedStatement stmt = conn.prepareStatement( sql );
		stmt.setInt( 1, proteinID );
		stmt.setInt( 2, getDatabaseID() );
		stmt.setString( 3, accessionString );
		stmt.setString( 4, description );
		
		if (url == null)
			stmt.setNull( 5, Types.VARCHAR );
		else
			stmt.setString( 5, url );

		
		stmt.setString( 6, "T" );
		
		stmt.executeUpdate();
		stmt.close(); stmt = null;

		log.info( "\t\t\t\tAdded new protein database entry." );
		
	}

	// Get the ID for the supplied sequence string, returns 0 if it's not in the database
	private int getSequenceID( String sequence ) throws Exception {
		
		String sql = "SELECT id FROM tblProteinSequence WHERE sequence = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString( 1, sequence );
		ResultSet rs = stmt.executeQuery();
		
		int sequenceID = 0;
		if (rs.next())
			sequenceID = rs.getInt( 1 );
		
		rs.close(); rs = null;
		stmt.close(); stmt = null;
		
		return sequenceID;
	}
	
	// Get the protein ID for the given sequenceID and species ID, returns 0 if it's not in the database
	private int getProteinID( int sequenceID, int speciesID ) throws Exception {
		
		String sql = "SELECT id FROM tblProtein WHERE sequenceID = ? AND speciesID = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setInt( 1, sequenceID );
		stmt.setInt( 2, speciesID );
		
		ResultSet rs = stmt.executeQuery();
		
		int id = 0;
		if (rs.next())
			id = rs.getInt( 1 );
		
		rs.close(); rs = null;
		stmt.close(); stmt = null;
		
		return id;
	}
	
	// Get the proteinDatabaseID for the given protein, database and accession string, returns 0 if non exists
	private int getProteinDatabaseID( int proteinID, String accessionString ) throws Exception {

		String sql = "SELECT id FROM tblProteinDatabase WHERE proteinID = ? AND databaseID = ? AND accessionString = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setInt( 1, proteinID );
		stmt.setInt( 2, getDatabaseID() );
		stmt.setString( 3, accessionString );
		
		ResultSet rs = stmt.executeQuery();
		
		int id = 0;
		if (rs.next())
			id = rs.getInt( 1 );
		
		rs.close(); rs = null;
		stmt.close(); stmt = null;
		
		return id;
	}
	
	
	// Insert the supplied protein (designated by sequenceID and speciesID) into the database and return its ID
	private int insertProtein( int sequenceID, int speciesID) throws Exception {
		
		String sql = "SELECT * FROM tblProtein WHERE sequenceID = " + sequenceID + " AND speciesID = " + speciesID;
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = stmt.executeQuery(sql);
		
		int proteinID = 0;
		
		if (rs.next())
			proteinID = rs.getInt("id");
		else {
			rs.moveToInsertRow();
			rs.updateInt( "sequenceID", sequenceID );
			rs.updateInt( "speciesID", speciesID );
			rs.insertRow();
			rs.last();
			
			proteinID = rs.getInt( "id" );
		}
		
		rs.close(); rs = null;
		stmt.close(); stmt = null;
		
		if (proteinID == 0)
			throw new Exception( "Got 0 for proteinID after calling insertProtein()" );

		log.info( "\t\t\tAdded new protein: " + proteinID );
		return proteinID;
	}
	
	// Insert the given sequence into the sequence table and return the ID of the new entry
	private int insertSequence( String sequence ) throws Exception {

		String sql = "SELECT * FROM tblProteinSequence WHERE sequence = ?";
		PreparedStatement stmt = conn.prepareStatement( sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
		stmt.setString( 1, sequence );
		ResultSet rs = stmt.executeQuery();
		
		int sequenceID = 0;
		
		if (rs.next())
			sequenceID = rs.getInt( 1 );
		else {
			rs.moveToInsertRow();
			rs.updateString( "sequence", sequence );
			rs.insertRow();
			rs.last();
			
			sequenceID = rs.getInt( "id" );
		}
		
		rs.close(); rs = null;
		stmt.close(); stmt = null;
		
		if (sequenceID == 0)
			throw new Exception( "Got 0 for sequenceID after calling insertSequence()" );

		log.info( "\tAdded new sequence: " + sequenceID );
		return sequenceID;
	}
	
	
	
	// the connection used by this parser
	protected Connection conn;

	private String filename;

	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * Set the database connection to be used by this parser
	 * @param connection
	 */
	public void setConnection( Connection connection ) { this.conn = connection; }

}
