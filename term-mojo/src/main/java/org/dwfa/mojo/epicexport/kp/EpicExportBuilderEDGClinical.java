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

import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.AbstractEpicExportBuilder;
import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;
import org.dwfa.mojo.epicexport.I_ExportFactory;

public class EpicExportBuilderEDGClinical extends AbstractEpicExportBuilder implements I_EpicLoadFileBuilder {
	public static final int DISPLAY_NAME = 2;
	public static final int ITEM_11 = 11;
	public static final String[] INTERESTED_ITEMS = {"2", "40", "50", "100", "2000", "200", "7010", "7000", "80", "91", "207"};
	public static final String[] EREC_ITEMS = {"50", "7010"};
	public static final String[] MANDATORY_ITEMS = {"2", "40", "7000", "7010", "91", "80", "100", "207"};
	
	public String masterfile = EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL;
	
	public EpicExportBuilderEDGClinical(I_ExportFactory factory, EpicExportManager em) {
		super(factory, em);
		this.setExportIfTheseItemsChanged(INTERESTED_ITEMS);
	}

	public String getEpicItemNumber(int refsetNumber) {
		// TODO just for now return the same number
		return new Integer(refsetNumber).toString();
	}
	
	public void writeRecord(String version, List<String> regions) throws Exception {
		
		if (this.isChangedRecord()) {
			if (getFirstItem("11") == null) {
				//"NRNC" Its a new record
				if (! this.allItemsArePopulated(MANDATORY_ITEMS))
					AceLog.getAppLog().warning("One or more mandatory items are missing for record " + super.toString());
				//	throw new Exception("One or more mandatory items are missing");
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "nrnc"));
				this.setWriter(writer);
				writer.newRecord();
				writeLiteralItem("1", "");
				writeItems("2", "35", "40");
				// writeLiteralItem("35x", this.getParentUUID());
				if (this.itemIsPopulated("2000"))
					writeItem("2000");
				else if (this.itemIsPopulated("200"))
					writeItem("200");
				/* else
					throw new Exception("Missing both items 200 and 2000");
					*/
				writeItemsIfChanged("50", "80", "91", "100", "207", "7000", "7010");
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
					writeLiteralItem("20", "L");
					writeItem("40");
					// writeLiteralItem("35x", this.getParentUUID());
					writeItem("35");
					writeItemIfChanged("100");
					if (this.itemIsPopulated("2000"))
						writeItem("2000");
					else if (this.itemIsPopulated("200"))
						writeItem("200");
					writeItemIfChanged("207");
					writeItemIfChanged("50");
					writeItemIfChanged("80");
					writeItemIfChanged("91");
					writeItemIfChanged("7000");
					writeItemIfChanged("7010");
					writer.saveRecord();
				} else {
					// "ERNC" Existing record new contact
					I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "ernc"));
					this.setWriter(writer);
					writer.newRecord();
					writeItem("2");
					writeItem("11", "1");
					if (this.itemIsPopulated("2000"))
						writeItem("2000");
					else if (this.itemIsPopulated("200"))
						writeItem("200");
					// writeLiteralItem("35x", this.getParentUUID());
					writeItem("35");
					writeItem("40");
					writeItemIfChanged("100");
					writeItemIfChanged("207");
					writeItemIfChanged("50");
					writeItemIfChanged("80");
					writeItemIfChanged("91");
					writeItemIfChanged("7000");
					writeItemIfChanged("7010");
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