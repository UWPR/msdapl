/**
 * DatabaseHelper.java
 * @author Vagisha Sharma
 * Jun 25, 2011
 */
package org.yeastrc.ms.parser.fasta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 */
public class DatabaseHelper {

	private DatabaseHelper() {}
	
	
	private static String getNrseqUrl(boolean jdbcCompliantTruncation) {
		
		if(jdbcCompliantTruncation)
			return "jdbc:mysql://"+DatabaseProperties.getNrseqDbHost()+"/"+DatabaseProperties.getNrseqDbName()+
					"?autoReconnect=true";
		else
			return "jdbc:mysql://"+DatabaseProperties.getNrseqDbHost()+"/"+DatabaseProperties.getNrseqDbName()+
					"?autoReconnect=true&jdbcCompliantTruncation=false";
	}
	
	private static String getNcbiTaxUrl(boolean jdbcCompliantTruncation) {
		
		if(jdbcCompliantTruncation)
			return "jdbc:mysql://"+DatabaseProperties.getNcbiTaxDbHost()+"/"+DatabaseProperties.getNcbiTaxDbName()+
					"?autoReconnect=true";
		else
			return "jdbc:mysql://"+DatabaseProperties.getNcbiTaxDbHost()+"/"+DatabaseProperties.getNcbiTaxDbName()+
					"?autoReconnect=true&jdbcCompliantTruncation=false";
	}
	
	
	// ---------------------------------------------------------------------------------------
	// Connection to the YRC_NRSEQ database
	// ---------------------------------------------------------------------------------------
	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		
		return getConnection(true);
	}
	
	public static Connection getConnection(boolean jdbcCompliantTruncation) throws SQLException, ClassNotFoundException {
		
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(getNrseqUrl(jdbcCompliantTruncation), 
											DatabaseProperties.getNrseqDbUser(), DatabaseProperties.getNrseqDbPassword());
		
	}
	
	public static Connection getNrseqConnection() throws SQLException, ClassNotFoundException {
		
		return getNrseqConnection(true);
	}
	
	public static Connection getNrseqConnection(boolean jdbcCompliantTruncation) throws SQLException, ClassNotFoundException {
		
		return getConnection(jdbcCompliantTruncation);
	}
	
	// ---------------------------------------------------------------------------------------
	// Connection to the database that has the NCBI_Taxonomy table
	// ---------------------------------------------------------------------------------------
	public static Connection getNcbiTaxConnection() throws SQLException, ClassNotFoundException {
		return getNcbiTaxConnection(true);
	}
	
	public static Connection getNcbiTaxConnection(boolean jdbcCompliantTruncation) throws SQLException, ClassNotFoundException {
		
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(getNcbiTaxUrl(jdbcCompliantTruncation), 
											DatabaseProperties.getNcbiTaxDbUser(), DatabaseProperties.getNcbiTaxDbPassword());
	}
}
