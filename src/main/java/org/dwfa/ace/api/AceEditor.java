package org.dwfa.ace.api;

import java.util.List;

public class AceEditor {

   private static List<I_ConfigAceFrame> aceFrames;

   public static List<I_ConfigAceFrame> getAceFrames() {
      return aceFrames;
   }

   public static void setAceFrames(List<I_ConfigAceFrame> aceFrames) {
      AceEditor.aceFrames = aceFrames;
   }

}
