/**
 * ProteinDatabaseId.java
 * @author Vagisha Sharma
 * Jul 13, 2011
 */
package org.yeastrc.ms.parser.fasta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * 
 */
public class ProteinDatabaseId {

	private static int sgdDatabaseId;
	private static int wormbaseDatabaseId;
	private static int flybaseDatabaseId;
	private static int swissprotDatabaseId;
	
	private ProteinDatabaseId() {}
	
	public static int get(ProteinDatabaseName name) throws Exception {
		
		if(name == ProteinDatabaseName.SGD) {
			return getSgdDatabaseId();
		}
		else if(name == ProteinDatabaseName.WORMBASE) {
			return getWormbaseDatabaseId();
		}
		else if(name == ProteinDatabaseName.FLYBASE) {
			return getFlybaseDatabaseId();
		}
		else if(name == ProteinDatabaseName.SWISSPROT) {
			return getSwissprotDatabaseId();
		}
		else
			throw new Exception("Don't know how to get database ID for "+name.getDbName());
		
	}
	
	private static int getDatabaseId(ProteinDatabaseName name) throws SQLException, ClassNotFoundException {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "SELECT id FROM tblDatabase where name=\""+name.getDbName()+"\"";
			conn = DatabaseHelper.getNrseqConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next()) {
				return rs.getInt("id");
			}
			return 0;
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	private static int getSgdDatabaseId() throws SQLException, ClassNotFoundException {
		if(sgdDatabaseId == 0) {
			sgdDatabaseId = getDatabaseId(ProteinDatabaseName.SGD);
		}
		return sgdDatabaseId;
	}
	
	private static int getWormbaseDatabaseId() throws SQLException, ClassNotFoundException {
		if(wormbaseDatabaseId == 0) {
			wormbaseDatabaseId = getDatabaseId(ProteinDatabaseName.WORMBASE);
		}
		return wormbaseDatabaseId;
	}
	
	private static int getFlybaseDatabaseId() throws SQLException, ClassNotFoundException {
		if(flybaseDatabaseId == 0) {
			flybaseDatabaseId = getDatabaseId(ProteinDatabaseName.FLYBASE);
		}
		return flybaseDatabaseId;
	}
	
	private static int getSwissprotDatabaseId() throws SQLException, ClassNotFoundException {
		if(swissprotDatabaseId == 0) {
			swissprotDatabaseId = getDatabaseId(ProteinDatabaseName.SWISSPROT);
		}
		return swissprotDatabaseId;
	}
	
//	public static void main(String[] args) throws Exception {
//		
//		for(ProteinDatabaseName name: ProteinDatabaseName.values()) {
//			System.out.println(name.getDbName()+"  "+ProteinDatabaseId.get(name));
//		}
//	}
}
