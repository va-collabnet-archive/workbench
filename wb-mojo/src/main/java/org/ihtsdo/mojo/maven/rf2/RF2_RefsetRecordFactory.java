package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.log.AceLog;

public class RF2_RefsetRecordFactory {
	
	protected static final int REFSET_ID = 4;
	
	protected static final String LINE_TERMINATOR = "\r\n";
    protected static final String TAB_CHARACTER = "\t";

	public static List<? extends Rf2_RefsetBaseRecord> parseRefset(Rf2File f, Long[] exclusions, Class recordType)
            throws Exception {

		Rf2_RefsetBaseRecord dummyRecord = null; 
		if (recordType.equals(Rf2_RefsetCIRecord.class)) {
			dummyRecord = new Rf2_RefsetCIRecord(null, "1970-01-01 00:00:00", 
					false, null, 0, 0, 0, 0);
		} else if (recordType.equals(Rf2_RefsetCCIRecord.class)) {
			dummyRecord = new Rf2_RefsetCCIRecord(null, "1970-01-01 00:00:00", 
					false, null, 0, 0, 0, 0, 0);
		} else if (recordType.equals(Rf2_RefsetCCSRecord.class)) {
			dummyRecord = new Rf2_RefsetCCSRecord(null, "1970-01-01 00:00:00", 
					false, null, 0, 0, 0, 0, null);
		} else if (recordType.equals(Rf2_RefsetSSRecord.class)) {
			dummyRecord = new Rf2_RefsetSSRecord(null, "1970-01-01 00:00:00", 
					false, null, 0, 0, null, null);
		} else {
			throw new Exception("Did not provide a record type handled by this factory");
		}
		
        int count = Rf2File.countFileLines(f);
        int countExludedMembers = 0;
        int currentCount = 0;
        List<? extends Rf2_RefsetBaseRecord> a = dummyRecord.getEmptyRecordList();        

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.file),
                "UTF-8"));
        Set<Long> idSet = new HashSet<>();

        br.readLine(); // Header row
        currentCount++;
        try {
			while (br.ready()) {
			    String[] line = br.readLine().split(TAB_CHARACTER);
			    currentCount++;

			    Long refsetIdL = Long.parseLong(line[REFSET_ID]);
			    boolean found = false;
			    if (exclusions != null) {
			        for (Long excludedId : exclusions) {
			            if (excludedId.compareTo(refsetIdL) == 0) {
			                found = true;
			            }
			        }
			    }
			    if (found) {
			        countExludedMembers++;
			        continue;
			    }
			    idSet.add(refsetIdL);
			    dummyRecord.addRecord(a, line);			    
			}
		} catch (NumberFormatException e) {
			AceLog.getAppLog().severe("Error parsing Refset recors: File=" + f.file.getName() + " Line=" + currentCount);
			throw e;
		}

        Long[] aLongs = (Long[]) idSet.toArray(new Long[0]);
        StringBuilder sb = new StringBuilder();
        sb.append("Concept Refset SCT IDs kept:\r\n");
        sb.append(f.file.getName());
        sb.append("\r\n");
        for (Long l : aLongs) {
            sb.append(l.toString());
            sb.append("\t");
            sb.append(Rf2x.convertSctIdToUuidStr(l));
            sb.append("\r\n");
        }
        Logger.getLogger(RF2_RefsetRecordFactory.class.getName()).info(sb.toString());

        sb = new StringBuilder();
        sb.append("Concept Refset SCT IDs excluded:\r\n");
        sb.append(f.file.getName());
        sb.append("\r\n");
        if (exclusions != null) {
            for (Long l : exclusions) {
                sb.append(l.toString());
                sb.append("\t");
                sb.append(Rf2x.convertSctIdToUuidStr(l));
                sb.append("\r\n");
            }
        } else {
            sb.append("none.\r\n");
        }
        Logger.getLogger(RF2_RefsetRecordFactory.class.getName()).info(sb.toString());

        sb = new StringBuilder();
        sb.append("Filter Stats\r\n");
        sb.append(f.file.getName());
        sb.append("\r\nTotal members viewed   =\t");
        sb.append(count);
        sb.append("\r\nTotal members kept     =\t");
        sb.append(a.size());
        sb.append("\r\nTotal members excluded =\t");
        sb.append(countExludedMembers);
        sb.append("\r\n");
        Logger.getLogger(RF2_RefsetRecordFactory.class.getName()).info(sb.toString());

        return a;
    }
}
