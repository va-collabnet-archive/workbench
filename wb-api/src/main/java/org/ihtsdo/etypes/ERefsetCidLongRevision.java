package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;

import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidLongRevision extends TkRefsetCidLongRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidLongRevision() {
      super();
   }

   public ERefsetCidLongRevision(I_ExtendByRefPartCidLong part) throws IOException {
      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      longValue  = part.getLongValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      time       = part.getTime();
   }

   public ERefsetCidLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
