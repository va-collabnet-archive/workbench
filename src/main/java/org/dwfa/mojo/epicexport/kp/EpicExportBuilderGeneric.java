package org.dwfa.mojo.epicexport.kp;

import java.util.List;

import org.dwfa.mojo.epicexport.AbstractEpicExportBuilder;
import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;
import org.dwfa.mojo.epicexport.I_ExportFactory;

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
