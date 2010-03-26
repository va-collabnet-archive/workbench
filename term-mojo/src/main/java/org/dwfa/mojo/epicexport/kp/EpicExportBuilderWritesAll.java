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

import java.util.List;

import org.dwfa.mojo.epicexport.AbstractEpicExportBuilder;
import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;
import org.dwfa.mojo.epicexport.I_ExportFactory;

public class EpicExportBuilderWritesAll extends AbstractEpicExportBuilder implements I_EpicLoadFileBuilder {
	
	public String masterfile;
	
	public EpicExportBuilderWritesAll(I_ExportFactory exportManager, EpicExportManager em) {
		super(exportManager, em);
	}

		
	public void writeRecord(String version, List<String> regions) throws Exception {
		
		I_EpicExportRecordWriter writer = getExportManager().getWriter(this.getMasterfile());
		this.setWriter(writer);
		writer.newRecord();
		if (this.anyItemsHaveChanges())
			this.writeLiteralItem("rel_ver", version);
		
		this.writeAll();
		if (regions != null)
			this.writeLiteralItem("300000", stringArrayToList(regions, ","));
		writer.saveRecord();
	}

	public String getMasterfile() {
		return masterfile;
	}

	public void setMasterfile(String masterfile) {
		this.masterfile = masterfile;
	}
	
	
}

