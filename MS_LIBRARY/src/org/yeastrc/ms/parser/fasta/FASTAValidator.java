/*
 * FASTAValidator.java
 * Created on Aug 29, 2006
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.ms.parser.fasta;

import java.util.Arrays;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Aug 29, 2006
 */

public class FASTAValidator {

	private static final String[] validProteinResidues = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			                                               "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	
	/**
	 * Check supplied sequence to see if it is a valid protein sequence
	 * according to the FASTA format definition
	 * @param sequence
	 * @return
	 */
	public static boolean validProteinSequence(String sequence) throws Exception {

		/*
			A  alanine                         P  proline
		    B  aspartate or asparagine         Q  glutamine
		    C  cystine                         R  arginine
		    D  aspartate                       S  serine
		    E  glutamate                       T  threonine
		    F  phenylalanine                   U  selenocysteine
		    G  glycine                         V  valine
		    H  histidine                       W  tryptophan
		    I  isoleucine                      Y  tyrosine
		    K  lysine                          Z  glutamate or glutamine
		    L  leucine                         X  any
		    M  methionine                      *  translation stop
		    N  asparagine                      -  gap of indeterminate length
		    J  
		 */
		
		//String[] residues = sequence.split("");
		
		char[] residues = sequence.toCharArray();
		
		for (int i = 0; i < residues.length; i++) {
			if (Arrays.binarySearch( validProteinResidues, String.valueOf(residues[i])) < 0) {
				if (residues[i] != '*' && residues[i] != '-' )
					throw new Exception ( "Invalid sequence.  Invalid residue was: '" + residues[i] + "' in sequence:\n" + sequence );
					//return false;
			}
		}
		
		return true;
	}
	
}
