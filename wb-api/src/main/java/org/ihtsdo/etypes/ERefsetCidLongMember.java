package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.ArrayList;

public class ERefsetCidLongMember extends TkRefsetCidLongMember {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidLongMember() {
      super();
   }

   public ERefsetCidLongMember(I_ExtendByRef m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      int partCount = m.getMutableParts().size();

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentId());

      I_ExtendByRefPartCidLong part = (I_ExtendByRefPartCidLong) m.getMutableParts().get(0);

      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      longValue  = part.getLongValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      time       = part.getTime();

      if (partCount > 1) {
         extraVersions = new ArrayList<TkRefsetCidLongRevision>(partCount - 1);

         for (int i = 1; i < partCount; i++) {
            extraVersions.add(
                new ERefsetCidLongRevision((I_ExtendByRefPartCidLong) m.getMutableParts().get(i)));
         }
      }
   }

   public ERefsetCidLongMember(I_ExtendByRefVersion m) throws IOException {
       super(m);
   }

   public ERefsetCidLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}