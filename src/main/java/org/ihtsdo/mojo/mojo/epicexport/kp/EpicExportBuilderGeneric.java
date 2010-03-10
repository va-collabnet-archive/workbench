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
import org.ihtsdo.mojo.mojo.epicexport.I_ExportFactory;

public class EpicExportBuilderGeneric extends AbstractEpicExportBuilder implements I_EpicLoadFileBuilder {
	public static final int DISPLAY_NAME = 2;
	public static final int ITEM_11 = 11;
	
	public String masterfile;
	
	public EpicExportBuilderGeneric(I_ExportFactory factory, EpicExportManager em, String masterfile) {
		super(factory, em);
		this.masterfile = masterfile;
	}

	public void writeRecord(String version, List<String> regions) throws Exception {
		
		if (this.isChangedRecord()) {
			if (!hasItem("11")) {
				// It's a new record
				addErrorIfTrue(! this.allItemsArePopulated(getMandatoryItems()),
						"One or more mandatory items are missing");
				
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version));
				this.setWriter(writer);
				writer.newRecord();
				writeAnyErrors();
				writeLiteralItem("1", "");
				writeItems(getAlwaysWriteTheseItemsForNewRecord());
				writeItemsIfChanged(getItemsToWriteIfChanged());
				writer.saveRecord();
			}
			else {
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version));
				this.setWriter(writer);
				writer.newRecord();
				writeItem("11", "1");
				this.writeItems(this.getAlwaysWriteTheseItemsForExistingRecord());
				this.writeItemsIfChanged(this.getItemsToWriteIfChanged());
				writer.saveRecord();
			}
		}
	}
	
	private String getWriterName(String masterfile, String version) {
		StringBuffer ret = new StringBuffer(masterfile);
		if (version != null) {
			ret.append('_');
			ret.append(version);
		}
		if (this.hasErrors()) {
			ret.append(".error");
		}
		return ret.toString();
	}

	

}
