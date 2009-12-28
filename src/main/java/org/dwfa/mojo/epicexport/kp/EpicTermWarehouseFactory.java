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
package org.dwfa.mojo.epicexport.kp;

import java.sql.DriverManager;
import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;
import org.dwfa.mojo.epicexport.I_ExportFactory;
import org.dwfa.mojo.epicexport.I_ExportValueConverter;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter;

import com.mysql.jdbc.Connection;

public class EpicTermWarehouseFactory implements I_ExportFactory {

	Connection connection;
    public EpicExportManager getExportManager(String baseDir) {
        return new EpicExportManager(baseDir, this);
    }

    public EpicExportManager getExportManager(String dburl, String user, String pw) throws Exception {
    	dburl = new String("jdbc:mysql://").concat(dburl);
    	Class.forName("com.mysql.jdbc.Driver").newInstance();
    	Connection conn = (Connection) DriverManager.getConnection(dburl, user, pw);
    	return new EpicExportManager(conn, this);
    }

    public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile, EpicExportManager em) throws Exception {
        I_EpicLoadFileBuilder ret = new EpicExportBuilderWritesAll(this, em);
        ret.setMasterfile(masterfile);
        return ret;
    }

    
    public I_EpicExportRecordWriter getWriter(String writerName, String baseDir, Connection conn) throws Exception {

    	I_EpicExportRecordWriter ret = new EpicDataWarehouseWriter(writerName, conn);
    	return ret;
    }

    public I_RefsetUsageInterpreter getInterpreter() {
    	return new RefsetUsageInterpreter();
    }
    
    public I_ExportValueConverter getValueConverter(int startingVersion) {
    	return new ExportValueConverter(startingVersion);
    }
}

