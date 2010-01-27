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
package org.dwfa.mojo.epicexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.dwfa.ace.log.AceLog;
import com.mysql.jdbc.Connection;

/**
 * Class used to manage a list of export writers, and export builders. Used to
 * determine where to place an export. If a particular writer or builder is not present,
 * will create a new instance and place it on the list. A builder is a controlling object that queues
 * up data to export, then uses business logic to determine what to export.  The builder directly
 * controls one or more writers, which is a class that directly does the grunt work of writing to an 
 * export source.
 * 
 * @author Steven Neiner
 * @param baseDir - The location of the exported files
 * @param exportFactory - The factory to produce new writers and builders
 */
public class EpicExportManager {
    // private List<EpicExportWriter> writers;
    // private List<AbstractEpicLoadFileBuilder> builders;
    
    /**
     * The directory where the output will end up
     * 
     * @parameter
     */
	private String baseDir;
    private Connection connection;
    private I_ExportFactory exportFactory;
    private HashMap<String, I_EpicExportRecordWriter> writers = new HashMap<String, I_EpicExportRecordWriter>();
    private HashMap<String, I_EpicLoadFileBuilder> builders = new HashMap<String, I_EpicLoadFileBuilder>();

    public EpicExportManager() {
    	
    }
    public EpicExportManager(String baseDir, I_ExportFactory exportFactory) {
        this.baseDir = baseDir;
        if (!this.baseDir.endsWith("/")) {
            this.baseDir = this.baseDir.concat("/");
        }
        this.exportFactory = exportFactory;
    }

    public EpicExportManager(Connection conn, I_ExportFactory exportFactory) {
        this.connection = conn;
        this.exportFactory = exportFactory;

    }

    
    /**
     * Looks for an existing load file writer that handles the named record
     * type, and creates a new writer if not present.
     * 
     * @param writerName - The name of the writer to look for
     * @return The writer that handles the requested record type
     * @throws Exception
     */
    public I_EpicExportRecordWriter getWriter(String writerName) throws Exception {
        I_EpicExportRecordWriter ret = null;
        ret = writers.get(writerName);
        if (ret == null) {
            ret = exportFactory.getWriter(writerName, this.baseDir, this.connection);
            writers.put(writerName, ret);
        }
        return ret;
    }

    /**
     * Looks for an existing export builder that handles the named record
     * type, and creates a new writer if not present.
     * 
     * @param masterfile - The name of the builder to look for
     * @return The builder that handles the requested record type
     * @throws Exception
     */
    public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile) throws Exception {
        I_EpicLoadFileBuilder ret = builders.get(masterfile);
        if (ret == null) {
        	AceLog.getAppLog().info("Getting builder for masterfile: " + masterfile);        	
            ret = this.exportFactory.getLoadFileBuilder(masterfile, this);
            if (ret != null)
                builders.put(masterfile, ret);
        }
        return ret;
    }

    /**
     * Exports a record to an export target.  Will do this by finding an appropriate builder for the
     * records masterfile, wrting all of the record contents to that builder's queue, then calling
     * the builder's writerecord method.
     * 
     * @param term - The class used to describe an external term record.
     * @throws Exception
     */
    public void exportExternalTermRecord(ExternalTermRecord term) throws Exception {
    	I_EpicLoadFileBuilder builder = getLoadFileBuilder(term.getMasterFileName());
    	if (builder != null) {
    		builder.clearRecordContents();
    		for (ExternalTermRecord.Item i: term.getAllItems()) {
    			builder.addItemForExport(i.getName(), i.getValue(), i.getPreviousValue());
    		}
    		builder.writeRecord(term.getVersion(), term.getRegions());
    	}
    }
    
    /**
     * Used to close files, database connections (if you want) for any export writers.  Optionally
     * pass the buffered writer of a file, and the summary text, a description of work done, will be
     * written to that file.
     * 
     * @param summaryFileBufferedWriter (optional) A description of work done will be
     * written to that file.
     * @throws IOException
     */
    public void close(BufferedWriter summaryFileBufferedWriter) throws IOException {

        for (Iterator<I_EpicExportRecordWriter> i = writers.values().iterator(); i.hasNext();) {
            I_EpicExportRecordWriter w = i.next();
            String text = w.getSummary();
            AceLog.getAppLog().info(text);
            if (summaryFileBufferedWriter != null)
            	summaryFileBufferedWriter.write(text.concat("\r\n"));
            w.close();
        }
    }
    
    public void close() throws IOException {
    	close(null);
    }

    public boolean isEqualOrBothNull(String c1, String c2) {
        boolean ret = false;
        if (c1 == null && c2 == null)
            ret = true;
        else if (c1 != null && c2 != null)
            ret = c1.compareToIgnoreCase(c2) == 0;
        return ret;
    }

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
    
	public void clearAllContents() {
		for (int i = 0; i < builders.size(); i++) {
			
			I_EpicLoadFileBuilder b = builders.get(i);
			if (b != null)
				b.clearRecordContents();
		}
	}

	public String getBaseDir() {
		return baseDir;
	}
	
	
    
}
