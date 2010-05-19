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
package org.ihtsdo.mojo.maven.derby.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DerbyBackedUuidSctidFixedMap implements Map<UUID, Long> {
    private Connection conn;
    private PreparedStatement containsKey;
    private PreparedStatement containsValue;
    private PreparedStatement getValue;
    private PreparedStatement count;

    public static DerbyBackedUuidSctidFixedMap read(File fixedMapFile) throws IOException, ClassNotFoundException {
        return new DerbyBackedUuidSctidFixedMap(new File(fixedMapFile.getParent(), fixedMapFile.getName() + ".bdb"),
            fixedMapFile);
    }

    private DerbyBackedUuidSctidFixedMap(File dbFolder, File fixedMapFile) throws IOException, ClassNotFoundException {
        super();
        File dbErrLog = new File(dbFolder.getParentFile(), "derbyErr.log");
        dbErrLog.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(dbErrLog);
        fw.append("Created by DerbyBackedUuidSctidMap.\n");
        fw.close();
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        try {
            conn = DriverManager.getConnection("jdbc:derby:directory:" + dbFolder.getCanonicalPath() + ";create=true;");
            conn.setAutoCommit(false);
            conn.createStatement().execute("CREATE TABLE UUID_SCT_MAP (MSB BIGINT, LSB BIGINT, SCTID BIGINT)");
            containsKey = conn.prepareStatement("SELECT COUNT(SCTID) FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
            containsValue = conn.prepareStatement("SELECT COUNT(SCTID) FROM UUID_SCT_MAP WHERE SCTID = ?");
            getValue = conn.prepareStatement("SELECT SCTID FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
            count = conn.prepareStatement("SELECT COUNT(*) FROM UUID_SCT_MAP");
            PreparedStatement insert = conn.prepareStatement("INSERT INTO UUID_SCT_MAP(MSB, LSB, SCTID) VALUES(?, ?, ?)");
            System.out.println("Reading map file: " + fixedMapFile.getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(fixedMapFile));

            String uuidLineStr;
            String sctIdLineStr;

            while ((uuidLineStr = br.readLine()) != null) { // while loop begins
                // here
                sctIdLineStr = br.readLine();
                Long sctId = Long.parseLong(sctIdLineStr);
                for (String uuidStr : uuidLineStr.split("\t")) {
                    UUID uuid = UUID.fromString(uuidStr);
                    insert.setLong(1, uuid.getMostSignificantBits());
                    insert.setLong(2, uuid.getLeastSignificantBits());
                    insert.setLong(3, sctId);
                    insert.execute();
                }
            } // end while
            br.close();
            conn.createStatement().execute("CREATE INDEX UUID_IDX ON UUID_SCT_MAP (MSB, LSB)");
            conn.createStatement().execute("CREATE INDEX SCTID_IDX ON UUID_SCT_MAP (SCTID)");
            conn.commit();
        } catch (SQLException e) {
            toRuntimeException(e);
        }
    }

    private void toRuntimeException(Exception ex) throws RuntimeException {
        throw new RuntimeException(ex);

    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        try {
            UUID uuid = (UUID) key;
            containsKey.setLong(1, uuid.getMostSignificantBits());
            containsKey.setLong(2, uuid.getLeastSignificantBits());
            ResultSet results = containsKey.executeQuery();
            results.next();
            int count = results.getInt(1);
            results.close();
            return count > 0;
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return false;
    }

    public boolean containsValue(Object value) {
        try {
            Long sctid = (Long) value;
            containsValue.setLong(1, sctid);
            ResultSet results = containsValue.executeQuery();
            results.next();
            int count = results.getInt(1);
            results.close();
            return count > 0;
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return false;
    }

    public Set<java.util.Map.Entry<UUID, Long>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public Long get(Object key) {
        try {
            UUID uuid = (UUID) key;
            getValue.setLong(1, uuid.getMostSignificantBits());
            getValue.setLong(2, uuid.getLeastSignificantBits());
            ResultSet results = getValue.executeQuery();
            results.next();
            long sctid = results.getLong(1);
            results.close();
            return sctid;
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return Long.MIN_VALUE;
    }

    public boolean isEmpty() {
        try {
            ResultSet results = count.executeQuery();
            results.next();
            int count = results.getInt(1);
            results.close();
            return count == 0;
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return false;
    }

    public Set<UUID> keySet() {
        throw new UnsupportedOperationException();
    }

    public Long put(UUID key, Long value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends UUID, ? extends Long> t) {
        throw new UnsupportedOperationException();
    }

    public Long remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        try {
            ResultSet results = count.executeQuery();
            results.next();
            int count = results.getInt(1);
            results.close();
            return count;
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return -1;
    }

    public Collection<Long> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (conn != null && conn.isClosed() == false) {
            conn.close();
        }
    }

}
