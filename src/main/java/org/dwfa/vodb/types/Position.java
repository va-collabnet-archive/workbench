package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseException;

public class Position implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int version;
	Path path;

	public Path getPath() {
		return path;
	}

	public int getVersion() {
		return version;
	}

	public Position(int version, Path path) {
		super();
		this.version = version;
		this.path = path;
	}

	public boolean isSubsequentOrEqualTo(int version, int pathId) {
		if (equals(version, pathId)) {
			return true;
		}
		if (path.conceptId == pathId) {
			return this.version >= version;
		}
		return checkSubsequentOrEqualToOrigins(path.getOrigins(), version, pathId);
	}

	public boolean isAntecedentOrEqualTo(int version, int pathId) {
		if (equals(version, pathId)) {
			return true;
		}
		if (path.conceptId == pathId) {
			return this.version <= version;
		}
		return checkAntecedentOrEqualToOrigins(path.getOrigins(), version, pathId);
	}
	private boolean checkSubsequentOrEqualToOrigins(List<Position> origins, int testVersion, int testPathId) {
		for (Position origin: origins) {
			if (testPathId == origin.path.conceptId) {
				return origin.version >= testVersion;
			} else if (checkSubsequentOrEqualToOrigins(origin.path.origins, testVersion, testPathId)) {
				return true;
			}
		}
		return false;
	}
	private boolean checkAntecedentOrEqualToOrigins(List<Position> origins, int testVersion, int testPathId) {
		for (Position origin: origins) {
			if (testPathId == origin.path.conceptId) {
				return origin.version <= testVersion;
			} else if (checkAntecedentOrEqualToOrigins(origin.path.origins, testVersion, testPathId)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAntecedentOrEqualTo(Position another) {
		if (equals(another)) {
			return true;
		}
		if (path.conceptId == another.path.conceptId) {
			return version <= another.version;
		}
		return checkAntecedentOrEqualToOrigins(another.path.origins);
	}
	
	public boolean checkAntecedentOrEqualToOrigins(List<Position> origins) {
		for (Position origin: origins) {
			if (path.conceptId == origin.path.conceptId) {
				return version <= origin.version;
			} else if (checkAntecedentOrEqualToOrigins(origin.path.origins)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSubsequentOrEqualTo(Position another) {
		return another.isAntecedentOrEqualTo(this);
	}
	
	public boolean equals(int version, int pathId) {
		return ((this.version == version) && (path.conceptId == pathId));
	}

	public boolean equals(Position another) {
		return ((version == another.version) && (path.conceptId == another.path.conceptId));
	}

	@Override
	public boolean equals(Object obj) {
		return equals((Position) obj);
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {version, path.conceptId});
	}

	public int getDepth(int pathId) {
		int depth = 0;
		if (pathId == path.conceptId) {
			return depth;
		}
		List<Position> depthOrigins = new ArrayList<Position>(path.origins);
		while (depthOrigins.size() > 0) {
			depth++;
			for (Position o: depthOrigins) {
				if (o.path.conceptId == pathId) {
					return depth;
				}
			}
			List<Position> newOrigins = new ArrayList<Position>();
			for (Position p: depthOrigins) {
				newOrigins.addAll(p.path.getOrigins());
			}
			depthOrigins = newOrigins;
		}
		
		
		return Integer.MAX_VALUE;
	}
	public static void writePosition(ObjectOutputStream out, Position p) throws IOException {
		out.writeInt(p.version);
		try {
			out.writeObject(AceConfig.vodb.nativeToUuid(p.path.conceptId));
		} catch (DatabaseException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}
		out.writeInt(p.path.origins.size());
		for (Position origin: p.path.origins) {
			writePosition(out, origin);
		}
	}

	@SuppressWarnings("unchecked")
	public static Position readPosition(ObjectInputStream in) throws IOException, ClassNotFoundException  {
		int version = in.readInt();
		int pathConceptId;
		try {
			pathConceptId = AceConfig.vodb.uuidToNative((List<UUID>) in.readObject());
		} catch (TerminologyException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}
		int size = in.readInt();
		List<Position> origins = new ArrayList<Position>(size);
		for (int i = 0; i < size; i++) {
			origins.add(readPosition(in));
        }
		Path p = new Path(pathConceptId, origins);
        return new Position(version, p);
	}

	public static Set<Position> readPositionSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int size = in.readInt();
		Set<Position> positions = new HashSet<Position>(size);
		for (int i = 0; i < size; i++) {
			positions.add(readPosition(in));
		}
		return positions;
	}
	
	public static void writePositionSet(ObjectOutputStream out, Set<Position> positions) throws IOException {
		out.writeInt(positions.size());
		for (Position p: positions) {
			writePosition(out, p);
		}
	}
	static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

	public String toString() {
		StringBuffer buff = new StringBuffer();
		ConceptBean cb = ConceptBean.get(path.getConceptId());
		try {
			buff.append(cb.getInitialText());
		} catch (DatabaseException e) {
			buff.append(e.getMessage());
			e.printStackTrace();
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
