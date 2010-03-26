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
import org.dwfa.mojo.epicexport.EpicExportWriter;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;
import org.dwfa.mojo.epicexport.I_ExportFactory;

public class EpicExportBuilderEDGClinical extends AbstractEpicExportBuilder implements I_EpicLoadFileBuilder {
	public static final int DISPLAY_NAME = 2;
	public static final int ITEM_11 = 11;
	public static final String[] INTERESTED_ITEMS = {"2", "40", "50", "100", "2000", "200", "7010", "7000", "80", "91", "207"};
	public static final String[] EREC_ITEMS = {"5", "50", "7010"};
	public static final String[] MANDATORY_ITEMS = {"2", "40", "7000", "7010", "91", "80", "100", "207"};
	
	public String masterfile = EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_CLINICAL;
	
	public EpicExportBuilderEDGClinical(I_ExportFactory factory, EpicExportManager em) {
		super(factory, em);
		this.setExportIfTheseItemsChanged(INTERESTED_ITEMS);
	}

	public void writeRecord(String version, List<String> regions) throws Exception {
		
		if (this.isChangedRecord()) {
			if (hasItem("11") == false) {
				//"NRNC" Its a new record
				addErrorIfTrue(! this.allItemsArePopulated(MANDATORY_ITEMS),
						"One or more mandatory items are missing");
				addErrorIfTrue(!this.itemIsPopulated("2000") && !this.itemIsPopulated("200"),
					"Missing both item 200 and 2000");
				
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "nrnc"));
				this.setWriter(writer);
				writer.newRecord();
				if (writer.getRecordsWrittenCount() == 0)
					writeHeader();
				writeAnyErrors();
				writeLiteralItem("1", "");
				writeItems("2", "35", "40");
				// writeLiteralItem("35x", this.getParentUUID());
				if (this.itemIsPopulated("2000"))
					writeItem("2000");
				else if (this.itemIsPopulated("200"))
					writeItem("200");
				writeItemsIfChanged("50", "80", "91", "100", "207", "7000", "7010");
				// writeItem("rootuuid");			
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
					writeItem("5");
					writeLiteralItem("20", "L");
					writeItem("40");
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
					// writeItem("rootuuid");	
					writer.saveRecord();
				} else {
					// "ERNC" Existing record new contact
					I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "ernc"));
					this.setWriter(writer);
					writer.newRecord();
					if (writer.getRecordsWrittenCount() == 0)
						writeHeader();
					writeItem("2");
					writeItem("5");
					writeItem("11", "1");
					if (this.itemIsPopulated("2000"))
						writeItem("2000");
					else if (this.itemIsPopulated("200"))
						writeItem("200");
					writeItem("35");
					writeItem("40");
					writeItemIfChanged("100");
					writeItemIfChanged("207");
					writeItemIfChanged("50");
					writeItemIfChanged("80");
					writeItemIfChanged("91");
					writeItemIfChanged("7000");
					writeItemIfChanged("7010");
					// writeItem("rootuuid");
					writer.saveRecord();
				}
				if (this.hasItem("5")) {
					if (this.getFirstItem("5").getValue().toString().equals(ExportValueConverter.SOFT_DELETE_FLAG)) {
						EpicExportWriter.writeSoftDeleteFile(getExportManager(), 
								new String("softdelete_").concat(masterfile).concat("_").concat(version),
								this.getFirstItem("11").getValue().toString(), masterfile);
					}
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
		if (this.hasErrors()) {
			ret.append(".error");
		}
		return ret.toString();
	}
	
	public void writeHeader() throws Exception {
		this.writeLine("##INI=EDG");
		this.writeLine("##VAR=TRACK_EXTID^TRACK_EDG200");
	}
}