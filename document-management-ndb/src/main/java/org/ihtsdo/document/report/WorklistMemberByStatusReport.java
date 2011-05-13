package org.ihtsdo.document.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.view.JasperViewer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.WorkListChooser;

import au.com.bytecode.opencsv.CSVReader;

public class WorklistMemberByStatusReport implements I_Report {

	@Override
	public File getCsv() throws Exception {
		File csvFile = null;
		boolean dataFound = false;
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			WorkListChooser wlChooser = new WorkListChooser(config);
			WorkList wl = wlChooser.showModalDialog();
			csvFile = File.createTempFile("workset_member_", ".csv");
			PrintWriter pw = new PrintWriter(csvFile);
			pw.append("worklist|status|term|target prefered|last user");
			pw.println();

			if (wl != null) {
				HashMap<String, List<WorkListMember>> wlMembersByStatus = null;
				List<WorkListMember> wlMembers = TerminologyProjectDAO.getAllWorkListMembers(wl, config);
				
				I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist(wl, config);
				int projectId = project.getId();
				
				Integer targetLanguage = TerminologyProjectDAO.getTargetLanguageRefsetIdForProjectId(projectId, config);
				
				if (wlMembers != null) {
					wlMembersByStatus = new HashMap<String, List<WorkListMember>>();
					for (WorkListMember wlMember : wlMembers) {
						I_GetConceptData activitiStatus = tf.getConcept(wlMember.getActivityStatus());
						if (wlMembersByStatus.containsKey(activitiStatus.toString())) {
							wlMembersByStatus.get(activitiStatus.toString()).add(wlMember);
						} else {
							List<WorkListMember> wlms = new ArrayList<WorkListMember>();
							wlms.add(wlMember);
							wlMembersByStatus.put(activitiStatus.toString(), wlms);
						}
					}
					if (wlMembersByStatus != null) {
						Set<String> keySet = wlMembersByStatus.keySet();
						for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
							String key = iterator.next();
							List<WorkListMember> members = wlMembersByStatus.get(key);
							for (WorkListMember workListMember : members) {
								dataFound = true;
								pw.append(wl.getName() + "|");
								pw.append(key + "|");

								List<UUID> uuids = workListMember.getUids();
								I_GetConceptData concept = tf.getConcept(uuids);
								
								I_IntSet descriptionTypes =  tf.newIntSet();
								I_IntSet allowedStatus = tf.newIntSet();

								descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
								allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
								allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));

								List<ContextualizedDescription> descriptions = ContextualizedDescription.getContextualizedDescriptions(concept.getConceptNid(),targetLanguage,allowedStatus,
										descriptionTypes,config.getViewPositionSetReadOnly(), true);
								String sourceDesc = "";
								for (ContextualizedDescription desc : descriptions) {
									if(desc.getLang().equals(ArchitectonicAuxiliary.LANG_CODE.EN.getFormatedLanguageCode())){
										sourceDesc = desc.getText();
									}
								}
								pw.append(sourceDesc+ "|");
								
								descriptionTypes = tf.newIntSet();
								allowedStatus = tf.newIntSet();
								
								descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
								allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
								allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
								
								descriptions = ContextualizedDescription.getContextualizedDescriptions(concept.getConceptNid(),targetLanguage,allowedStatus,
										descriptionTypes,config.getViewPositionSetReadOnly(), true);
								String targetPreferred = "";
								for (ContextualizedDescription desc : descriptions) {
									if(desc.getLanguageRefsetId() == targetLanguage){
										if(desc.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids())){
											targetPreferred = desc.getText();
										}
									}
								}
								pw.append(targetPreferred +"|");
								pw.append(workListMember.getLastAuthorName());
								pw.println();
							}
						}
						pw.flush();
						pw.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		if (dataFound) {
			return csvFile;
		} else {
			return null;
		}
	}

	@Override
	public File getExcelPivotTableWorkBook() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getExcelSourceWorkbook() throws Exception {
		File csvFile = this.getCsv();
		if (csvFile == null) {
			return null;
		}
		FileReader input;
		CSVReader reader;
		File excelRep = new File("reports/templates/worklist_members_byStatus.xls");
		try {
			input = new FileReader(csvFile);
			reader = new CSVReader(input, '|');
			if (excelRep.exists()) {
				Workbook wb = ExcelReportUtil.readFile(excelRep);
				FileOutputStream out = new FileOutputStream(excelRep);

				wb.removeSheetAt(1);
				wb.createSheet("Data");
				Sheet s = wb.getSheetAt(1);

				Row r = null;
				Cell cell = null;

				CellStyle headCellStyle = wb.createCellStyle();
				CellStyle cs2 = wb.createCellStyle();

				Font titleFont = wb.createFont();
				Font f2 = wb.createFont();

				titleFont.setFontHeightInPoints((short) 15);
				titleFont.setColor((short) Font.COLOR_NORMAL);
				titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

				f2.setFontHeightInPoints((short) 10);
				f2.setColor((short) Font.COLOR_NORMAL);

				headCellStyle.setFont(titleFont);

				cs2.setBorderBottom(CellStyle.BORDER_THIN);
				cs2.setFont(f2);

				String[] nextLine;
				int rownum = 0;
				while ((nextLine = reader.readNext()) != null) {
					r = s.createRow(rownum);
					for (short cellnum = (short) 0; cellnum < nextLine.length; cellnum++) {
						cell = r.createCell(cellnum);
						if (rownum == 0) {
							cell.setCellStyle(headCellStyle);
						} else {
							cell.setCellStyle(cs2);
						}

						Integer num = null;
						try{
							num = Integer.valueOf(nextLine[cellnum]);
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						}catch (Exception e) {}
						
						if(num != null){
							cell.setCellValue(num);
						}else{
							cell.setCellValue(nextLine[cellnum]);
						}
						
						if (s.getColumnWidth(cellnum) < 3000 + nextLine[cellnum].length() * 200) {
							s.setColumnWidth((short) (cellnum), (short) (3000 + nextLine[cellnum].length() * 200));
						}
					}
					rownum++;
				}
				wb.setActiveSheet(0);
				wb.write(out);
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-hh-mm");
		Date date = new Date();
		File reportCopy = new File("reports/" + sdf.format(date) + "_worklist_member_by_status.xls");
		try {
			ExcelReportUtil.copyFile(excelRep, reportCopy);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return reportCopy;
	}

	@Override
	public JFrame getReportPanel() throws Exception {
		JasperViewer jviewer = null;
		File csvFile = this.getCsv();
		if (csvFile == null) {
			return null;
		}
		try {
			JasperCompileManager.compileReportToFile("reports/templates/demo_report.jrxml");
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		String fileName = "reports/templates/demo_report.jasper";
		try {
			// Fill the report using an empty data source
			JRCsvDataSource csvDataSource = new JRCsvDataSource(csvFile);
			csvDataSource.setRecordDelimiter(System.getProperty("line.separator"));
			csvDataSource.setFieldDelimiter('|');

			csvDataSource.setUseFirstRowAsHeader(true);
			JasperPrint print = JasperFillManager.fillReport(fileName, null, csvDataSource);

			jviewer = new JasperViewer(print, false);

		} catch (JRException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return jviewer;
	}

	@Override
	public String toString() {
		return "Worklist members by status report";
	}

}
