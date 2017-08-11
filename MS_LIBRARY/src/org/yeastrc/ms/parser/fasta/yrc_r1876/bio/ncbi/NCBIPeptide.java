/*
 * NCBIPeptide.java
 * Created on Feb 10, 2005
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.ncbi;

import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein.Peptide;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Feb 10, 2005
 */

public class NCBIPeptide extends Peptide {

	/* (non-Javadoc)
	 * @see org.yeastrc.bio.protein.Peptide#getMolecularWeight()
	 */
	public double getMolecularWeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.yeastrc.bio.protein.Peptide#getPI()
	 */
	public double getPI() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.yeastrc.bio.sequence.Sequence#getSequenceString()
	 */
	public String getSequenceString() {
		// TODO Auto-generated method stub
		return this.sequence;
	}
	
	/**
	 * Set the sequence for this Peptide
	 * @param seq
	 */
	public void setSequenceString(String seq) {
		this.sequence = seq;
	}

	private String sequence;
	
}
