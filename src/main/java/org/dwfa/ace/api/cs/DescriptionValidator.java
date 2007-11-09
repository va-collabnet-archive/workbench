package org.dwfa.ace.api.cs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.TerminologyException;

public class DescriptionValidator extends SimpleValidator {

   @Override
   protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf) throws IOException, TerminologyException {
      /*
       * The universal bean descriptions must be converted and compared with a thin descriptios from the term factory.
       * This validator will return false if, for each description in the UniverasalAceBean:
       * 1. The concept ids are not equal
       * 2. One of the starting descriptions (descriptions whose time is not Long.MAX_VALUE)
       * 3. The number of starting descriptions equals the number of descriptions
       */
      for (UniversalAceDescription desc : bean.getDescriptions()) {
         Set<I_DescriptionPart> startParts = new HashSet<I_DescriptionPart>();
         I_DescriptionVersioned thinDesc = tf.getDescription(tf.uuidToNative(desc.getDescId()));
         if (thinDesc.getConceptId() != tf.uuidToNative(desc.getConceptId())) {
            return false; // Test 1
         }
         for (UniversalAceDescriptionPart part : desc.getVersions()) {
             if (part.getTime() != Long.MAX_VALUE) {
                I_DescriptionPart newPart = tf.newDescriptionPart();
                 newPart.setInitialCaseSignificant(part.getInitialCaseSignificant());
                 newPart.setLang(part.getLang());
                 newPart.setPathId(tf.uuidToNative(part.getPathId()));
                 newPart.setStatusId(tf.uuidToNative(part.getStatusId()));
                 newPart.setText(part.getText());
                 newPart.setTypeId(tf.uuidToNative(part.getTypeId()));
                 newPart.setVersion(tf.convertToThinVersion(part.getTime()));

                 startParts.add(newPart);
                 if (thinDesc.getVersions().contains(newPart) == false) {
                     return false; //test 2
                 }
             }
         }
         if (startParts.size() != thinDesc.getVersions().size()) {
            System.out.println("number of description parts is different");
            return false; // test 3
         }
      }

      //passed all tests for all descriptions
      return true;
   }

}
