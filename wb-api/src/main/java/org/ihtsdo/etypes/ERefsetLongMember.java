package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.ArrayList;

public class ERefsetLongMember extends TkRefsetLongMember {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetLongMember() {
      super();
   }

   public ERefsetLongMember(I_ExtendByRef m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      int partCount = m.getMutableParts().size();

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentNid());

      I_ExtendByRefPartLong part = (I_ExtendByRefPartLong) m.getMutableParts().get(0);

      longValue  = part.getLongValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      time       = part.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetLongRevision>(partCount - 1);

         for (int i = 1; i < partCount; i++) {
            revisions.add(new ERefsetLongRevision((I_ExtendByRefPartLong) m.getMutableParts().get(i)));
         }
      }
   }

   public ERefsetLongMember(I_ExtendByRefVersion m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentId());

      I_ExtendByRefPartLong part = (I_ExtendByRefPartLong) m.getMutablePart();

      longValue  = part.getLongValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      time       = part.getTime();
   }

   public ERefsetLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
