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

import java.util.List;

import org.ihtsdo.mojo.mojo.epicexport.AbstractEpicExportBuilder;
import org.ihtsdo.mojo.mojo.epicexport.EpicExportManager;
import org.ihtsdo.mojo.mojo.epicexport.I_EpicExportRecordWriter;
import org.ihtsdo.mojo.mojo.epicexport.I_EpicLoadFileBuilder;

public class EpicExportBuilderEDGBilling extends AbstractEpicExportBuilder implements I_EpicLoadFileBuilder {
	public static final int DISPLAY_NAME = 2;
	public static final int ITEM_11 = 11;
	public static final String[] INTERESTED_ITEMS = {"2", "40", "40", "46", "47", "91", "207", "2000", "7000"};
	public static final String[] EREC_ITEMS = {"50", "7010"};
	
	private String masterfile = EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_BILLING;
	
	public EpicExportBuilderEDGBilling(EpicLoadFileFactory exportManager, EpicExportManager em) {
		super(exportManager, em);
		this.setExportIfTheseItemsChanged(INTERESTED_ITEMS);
	}

	public void writeRecord(String version, List<String> regions) throws Exception {
		
		if (this.isChangedRecord()) {
			if (getFirstItem("11") == null) {
				//"NRNC" Its a new record
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "nrnc"));
				this.setWriter(writer);
				// if (writer.getRecordsWrittenCount() == 0)
				//	writer.writeLine("#HELLO WORLD");
				writer.newRecord();
				writeLiteralItem("1", "");
				writeItem("2");
				writeItem("35");
				writeItem("40");
				writeItem("2000");
				writeItem("20");
				writeItem("46");
				writeItem("47");
				writeItem("91");
				writeItem("7000");
				writeItem("2000");
				writeItem("207");
				writer.saveRecord();
				
			}
			else {
				if (this.onlyHasChangesIn(EREC_ITEMS)) {
					// "EREC" Existing record existing contact
					I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "erec"));
					this.setWriter(writer);
					writer.newRecord();
					writeItem("11", "1");
					writeItem("2");
					writer.saveRecord();
				} else {
					// "ERNC" Existing record new contact
					I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "ernc"));
					this.setWriter(writer);
					writer.newRecord();
					writeItem("2");
					writeItem("11", "1");
					writeItem("40");
					writeItemIfChanged("20");
					writeItemIfChanged("46");
					writeItemIfChanged("47");
					writeItemIfChanged("91");
					writeItemIfChanged("7000");
					writeItemIfChanged("2000");
					writeItemIfChanged("207");
					writer.saveRecord();
				}
			}
		}
	}
	
	private String getWriterName(String masterfile, String version, String contact) {
		StringBuffer ret = new StringBuffer(masterfile);
		if (version != null) {
			ret.append('_');
			ret.append(version);
		}
		if (contact != null) {
			ret.append('_');
			ret.append(contact);
		}
		return ret.toString();
	}
}

