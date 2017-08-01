/**
 * DatabaseName.java
 * @author Vagisha Sharma
 * Jul 8, 2011
 */
package org.yeastrc.ms.parser.fasta;

/**
 * !!!! NOTE: This file should be kept in sync with the the YRC_BioDb_Builders project
 *            org.yeastrc.db.ProteinDatabasName
 */
public enum ProteinDatabaseName {

	SGD ("SGD"),
	FLYBASE("FlyBase"),
	WORMBASE("WormBase"),
	SWISSPROT("Swiss-Prot"),
	ECOGENE("EcoGene");
	
	private String name;
	
	private ProteinDatabaseName(String name) {
		this.name = name;
	}
	
	public String getDbName() {
		return name;
	}
}
