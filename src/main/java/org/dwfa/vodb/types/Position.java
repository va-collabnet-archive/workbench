package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseException;

public class Position implements I_Position {

   private int version;
	private I_Path path;

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#getPath()
	 */
	public I_Path getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#getVersion()
	 */
	public int getVersion() {
		return version;
	}

	public Position(int version, I_Path path) {
		super();
      if (path == null) {
         throw new IllegalArgumentException("path cannot be null");
      }
		this.version = version;
		this.path = path;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#isSubsequentOrEqualTo(int, int)
	 */
	public boolean isSubsequentOrEqualTo(int version, int pathId) {
		if (equals(version, pathId)) {
			return true;
		}
		if (path.getConceptId() == pathId) {
			return this.version >= version;
		}
		return checkSubsequentOrEqualToOrigins(path.getOrigins(), version, pathId);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#isAntecedentOrEqualTo(int, int)
	 */
	public boolean isAntecedentOrEqualTo(int version, int pathId) {
		if (equals(version, pathId)) {
			return true;
		}
		if (path.getConceptId() == pathId) {
			return this.version <= version;
		}
		return checkAntecedentOrEqualToOrigins(path.getOrigins(), version, pathId);
	}
	private boolean checkSubsequentOrEqualToOrigins(List<I_Position> origins, int testVersion, int testPathId) {
		for (I_Position origin: origins) {
			if (testPathId == origin.getPath().getConceptId()) {
				return origin.getVersion() >= testVersion;
			} else if (checkSubsequentOrEqualToOrigins(origin.getPath().getOrigins(), testVersion, testPathId)) {
				return true;
			}
		}
		return false;
	}
	private boolean checkAntecedentOrEqualToOrigins(List<I_Position> origins, int testVersion, int testPathId) {
		for (I_Position origin: origins) {
			if (testPathId == origin.getPath().getConceptId()) {
				return origin.getVersion() <= testVersion;
			} else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins(), testVersion, testPathId)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#isAntecedentOrEqualTo(org.dwfa.vodb.types.Position)
	 */
	public boolean isAntecedentOrEqualTo(I_Position another) {
		if (equals(another)) {
			return true;
		}
		if (path.getConceptId() == another.getPath().getConceptId()) {
			return version <= another.getVersion();
		}
		return checkAntecedentOrEqualToOrigins(another.getPath().getOrigins());
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#checkAntecedentOrEqualToOrigins(java.util.List)
	 */
	public boolean checkAntecedentOrEqualToOrigins(List<I_Position> origins) {
		for (I_Position origin: origins) {
			if (path.getConceptId() == origin.getPath().getConceptId()) {
				return version <= origin.getVersion();
			} else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins())) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#isSubsequentOrEqualTo(org.dwfa.vodb.types.I_Position)
	 */
	public boolean isSubsequentOrEqualTo(I_Position another) {
		return another.isAntecedentOrEqualTo(this);
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#equals(int, int)
	 */
	public boolean equals(int version, int pathId) {
		return ((this.version == version) && (path.getConceptId() == pathId));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#equals(org.dwfa.vodb.types.Position)
	 */
	public boolean equals(I_Position another) {
		return ((version == another.getVersion()) && (path.getConceptId() == another.getPath().getConceptId()));
	}

	@Override
	public boolean equals(Object obj) {
		return equals((I_Position) obj);
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {version, path.getConceptId()});
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_Position#getDepth(int)
	 */
	public int getDepth(int pathId) {
		int depth = 0;
		if (pathId == path.getConceptId()) {
			return depth;
		}
		List<I_Position> depthOrigins = new ArrayList<I_Position>(path.getOrigins());
		while (depthOrigins.size() > 0) {
			depth++;
			for (I_Position o: depthOrigins) {
				if (o.getPath().getConceptId() == pathId) {
					return depth;
				}
			}
			List<I_Position> newOrigins = new ArrayList<I_Position>();
			for (I_Position p: depthOrigins) {
				newOrigins.addAll(p.getPath().getOrigins());
			}
			depthOrigins = newOrigins;
		}
		
		
		return Integer.MAX_VALUE;
	}
	public static void writePosition(ObjectOutputStream out, I_Position p) throws IOException {
		out.writeInt(p.getVersion());
		try {
			out.writeObject(AceConfig.getVodb().nativeToUuid(p.getPath().getConceptId()));
		} catch (DatabaseException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}
		out.writeInt(p.getPath().getOrigins().size());
		for (I_Position origin: p.getPath().getOrigins()) {
			writePosition(out, origin);
		}
	}

	@SuppressWarnings("unchecked")
	public static I_Position readPosition(ObjectInputStream in) throws IOException, ClassNotFoundException  {
		int version = in.readInt();
		int pathConceptId;
		try {
			pathConceptId = AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject());
		} catch (TerminologyException e) {
			IOException newEx = new IOException(e.getLocalizedMessage());
			newEx.initCause(e);
			throw newEx;
		}
		int size = in.readInt();
		List<I_Position> origins = new ArrayList<I_Position>(size);
		for (int i = 0; i < size; i++) {
			origins.add(readPosition(in));
        }
		  Path p = new Path(pathConceptId, origins);
        return new Position(version, p);
	}

	public static Set<I_Position> readPositionSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int size = in.readInt();
		Set<I_Position> positions = Collections.synchronizedSet(new HashSet<I_Position>(size));
		for (int i = 0; i < size; i++) {
         try {
        	I_Position position = readPosition(in);
        	I_GetConceptData pathConcept = LocalVersionedTerminology.get().getConcept(position.getPath().getConceptId());
        	I_Path path = LocalVersionedTerminology.get().getPath(pathConcept.getUids());
            positions.add(LocalVersionedTerminology.get().newPosition(path, position.getVersion()));
         } catch (IOException ex) {
            if (ex.getCause() != null && NoMappingException.class.isAssignableFrom(ex.getCause().getClass())) {
               AceLog.getAppLog().alertAndLogException(ex.getCause());
            } else {
               throw ex;
            }
         } catch (TerminologyException ex) {
             if (ex.getCause() != null && NoMappingException.class.isAssignableFrom(ex.getCause().getClass())) {
                 AceLog.getAppLog().alertAndLogException(ex.getCause());
              } else {
                 throw new IOException(ex);
              }
		}
		}
		return positions;
	}
	
	public static void writePositionSet(ObjectOutputStream out, Set<I_Position> viewPositions) throws IOException {
		out.writeInt(viewPositions.size());
		for (I_Position p: viewPositions) {
			writePosition(out, p);
		}
	}
	static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

	public String toString() {
		StringBuffer buff = new StringBuffer();
		ConceptBean cb = ConceptBean.get(path.getConceptId());
		try {
			buff.append(cb.getInitialText());
		} catch (IOException e) {
			buff.append(e.getMessage());
			AceLog.getAppLog().alertAndLogException(e);
		}
		buff.append(": ");
		if (version == Integer.MAX_VALUE) {
			buff.append("Latest");
		} else if (version == Integer.MIN_VALUE) {
			buff.append("BOT");
		} else {
			Date positionDate = new Date(ThinVersionHelper.convert(version));
			buff.append(dateFormatter.format(positionDate));
		}
		return buff.toString();
		
	}
}
