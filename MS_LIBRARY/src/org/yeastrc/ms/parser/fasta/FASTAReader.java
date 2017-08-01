/*
 * FASTAReader.java
 * Created on Aug 29, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Aug 29, 2006
 */

public class FASTAReader {

	// private constructor
	private FASTAReader() { }

	/**
	 * Get an instance of this class
	 * @param filename The filename of the FASTA file to read
	 * @return
	 * @throws Exception If there is a problem
	 */
	public static FASTAReader getInstance( String filename ) throws Exception {
		
		if (filename == null)
			throw new Exception( "filename may not be null" );
		
		FASTAReader reader = new FASTAReader();
		reader.br = new BufferedReader( new FileReader( new File( filename ) ) );
		
		return reader;
	}
	
	/**
	 * Get an instance of this class
	 * @param is An InputStream for the FASTA data
	 * @return
	 * @throws Exception If there is a problem
	 */
	public static FASTAReader getInstance( InputStream is ) throws Exception {
		
		if (is == null)
			throw new Exception( "is may not be null" );
		
		
		FASTAReader reader = new FASTAReader();
		reader.br = new BufferedReader( new InputStreamReader( is ) );
				
		return reader;
	}
	

	/**
	 * Read the next header(s) and sequence from the FASTA file, and save them to the header set and String supplied
	 * @param headers The Set to which to save the Set of Strings that are FASTA headers for the returned sequence.<BR>
	 * 				  The leading ">" is removed from the header(s)
	 * @param sequence The sequence matching the header(s)
	 * @return returns true if new headers/sequence were read, false if none were read (end of file)
	 * @throws Exception If there is a problem
	 */
	public boolean readNext( Set headers, StringBuffer sequence ) throws Exception {
		
		/*
		 * It is assumed the last read correctly returned a Set of headers and a sequence
		 * So, it is therefor assumed the BufferedReader's next line read will be a header line
		 * followed by sequence lines (unless last read returned false (end of file) )
		 */

		String line = null;
		if( this.lineNumber == 0 )
			this.lastLineRead = this.br.readLine();
		
		line = this.lastLineRead;
		
		if (line == null) return false;			// we've reached the end of the file
		this.lineNumber++;
		
		if (!line.startsWith( ">" ) )
			throw new Exception( "Line Number: " + this.lineNumber + " - Expected header line, but line did not start with \">\"." );
		
		line = line.substring(1, line.length());	// strip off the leading ">" on the header line
		
		/*
		 * In FASTA files, multiple headers can be associated with the same sequence, and will
		 * be present on the same line.  The separate headers are separated by the CONTROL-A
		 * character, so we split on that here, and save each to the headers Set
		 */
		String[] lineHeaders = line.split("\\cA");
		for (int i = 0; i < lineHeaders.length; i++) headers.add( lineHeaders[i] );
		
		// The next line must be a sequence line
		line = this.br.readLine();
		this.lastLineRead = line;
		
		while (line.startsWith( ";" )) {
			this.lineNumber++;
			line = this.br.readLine();
			this.lastLineRead = line;
		}
		if (line == null || line.startsWith( ">" ))
			throw new Exception( "Did not get a sequence line after a header line (Line Number: " + this.lineNumber );
		
		
		// loop through the file, reading sequence lines until we hit the next header line (or the end of the file)
		while (line != null) {
			
			//If we've reached a new header line (marked with a leading ">"), then we're done.
			if (line.startsWith( ">" )) {
				break;
			}
			
			this.lineNumber++;
			
			// build the sequence, if it's not a comment line
			if (!line.startsWith( ";" )) {
				
				// upper-case the sequence line
				line = line.toUpperCase();
				
				sequence.append( line );
			}
			
			line  = this.br.readLine();
			this.lastLineRead = line;
		}
		
		// If we've made it here, we've read another sequence entry in the FASTA data
		return true;
	}
	
	private BufferedReader br;
	private int lineNumber;
	private String lastLineRead;
}
