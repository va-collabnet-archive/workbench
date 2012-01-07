package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.ArrayList;

public class ERefsetCidCidStrMember extends TkRefsetCidCidStrMember {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidStrMember() {
      super();
   }

   public ERefsetCidCidStrMember(I_ExtendByRef m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      int partCount = m.getMutableParts().size();

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentId());

      I_ExtendByRefPartCidCidString part = (I_ExtendByRefPartCidCidString) m.getMutableParts().get(0);

      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      c2Uuid     = Terms.get().nidToUuid(part.getC2id());
      strValue   = part.getStringValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      time       = part.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetCidCidStrRevision>(partCount - 1);

         for (int i = 1; i < partCount; i++) {
            revisions.add(
                new ERefsetCidCidStrRevision((I_ExtendByRefPartCidCidString) m.getMutableParts().get(i)));
         }
      }
   }

   public ERefsetCidCidStrMember(I_ExtendByRefVersion m) throws IOException {
       super(m);
   }

   public ERefsetCidCidStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
