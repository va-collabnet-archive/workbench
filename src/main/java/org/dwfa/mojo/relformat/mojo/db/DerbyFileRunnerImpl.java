package org.dwfa.mojo.relformat.mojo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DerbyFileRunnerImpl implements DerbyFileRunner {

    private Connection connection;
    private Statement statement;


    public DerbyFileRunnerImpl(final String driver) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new DerbyFileRunnerException(e);
        }
    }

    public void connect(final String url) {
        try {
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new DerbyFileRunnerException(e);
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
        } catch (SQLException e) {
            throw new DerbyFileRunnerException(e);
        }
    }
}
