package org.ihtsdo.rf2.changeanalyzer.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.ihtsdo.rf2.changeanalyzer.data.Rf2LanguageRefsetRow;

public class Rf2LanguageRefsetFile extends Rf2RefsetFile<Rf2LanguageRefsetRow> {

	public Rf2LanguageRefsetFile(String filePath) {
		super(filePath);
	}

	@Override
	protected void loadFile() throws Exception {
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		br.readLine();
		rows = new HashMap<String, Set<Rf2LanguageRefsetRow>>();
		while (br.ready()) {
			String line = br.readLine();
			Rf2LanguageRefsetRow currentRow = new Rf2LanguageRefsetRow(line);
			if (rows.containsKey(currentRow.getId())) {
				rows.get(currentRow.getId()).add(currentRow);
			} else {
				Set<Rf2LanguageRefsetRow> rowSet = new HashSet<Rf2LanguageRefsetRow>();
				rowSet.add(currentRow);
				rows.put(currentRow.getId(), (Set<Rf2LanguageRefsetRow>) rowSet);
			}
		}

		br.close();
	}

	public ArrayList<String> getAcceptabilityIdChanged(String startDate, String endDate) {
		ArrayList<String> result = new ArrayList<String>();
		Set<String> keyset = rows.keySet();
		for (String long1 : keyset) {
			Set<Rf2LanguageRefsetRow> currentRows = rows.get(long1);
			String minorDate = "99999999";
			String majorDate = "00000000";
			String firstField = null;
			String lastField = null;
			for (Rf2LanguageRefsetRow rf2Row : currentRows) {
				if (between(startDate, endDate, rf2Row) && rf2Row.getEffectiveTime().compareTo(minorDate) <= 0) {
					minorDate = rf2Row.getEffectiveTime();
					firstField = rf2Row.getAcceptabilityId();
				}
				if (between(startDate, endDate, rf2Row) && rf2Row.getEffectiveTime().compareTo(majorDate) >= 0) {
					majorDate = rf2Row.getEffectiveTime();
					lastField = rf2Row.getAcceptabilityId();
				}
			}
			if (firstField != null && lastField != null && !firstField.equals(lastField)) {
				result.add(long1);
			}
		}
		return result;
	}

}
