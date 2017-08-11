package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.protein;

/*
 * IProtein.java
 * Created on Sep 30, 2004
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

import org.yeastrc.ms.parser.fasta.yrc_r1876.bio.taxonomy.Species;

import java.util.Set;

/**
 * Describes a Protein in the context of a the YRC API.
 * <p>A Protein is meant to describe the properties of an accepted or predicted
 * and complete list of amino acids that produce a functional product--in a specific
 * species.  So two equal protein sequences that describe protein in different
 * species will not be equal... their their sequence properties will be identical.  However,
 * they will most likely have different names, and so forth.
 *
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Sep 30, 2004
 */

public abstract class Protein {

    /**
     * Get the hashcode of this protein.
     */
    public int hashCode() {
        return ( this.getPeptide().getSequenceString() +
                String.valueOf(this.getSpecies().getId())).hashCode();
    }

    /**
     * Will be true if the peptide sequence and species are the same.
     */
    public boolean equals(Object o) {
        Protein po = (Protein)o;
        if (po.getSpecies().equals(this.getSpecies()) &&
                po.getPeptide().equals(this.getPeptide()))
            return true;

        return false;
    }

    /**
     * Return the Peptide sequence object for this Protein, which provides
     * access to the actual sequence string, as well as properites that pertain
     * to this sequence.
     * @return The peptide sequence object for this Protein.
     */
    public abstract Peptide getPeptide();

    /**
     * Gets the species for this protein.
     * @return The species for this protein.
     */
    public abstract Species getSpecies();

    /**
     * Gets the systematic name for this protein.
     * @return The systematic name for this protein.
     */
    public abstract Set<String> getSystematicNames() throws Exception;

    /**
     * Get a String (human readable) description of this protein.
     * @return A description of this protein.
     * @throws Exception
     */
    public abstract String getDescription() throws Exception;
}