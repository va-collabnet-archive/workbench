package org.ihtsdo.qadb.helper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.ihtsdo.qadb.data.DispositionStatus;
import org.ihtsdo.qadb.data.view.RulesReportColumn;
import org.ihtsdo.qadb.data.view.RulesReportLine;

public class RuleReportLineComparator implements Comparator<RulesReportLine> {

	private LinkedHashMap<Integer, Boolean> sortBy;
	private List<DispositionStatus> dispStatuses;

	public RuleReportLineComparator(LinkedHashMap<Integer, Boolean> sortBy, List<DispositionStatus> dispStatuses) {
		this.sortBy = sortBy;
		this.dispStatuses = dispStatuses;
	}

	@Override
	public int compare(RulesReportLine o1, RulesReportLine o2) {
		int result = 0;
		Set<Integer> sortKeys = sortBy.keySet();

		HashMap<String, Integer> dispStatusCounts1 = o1.getDispositionStatusCount();
		HashMap<String, Integer> dispStatusCounts2 = o2.getDispositionStatusCount();

		for (Integer key : sortKeys) {
			boolean order = sortBy.get(key);

			if (key == RulesReportColumn.CATEGORY) {
				String category1 = o1.getRule().getCategory();
				String category2 = o2.getRule().getCategory();
				if (category1 != null && category2 != null) {
					if (category1.compareTo(category2) > 0) {
						return order ? 1 : -1;
					} else if (category1.compareTo(category2) < 0) {
						return order ? -1 : 1;
					}
				}
			} else if (key == RulesReportColumn.CLEARED) {
				String cleardUuid = "";
				for (DispositionStatus dsp : dispStatuses) {
					if (dsp.getName().equalsIgnoreCase("cleared")) {
						cleardUuid = dsp.getDispositionStatusUuid();
					}
				}
				Integer cleard1 = dispStatusCounts1.get(cleardUuid);
				Integer cleard2 = dispStatusCounts2.get(cleardUuid);
				if (cleard1.compareTo(cleard2) > 0) {
					return order ? 1 : -1;
				} else if (cleard1.compareTo(cleard2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.DEFERRED) {
				String differedUuid = "";
				for (DispositionStatus dsp : dispStatuses) {
					if (dsp.getName().equalsIgnoreCase("deferred")) {
						differedUuid = dsp.getDispositionStatusUuid();
					}
				}
				Integer diferred1 = dispStatusCounts1.get(differedUuid);
				Integer diferred2 = dispStatusCounts2.get(differedUuid);
				if (diferred1.compareTo(diferred2) > 0) {
					return order ? 1 : -1;
				} else if (diferred1.compareTo(diferred2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.ESCALATED) {
				String escalatedUuid = "";
				for (DispositionStatus dsp : dispStatuses) {
					if (dsp.getName().equalsIgnoreCase("escalated")) {
						escalatedUuid = dsp.getDispositionStatusUuid();
					}
				}
				Integer escalated1 = dispStatusCounts1.get(escalatedUuid);
				Integer escalated2 = dispStatusCounts2.get(escalatedUuid);
				if (escalated1.compareTo(escalated2) > 0) {
					return order ? 1 : -1;
				} else if (escalated1.compareTo(escalated2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.IN_DISCUTION) {
				String inDiscutionUuid = "";
				for (DispositionStatus dsp : dispStatuses) {
					if (dsp.getName().equalsIgnoreCase("in discussion")) {
						inDiscutionUuid = dsp.getDispositionStatusUuid();
					}
				}
				Integer inDiscution1 = dispStatusCounts1.get(inDiscutionUuid);
				Integer inDiscution2 = dispStatusCounts2.get(inDiscutionUuid);
				if (inDiscution1.compareTo(inDiscution2) > 0) {
					return order ? 1 : -1;
				} else if (inDiscution1.compareTo(inDiscution2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.CLOSED) {
				Integer closed1 = o1.getStatusCount().get(false);
				Integer closed2 = o2.getStatusCount().get(false);
				if (closed1.compareTo(closed2) > 0) {
					return order ? 1 : -1;
				} else if (closed1.compareTo(closed2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.OPEN) {
				Integer open1 = o1.getStatusCount().get(true);
				Integer open2 = o2.getStatusCount().get(true);
				if (open1.compareTo(open2) > 0) {
					return order ? 1 : -1;
				} else if (open1.compareTo(open2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.RULE_CODE) {
				String ruleCode1 = o1.getRule().getRuleCode();
				String ruleCode2 = o2.getRule().getRuleCode();
				if (ruleCode1.compareTo(ruleCode2) > 0) {
					return order ? 1 : -1;
				} else if (ruleCode1.compareTo(ruleCode2) < 0) {
					return order ? -1 : 1;
				}
			} else if (key == RulesReportColumn.RULE_NAME) {
				String ruleName1 = o1.getRule().getName();
				String ruleName2 = o2.getRule().getName();
				if (ruleName1.compareTo(ruleName2) > 0) {
					return order ? 1 : -1;
				} else if (ruleName1.compareTo(ruleName2) < 0) {
					return order ? -1 : 1;
				}
			}else if (key == RulesReportColumn.SEVERITY) {
				String severity1 = o1.getRule().getSeverity().getSeverityName();
				String severity2 = o2.getRule().getSeverity().getSeverityName();
				if (severity1.compareTo(severity2) > 0) {
					return order ? 1 : -1;
				} else if (severity1.compareTo(severity2) < 0) {
					return order ? -1 : 1;
				}
			}
		}
		return result;
	}

}
