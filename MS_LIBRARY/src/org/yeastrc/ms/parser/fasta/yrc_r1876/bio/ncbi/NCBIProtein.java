/*
 * NCBIProtein.java
 * Created on Feb 10, 2005
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.ncbi;

import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein.Peptide;
import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein.Protein;
import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.taxonomy.Species;

import java.util.Set;


/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Feb 10, 2005
 */

public class NCBIProtein extends Protein {

	
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param peptide The peptide to set.
	 */
	public void setPeptide(NCBIPeptide peptide) {
		this.peptide = peptide;
	}
	
	/**
	 * @param species The species to set.
	 */
	public void setSpecies(Species species) {
		this.species = species;
	}
	
	/**
	 * @param systematicNames The systematicNames to set.
	 */
	public void setSystematicNames(Set<String> systematicNames) {
		this.systematicNames = systematicNames;
	}
	
	/* (non-Javadoc)
	 * @see org.yeastrc.bio.protein.Protein#getPeptide()
	 */
	public Peptide getPeptide() {
		return this.peptide;
	}

	/* (non-Javadoc)
	 * @see org.yeastrc.bio.protein.Protein#getSpecies()
	 */
	public Species getSpecies() {
		return this.species;
	}

	/* (non-Javadoc)
	 * @see org.yeastrc.bio.protein.Protein#getSystematicNames()
	 */
	public Set<String> getSystematicNames() throws Exception {
		return this.systematicNames;
	}

	/* (non-Javadoc)
	 * @see org.yeastrc.bio.protein.Protein#getDescription()
	 */
	public String getDescription() throws Exception {
		return this.description;
	}

	// Instance vars
	private NCBIPeptide peptide;		// The peptide representing this protein's sequence
	private Species species;		// The species for this protein
	private Set<String> systematicNames;	// The gi #'s associated with this protein
	private String description;		// A description of this protein
}
