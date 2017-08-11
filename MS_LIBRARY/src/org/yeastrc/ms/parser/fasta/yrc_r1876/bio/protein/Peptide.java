package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein;

import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.sequence.Sequence;

/**
 * Describes the interface to a Peptide
 *
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Sep 30, 2004
 */

public abstract class Peptide extends Sequence {

    /**
     * Get the molecular weight of this peptide sequence
     * @return The molecular weight of this peptide sequence
     */
    public abstract double getMolecularWeight();

    /**
     * Get the pI of this peptide sequence
     * @return The pI of this peptide sequence
     */
    public abstract double getPI();

    /**
     * Get the peptide sequence formatted in standard way, fit for display on a fixed-width font
     * @return
     */
    public String getSequenceStringFormatted() {
        String retStr =    "      1          11         21         31         41         51         \n";
        retStr +=        "      |          |          |          |          |          |          \n";
        retStr +=        "    1 ";

        char[] residues = this.getSequenceString().toCharArray();
        int counter = 0;

        for (int i = 0; i < residues.length; i++ ) {
            retStr += residues[i];

            counter++;
            if (counter % 60 == 0) {
                if (counter < 1000) retStr += " ";
                if (counter< 100) retStr += " ";

                retStr += String.valueOf(counter);
                retStr += "\n ";

                if (counter < 100) retStr += " ";
                if (counter < 1000) retStr += " ";
                retStr += String.valueOf(counter + 1) + " ";

            } else if (counter % 10 == 0) {
                retStr += " ";
            }

        }

        return retStr;
    }

}
