package org.dwfa.maven.derby;

import org.apache.derby.tools.ij;
import org.apache.maven.plugin.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DerbyClientImpl implements DerbyClient {

    private Connection connection;
    private final String databaseLocation;
    private final String errorLogPath;
    private Log logger;

    public DerbyClientImpl(final String databaseLocation, final String errorLogPath, final Log logger) {
        this.databaseLocation = databaseLocation;
        this.errorLogPath = errorLogPath;
        this.logger = logger;
    }
    
    public void openConnection() {
        System.getProperties().setProperty("derby.infolog.append", "true");
        System.getProperties().setProperty("derby.stream.error.file", errorLogPath);
        System.getProperties().setProperty("derby.system.durability", "test");
        System.setProperty("derby.system.home", "./target");

        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            connection = DriverManager.getConnection("jdbc:derby:directory:" + databaseLocation + ";create=true;");
        } catch (Exception e) {
            logAndThrow(e);
        }
    }

    public void executeScript(final String fileName) {
        executeScript(fileName, false);
    }

    public void executeScript(final String fileName, final boolean verbose) {
        try {
            InputStream in = new FileInputStream(fileName);
            String inputEncoding = "US-ASCII";
            //possible memory probs for large files.            
            OutputStream out = (verbose) ? new ByteArrayOutputStream() : new NullOuputStream();
            String outputEncoding = null;
            int errors = ij.runScript(connection, in, inputEncoding, out, outputEncoding);
            dumpSQLIfVerbose(out, verbose);
            logAndThowIfErrors(errors);
        } catch (IOException e) {
            logAndThrow(e);
        }
    }

    private void dumpSQLIfVerbose(final OutputStream sqlOut, final boolean verbose) {
        if (verbose) {
            logger.info(sqlOut.toString());
        }
    }

    public void closeConnection() {
        try {
            connection.close();
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            logger.info(e.getMessage());
            //A clean shutdown always throws SQL exception XJ015, which can be ignored.
            //http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
        }
    }

    private void logAndThowIfErrors(final int errors) {
        if (errors > 0) {
            logAndThrow("Number of script execution errors: " + errors);
        }
    }

    private void logAndThrow(final String message) {
        logger.error(message);
        throw new DerbyClientException(message);
    }

    private void logAndThrow(final Exception e) {
        logger.error(e);
        throw new DerbyClientException(e);
    }
}
