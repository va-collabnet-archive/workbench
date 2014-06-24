package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.ArrayList;

public class ERefsetCidLongMember extends TkRefexUuidLongMember {
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
      componentUuid = Terms.get().nidToUuid(m.getComponentNid());

      I_ExtendByRefPartCidLong part = (I_ExtendByRefPartCidLong) m.getMutableParts().get(0);

      uuid1     = Terms.get().nidToUuid(part.getC1id());
      long1  = part.getLongValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();

      if (partCount > 1) {
         extraVersions = new ArrayList<TkRefexUuidLongRevision>(partCount - 1);

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
