/**
 * DatabaseProperties.java
 * @author Vagisha Sharma
 * Jun 25, 2011
 */
package org.yeastrc.ms.parser.fasta;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * 
 */
public class DatabaseProperties {


	private static final Logger log = Logger.getLogger(DatabaseProperties.class);

	private static String DB_NRSEQ_HOST;
	private static String DB_NRSEQ_NAME;
	private static String DB_NRSEQ_USER;
	private static String DB_NRSEQ_PASSWD;
	private static String DB_NCBI_TAX_HOST;
	private static String DB_NCBI_TAX_NAME;
	private static String DB_NCBI_TAX_USER;
	private static String DB_NCBI_TAX_PASSWD;
	private static String DB_MAINDB_HOST;
	private static String DB_MAINDB_NAME;
	private static String DB_MAINDB_USER;
	private static String DB_MAINDB_PASSWD;
	
	
	static {
		String file = "fasta_parser.properties";
		
		Properties props = new Properties();
		InputStream is = null;
		try {
			
			is = new FileInputStream(file);
			props.load(is);
			

			DB_NRSEQ_HOST = props.getProperty("db.nrseq.host", "localhost");
			DB_NRSEQ_NAME = props.getProperty("db.nrseq.name", "YRC_NRSEQ");
			DB_NRSEQ_USER = props.getProperty("db.nrseq.username", "root");
			DB_NRSEQ_PASSWD = props.getProperty("db.nrseq.password", "");
			
			DB_NCBI_TAX_HOST = props.getProperty("db.ncbi_tax.host", "localhost");
			DB_NCBI_TAX_NAME = props.getProperty("db.ncbi_tax.name", "NCBI");
			DB_NCBI_TAX_USER = props.getProperty("db.ncbi_tax.username", "root");
			DB_NCBI_TAX_PASSWD = props.getProperty("db.ncbi_tax.password", "");
			
			DB_MAINDB_HOST = props.getProperty("db.maindb.host", "localhost");
			DB_MAINDB_NAME = props.getProperty("db.maindb.name", "yrc");
			DB_MAINDB_USER = props.getProperty("db.maindb.username", "root");
			DB_MAINDB_PASSWD = props.getProperty("db.maindb.password", "");

		}
		catch (IOException e) {
			log.error("Error reading properties file "+file, e);
		}
		finally {
			if(is != null) try {is.close();} catch(IOException e){}
		}

	}

	private DatabaseProperties() {}

	public static String getNrseqDbHost() {
		return DB_NRSEQ_HOST;
	}
	
	public static String getNrseqDbName() {
		return DB_NRSEQ_NAME;
	}
	
	public static String getNrseqDbUser() {
		return DB_NRSEQ_USER;
	}
	
	public static String getNrseqDbPassword() {
		return DB_NRSEQ_PASSWD;
	}

	public static String getNcbiTaxDbHost() {
		return DB_NCBI_TAX_HOST;
	}

	public static String getNcbiTaxDbName() {
		return DB_NCBI_TAX_NAME;
	}

	public static String getNcbiTaxDbUser() {
		return DB_NCBI_TAX_USER;
	}

	public static String getNcbiTaxDbPassword() {
		return DB_NCBI_TAX_PASSWD;
	}
	
	public static String getMainDbHost() {
		return DB_MAINDB_HOST;
	}

	public static String getMainDbName() {
		return DB_MAINDB_NAME;
	}

	public static String getMainDbUser() {
		return DB_MAINDB_USER;
	}

	public static String getMainDbPassword() {
		return DB_MAINDB_PASSWD;
	}
	
	
}
