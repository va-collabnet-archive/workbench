/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            // A clean shutdown always throws SQL exception XJ015, which can be
            // ignored.
            // http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
        }
    }
}
