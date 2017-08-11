package org.yeastrc.ms.parser.fasta.yrc_r1876.bio.taxonomy;

import java.sql.SQLException;

/**
 * Encapulates data and information relating to the simple taxanomic information
 * for a Species.
 *
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Sep 30, 2004
 */

public class Species {

    /**
     * Returns true if the NCBI Taxonomy ID of the two species is the same.
     */
    public boolean equals(Object o) {
        if ( ((Species)o).getId() == this.getId() ) return true;
        return false;
    }

    /**
     * Returns the NCBI Taxnonomy ID for this species
     * @return
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns the name of this species.
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get an instance of a Species w/ the supplied species ID (NCBI Taxonomy ID)
     * @param speciesID
     * @return
     * @throws SQLException
     */
    public static Species getInstance( int speciesID ) throws SQLException {
        Species sp = new Species();
        sp.setId( speciesID );
        return sp;
    }

    /**
     * Set the NCBI ID of this species.
     * @param id The NCBI ID of this species.
     * @throws SQLException If there was a database problem looking up the name.
     */
    public void setId(int id) throws SQLException {

        // Check the id for a name.
        TaxonomySearcher ts = TaxonomySearcher.getInstance();
        this.name = ts.getName(id);
        this.id = id;
    }

    /**
     * Constructor!
     */
    public Species() {
        this.name = null;
        this.id = 0;
    }

    // Instance vars
    private String name;
    private int id;

}