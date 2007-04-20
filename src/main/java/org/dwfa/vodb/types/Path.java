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

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseException;

public class Path implements I_Transact, Serializable, I_Path {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int conceptId;

	List<I_Position> origins;

	public Path(int conceptId, List<I_Position> origins) {
		super();
		this.conceptId = conceptId;
		if (origins != null) {
			this.origins = Collections.unmodifiableList(origins);
		} else {
			this.origins = Collections
					.unmodifiableList(new ArrayList<I_Position>(0));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Path#getConceptId()
	 */
	public int getConceptId() {
		return conceptId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Path#getOrigins()
	 */
	public List<I_Position> getOrigins() {
		return origins;
	}

	public static List<I_Path> makeBasePaths(VodbEnv vodb)
			throws DatabaseException, ParseException, TerminologyException,
			IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		List<I_Path> basePaths = new ArrayList<I_Path>();
		Path aceRelease = new Path(
				vodb
						.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
								.getUids()), null);
		basePaths.add(aceRelease);

		Position latestAceRelease = new Position(Integer.MAX_VALUE, aceRelease);
		List<I_Position> origins = new ArrayList<I_Position>(1);
		origins.add(latestAceRelease);

		Path snomedCore = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE
						.getUids()), origins);
		basePaths.add(snomedCore);

		Date releaseDate = dateFormat.parse("2006-07-31");

		Position snomed20060731Pos = new Position(ThinVersionHelper
				.convert(releaseDate.getTime()), snomedCore);
		List<I_Position> snomed20060731Origins = new ArrayList<I_Position>(1);
		snomed20060731Origins.add(snomed20060731Pos);
		Path snomed20060731 = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20060731
						.getUids()), snomed20060731Origins);
		basePaths.add(snomed20060731);

		releaseDate = dateFormat.parse("2006-01-31");
		Position snomed20060131Pos = new Position(ThinVersionHelper
				.convert(releaseDate.getTime()), snomedCore);
		List<I_Position> snomed20060131Origins = new ArrayList<I_Position>(1);
		snomed20060131Origins.add(snomed20060131Pos);
		Path snomed20060131 = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20060131
						.getUids()), snomed20060131Origins);
		basePaths.add(snomed20060131);

		releaseDate = dateFormat.parse("2005-07-31");
		Position snomed20050731Pos = new Position(ThinVersionHelper
				.convert(releaseDate.getTime()), snomedCore);
		List<I_Position> snomed20050731Origins = new ArrayList<I_Position>(1);
		snomed20050731Origins.add(snomed20050731Pos);
		Path snomed20050731 = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20050731
						.getUids()), snomed20050731Origins);
		basePaths.add(snomed20050731);

		releaseDate = dateFormat.parse("2005-01-31");
		Position snomed20050131Pos = new Position(ThinVersionHelper
				.convert(releaseDate.getTime()), snomedCore);
		List<I_Position> snomed20050131Origins = new ArrayList<I_Position>(1);
		snomed20050131Origins.add(snomed20050131Pos);
		Path snomed20050131 = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20050131
						.getUids()), snomed20050131Origins);
		basePaths.add(snomed20050131);

		Path icd10Am = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.ICD_10_AM
						.getUids()), origins);
		basePaths.add(icd10Am);

		return basePaths;

	}

	public static List<I_Path> makeTestSnomedPaths(VodbEnv vodb)
			throws DatabaseException, ParseException, TerminologyException,
			IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		List<I_Path> basePaths = new ArrayList<I_Path>();
		Path aceRelease = new Path(
				vodb
						.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
								.getUids()), null);
		basePaths.add(aceRelease);

		Position latestAceRelease = new Position(Integer.MAX_VALUE, aceRelease);
		List<I_Position> origins = new ArrayList<I_Position>(1);
		origins.add(latestAceRelease);

		Path snomedCore = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE
						.getUids()), origins);

		Date releaseDate = dateFormat.parse("2006-07-31");

		Position snomed20060731Pos = new Position(ThinVersionHelper
				.convert(releaseDate.getTime()), snomedCore);
		List<I_Position> snomed20060731Origins = new ArrayList<I_Position>(1);
		snomed20060731Origins.add(snomed20060731Pos);
		Path snomed20060731 = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20060731
						.getUids()), snomed20060731Origins);
		basePaths.add(snomed20060731);

		releaseDate = dateFormat.parse("2005-01-31");
		Position snomed20050131Pos = new Position(ThinVersionHelper
				.convert(releaseDate.getTime()), snomedCore);
		List<I_Position> snomed20050131Origins = new ArrayList<I_Position>(1);
		snomed20050131Origins.add(snomed20050131Pos);
		Path snomed20050131 = new Path(vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_20050131
						.getUids()), snomed20050131Origins);
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
			vodb.setup(new File(fileStr), false, 600000000L);

			writeBasePaths(vodb);
			vodb.sync();
			vodb.close();
		} catch (Exception ex) {
			AceLog.getLog().alertAndLogException(ex);
		}
		System.exit(0);
	}

	public static void writeBasePaths(VodbEnv vodb) throws DatabaseException,
			ParseException, TerminologyException, IOException {
		List<I_Path> basePaths = makeBasePaths(vodb);
		for (I_Path p : basePaths) {
			vodb.writePath(p);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Path#getMatchingPath(int)
	 */
	public I_Path getMatchingPath(int pathId) {
		if (conceptId == pathId) {
			return this;
		}
		for (I_Position origin : origins) {
			if (origin.getPath().getMatchingPath(pathId) != null) {
				return origin.getPath();
			}
		}
		return null;
	}

	public static String toHtmlString(I_Path path) throws IOException {
		StringBuffer buff = new StringBuffer();
		buff.append("<html><font color='blue' size='+1'><u>");
		ConceptBean cb = ConceptBean.get(path.getConceptId());
		buff.append(cb.getInitialText());
		buff.append("</u></font>");
		if (path != null) {
			for (I_Position origin : path.getOrigins()) {
				buff.append("<br>&nbsp;&nbsp;&nbsp;Origin: ");
				buff.append(origin);
			}
		}
		return buff.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Path#abort()
	 */
	public void abort() {
		// Nothing to do...
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Path#commit(int, java.util.Set)
	 */
	public void commit(int version, Set<TimePathId> values) throws IOException {
		try {
			AceConfig.vodb.writePath(this);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Path#convertIds(org.dwfa.ace.api.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		conceptId = jarToDbNativeMap.get(conceptId);
		for (I_Position origin : origins) {
			origin.getPath().convertIds(jarToDbNativeMap);
		}
	}

	public UniversalAcePath getUniversal() throws IOException,
			TerminologyException {
		try {
			List<UniversalAcePosition> universalOrigins = new ArrayList<UniversalAcePosition>(
					origins.size());
			for (I_Position position : origins) {
				universalOrigins.add(new UniversalAcePosition(AceConfig.vodb
						.nativeToUuid(position.getPath().getConceptId()),
						ThinVersionHelper.convert(position.getVersion())));
			}
			return new UniversalAcePath(AceConfig.vodb.nativeToUuid(conceptId),
					universalOrigins);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

}
