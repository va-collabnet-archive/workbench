package org.ihtsdo.qadb.helper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.qadb.data.view.QACasesReportColumn;
import org.ihtsdo.qadb.data.view.QACasesReportLine;

public class CaseReportLineComparator implements Comparator<QACasesReportLine> {

	private LinkedHashMap<Integer, Boolean> sortBy;
	private static final Logger logger = Logger.getLogger(CaseReportLineComparator.class);
	public CaseReportLineComparator(LinkedHashMap<Integer, Boolean> sortBy) {
		this.sortBy = sortBy;
	}

	@Override
	public int compare(QACasesReportLine o1, QACasesReportLine o2) {
		int result = 0;
		Set<Integer> sortKeys = sortBy.keySet();

		for (Integer key : sortKeys) {
			boolean order = sortBy.get(key);

			if (key == QACasesReportColumn.ASSIGNED_TO.getColumnNumber()) {
				String assignedTo1 = o1.getQaCase().getAssignedTo();
				String assignedTo2 = o2.getQaCase().getAssignedTo();
				if (assignedTo1 != null && assignedTo2 != null) {
					if (assignedTo1.compareTo(assignedTo2) > 0) {
						return order ? 1 : -1;
					} else if (assignedTo1.compareTo(assignedTo2) < 0) {
						return order ? -1 : 1;
					}
				}
			}else if (key == QACasesReportColumn.CONCEPT_NAME.getColumnNumber()) {
				String conceptName1 = o1.getComponent().getComponentName();
				String conceptName2 = o2.getComponent().getComponentName();
				if (conceptName1 != null && conceptName2 != null) {
					if (conceptName1.compareTo(conceptName2) > 0) {
						return order ? 1 : -1;
					} else if (conceptName1.compareTo(conceptName2) < 0) {
						return order ? -1 : 1;
					}
				}
			}else if (key == QACasesReportColumn.DISPOSITION.getColumnNumber()) {
				String disp1 = o1.getDisposition().getName();
				String disp2 = o2.getDisposition().getName();
				if (disp1 != null && disp2 != null) {
					if (disp1.compareTo(disp2) > 0) {
						return order ? 1 : -1;
					} else if (disp1.compareTo(disp2) < 0) {
						return order ? -1 : 1;
					}
				}
			}else if (key.equals(QACasesReportColumn.STATUS)) {
				Boolean status1 = o1.getQaCase().isActive();
				Boolean status2 = o2.getQaCase().isActive();
				if (status1 != null && status2 != null) {
					if (status1.compareTo(status2) > 0) {
						return order ? 1 : -1;
					} else if (status1.compareTo(status2) < 0) {
						return order ? -1 : 1;
					}
				}
			}else if (key == QACasesReportColumn.TIME.getColumnNumber()) {
				String time1 = o1.getDisposition().getName();
				String time2 = o2.getDisposition().getName();
				if (time1 != null && time2 != null) {
					if (time1.compareTo(time2) > 0) {
						return order ? 1 : -1;
					} else if (time1.compareTo(time2) < 0) {
						return order ? -1 : 1;
					}
				}
			}
		}
		return result;
	}

}
