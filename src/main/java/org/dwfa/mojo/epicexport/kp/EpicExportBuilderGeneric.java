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
	private String[] mandatoryItems;
	private String[] alwaysWriteTheseItemsForNewRecord;
	private String[] alwaysWriteTheseItemsForExistingRecord;
	private String[] itemsToWriteIfChanged;
	
	public EpicExportBuilderGeneric(I_ExportFactory factory, EpicExportManager em, String masterfile) {
		super(factory, em);
		this.masterfile = masterfile;
	}

	public void writeRecord(String version, List<String> regions) throws Exception {
		
		if (this.isChangedRecord()) {
			if (getFirstItem("11") == null) {
				//"NRNC" Its a new record
				addErrorIfTrue(! this.allItemsArePopulated(this.mandatoryItems),
						"One or more mandatory items are missing");
				
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "nrnc"));
				this.setWriter(writer);
				writer.newRecord();
				writeAnyErrors();
				writeLiteralItem("1", "");
				writeItems(this.alwaysWriteTheseItemsForNewRecord);
				writeItemsIfChanged(this.itemsToWriteIfChanged);
				writer.saveRecord();
			}
			else {
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "erec"));
				this.setWriter(writer);
				writer.newRecord();
				this.writeItems(this.alwaysWriteTheseItemsForExistingRecord);
				this.writeItemsIfChanged(this.itemsToWriteIfChanged);
				writer.saveRecord();
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

	public String[] getMandatoryItems() {
		return mandatoryItems;
	}

	public void setMandatoryItems(String[] mandatoryItems) {
		this.mandatoryItems = mandatoryItems;
	}

	public String[] getAlwaysWriteTheseItemsForNewRecord() {
		return alwaysWriteTheseItemsForNewRecord;
	}

	public void setAlwaysWriteTheseItemsForNewRecord(
			String[] alwaysWriteTheseItemsForNewRecord) {
		this.alwaysWriteTheseItemsForNewRecord = alwaysWriteTheseItemsForNewRecord;
	}

	public String[] getAlwaysWriteTheseItemsForExistingRecord() {
		return alwaysWriteTheseItemsForExistingRecord;
	}

	public void setAlwaysWriteTheseItemsForExistingRecord(
			String[] alwaysWriteTheseItemsForExistingRecord) {
		this.alwaysWriteTheseItemsForExistingRecord = alwaysWriteTheseItemsForExistingRecord;
	}

	public String[] getItemsToWriteIfChanged() {
		return itemsToWriteIfChanged;
	}

	public void setItemsToWriteIfChanged(String[] itemsToWriteIfChanged) {
		this.itemsToWriteIfChanged = itemsToWriteIfChanged;
	}
	
	

}
