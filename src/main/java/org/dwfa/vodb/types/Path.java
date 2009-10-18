package org.dwfa.vodb.types;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseException;

public class Path implements I_Transact, I_Path {
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

	public boolean equals(I_Path another) {
		return (conceptId == another.getConceptId());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (I_Path.class.isAssignableFrom(obj.getClass())) {
			return equals((I_Path) obj);
		}
		return false;
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

		List<I_Path> basePaths = new ArrayList<I_Path>();
        
		Path aceRelease = new Path(
				vodb
						.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
								.getUids()), null);
		basePaths.add(aceRelease);

		Position latestAceRelease = new Position(Integer.MAX_VALUE, aceRelease);
		List<I_Position> origins = new ArrayList<I_Position>(1);
		origins.add(latestAceRelease);
        
		
        addIfFound(vodb, basePaths, origins, ArchitectonicAuxiliary.Concept.TGA_DATA);
        addIfFound(vodb, basePaths, origins, ArchitectonicAuxiliary.Concept.AMT_SOURCE_DATA);
        addIfFound(vodb, basePaths, origins, ArchitectonicAuxiliary.Concept.SNOMED_CORE);

		return basePaths;

	}

    private static void addIfFound(VodbEnv vodb, List<I_Path> basePaths, List<I_Position> origins, Concept pathConcept) throws TerminologyException, IOException {
        try {
            Path path = new Path(vodb.uuidToNative(pathConcept.getUids()), origins);
            basePaths.add(path);
        } catch (NoMappingException ex) {
            AceLog.getAppLog().info("Exception processing: "  + pathConcept.name() + ": " + ex.toString());
        }
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
			AceLog.getAppLog().alertAndLogException(ex);
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
		AceConfig.getVodb().writePath(this);
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
				universalOrigins.add(new UniversalAcePosition(AceConfig.getVodb()
						.nativeToUuid(position.getPath().getConceptId()),
						ThinVersionHelper.convert(position.getVersion())));
			}
			return new UniversalAcePath(AceConfig.getVodb().nativeToUuid(conceptId),
					universalOrigins);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

   public static void writePath(ObjectOutputStream out, I_Path p) throws IOException {
      try {
         out.writeObject(AceConfig.getVodb().nativeToUuid(p.getConceptId()));
      } catch (DatabaseException e) {
         IOException newEx = new IOException();
         newEx.initCause(e);
         throw newEx;
      }
      out.writeInt(p.getOrigins().size());
      for (I_Position origin: p.getOrigins()) {
         Position.writePosition(out, origin);
      }
   }

   @SuppressWarnings("unchecked")
   public static I_Path readPath(ObjectInputStream in) throws IOException, ClassNotFoundException  {
      int pathId;
      try {
         pathId = AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject());
      } catch (TerminologyException e) {
         IOException newEx = new IOException();
         newEx.initCause(e);
         throw newEx;
      }
      int size = in.readInt();
      List<I_Position> origins = new ArrayList<I_Position>(size);
      for (int i = 0; i < size; i++) {
         origins.add(Position.readPosition(in));
        }
        return new Path(pathId, origins);
   }

   public static Set<I_Path> readPathSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int size = in.readInt();
      Set<I_Path> positions = new HashSet<I_Path>(size);
      for (int i = 0; i < size; i++) {
         try {
            positions.add(readPath(in));
         } catch (IOException ex) {
            if (NoMappingException.class.isAssignableFrom(ex.getCause().getClass())) {
               AceLog.getAppLog().alertAndLogException(ex);
            } else {
               throw ex;
            }
         }
      }
      return positions;
   }
   
   public static void writePathSet(ObjectOutputStream out, Set<I_Path> viewPositions) throws IOException {
      out.writeInt(viewPositions.size());
      for (I_Path p: viewPositions) {
         writePath(out, p);
      }
   }
   public String toString() {
      StringBuffer buff = new StringBuffer();
      ConceptBean cb = ConceptBean.get(getConceptId());
      try {
         buff.append(cb.getInitialText());
      } catch (IOException e) {
         buff.append(e.getMessage());
         AceLog.getAppLog().alertAndLogException(e);
      }
      return buff.toString();      
   }

   public String toHtmlString() throws IOException {
	return Path.toHtmlString(this);
   }
}
