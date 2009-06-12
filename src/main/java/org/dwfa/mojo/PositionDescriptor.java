package org.dwfa.mojo;

import org.dwfa.ace.api.ConceptDescriptor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class PositionDescriptor {
   private ConceptDescriptor path;
   private String timeString;
   public ConceptDescriptor getPath() {
      return path;
   }
   public void setPath(ConceptDescriptor path) {
      this.path = path;
   }
   public String getTimeString() {
      return timeString;
   }
   public void setTimeString(String timeString) {
      this.timeString = timeString;
   }
   
   public I_Position getPosition() throws Exception {
      I_GetConceptData pathConcept = path.getVerifiedConcept();
      I_Path pathForPosition = LocalVersionedTerminology.get().getPath(pathConcept.getUids());
      int version = ThinVersionHelper.convert(timeString);
      return LocalVersionedTerminology.get().newPosition(pathForPosition, version);
   }
   
   public String toString() {
	   return "Path: " + path + " position: " + timeString;
   }
}
