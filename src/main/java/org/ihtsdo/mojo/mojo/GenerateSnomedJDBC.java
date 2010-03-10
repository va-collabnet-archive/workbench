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
package org.ihtsdo.mojo.mojo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * Note that SNOMED ids are "unsigned", if we where to convert to integers, or
 * other
 * representation, starting at MinInt + 1, and using MinInt for the equivalent
 * of "null" , might be
 * a good option.
 * 
 * @author kec
 * 
 */
public abstract class GenerateSnomedJDBC extends ProcessSnomedSources {

    private Connection conn;

    private int counter = 0;

    private int countSinceCommit = 0;

    private static int COMMIT_INTERVAL = 5000;

    public void countAndCommit() throws SQLException {
        counter++;
        countSinceCommit++;
        if (countSinceCommit > COMMIT_INTERVAL) {
            countSinceCommit = 0;
            this.conn.commit();
        }
    }

    public abstract String longDataType();

    protected void createTables() throws SQLException {
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table CONCEPTS");
            statement.execute("drop table RELATIONSHIPS");
            statement.execute("drop table DESCRIPTIONS");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        statement.execute(createConceptTableDdl());
        statement.execute(createRelationshipTableDdl());
        statement.execute(createDescriptionTableDdl());
        getLog().info("Created tables");
    }

    private String createConceptTableDdl() {
        StringBuffer ddl = new StringBuffer();
        ddl.append("CREATE table CONCEPTS (");
        ddl.append("ID " + longDataType() + ", ");
        ddl.append("VERSION " + longDataType() + ", ");
        ddl.append("STATUS INTEGER, ");
        ddl.append("DEFINED CHAR, ");
        ddl.append("PRIMARY KEY (ID, VERSION))");
        return ddl.toString();
    }

    private String createRelationshipTableDdl() {
        StringBuffer ddl = new StringBuffer();
        ddl.append("CREATE table RELATIONSHIPS (");
        ddl.append("ID " + longDataType() + ", ");
        ddl.append("VERSION " + longDataType() + ", ");
        ddl.append("C1ID " + longDataType() + ", ");
        ddl.append("TYPEID " + longDataType() + ", ");
        ddl.append("C2ID " + longDataType() + ", ");
        ddl.append("CHARID " + longDataType() + ", ");
        ddl.append("REFID " + longDataType() + ", ");
        ddl.append("ROLEGRP INTEGER, ");
        ddl.append("PRIMARY KEY (ID, VERSION))");
        return ddl.toString();
    }

    private String createDescriptionTableDdl() {
        StringBuffer ddl = new StringBuffer();
        ddl.append("CREATE table DESCRIPTIONS (");
        ddl.append("ID " + longDataType() + ", ");
        ddl.append("VERSION " + longDataType() + ", ");
        ddl.append("STATUS INTEGER, ");
        ddl.append("CID " + longDataType() + ", ");
        ddl.append("INITCAP CHAR, ");
        ddl.append("TYPE INTEGER, ");
        ddl.append("TEXT VARCHAR(1024), ");
        ddl.append("LANG VARCHAR(10), ");
        ddl.append("PRIMARY KEY (ID, VERSION))");
        return ddl.toString();
    }

    public void cleanup() throws Exception {
        conn.commit();
        conn.close();
        getLog().info("Closed database. Added " + counter + " components");
    }

    PreparedStatement conceptStatement;

    public PreparedStatement getConceptStatement() throws SQLException {
        if (conceptStatement == null) {
            conceptStatement = conn.prepareStatement("INSERT INTO CONCEPTS(ID, VERSION, STATUS, DEFINED) VALUES (?, ?, ?, ?)");
        }
        return conceptStatement;
    }

    PreparedStatement descriptionStatement;

    public PreparedStatement getDescriptionStatement() throws SQLException {
        if (descriptionStatement == null) {
            descriptionStatement = conn.prepareStatement("INSERT INTO DESCRIPTIONS(ID, VERSION, STATUS, CID, INITCAP, TYPE, TEXT, LANG) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        }
        return descriptionStatement;
    }

    PreparedStatement relStatement;

    public PreparedStatement getRelStatement() throws SQLException {
        if (relStatement == null) {
            relStatement = conn.prepareStatement("INSERT INTO RELATIONSHIPS (ID, VERSION, C1ID, TYPEID, C2ID, CHARID, REFID, ROLEGRP) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        }
        return relStatement;
    }

    public void writeConcept(Date releaseDate, long conceptKey, int conceptStatus, int defChar) throws IOException,
            SQLException {
        PreparedStatement insert = getConceptStatement();
        insert.setLong(1, conceptKey);
        insert.setLong(2, releaseDate.getTime());
        insert.setInt(3, conceptStatus);
        if (defChar == 1) {
            insert.setString(4, "T");
        } else {
            insert.setString(4, "F");
        }
        insert.execute();
        countAndCommit();
    }

    public void writeRelationship(Date releaseDate, long relID, long conceptOneID, long relationshipTypeConceptID,
            long conceptTwoID, long characteristic, long refinability, int group) throws IOException, SQLException {
        PreparedStatement insert = getRelStatement();
        insert.setLong(1, relID);
        insert.setLong(2, releaseDate.getTime());
        insert.setLong(3, conceptOneID);
        insert.setLong(4, relationshipTypeConceptID);
        insert.setLong(5, conceptTwoID);
        insert.setLong(6, characteristic);
        insert.setLong(7, refinability);
        insert.setInt(8, group);
        insert.execute();
        countAndCommit();

    }

    public void writeDescription(Date releaseDate, long descriptionId, int status, long conceptId, String text,
            int capStatus, int typeInt, String lang) throws IOException, SQLException {
        PreparedStatement insert = getDescriptionStatement();
        insert.setLong(1, descriptionId);
        insert.setLong(2, releaseDate.getTime());
        insert.setInt(3, status);
        insert.setLong(4, conceptId);
        if (capStatus == 1) {
            insert.setString(5, "T");
        } else {
            insert.setString(5, "F");
        }
        insert.setInt(6, typeInt);
        insert.setString(7, text);
        insert.setString(8, lang);
        insert.execute();
        countAndCommit();

    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
