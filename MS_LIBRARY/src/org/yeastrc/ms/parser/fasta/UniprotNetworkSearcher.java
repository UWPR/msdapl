package org.yeastrc.ms.parser.fasta;


import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniprotNetworkSearcher {

	private final String SEARCH_URL = "http://www.ebi.ac.uk/cgi-bin/dbfetch?db=uniprotkb&style=raw&id=";

	private static final Logger log = Logger.getLogger("FastaParserLog");
	/**
	 * Get the taxonomy ID for the supplied uniprot/swissprot database accession
	 * string.
	 * @param accession
	 * @return The NCBI taxonomy ID number, 0 if not found
	 * @throws Exception
	 */
	public int getTaxonomyID( String accession ) throws Exception {
		int taxid = 0;

		log.info( "\nTrying Uniprot network lookup for " + accession + "..." );

		URL url = new URL( SEARCH_URL + accession );
		InputStream is = url.openStream();
		BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
		String line;
		
		while ((line = br.readLine()) != null) {
			if( !line.startsWith( "OX" ) ) continue;
			if( line.startsWith( "//" ) ) break;
				
			Pattern p = Pattern.compile( "^OX\\s+NCBI_TaxID=(\\d+)\\D.*$" );
			Matcher m = p.matcher( line );
			
			if( !m.matches() ) continue;
			
			taxid = Integer.parseInt( m.group( 1 ) );
			break;
		}
		
		br.close();
		is.close();
		
		//System.out.println( " Got tax id " + taxid );
		
		return taxid;
	}
}
