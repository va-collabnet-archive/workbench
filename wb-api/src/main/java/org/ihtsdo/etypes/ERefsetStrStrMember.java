package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStrStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrRevision;

public class ERefsetStrStrMember extends TkRefsetStrStrMember {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetStrStrMember() {
      super();
   }

   public ERefsetStrStrMember(I_ExtendByRef m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      int partCount = m.getMutableParts().size();

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentNid());

      I_ExtendByRefPartStrStr part = (I_ExtendByRefPartStrStr) m.getMutableParts().get(0);

      string1   = part.getString1Value();
      string2   = part.getString1Value();
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetStrStrRevision>(partCount - 1);

         for (int i = 1; i < partCount; i++) {
            revisions.add(new ERefsetStrStrRevision((I_ExtendByRefPartStrStr) m.getMutableParts().get(i)));
         }
      }
   }

   public ERefsetStrStrMember(I_ExtendByRefVersion m) throws IOException {
       super(m);
   }

   public ERefsetStrStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
