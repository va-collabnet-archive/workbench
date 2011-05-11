package org.ihtsdo.document.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.panel.ProjectDatePeriodDialog;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

import au.com.bytecode.opencsv.CSVReader;

public class ProjectHistoricalReport implements I_Report {

	private SimpleDateFormat formatter;
	private I_TermFactory tf;

	public ProjectHistoricalReport() {
		super();
		formatter = new SimpleDateFormat("dd-MMM-yyyy");
		tf = Terms.get();
	}

	@Override
	public File getExcelSourceWorkbook() throws Exception {
		File csvFile = this.getCsv();
		if (csvFile == null) {
			return null;
		}

		FileReader input;
		CSVReader reader;
		File excelRep = new File("reports/templates/project_history_report.xls");
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
						cell.setCellValue(nextLine[cellnum]);
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
		File reportCopy = new File("reports/" + sdf.format(date) + "_project_history_report.xls");
		
		try {
			ExcelReportUtil.copyFile(excelRep, reportCopy);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return reportCopy;
	}

	@Override
	public File getCsv() throws Exception {
		File csvFile = null;
		boolean dataFound = false;
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_ConfigAceFrame configVer;

			csvFile = File.createTempFile("project_historical_report", ".csv");
			PrintWriter pw = new PrintWriter(csvFile);
			pw.append("date|project|workset|worklist|term|status|user");
			pw.println();

			HashMap<String, Object> data = new ProjectDatePeriodDialog(config).showModalDialog();
			if (data != null) {
				I_TerminologyProject project = (I_TerminologyProject) data.get(ProjectDatePeriodDialog.PROJECT_KEY);
				String projectName = project.getName();

				GregorianCalendar endCal = (GregorianCalendar) data.get(ProjectDatePeriodDialog.END_DATE_KEY);
				Long endThickVer = endCal.getTimeInMillis();

				GregorianCalendar startCal = (GregorianCalendar) data.get(ProjectDatePeriodDialog.START_DATE_KEY);
				Long startThickVer = startCal.getTimeInMillis();

				Long period = (Long) data.get(ProjectDatePeriodDialog.PERIOD_KEY);
				Long thickPeriod = period * 24 * 60 * 60 * 1000;

				while (endThickVer >= startThickVer) {

					if (endThickVer.equals(startThickVer)) {
						startThickVer = startThickVer + 1000;
					}

					configVer = getVersionedConfig(config, endThickVer);
					String strDate = formatter.format(endThickVer);

					List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, configVer);
					for (WorkSet workSet : workSets) {
						String worksetName = workSet.getName();
						List<WorkList> Worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, configVer);
						for (WorkList wl : Worklists) {
							String worklistName = wl.getName();
							HashMap<String, List<WorkListMember>> wlMembersByStatus = null;
							List<WorkListMember> wlMembers = TerminologyProjectDAO.getAllWorkListMembers(wl, configVer);
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
											pw.append(strDate + "|");
											pw.append(projectName + "|");
											pw.append(worksetName + "|");
											pw.append(worklistName + "|");
											pw.append(workListMember.getName() + "|");
											pw.append(key + "|");
											pw.append(workListMember.getLastAuthorName());
											pw.println();
										}
									}
								}
							}
						}
					}
					endThickVer = endThickVer - thickPeriod;
				}
				if (!dataFound) {
					return null;
				}
				pw.flush();
				pw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return csvFile;
	}

	private I_ConfigAceFrame getVersionedConfig(I_ConfigAceFrame con, Long thickVer) throws Exception {
		I_ConfigAceFrame verConfig = null;
		verConfig = tf.newAceFrameConfig();

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(con);
			oos.flush();

			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);
			verConfig = (I_ConfigAceFrame) ois.readObject();

			Set<PositionBI> vPosSet = con.getViewPositionSet();
			for (PositionBI positionBI : vPosSet) {
				verConfig.removeViewPosition(positionBI);
			}

			Set<PathBI> a = con.getEditingPathSet();
			for (PathBI pathBI : a) {
				verConfig.addViewPosition(tf.newPosition(pathBI, thickVer));
			}

		} catch (Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			throw e;
		} finally {
			oos.close();
			ois.close();
		}
		return verConfig;
	}

	@Override
	public File getExcelPivotTableWorkBook() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JFrame getReportPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return "Project history report";
	}
}
