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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

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
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.report.model.UserStatusCount;
import org.ihtsdo.document.report.model.UserStatusCountComparator;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.view.TranslationProjectDialog;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The Class AccumulatedStatusChanges.
 */
public class AccumulatedStatusChanges extends SwingWorker<File, String> implements I_Report {

	/** The formatter. */
	private SimpleDateFormat formatter;

	/** The tf. */
	private I_TermFactory tf;

	/** The user roles cache. */
	private HashMap<I_GetConceptData, String> userRolesCache;

	/**
	 * Instantiates a new accumulated status changes.
	 */
	public AccumulatedStatusChanges() {
		super();
		formatter = new SimpleDateFormat("dd-MMM-yyyy");
		userRolesCache = new HashMap<I_GetConceptData, String>();
		tf = Terms.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#getExcelSourceWorkbook()
	 */
	@Override
	public File getExcelSourceWorkbook() throws Exception {
		userRolesCache = new HashMap<I_GetConceptData, String>();
		File csvFile = this.getCsv();
		if (csvFile == null) {
			return null;
		}

		FileReader input;
		CSVReader reader;
		File excelRep = new File("reports/templates/accumulated_status_changes.xls");
		File reportCopy = null;
		try {
			input = new FileReader(csvFile);
			reader = new CSVReader(input, '|');
			if (excelRep.exists()) {

				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-hh-mm");
				Date date = new Date();
				reportCopy = new File("reports/" + sdf.format(date) + "_accumulated_status_changes.xls");

				try {
					ExcelReportUtil.copyFile(excelRep, reportCopy);
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}

				Workbook wb = ExcelReportUtil.readFile(reportCopy);
				FileOutputStream out = new FileOutputStream(reportCopy);
				wb.getSheetIndex("Data");
				wb.removeSheetAt(wb.getSheetIndex("Data"));
				wb.createSheet("Data");
				Sheet s = wb.getSheet("Data");
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
						if (cellnum == nextLine.length - 1) {
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						}
						Integer num = null;
						try {
							num = Integer.valueOf(nextLine[cellnum]);
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
	 * @see org.ihtsdo.document.report.I_Report#getCsv()
	 */
	@Override
	public File getCsv() throws Exception {
		File csvFile = null;
		boolean dataFound = false;
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-mm-hh-ss");
			csvFile = File.createTempFile("acumulated_status_changes_" + sdf.format(new Date()), ".csv");
			PrintWriter pw = new PrintWriter(csvFile);
			pw.append("project|workset|worklist|date|role|author|status|count");
			pw.println();

			I_TerminologyProject project = new TranslationProjectDialog().showModalDialog();
			if (project != null) {
				String projectName = project.getName();

				List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
				for (WorkSet workSet : workSets) {
					// Workset Loop
					String worksetName = workSet.getName();
					List<WorkList> Worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList wl : Worklists) {
						// WORKLIST LOOP
						String worklistName = wl.getName();
						PromotionAndAssignmentRefset wlPromRefset = wl.getPromotionRefset(config);
						try {
							ArrayList<UserStatusCount> results = new ArrayList<UserStatusCount>();
							List<WorkListMember> members = wl.getWorkListMembers();
							for (WorkListMember member : members) {
								I_GetConceptData wlMemberConcept = member.getConcept();
								Collection<? extends RefexChronicleBI<?>> promMembers = wlMemberConcept.getAnnotations();
								for (RefexChronicleBI<?> promMember : promMembers) {
									if (promMember.getRefexNid() == wlPromRefset.getRefsetId()) {
										Collection<?> promVersions = promMember.getVersions();
										for (Object object : promVersions) {
											if (object instanceof RefexNidNidVersionBI) {
												RefexNidNidVersionBI version = (RefexNidNidVersionBI) object;
												I_GetConceptData statusConcept = tf.getConcept(version.getNid1());
												boolean sameDestinationAndAuthor = version.getNid2() != version.getAuthorNid();
												I_GetConceptData previousStatus = wlPromRefset.getPreviousPromotionStatus(wlMemberConcept.getConceptNid(), config);
												if (previousStatus == null) {
													continue;
												}
												I_GetConceptData wlMemeberActivityStatus = member.getActivityStatus();
												boolean sameStatus = previousStatus.getConceptNid() == wlMemeberActivityStatus.getConceptNid();
												//if (!statusConcept.getUids().iterator().next().equals(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next()) && (!sameDestinationAndAuthor || !sameStatus)) {
												if (!statusConcept.getUids().iterator().next().equals(UUID.fromString("ed055320-2c26-50f8-91fd-a8533f5db793")) && (!sameDestinationAndAuthor || !sameStatus)) {
													dataFound = true;
													UserStatusCount current = new UserStatusCount();
													current.setDate(formatter.format(new Date(version.getTime())));
													I_GetConceptData user = tf.getConcept(version.getAuthorNid());
													current.setUserName(user + "");
													current.setStatus(statusConcept + "");
													current.setRole(getUserRole(wl, user));
													results.add(current);
												}
											}
										}
									}
								}
							}
							if (!results.isEmpty()) {
								Collections.sort(results, new UserStatusCountComparator());
								UserStatusCount first = results.get(0);
								int count = 0;
								for (UserStatusCount userStatusCount : results) {
									if (userStatusCount.equals(first)) {
										count++;
										if (count == results.size()) {
											pw.append(projectName + "|");
											pw.append(worksetName + "|");
											pw.append(worklistName + "|");
											pw.append(userStatusCount.getDate() + "|");
											pw.append(userStatusCount.getRole() + "|");
											pw.append(userStatusCount.getUserName() + "|");
											pw.append(userStatusCount.getStatus() + "|");
											pw.append(count + "");
											pw.println();
										}
									} else {
										pw.append(projectName + "|");
										pw.append(worksetName + "|");
										pw.append(worklistName + "|");
										pw.append(first.getDate() + "|");
										pw.append(first.getRole() + "|");
										pw.append(first.getUserName() + "|");
										pw.append(first.getStatus() + "|");
										pw.append(count + "");
										pw.println();
										first = userStatusCount;
										count = 1;
									}
								}
							}
						} catch (TerminologyException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}
			}
			if (!dataFound) {
				return null;
			}
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return csvFile;

	}

	/**
	 * Gets the user role.
	 * 
	 * @param workList
	 *            the work list
	 * @param user
	 *            the user
	 * @return the user role
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	private String getUserRole(WorkList workList, I_GetConceptData user) throws IOException, TerminologyException {
		if (userRolesCache.containsKey(user)) {
			return userRolesCache.get(user).toString();
		} else {
			Set<WfRole> roles = getRolesForUser(user, workList);
			if (roles.size() > 1) {
				AceLog.getAppLog().alertAndLog(Level.WARNING, "User: " + user.toString() + " has more then one role, using the first one in report.", new Exception("User: " + user.toString() + " has more then one role, using the first one in report."));
			} else if (roles.size() < 1) {
				AceLog.getAppLog().alertAndLog(Level.WARNING, "User: " + user.toString() + " has no roles in this project.", new Exception("User: " + user.toString() + " has no roles in this project."));
				userRolesCache.put(user, "No role");
				return "No role";
			}
			StringBuilder sb = new StringBuilder();
			for (WfRole role : roles) {
				sb.append(role.getName() + " ");
			}
			userRolesCache.put(user, sb.toString().trim());
			return sb.toString().trim();
		}
	}

	/**
	 * Gets the roles for user.
	 * 
	 * @param user
	 *            the user
	 * @param worklist
	 *            the worklist
	 * @return the roles for user
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	private Set<WfRole> getRolesForUser(I_GetConceptData user, WorkList worklist) throws IOException, TerminologyException {
		Set<WfRole> returnRoles = new HashSet<WfRole>();
		for (WfMembership loopMembership : worklist.getWorkflowUserRoles()) {
			if (user.getUids().contains(loopMembership.getUser().getId())) {
				returnRoles.add(loopMembership.getRole());
			}
		}
		return returnRoles;
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
	 * @see org.ihtsdo.document.report.I_Report#getReportPanel()
	 */
	@Override
	public JFrame getReportPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Accumulated status changes report";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.document.report.I_Report#cancelReporting()
	 */
	@Override
	public void cancelReporting() throws Exception {
		if (isCancelled()) {
			cancel(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected File doInBackground() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
