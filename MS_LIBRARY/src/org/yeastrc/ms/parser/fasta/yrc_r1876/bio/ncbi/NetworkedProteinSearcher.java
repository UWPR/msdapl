/*
 * NetworkedSearcher.java
 * Created on Feb 10, 2005
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.ncbi;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein.Protein;
import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.taxonomy.Species;


/**
 * A class for searcing the NCBI's protein database, which is accomplished over the Internet
 * using NCBI's network API described at http://eutils.ncbi.nlm.nih.gov/entrez/query/static/esearch_help.html
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Feb 10, 2005
 */

public class NetworkedProteinSearcher {

	/** The URL to use to search NCBI's database via the web, only the gi # should be appended to the end */
	private static final String NCBI_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=protein&id=";
	
	/** The URL to use to search NCBI's protein database for the sequence for a given gi */
	private static final String SEQ_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&complexity=1&rettype=xml&id=";
	
	/**
	 * Search the NCBI protein database for the Gi that has been set
	 * @return The Protein object populated w/ parameters for the matching protein,
	 * 		   or null if no matches were found.
	 */
	public Protein getProteinFromNCBI() throws Exception {
		if (this.getGi() == 0) return null;
		
		// Set up the URL
		URL url = new URL (NetworkedProteinSearcher.NCBI_URL + String.valueOf(this.getGi()));
		//System.out.println("URL: " + url);
		
		// Get the InputStream and parse the XML
		//System.out.println("Connecting to NCBI...");
		DOMParser parser = new DOMParser();
		InputStream is = url.openStream();
	    InputSource source = new InputSource(is);

	    //System.out.println("Parsing XML to DOM...");
	    parser.parse(source);
		
		// clean up
		is.close();
		is = null;
		url = null;
		//System.out.println("Connection closed...");
		
		/*
		 * Parse the XML.
		 * 
		 * A successful search will have the form:
		 * <?xml version="1.0"?>
		 * <!DOCTYPE eSummaryResult PUBLIC "-//NLM//DTD eSummaryResult, 11 May 2002//EN" "http://www.ncbi.nlm.nih.gov/entrez/query/DTD/eSummary_041029.dtd">
		 * <eSummaryResult>
		 * <DocSum>
		 *		<Id>123334</Id>
		 *		<Item Name="Caption" Type="String">P10180</Item>
		 *		<Item Name="Title" Type="String">Homeobox protein cut</Item>
		 *		<Item Name="Extra" Type="String">gi|123334|sp|P10180|HMCU_DROME</Item>
		 *		<Item Name="Gi" Type="Integer">123334</Item>
		 *		<Item Name="CreateDate" Type="String">1989/03/01</Item>
		 *		<Item Name="UpdateDate" Type="String">2005/01/25</Item>
		 *		<Item Name="Flags" Type="Integer">0</Item>
		 *		<Item Name="TaxId" Type="Integer">7227</Item>
		 * </DocSum>
		 * </eSummaryResult>
		 * 
		 * An empty search result appears as:
		 * <?xml version="1.0"?>
		 * <!DOCTYPE eSummaryResult PUBLIC "-//NLM//DTD eSummaryResult, 11 May 2002//EN" "http://www.ncbi.nlm.nih.gov/entrez/query/DTD/eSummary_041029.dtd">
		 *	<eSummaryResult>
		 *		<ERROR>Empty id list - nothing todo</ERROR>
		 *	</eSummaryResult>
		 *
		 */
		
		//System.out.println("Parsing DOM...");
		Document doc = parser.getDocument();
		NodeList nodes = doc.getElementsByTagName("ERROR");
		if (nodes.getLength() > 0) {
			// Error in the search (e.g. no matches)
			nodes = null;
			doc = null;
			parser = null;
			
			throw new Exception ("Got an ERROR in the NCBI network search.");
		}

		nodes = doc.getElementsByTagName("Item");
		if (nodes.getLength() < 1) {
			// Error in the search (e.g. no matches)
			nodes = null;
			doc = null;
			parser = null;
			
			throw new Exception (" Got no matches in the NCBI network search.");
		}
		
		// The protein to return
		NCBIProtein prot = new NCBIProtein();
		Set<String> names = new HashSet<String>();
		names.add("gi|" + this.getGi());
		
		// Loop through the data and set attributes in the protein
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Text data = (Text) node.getFirstChild();

			if (node.getAttributes().getNamedItem("Name").getNodeValue().equals("TaxId")) {
				Species spec = new Species();
				int specID = Integer.parseInt(data.getNodeValue());
				spec.setId(specID);
				prot.setSpecies(spec);
				//System.out.println("Set species: " + spec.getId());
				continue;
			}

			if (node.getAttributes().getNamedItem("Name").getNodeValue().equals("Title")) {
				prot.setDescription(data.getNodeValue());
				continue;
			}	
		}
		
		// Clean up some more
		nodes = null;
		doc = null;
		parser = null;
		//System.out.println("Done parsing DOM...");
		
		// Set the peptide sequence in the protein object
		NCBIPeptide pep = new NCBIPeptide();
		pep.setSequenceString(this.getSequenceFromNCBI());

		if (pep.getSequenceString() == null)
			throw new Exception("Got a null sequence out of peptide after putting it in...");	
		
		prot.setPeptide(pep);
		
		return prot;
	}
	
	/**
	 * Return the Sequence, from NCBI for the set gi number (must be a protein)
	 * @return The sequence as a String, or null if not found
	 * @throws Exception If there is a prob
	 */
	public String getSequenceFromNCBI() throws Exception {
		if (this.getGi() == 0) return null;
		
		// Set up the URL
		URL url = new URL (NetworkedProteinSearcher.SEQ_URL + String.valueOf(this.getGi()));
		
		
		// Get the InputStream and parse the XML
		//System.out.println("Opening connection to NCBI (sequence)");
		//System.out.println("URL: " + url);
		DOMParser parser = new DOMParser();
		InputStream is = url.openStream();
	    InputSource source = new InputSource(is);

	    //System.out.println("Parsing XML to DOM...");
	    parser.parse(source);
		
		// clean up
		is.close();
		is = null;
		url = null;
		//System.out.println("Closing connection...");

		/*
		 * The Stream should contain the following UNIQUE Element in the document (Example):
		 *                 <IUPACaa>GVSGSCNIDVVCPEGNGHRDVIRSVAAYSKQGTMWCTGSLVNNSANDKKMYFLTA</IUPACaa>
		 */
		
		Document doc = parser.getDocument();
		NodeList nodes = doc.getElementsByTagName("IUPACaa");
		if (nodes.getLength() < 1) {
			// Error in the search (e.g. no matches)
			nodes = null;
			doc = null;
			parser = null;
			
			//System.out.println("Got no XML nodes for:\n" + url);
			throw new Exception ("Got not matches when searching NCBI for sequence information.");
		}

		Text data = (Text) nodes.item(0).getFirstChild();
		String sequence = data.getNodeValue();
		
		// Clean
		data = null;
		nodes = null;
		doc = null;
		parser = null;
		
		//System.out.println("Returning sequence (gi|" + gi + "): " + sequence);
		return sequence;
	}
	
	/** Construct! */
	public NetworkedProteinSearcher() {
		this.gi = 0;
	}
	
	/**
	 * @return Returns the gi.
	 */
	public int getGi() {
		return gi;
	}
	/**
	 * @param gi The gi to set.
	 */
	public void setGi(int gi) {
		this.gi = gi;
	}
	// The NCBI gi accession number
	private int gi;
}
