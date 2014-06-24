package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.ArrayList;

public class ERefsetCidCidCidMember extends TkRefexUuidUuidUuidMember {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidCidMember() {
      super();
   }

   public ERefsetCidCidCidMember(I_ExtendByRef m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      int partCount = m.getMutableParts().size();

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentNid());

      I_ExtendByRefPartCidCidCid part = (I_ExtendByRefPartCidCidCid) m.getMutableParts().get(0);

      uuid1     = Terms.get().nidToUuid(part.getC1id());
      uuid2     = Terms.get().nidToUuid(part.getC2id());
      uuid3     = Terms.get().nidToUuid(part.getC3id());
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefexUuidUuidUuidRevision>(partCount - 1);

         for (int i = 1; i < partCount; i++) {
            revisions.add(
                new ERefsetCidCidCidRevision((I_ExtendByRefPartCidCidCid) m.getMutableParts().get(i)));
         }
      }
   }

   public ERefsetCidCidCidMember(I_ExtendByRefVersion m) throws IOException {
       super(m);
   }

   public ERefsetCidCidCidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
