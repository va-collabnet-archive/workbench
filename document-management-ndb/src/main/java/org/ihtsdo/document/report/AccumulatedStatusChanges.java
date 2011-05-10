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
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.report.model.UserStatusCount;
import org.ihtsdo.document.report.model.UserStatusCountComparator;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.panel.TranslationProjectDialog;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.api.Precedence;

import au.com.bytecode.opencsv.CSVReader;

public class AccumulatedStatusChanges implements I_Report {

	private SimpleDateFormat formatter;
	private I_TermFactory tf;

	public AccumulatedStatusChanges() {
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
		File excelRep = new File("reports/templates/accumulated_status_changes.xls");
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
		File reportCopy = new File("reports/" + sdf.format(date) + "_accumulated_status_changes.xls");

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

			csvFile = File.createTempFile("project_historical_report", ".csv");
			PrintWriter pw = new PrintWriter(csvFile);
			pw.append("project|workset|worklist|date|author|status|count");
			pw.println();

			TranslationProject project = new TranslationProjectDialog().showModalDialog();
			if (project != null) {
				String projectName = project.getName();

				List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
				for (WorkSet workSet : workSets) {
					String worksetName = workSet.getName();
					List<WorkList> Worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList wl : Worklists) {
						String worklistName = wl.getName();
						HashMap<WorkListMember, I_ExtendByRef> workListMembers = new HashMap<WorkListMember, I_ExtendByRef>();
						try {
							Collection<? extends I_ExtendByRef> membersExtensions = tf.getRefsetExtensionMembers(wl.getId());
							List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();
							ArrayList<UserStatusCount> results = new ArrayList<UserStatusCount>();
							for (I_ExtendByRef extension : membersExtensions) {
								I_IntSet allowedStatus = Terms.get().newIntSet();
								allowedStatus.addAll(config.getAllowedStatus().getSetValues());
								allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
								allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
								members.add(tf.getConcept(extension.getComponentNid()));
							}
							for (I_GetConceptData member : members) {
								WorkListMember workListMember = null;
								I_TermFactory termFactory = Terms.get();

								try {
									I_GetConceptData workListRefset = termFactory.getConcept(wl.getId());

									I_IntSet descriptionTypes = termFactory.newIntSet();
									descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

									List<? extends I_DescriptionTuple> descTuples = member.getDescriptionTuples(config.getAllowedStatus(), descriptionTypes,
											config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
									String name;
									if (!descTuples.isEmpty()) {
										name = descTuples.iterator().next().getText();
										for (I_DescriptionTuple loopTup : descTuples) {
											if (loopTup.getLang().equals(ArchitectonicAuxiliary.LANG_CODE.EN.getFormatedLanguageCode())) {
												name = loopTup.getText();
											}
										}
									} else {
										name = "No FSN!";
									}
									Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(member.getConceptNid());
									I_ExtendByRef history = null;
									for (I_ExtendByRef extension : extensions) {
										if (extension.getRefsetId() == wl.getPromotionRefset(config).getRefsetId()) {
											history = extension;
										}
										if (extension.getRefsetId() == workListRefset.getConceptNid()) {
											PromotionRefset promotionRefset = wl.getPromotionRefset(config);
											I_GetConceptData status = promotionRefset.getPromotionStatus(member.getConceptNid(), config);
											Long statusDate = promotionRefset.getLastStatusTime(member.getConceptNid(), config);
											workListMember = new WorkListMember(name, member.getConceptNid(), member.getUids(), wl.getUids().iterator().next(), wl.getDestination(), status
													.getUids().iterator().next(), statusDate);
										}
									}
									if (history != null) {
										workListMembers.put(workListMember, history);
									}
								} catch (TerminologyException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							Set<WorkListMember> keys = workListMembers.keySet();
							for (WorkListMember workListMember : keys) {
								I_ExtendByRef history = workListMembers.get(workListMember);
								List<? extends I_ExtendByRefPartCid> parts = (List<? extends I_ExtendByRefPartCid>) history.getMutableParts();
								for (I_ExtendByRefPartCid part : parts) {
									I_GetConceptData concept = tf.getConcept(part.getC1id());
									if (!concept.getUids().iterator().next().equals(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next())
											&& !concept.getUids().iterator().next().equals(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_DELIVERED_STATUS.getUids().iterator().next())) {
										dataFound = true;
										UserStatusCount current = new UserStatusCount();
										current.setDate(formatter.format(new Date(part.getTime())));
										current.setUserName(tf.getConcept(part.getAuthorNid()) + "");
										current.setStatus(concept + "");
										results.add(current);
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
									} else {
										pw.append(projectName + "|");
										pw.append(worksetName + "|");
										pw.append(worklistName + "|");
										pw.append(userStatusCount.getDate() + "|");
										pw.append(userStatusCount.getUserName() + "|");
										pw.append(userStatusCount.getStatus() + "|");
										pw.append(count+"");
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
		return "Accumulated status canges report";
	}
}
