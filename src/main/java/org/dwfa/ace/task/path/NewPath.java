package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.status.SetStatusUtil;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * @author Ming Zhang
 *
 * @created 15/01/2008
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/path", type = BeanType.TASK_BEAN) })

/*This task has the same function with the "new path" in the preference panel */
public class NewPath extends AbstractTask {

	
private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   // path's origin, need to be a "real" path
   private TermEntry originPathTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());  
   
   //parent in the hierarchy, it could be any concept
   private TermEntry parentPathTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());

   private String originTime = "latest";

   private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
   
   private String DescriptionForNewPath = "description";

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(originPathTermEntry);
      out.writeObject(originTime);
      out.writeObject(profilePropName);
      out.writeObject(parentPathTermEntry );
      out.writeObject(DescriptionForNewPath);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
    	  originPathTermEntry = (TermEntry)in.readObject();
    	  originTime = (String)in.readObject();
          profilePropName = (String)in.readObject();
          parentPathTermEntry = (TermEntry)in.readObject();
          DescriptionForNewPath = (String)in.readObject();    
         
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      // Nothing to do...

   }

   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      try {
         I_TermFactory tf = LocalVersionedTerminology.get();
         I_ConfigAceFrame activeProfile = tf.getActiveAceFrameConfig();
         Set<I_Path> savedEditingPaths = new HashSet<I_Path>(activeProfile.getEditingPathSet());
         try {
            //create parent of path 
            I_GetConceptData newPathConcept = createComponents(DescriptionForNewPath, tf, activeProfile,parentPathTermEntry);

            tf.commit();
            // create origin of path 
            Set<I_Position> origins = new HashSet<I_Position>();

            I_Path originPath = tf.getPath(originPathTermEntry.ids);
            origins.add(tf.newPosition(originPath, tf.convertToThinVersion(originTime)));

            I_Path editPath = tf.newPath(origins, newPathConcept);
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            profile.getEditingPathSet().clear();
            profile.addEditingPath(editPath);
            profile.getViewPositionSet().clear();
            profile.addViewPosition(tf.newPosition(editPath, Integer.MAX_VALUE));
            tf.commit();

         } catch (Exception e) {
            throw new TaskFailedException(e);
         }
         activeProfile.getEditingPathSet().clear();
         activeProfile.getEditingPathSet().addAll(savedEditingPaths);
         return Condition.CONTINUE;
      } catch (Exception e) {
         throw new TaskFailedException(e);
      }
   }
   
   
   protected static I_GetConceptData createComponents(String Description, I_TermFactory tf, I_ConfigAceFrame activeProfile, 
		   TermEntry parent ) throws NoSuchAlgorithmException, UnsupportedEncodingException, TerminologyException, IOException {
	      
	      UUID type5ConceptId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, Description);

	      I_GetConceptData newPathConcept = tf.newConcept(type5ConceptId, false, activeProfile);

	      I_GetConceptData statusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
	      
	      SetStatusUtil.setStatusOfConceptInfo(statusConcept,newPathConcept.getConceptAttributes().getTuples());
	      
	      I_DescriptionVersioned idv = tf.newDescription(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parent.ids[0] + Description), newPathConcept, "en",
	            Description, ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), activeProfile);
	      SetStatusUtil.setStatusOfDescriptionInfo(statusConcept,idv.getTuples());

	      I_GetConceptData relType = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
	      I_GetConceptData relDestination = tf.getConcept(parent.ids);
	      I_GetConceptData relCharacteristic = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
	            .getUids());
	      I_GetConceptData relRefinability = tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
	      
	      UUID relId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parent.ids[0] + Description + "relid");
	      tf.newRelationship(relId, newPathConcept, relType, relDestination, relCharacteristic, relRefinability,
	            statusConcept, 0, activeProfile);
	      return newPathConcept;
	   }
   public Collection<Condition> getConditions() {
      return CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
      return new int[] {};
   }

   public String getOriginTime() {
      return originTime;
   }

   public void setOriginTime(String originTime) {
      this.originTime = originTime;
   }

   public TermEntry getParentPathTermEntry() {
      return originPathTermEntry;
   }

   public void setParentPathTermEntry(TermEntry parentPath) {
      this.originPathTermEntry = parentPath;
   }

   public String getProfilePropName() {
      return profilePropName;
   }

   public void setProfilePropName(String profilePropName) {
      this.profilePropName = profilePropName;
   }

public TermEntry getOriginPathTermEntry() {
	return originPathTermEntry;
}

public void setOriginPathTermEntry(TermEntry originPathTermEntry) {
	this.originPathTermEntry = originPathTermEntry;
}

public String getDescriptionForNewPath() {
	return DescriptionForNewPath;
}

public void setDescriptionForNewPath(String descriptionForNewPath) {
	DescriptionForNewPath = descriptionForNewPath;
}

}
