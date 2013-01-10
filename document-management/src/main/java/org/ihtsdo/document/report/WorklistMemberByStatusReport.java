/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.util.WorkflowSearcher;
import org.ihtsdo.project.view.WorkListChooser;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The Class WorklistMemberByStatusReport.
 */
public class WorklistMemberByStatusReport implements I_Report {

	/** The preferred. */
	private I_GetConceptData preferred;
	private I_GetConceptData synonim;
	private I_GetConceptData fsn;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#getCsv()
	 */
	@Override
	public File getCsv() throws Exception {
		File csvFile = null;
		boolean dataFound = false;
		try {
			I_TermFactory tf = Terms.get();
			WorkflowSearcher searcher = new WorkflowSearcher();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			WorkListChooser wlChooser = new WorkListChooser(config);
			WorkList wl = wlChooser.showModalDialog();
			csvFile = File.createTempFile("workset_member_", ".csv");
			PrintWriter pw = new PrintWriter(csvFile);
			pw.append("worklist|status|term|target preferred|last user");
			pw.println();
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonim = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			if (wl != null) {
				HashMap<String, List<WfInstance>> wlMembersByStatus = null;
				List<WfInstance> wfInstnaces = searcher.getAllWrokflowInstancesForWorklist(wl.getUids());

				I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist(wl, config);
				int projectId = project.getId();

				Integer targetLanguage = TerminologyProjectDAO.getTargetLanguageRefsetIdForProjectId(projectId, config);

				if (wfInstnaces != null) {
					wlMembersByStatus = new HashMap<String, List<WfInstance>>();
					for (WfInstance wfInstnace : wfInstnaces) {
						WfState state = wfInstnace.getState();
						if (wlMembersByStatus.containsKey(state.toString())) {
							wlMembersByStatus.get(state.toString()).add(wfInstnace);
						} else {
							List<WfInstance> wlms = new ArrayList<WfInstance>();
							wlms.add(wfInstnace);
							wlMembersByStatus.put(state.toString(), wlms);
						}
					}
					if (wlMembersByStatus != null) {
						Set<String> keySet = wlMembersByStatus.keySet();
						for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
							String key = iterator.next();
							List<WfInstance> members = wlMembersByStatus.get(key);
							for (WfInstance workListMember : members) {
								dataFound = true;
								pw.append(wl.getName() + "|");
								pw.append(key + "|");

								UUID componentId = workListMember.getComponentId();
								I_GetConceptData concept = Terms.get().getConcept(componentId);

								String sourceFsn = "";
								try {
									I_GetConceptData langRefset = null;
									List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
									I_TerminologyProject projectConcept = TerminologyProjectDAO.getProjectForWorklist(workListMember.getWorkList(), config);
									TranslationProject translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
									langRefset = translationProject.getSourceLanguageRefsets().get(0);
									descriptions = ContextualizedDescription.getContextualizedDescriptions(Terms.get().uuidToNative(componentId), langRefset.getConceptNid(), true);
									for (I_ContextualizeDescription description : descriptions) {
										if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
											if (description.getAcceptabilityId() == preferred.getConceptNid() && description.getTypeId() == fsn.getConceptNid() && isActive(description.getDescriptionStatusId()) && isActive(description.getExtensionStatusId())) {
												sourceFsn = description.getText();
											}
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

								I_IntSet descriptionTypes = tf.newIntSet();
								I_IntSet allowedStatus = tf.newIntSet();

								pw.append(sourceFsn + "|");

								descriptionTypes = tf.newIntSet();
								allowedStatus = tf.newIntSet();
								allowedStatus.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
								allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
								allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));

								descriptionTypes.add(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());

								String targetPreferred = "";
								try {
									I_GetConceptData langRefset = null;
									List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
									I_TerminologyProject projectConcept = TerminologyProjectDAO.getProjectForWorklist(workListMember.getWorkList(), config);
									TranslationProject translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
									langRefset = translationProject.getTargetLanguageRefset();
									descriptions = ContextualizedDescription.getContextualizedDescriptions(Terms.get().uuidToNative(componentId), langRefset.getConceptNid(), true);
									for (I_ContextualizeDescription description : descriptions) {
										if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
											if (description.getAcceptabilityId() == preferred.getConceptNid() && description.getTypeId() == synonim.getConceptNid() && isActive(description.getDescriptionStatusId()) && isActive(description.getExtensionStatusId())) {
												targetPreferred = description.getText();
											}
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

								pw.append(targetPreferred + "|");
								WfComponentProvider provider = new WfComponentProvider();
								WorkList workList = workListMember.getWorkList();
								PromotionAndAssignmentRefset promotionRefset = workList.getPromotionRefset(config);
								I_Identify nid = Terms.get().getId(workListMember.getComponentId());
								I_GetConceptData prevUser = promotionRefset.getPreviousUser(nid.getConceptNid(), config);
								pw.append(prevUser.toUserString());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#getExcelPivotTableWorkBook()
	 */
	@Override
	public File getExcelPivotTableWorkBook() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#getExcelSourceWorkbook()
	 */
	@Override
	public File getExcelSourceWorkbook() throws Exception {
		File csvFile = this.getCsv();
		if (csvFile == null) {
			return null;
		}
		FileReader input;
		CSVReader reader;
		File excelRep = new File("reports/templates/worklist_members_byStatus.xls");
		File reportCopy = null;
		try {
			input = new FileReader(csvFile);
			reader = new CSVReader(input, '|');
			if (excelRep.exists()) {

				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-hh-mm");
				Date date = new Date();
				reportCopy = new File("reports/" + sdf.format(date) + "_worklist_member_by_status.xls");
				try {
					ExcelReportUtil.copyFile(excelRep, reportCopy);
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}

				Workbook wb = ExcelReportUtil.readFile(reportCopy);
				FileOutputStream out = new FileOutputStream(reportCopy);

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
						try {
							num = Integer.valueOf(nextLine[cellnum]);
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						} catch (Exception e) {
						}

						if (num != null) {
							cell.setCellValue(num);
						} else {
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
		return reportCopy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#getReportPanel()
	 */
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
			jviewer.setTitle("Worklist members by status");
		} catch (JRException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return jviewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Worklist members by status report";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#cancelReporting()
	 */
	@Override
	public void cancelReporting() throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * Checks if is active.
	 * 
	 * @param statusId
	 *            the status id
	 * @return true, if is active
	 */
	public static boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		I_TermFactory tf = Terms.get();
		try {
			activeStatuses.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			activeStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			activeStatuses.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
			activeStatuses.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (activeStatuses.contains(statusId));
	}

}
