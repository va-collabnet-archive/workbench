package org.dwfa.mojo.relformat.mojo.db;

import org.apache.maven.plugin.logging.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DerbyFileRunnerImpl implements DerbyFileRunner {

    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    private final Log logger;
    private final boolean verbose;
    private Connection connection;
    private Statement statement;


    public DerbyFileRunnerImpl(final Log log, final boolean verbose) {
        logger = log;
        this.verbose = verbose;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new DerbyFileRunnerException(e);
        }
    }

    public void connect(final String databaseName) {
        try {
            String url = "jdbc:derby:" + databaseName + ";create=true;";
            recordUrl(url);
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new DerbyFileRunnerException(e);
        }
    }

    private void recordUrl(final String url) {
        if (verbose) {
            logger.info("Connecting with url: " + url);
        }
    }

    public void run(final String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new DerbyFileRunnerException(e);
        }
    }

    public void disconnect() {
        try {
            statement.close();
            connection.close();
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            logger.info(e.getMessage());
            //A clean shutdown always throws SQL exception XJ015, which can be ignored.
            //http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
        }        
    }
}
