package org.ihtsdo.rf2.file.delta.snapshot.utils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CommonUtils {
	public static String[] getSmallestArray(HashMap<BufferedReader, String[]> passedMap, int[] sortColumns) {

		List<String[]> mapValues = new ArrayList<String[]>(passedMap.values());
		//
		Collections.sort(mapValues, new ArrayComparator(sortColumns, false));

		return mapValues.get(0);
	}
}
