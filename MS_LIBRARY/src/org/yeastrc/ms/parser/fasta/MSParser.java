/*
 * MSParser.java
 * Created on Sep 7, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Sep 7, 2006
 */

public abstract class MSParser extends NR_SEQ_FASTAParser {

	
	/**
	 * Try to ascertain the species ID of the FASTA header received. Strategy is to attempt to
	 * find the species based on the parsing of the string first.  If unsuccessful, then attempt to do
	 * database lookups.  If that is unsuccessful, it will attempt to do WWW lookups.  If all fails,
	 * it will return 0 (no species)
	 * 
	 * @return The species ID ascertained, 0 if it could not be ascertained
	 */
	Pattern keratinp = Pattern.compile( "^\\S*KERATIN\\d+$" );
	Pattern ubiquitinp = Pattern.compile( "^\\S*UBIQUITIN\\d+$" );
	
	Pattern p4 = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.+)$");
	Pattern p3 = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(.+)$");
	
	Pattern bracketSpeciesPattern = Pattern.compile( "^.+\\[(.+)\\]$" );
	Pattern giPattern = Pattern.compile( "^(gi\\|\\d+).*$" );

	//Pattern swissprotPattern1 = Pattern.compile( "^(\\S)\\s+\\((\\S+)\\).*$" );
	//Pattern swissprotPattern2 = Pattern.compile( "^\\S+_(\\S+)\\s+.+$");
	
	// find patterns matching swiss-prot IDs (e.g. B6UN75_DROME) in accession strings of FASTA header lines
	Pattern newSwissprotPattern = Pattern.compile( "\\W*([A-Z0-9]+)_([A-Z0-9]+)\\W*" );	
	
	Pattern taxidPattern = Pattern.compile( "^.+\\s+Tax_Id=(\\d+)\\s+.+$" );
	
	Pattern cgdPattern = Pattern.compile( "^\\S+\\s+\\S+\\s+CGDID:\\S+\\s+.*$" );
	
	// Example: >YAL001C TFC3 SGDID:S000000001, Chr I from 151006-147594,151166-151097, reverse complement, Verified ORF
	private static final Pattern yeastSGDPattern = Pattern.compile("^.*SGDID:S\\d{9}.*$");
	
	Pattern uniprotPattern = Pattern.compile( "^([ABCOPQ]\\w\\w\\w\\w\\w)$" );
	
	// Example >UniRef90_A0AUS7 Putative uncharacterized protein (Fragment) n=1 Tax=Xenopus laevis RepID=A0AUS7_XENLA
	Pattern uniprotPattern2 = Pattern.compile( "^UniRef.+RepID=(\\S+).*$" );
	
	// >P68254 sp|P68254|1433T_MOUSE 14-3-3 protein theta OS=Mus musculus GN=Ywhaq PE=1 SV=1
	Pattern uniprotPattern3 = Pattern.compile( "^.+OS=([A-Za-z0-9 ]+) [A-Z][A-Z]=.+$" );
	//^.+OS=([A-Za-z0-9 ]+) [A-Z][A-Z]=.+$
	
	// Example FBpp0070640
	Pattern flyBasePattern = Pattern.compile("^FBpp\\d{7}+$");
	
	public int getSpecies(String header, int sequenceID ) throws Exception {
		Matcher m4 = null;
		Matcher gm = null;
		Matcher fb = null;
		
		// if header start with "contaminant_" strip it off, i wish they wouldn't put it on
		if (header.startsWith( "contaminant_" ))
			header = header.substring( 12, header.length() );
		
		// See if this is a reversed sequence.  If so, returned 0 (no species)
		String acc = getAccessionString( header );
		if (acc.startsWith( "Reverse_" ) || acc.startsWith("rev_"))
			return 0;
		
		// If this is a randomized sequence, return 0
		if ( acc.startsWith( "random_seq_" ) || acc.startsWith( "rand_" ) )
			return 0;
		
		// If this is a scrambled sequence, return 0
		if (acc.startsWith( "Scramble_" ))
			return 0;
		
		if (acc.equals( "Tub4" ))
			return 4932;
		
		if (header.startsWith( "CNAG_" ))
			return 235443;
		
		if (header.startsWith( "Toxoplasma_gondii|" ))
			return 5811;
		
		if ( header.startsWith( "jgi|Xentr4|" ) )
			return 8364;
		
		if ( header.startsWith( "jgi|Nemve1|" ) )
			return 45351;
		
		// Check for general human contaminants KERATIN and UBIQUITIN
		gm = this.keratinp.matcher( acc );
		if (gm.matches()) {
			return 9606;
		}
		gm = this.ubiquitinp.matcher( acc );
		if (gm.matches()) {
			return 9606;
		}
		
		// Check to see if this is an entry from WormBase, if so, returned C Elegans
		m4 = this.p4.matcher( header );
		if (m4.matches() && m4.group( 2 ).startsWith( "CE") && m4.group( 3 ).startsWith( "WBGene") ) {
			return 6239;
		}
		
		// Check if this is a FlyBase id
		fb = this.flyBasePattern.matcher(acc);
		if(fb.matches()) {
		    return 7227;
		}

		// Check to see if this is an entry from CGD (candida genome database)
		gm = this.cgdPattern.matcher( header );
		if (gm.matches())
			return 5476;
		
		// check to see if it's an SGD FASTA entry, if so use 4932
		Matcher m = yeastSGDPattern.matcher(header);
        if(m.matches())  
        	return 4932;
        
		// Check to see if they have a Tax_Id=# as part of their desc.  If so, use that #
		gm = this.taxidPattern.matcher( header );
		if (gm.matches()) {
			try {
				return Integer.parseInt( gm.group( 1 ) );
			} catch (Exception e) { ; }
		}
		
		int id = 0;
        
        // check the YRC_NRSEQ for this acc.  if we only find one species associated with it, use that
        /*
        id = NR_SEQAccSearcher.getInstance().getSpecies( acc );
        if (id != 0) return id;
        */
        
        
        
        // check to see if it's a mips entry
        
        
        
        // if this header starts with a gi#, check the local database for that specific gi (will hit NR and NCBI)
        // if this header starts with a gi#, check the local database for that specific gi (will hit NR and NCBI)
        gm = this.giPattern.matcher( acc );
        if (gm.matches()) {
            
            /*
            id = NR_SEQAccSearcher.getInstance().getSpecies( gm.group( 1 ) );
            //System.out.println( "##FOUND GI NUMBER:\t" + gm.group( 1 ) );
            
            if (id != 0) return id;
            */
            
            // try searching the gi2taxonomy table in the NCBI database for this gi number
            String[] gifields = gm.group( 1 ).split( "\\|" );
            
            if ( gifields.length == 2 ) {
                id = NCBITaxonomySearcher.getInstance().getSpeciesByGi( Integer.parseInt( gifields[ 1 ] ) );
                //System.out.println( "###CHECKING GI NUMBER IN gi2taxonomy:\t" + gifields[1] );
            }
            
            if ( id != 0 ) return id;           
        }
        
        // check to see if we can match based on the Uniprot species name designator (OS=)'
        gm = this.uniprotPattern3.matcher( header );
        if( gm.matches() ) {
        	String speciesName = gm.group( 1 );
        	        	
			// try the common names first
			if (speciesName.equals( "Escherichia coli K12")) return 83333;
			else if (speciesName.equals( "Escherichia coli" )) return 562;
			else if (speciesName.equals( "Arabidopsis thaliana" )) return 3702;
			else if (speciesName.equals( "Bos taurus" )) return 9913;
			else if (speciesName.equals( "Caenorhabditis elegans" )) return 6239;
			else if (speciesName.equals( "Chlamydomonas reinhardtii" )) return 3055;
			else if (speciesName.equals( "Danio rerio" )) return 7955;
			else if (speciesName.equals( "Dictyostelium discoideum" )) return 44689;
			else if (speciesName.equals( "Drosophila melanogaster" )) return 7227;
			else if (speciesName.equals( "Hepatitis C virus" )) return 11103;
			else if (speciesName.equals( "Homo sapiens" )) return 9606;
			else if (speciesName.equals( "Mus musculus" )) return 10090;
			else if (speciesName.equals( "Mycoplasma pneumoniae" )) return 2104;
			else if (speciesName.equals( "Oryza sativa" )) return 4530;
			else if (speciesName.equals( "Plasmodium falciparum" )) return 5833;
			else if (speciesName.equals( "Pneumocystis carinii" )) return 4754;
			else if (speciesName.equals( "Rattus norvegicus" )) return 10116;
			else if (speciesName.equals( "Saccharomyces cerevisiae" )) return 4932;
			else if (speciesName.equals( "Schizosaccharomyces pombe" )) return 4896;
			else if (speciesName.equals( "Takifugu rubripes" )) return 31033;
			else if (speciesName.equals( "Xenopus laevis" )) return 8355;
			else if (speciesName.equals( "Zea mays" )) return 4577;

        }
        
		
		// check to see if the header ends with a species name HEADER STRING [SPECIES NAME] and then look it up by name
		gm = this.bracketSpeciesPattern.matcher( header );
		if (gm.matches()) {
			String speciesName = gm.group( 1 );
			
			// try the common names first
			if (speciesName.equals( "Escherichia coli K12")) return 83333;
			else if (speciesName.equals( "Escherichia coli" )) return 562;
			else if (speciesName.equals( "Arabidopsis thaliana" )) return 3702;
			else if (speciesName.equals( "Bos taurus" )) return 9913;
			else if (speciesName.equals( "Caenorhabditis elegans" )) return 6239;
			else if (speciesName.equals( "Chlamydomonas reinhardtii" )) return 3055;
			else if (speciesName.equals( "Danio rerio" )) return 7955;
			else if (speciesName.equals( "Dictyostelium discoideum" )) return 44689;
			else if (speciesName.equals( "Drosophila melanogaster" )) return 7227;
			else if (speciesName.equals( "Hepatitis C virus" )) return 11103;
			else if (speciesName.equals( "Homo sapiens" )) return 9606;
			else if (speciesName.equals( "Mus musculus" )) return 10090;
			else if (speciesName.equals( "Mycoplasma pneumoniae" )) return 2104;
			else if (speciesName.equals( "Oryza sativa" )) return 4530;
			else if (speciesName.equals( "Plasmodium falciparum" )) return 5833;
			else if (speciesName.equals( "Pneumocystis carinii" )) return 4754;
			else if (speciesName.equals( "Rattus norvegicus" )) return 10116;
			else if (speciesName.equals( "Saccharomyces cerevisiae" )) return 4932;
			else if (speciesName.equals( "Schizosaccharomyces pombe" )) return 4896;
			else if (speciesName.equals( "Takifugu rubripes" )) return 31033;
			else if (speciesName.equals( "Xenopus laevis" )) return 8355;
			else if (speciesName.equals( "Zea mays" )) return 4577;

			
			else {
				// having failed the common names, look it up in the database
				id = NCBITaxonomySearcher.getInstance().getSpeciesByName( speciesName );
				if (id != 0) return id;				
			}
			
		}
		
		
		/*
		 * Removing these and replacing with new swiss-prot detection method
		 * Michael Riffle, December 2011

		
		// check swiss-prot here
		// this is necessary as an additional step, eventhough we've searched the whole YRC_NRSEQ, as the previous
		// search will not return a species if the acc matches multiple species.  By only searching swiss-prot, we
		// guarantee only a single species will be matched by an acc string
		gm = this.swissprotPattern1.matcher( header );
		if (gm.matches()) {
			id = SwissProtSearcher.getInstance().getSpeciesByAcc( gm.group( 1 ) );
			if (id != 0) return id;
		}

		// take another stab at a swiss-prot species id
		gm = this.swissprotPattern2.matcher( header );
		if (gm.matches()) {
			id = SwissProtSearcher.getInstance().getSpeciesByName( gm.group( 1 ) );
			if (id != 0) return id;
		}
		*/
		
		
		/*
		 * Use new swiss-prot pattern searcher
		 * Michael Riffle, December 2011
		 */
		gm = this.newSwissprotPattern.matcher( acc );
		if( gm.find() ) {
			
			String p1 = gm.group( 1 );
			String p2 = gm.group( 2 );
			
			// try doing a quick lookup based on the entire entry name
			id = SwissProtSearcher.getInstance().getSpeciesByAcc( p1 + "_" + p2 );
			if( id != 0 ) return id;

			/*
			 * The second part of the entry name denotes a specific organism.
			 * However, if it starts with 9, it denotes an taxonomy GROUP, and does not
			 * uniquely identify an organism. (if it starts with 9, we have to lookup
			 * via web services, done later.)
			 * 
			 * See: http://web.expasy.org/docs/userman.html for more information
			 * 
			 * Attempt a local lookup of the taxonomy ID based on the second part
			 */
			if( !p2.startsWith( "9" ) ) {				
				id = SwissProtSearcher.getInstance().getSpeciesByName( p2 );
				if (id != 0) return id;
			}
			
			/*
			 * If it could not be found locally, or if the second part began with
			 * a "9", then lookup via web services from uniprot
			 */
			UniprotNetworkSearcher uns = new UniprotNetworkSearcher();
			id = uns.getTaxonomyID( p1 + "_" + p2 );
			if( id != 0 ) return id;
			
		}
		
		
		
		//if it matches a gi, use the NCBI network searcher
		gm = this.giPattern.matcher( acc );
        if (gm.matches()) {
        	
        } else {
        	
        	//if it matches a uniprot acc use the network uniprot searcher
        	gm = this.uniprotPattern.matcher( acc );

        	if( gm.matches() ) {
        		UniprotNetworkSearcher uns = new UniprotNetworkSearcher();
        		id = uns.getTaxonomyID( acc );
        		if( id != 0 ) return id;
        	}
        	
        	//other uniprot pattern handler here
        	gm = this.uniprotPattern2.matcher( header );
        	if( gm.matches() ) {
        		UniprotNetworkSearcher uns = new UniprotNetworkSearcher();
        		id = uns.getTaxonomyID( gm.group( 1 ) );
        		if( id != 0 ) return id;
        	}
        	
        }
		
		
		// OK, now see if only one speciesID exists for all proteins that have this sequence, if so use that speciesID
		// also throw a warning, as this COULD be unreliable
		id = SequenceSearcher.getInstance().getSpecies( sequenceID );
		if ( id != 0 ) {
			System.out.println( "\n\t\t*** WARNING *** FOUND SPECIES BASED ON SEQUENCE FOR:\n\t\t" + header + "\n" );
			return id;
		}
		
		
		// if this is a gi#, and we still don't have a match, check NCBI database on web
		
		System.out.println( "\n\t\t*** WARNING *** COULD NOT FIND A SPECIES FOR:\n\t\t" + header + "\n" );
		
		return 0;
	}
	
	/**
	 * There is no URL associated with these
	 */
	public String getURL() {
		return null;
	}
}
