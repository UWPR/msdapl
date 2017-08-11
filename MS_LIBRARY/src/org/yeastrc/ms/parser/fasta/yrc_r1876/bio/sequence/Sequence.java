package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.sequence;

/**
        * Interface describes a sequence of any kind, including protein or nucleotide
        *
        * @author Michael Riffle <mriffle@u.washington.edu>
        * @version Sep 30, 2004
        */

public abstract class Sequence {

    /**
     * The length of the sequence.
     * @return The length of the sequence (number of residues)
     */
    public int getLength() {
        return this.getSequenceString().length();
    }

    /**
     * Returns a formatted sequence string formatted as HTML... that is with a &lt;BR&gt; every 60th place.
     * @return
     */
    public String getSequenceHTMLFormatted() {
        String retString = "";

        String[] residues = this.getSequenceString().split("");
        for (int i = 0; i < residues.length; i++) {
            if (i % 60 == 0 && i != 0)
                retString += "<BR>";
            retString += residues[i];
        }

        return retString;
    }

    /**
     * Return the String representation of the sequence, e.g.: ATTGACC... OR MIERFLLEFLCH...
     * @return The String representation of this sequence.
     */
    public abstract String getSequenceString();

}