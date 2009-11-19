package org.dwfa.mojo.epicexport.kp;

import org.dwfa.mojo.epicexport.AbstractEpicExportBuilder;
import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;

public class EpicExportBuilderEDGBilling extends AbstractEpicExportBuilder implements I_EpicLoadFileBuilder {
	public static final int DISPLAY_NAME = 2;
	public static final int ITEM_11 = 11;
	public static final String[] INTERESTED_ITEMS = {"2", "40", "40", "207", "2000"};
	public static final String[] EREC_ITEMS = {"50", "7010"};
	
	private String masterfile = EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING;
	
	public EpicExportBuilderEDGBilling(EpicLoadFileFactory exportManager, EpicExportManager em) {
		super(exportManager, em);
		this.setExportIfTheseItemsChanged(INTERESTED_ITEMS);
	}

	public String getEpicItemNumber(int refsetNumber) {
		// TODO just for now return the same number
		return new Integer(refsetNumber).toString();
	}
	
	public void writeRecord(String version) throws Exception {
		
		if (this.isChangedRecord()) {
			if (getFirstItem("11") == null) {
				//"NRNC" Its a new record
				I_EpicExportRecordWriter writer = getExportManager().getWriter(getWriterName(masterfile, version, "nrnc"));
				this.setWriter(writer);
				if (writer.getRecordsWrittenCount() == 0)
					writer.writeLine("#HELLO WORLD");
				writer.newRecord();
				writeItem("2");
				writeItem("40");
				writeItemIfChanged("2000");
				writeItemIfChanged("207");
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
