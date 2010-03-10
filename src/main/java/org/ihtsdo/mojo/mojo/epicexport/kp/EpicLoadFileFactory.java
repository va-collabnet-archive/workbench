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
package org.ihtsdo.mojo.mojo.epicexport.kp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.mojo.mojo.epicexport.EpicExportManager;
import org.ihtsdo.mojo.mojo.epicexport.EpicExportWriter;
import org.ihtsdo.mojo.mojo.epicexport.I_EpicExportRecordWriter;
import org.ihtsdo.mojo.mojo.epicexport.I_EpicLoadFileBuilder;
import org.ihtsdo.mojo.mojo.epicexport.I_ExportFactory;
import org.ihtsdo.mojo.mojo.epicexport.I_ExportValueConverter;
import org.ihtsdo.mojo.mojo.epicexport.I_RefsetUsageInterpreter;

import com.mysql.jdbc.Connection;

public class EpicLoadFileFactory implements I_ExportFactory {
	public static final String EPIC_MASTERFILE_NAME_EDG_BILLING = "edgbilling";
	public static final String EPIC_MASTERFILE_NAME_EDG_CLINICAL = "edgclinical";
	
	public static final String EPIC_MASTERFILE_NAME_WILDCARD = "*";
	public static final String DISPLAYNAME_ITEM = "2";
	
 	/**
 	 * The directory to output the load files to
 	 *
 	 * @parameter
 	 * @required
 	 */
	private String baseDir;

    public EpicExportManager getExportManager() {
        return new EpicExportManager(this.baseDir, this);
    }

    public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile, EpicExportManager em) throws Exception {
        I_EpicLoadFileBuilder ret;
        if (masterfile.equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_CLINICAL))
            ret = new EpicExportBuilderEDGClinical(this, em); 
        else if (masterfile.equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_BILLING))
            ret = new EpicExportBuilderEDGBilling(this, em);
        /* Sample usage of generic builder, used to limit need of separate classes for simple master files
        else if (masterfile.equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_CLINICAL)) {
        	ret = new EpicExportBuilderGeneric(this, em, "edgclinical");
        	ret.setExportIfTheseItemsChanged("2", "100", "50", "80", "91", "207", "7000", "7010");
        	ret.setAlwaysWriteTheseItemsForNewRecord("200", "40");
        	ret.setAlwaysWriteTheseItemsForExistingRecord("200", "40");
        	ret.setItemsToWriteIfChanged("2", "100", "50", "80", "91", "207", "200", "2000", "7000", "7010");
        	ret.setMandatoryItems("2", "40", "7000", "7010", "91", "80", "100", "207");
        }
        */
        else {
        	AceLog.getAppLog().warning("Using generic loadfile builder for unknown master file: " + masterfile);
        	ret = new EpicExportBuilderWritesAll(this, em);
        	ret.setMasterfile(masterfile);
        }
        return ret;
    }

    public I_EpicExportRecordWriter getWriter(String writerName, String baseDir, Connection conn) throws Exception {
        StringBuffer filename = new StringBuffer(baseDir);
        filename.append(writerName);
        filename.append(".txt");
        System.out.println("Creating " + filename.toString());
        File fw = new File(filename.toString());
        fw.mkdirs();
        if (fw.exists())
            fw.delete();

    	BufferedWriter bw = new BufferedWriter(new FileWriter(fw));
        
        EpicExportWriter ret = new EpicExportWriter(bw, fw.getName());
        return ret;
    }
    
    public I_RefsetUsageInterpreter getInterpreter() {
    	return new RefsetUsageInterpreter();
    }
    
    public I_ExportValueConverter getValueConverter(int startingVersion) {
    	return new ExportValueConverter(startingVersion);
    }
}
