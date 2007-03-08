package org.dwfa.vodb.types;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.jar.I_MapNativeToNative;

import com.sleepycat.je.DatabaseException;

public class Path implements I_Transact, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int conceptId;
	List<Position> origins;
	
	public Path(int conceptId, List<Position> origins) {
		super();
		this.conceptId = conceptId;
		if (origins != null) {
			this.origins = Collections.unmodifiableList(origins);			
		} else {
			this.origins = Collections.unmodifiableList(new ArrayList<Position>(0));
		}
	}

	public boolean equals(Path another) {
		return (conceptId == another.conceptId);
	}

	@Override
	public boolean equals(Object obj) {
		return equals((Path) obj);
	}

	@Override
	public int hashCode() {
		return conceptId;
	}

	public int getConceptId() {
		return conceptId;
	}

	public List<Position> getOrigins() {
		return origins;
	}
	
	public static List<Path> makeBasePaths(VodbEnv vodb) throws DatabaseException, ParseException, TerminologyException, IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		List<Path> basePaths = new ArrayList<Path>();
		Path aceRelease = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()), null);
		basePaths.add(aceRelease);

		
		Position latestAceRelease = new Position(Integer.MAX_VALUE, aceRelease);
		List<Position> origins = new ArrayList<Position>(1);
		origins.add(latestAceRelease);
		
		
		Path snomedCore = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), origins);
		basePaths.add(snomedCore);
		

		Date releaseDate = dateFormat.parse("2006-07-31");
		
		Position snomed20060731Pos = new Position(ThinVersionHelper.convert(releaseDate.getTime()), snomedCore);
		List<Position> snomed20060731Origins = new ArrayList<Position>(1);
		snomed20060731Origins.add(snomed20060731Pos);
		Path snomed20060731 = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20060731.getUids()), snomed20060731Origins);
		basePaths.add(snomed20060731);
		
		releaseDate = dateFormat.parse("2006-01-31");
		Position snomed20060131Pos = new Position(ThinVersionHelper.convert(releaseDate.getTime()), snomedCore);
		List<Position> snomed20060131Origins = new ArrayList<Position>(1);
		snomed20060131Origins.add(snomed20060131Pos);
		Path snomed20060131 = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20060131.getUids()), snomed20060131Origins);
		basePaths.add(snomed20060131);

		releaseDate = dateFormat.parse("2005-07-31");
		Position snomed20050731Pos = new Position(ThinVersionHelper.convert(releaseDate.getTime()), snomedCore);
		List<Position> snomed20050731Origins = new ArrayList<Position>(1);
		snomed20050731Origins.add(snomed20050731Pos);
		Path snomed20050731 = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20050731.getUids()), snomed20050731Origins);
		basePaths.add(snomed20050731);

		releaseDate = dateFormat.parse("2005-01-31");
		Position snomed20050131Pos = new Position(ThinVersionHelper.convert(releaseDate.getTime()), snomedCore);
		List<Position> snomed20050131Origins = new ArrayList<Position>(1);
		snomed20050131Origins.add(snomed20050131Pos);
		Path snomed20050131 = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20050131.getUids()), snomed20050131Origins);
		basePaths.add(snomed20050131);

		Path icd10Am = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ICD_10_AM.getUids()), origins);
		basePaths.add(icd10Am);
		
		return basePaths;

	}
	
	public static List<Path> makeTestSnomedPaths(VodbEnv vodb) throws DatabaseException, ParseException, TerminologyException, IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		List<Path> basePaths = new ArrayList<Path>();
		Path aceRelease = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()), null);
		basePaths.add(aceRelease);
		
		Position latestAceRelease = new Position(Integer.MAX_VALUE, aceRelease);
		List<Position> origins = new ArrayList<Position>(1);
		origins.add(latestAceRelease);
		
		
		Path snomedCore = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), origins);		

		Date releaseDate = dateFormat.parse("2006-07-31");
		
		Position snomed20060731Pos = new Position(ThinVersionHelper.convert(releaseDate.getTime()), snomedCore);
		List<Position> snomed20060731Origins = new ArrayList<Position>(1);
		snomed20060731Origins.add(snomed20060731Pos);
		Path snomed20060731 = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20060731.getUids()), snomed20060731Origins);
		basePaths.add(snomed20060731);
		
		releaseDate = dateFormat.parse("2005-01-31");
		Position snomed20050131Pos = new Position(ThinVersionHelper.convert(releaseDate.getTime()), snomedCore);
		List<Position> snomed20050131Origins = new ArrayList<Position>(1);
		snomed20050131Origins.add(snomed20050131Pos);
		Path snomed20050131 = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20050131.getUids()), snomed20050131Origins);
		basePaths.add(snomed20050131);
		
		return basePaths;

	}
	

	public static void main(String[] args) {
		try {
			String fileStr;
			if (args.length == 0) {
				fileStr = "berkeley-db";
			} else {
				fileStr = args[0];
			}
			VodbEnv vodb = new VodbEnv();
			vodb.setup(new File(fileStr), false);
			
			writeBasePaths(vodb);
			vodb.sync();
			vodb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void writeBasePaths(VodbEnv vodb) throws DatabaseException, ParseException, TerminologyException, IOException {
		List<Path> basePaths = makeBasePaths(vodb);
		for (Path p: basePaths) {
			vodb.writePath(p);
		}
	}

	public Path getMatchingPath(int pathId) {
		if (conceptId == pathId) {
			return this;
		}
		for (Position origin: origins) {
			if (origin.path.getMatchingPath(pathId) != null) {
				return origin.path;
			}
		}
		return null;
	}
	
    public static String toHtmlString(Path path) throws DatabaseException {
        StringBuffer buff = new StringBuffer();
        buff.append("<html><font color='blue' size='+1'><u>");
        ConceptBean cb = ConceptBean.get(path.getConceptId());
        buff.append(cb.getInitialText());
        buff.append("</u></font>");
        if (path != null) {
            for (Position origin: path.origins) {
                buff.append("<br>&nbsp;&nbsp;&nbsp;Origin: ");
                buff.append(origin);
            }
        }
        return buff.toString();
    }

	public void abort() {
		// Nothing to do...
	}

	public void commit(int version, Set<TimePathId> values) throws DatabaseException {
		AceConfig.vodb.writePath(this);
		
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		conceptId = jarToDbNativeMap.get(conceptId);
		for (Position origin: origins) {
			origin.path.convertIds(jarToDbNativeMap);
		}
	}

}
