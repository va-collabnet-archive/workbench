package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;

import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidCidCidRevision extends TkRefsetCidCidCidRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidCidRevision() {
      super();
   }

   public ERefsetCidCidCidRevision(I_ExtendByRefPartCidCidCid part) throws IOException {
      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      c2Uuid     = Terms.get().nidToUuid(part.getC2id());
      c3Uuid     = Terms.get().nidToUuid(part.getC3id());
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      time       = part.getTime();
   }

   public ERefsetCidCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
