package org.yeastrc.ms.service.pepxml;

import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.search.MsSearchDatabaseDAO;
import org.yeastrc.ms.domain.search.MsSearchDatabase;
import org.yeastrc.ms.domain.search.MsSearchDatabaseIn;
import org.yeastrc.ms.parser.fasta.DatabaseHelper;
import org.yeastrc.ms.parser.fasta.UWPRParser;
import org.yeastrc.ms.service.UploadException;
import org.yeastrc.nrseq.dao.NrSeqLookupUtil;

import java.io.File;
import java.sql.Connection;

/**
 * Created by vsharma on 8/1/2017.
 */
public class FastaUploaderTest {

    public static void main(String[] args)
    {
        MsSearchDatabase db = new MsSearchDatabase() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public int getSequenceDatabaseId() {
                return 0;
            }

            @Override
            public String getServerAddress() {
                return null;
            }

            @Override
            public String getServerPath() {
                return "G:\\WORK\\UWPR\\MSDaPl\\github\\msdapl\\MS_LIBRARY\\18mix.fasta";
            }

            @Override
            public String getDatabaseFileName() {
                return "18mix.fasta";
            }
        };

        // get the id of the search database used (will be used to look up protein ids later)
        int sequenceDatabaseId = 0;
        try {
            sequenceDatabaseId = getSearchDatabaseId(db);
        } catch (UploadException e) {
            e.printStackTrace();
            return;
        }

        if (sequenceDatabaseId == 0) {
            // This database does not exist in the database. Parse it and upload it.
            String fastaFile = db.getServerPath();
            File dbFile = new File(fastaFile);
            if(!dbFile.exists())
            {
                UploadException ex = new UploadException(UploadException.ERROR_CODE.SEARCHDB_NOT_FOUND);
                StringBuilder err = new StringBuilder("No database ID found for: ").append(db.getDatabaseFileName()).append(". ");
                err.append("Attempting to parse fasta file ").append(fastaFile);
                err.append(" Fasta file not found.");
                ex.setErrorMessage(err.toString());
                ex.printStackTrace();
                return;
            }

            try
            {
                System.out.println("Parsing FASTA file " + fastaFile);
                UWPRParser fastaParser = new UWPRParser();
                Connection conn = DatabaseHelper.getConnection();
                fastaParser.setConnection(conn);
                fastaParser.parseFASTA(dbFile, true); // Do a dry run first

                try {conn.close();}catch(Exception e){System.out.println("Error closing connection. " + e);}

                int noSpeciesFoundCount = fastaParser.getNoSpeciesFoundCount();
                if(noSpeciesFoundCount == 0)
                {
                    conn = DatabaseHelper.getConnection();
                    conn.setAutoCommit(false);
                    fastaParser.setConnection(conn);
                    fastaParser.parseFASTA(dbFile, false);
                    conn.commit();

                    try {conn.close();}catch(Exception e){System.out.println("Error closing connection. " + e);}
                }
                else
                {
                    System.out.println("No species found for " + fastaParser.getNoSpeciesFoundCount());
                }

                // Get the database ID of the FASTA file just uploaded
                sequenceDatabaseId = getSearchDatabaseId(db);
                System.out.println("Uploaded fasta database. YRC_NRSEQ id: " + sequenceDatabaseId);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static int getSearchDatabaseId(MsSearchDatabaseIn db) throws UploadException {
        String searchDbName = null;
        int dbId = 0;
        if (db != null) {

            DAOFactory daoFactory = DAOFactory.instance();
            MsSearchDatabaseDAO sequenceDbDao = daoFactory.getMsSequenceDatabaseDAO();
            // look in the msSequenceDatabaseDetail table first. We might already have this
            // database in there
            dbId = sequenceDbDao.getSequenceDatabaseId(db.getServerPath());
            if(dbId == 0) {
                searchDbName = db.getDatabaseFileName();
                dbId = NrSeqLookupUtil.getDatabaseId(searchDbName);
            }
        }
        return dbId;
    }
}