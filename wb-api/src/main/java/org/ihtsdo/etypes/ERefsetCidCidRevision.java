package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidCidRevision extends TkRefsetCidCidRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidRevision() {
      super();
   }

   public ERefsetCidCidRevision(I_ExtendByRefPartCidCid part) throws IOException {
      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      c2Uuid     = Terms.get().nidToUuid(part.getC2id());
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      time       = part.getTime();
   }

   public ERefsetCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }
}
