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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.util.WorkflowSearcher;
import org.ihtsdo.project.view.TranslationProjectListDialog;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The Class WorklistStateTotalsReport.
 */
public class WorklistStateTotalsReport implements I_Report {

	/** The states cache. */
	private HashMap<String, String> statesCache = new HashMap<String, String>();

	/* (non-Javadoc)
	 * @see org.ihtsdo.document.report.I_Report#getCsv()
	 */
	@Override
	public File getCsv() throws Exception {
		File csvFile = null;
		boolean dataFound = false;
		WorkflowSearcher searcher = new WorkflowSearcher();
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			csvFile = File.createTempFile("workset_worklist_st_totals", ".csv");
			PrintWriter writer = new PrintWriter(csvFile);
			writer.append("Project|WorkSet|WorkList|Status|Total|Persentage|wlStatusTotal|projStatusTotal");
			writer.println();

			TranslationProjectListDialog dialog = new TranslationProjectListDialog();
			ArrayList<I_TerminologyProject> projects = dialog.showModalDialog();
			if (projects != null) {
				for (I_TerminologyProject iTerminologyProject : projects) {
					HashMap<String, Integer> projectTotals = new HashMap<String, Integer>();
					String projectName = iTerminologyProject.getName();

					List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(iTerminologyProject,config);
					for (WorkSet workSet : worksets) {
						HashMap<String, Integer> worksetTotals = new HashMap<String, Integer>();
						String worksetName = workSet.getName();
						List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);

						for (WorkList workList : worklists) {
							if (workList != null) {
								String worklistName = workList.getName();

								LinkedHashMap<String, Integer> statusMembers = new LinkedHashMap<String, Integer>();
								
								List<WfInstance> wfInstances = searcher.getAllWrokflowInstancesForWorklist(workList.getUids());
								for (WfInstance wfInstance : wfInstances) {
									WfState state = wfInstance.getState();
									String firstLetterUpperCaseStatus = getFirstLetterToUpperCase(state.toString());
									
									if (statusMembers.containsKey(firstLetterUpperCaseStatus)) {
										Integer subTotal = statusMembers.get(firstLetterUpperCaseStatus);
										Integer projectSubTotal = projectTotals.get(firstLetterUpperCaseStatus);
										Integer worksetSubTotals = worksetTotals.get(firstLetterUpperCaseStatus);

										statusMembers.put(firstLetterUpperCaseStatus,subTotal + 1);
										projectTotals.put(firstLetterUpperCaseStatus,projectSubTotal + 1);
										worksetTotals.put(firstLetterUpperCaseStatus,worksetSubTotals + 1);
									} else {
										statusMembers.put(firstLetterUpperCaseStatus, 1);
										if (!projectTotals.containsKey(firstLetterUpperCaseStatus)) {
											projectTotals.put(firstLetterUpperCaseStatus,1);
										} else {
											Integer projectSubTotal = projectTotals.get(firstLetterUpperCaseStatus);
											projectTotals.put(firstLetterUpperCaseStatus,projectSubTotal + 1);
										}
										if (!worksetTotals.containsKey(firstLetterUpperCaseStatus)) {
											worksetTotals.put(firstLetterUpperCaseStatus,1);
										} else {
											Integer worksetSubTotals = worksetTotals.get(firstLetterUpperCaseStatus);
											worksetTotals.put(firstLetterUpperCaseStatus,worksetSubTotals + 1);
										}
									}
								}

								Set<String> keySet = statusMembers.keySet();
								ArrayList<String> orderdKeys = new ArrayList<String>(keySet);
								Collections.sort(orderdKeys);
								
								for (String key : orderdKeys) {
									dataFound = true;
									Integer total = statusMembers.get(key);
									Integer projTotal = projectTotals.get(key);
									Integer wsTotal = worksetTotals.get(key);
									if (total > 0 || projTotal > 0 || wsTotal > 0) {
										writer.append(projectName + '|');
										writer.append(worksetName + '|');
										writer.append(worklistName + '|');
										writer.append(key + '|');
										writer.append(total + "|");
										writer.append((total * 100 / wfInstances.size()) + "|");
										writer.append(wsTotal + "|");
										writer.append(projTotal + "");
										writer.println();
									}
								}
							}
						}
					}
				}
				writer.flush();
				writer.close();
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

	/**
	 * Gets the first letter to upper case.
	 *
	 * @param string the string
	 * @return the first letter to upper case
	 */
	private String getFirstLetterToUpperCase(String string) {
		if (statesCache.containsKey(string)) {
			return statesCache.get(string);
		} else {
			StringBuffer result = new StringBuffer();
			result.append(string.toUpperCase().charAt(0));
			for (int i = 1; i < string.length(); i++) {
				result.append(string.toUpperCase().charAt(i));
			}
			statesCache.put(string, result.toString());
			return result.toString();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.document.report.I_Report#getExcelPivotTableWorkBook()
	 */
	@Override
	public File getExcelPivotTableWorkBook() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.document.report.I_Report#getExcelSourceWorkbook()
	 */
	@Override
	public File getExcelSourceWorkbook() throws Exception {
		File csvFile;
		csvFile = this.getCsv();
		if (csvFile == null) {
			return null;
		}

		FileReader input;
		CSVReader reader;
		File excelRep = new File("reports/templates/worklist_state_totals.xls");
		File reportCopy = null;
		try {
			input = new FileReader(csvFile);
			reader = new CSVReader(input, '|');
			if (excelRep.exists()) {

				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-hh-mm");
				Date date = new Date();
				reportCopy = new File("reports/" + sdf.format(date) + "_worklist_state_totals.xls");

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

				wb.setSheetName(1, "Data");

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

	/* (non-Javadoc)
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
			JasperCompileManager.compileReportToFile("reports/templates/ProjectMembersStatusTotals.jrxml");
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		String fileName = "reports/templates/ProjectMembersStatusTotals.jasper";
		try {
			// Fill the report using an empty data source
			if (csvFile != null) {
				JRCsvDataSource csvDataSource = new JRCsvDataSource(csvFile);
				csvDataSource.setRecordDelimiter(System.getProperty("line.separator"));
				csvDataSource.setFieldDelimiter('|');

				csvDataSource.setUseFirstRowAsHeader(true);
				JasperPrint print = JasperFillManager.fillReport(fileName, null, csvDataSource);

				jviewer = new JasperViewer(print, false);
				jviewer.setTitle("Worklist state totals");
			}

		} catch (JRException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jviewer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Project worklists status totals";
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.document.report.I_Report#cancelReporting()
	 */
	@Override
	public void cancelReporting() throws Exception {
		// TODO Auto-generated method stub

	}

}
