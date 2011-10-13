package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;

import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidIntRevision extends TkRefsetCidIntRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidIntRevision() {
      super();
   }

   public ERefsetCidIntRevision(I_ExtendByRefPartCidInt part) throws IOException {
      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      intValue   = part.getIntValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      time       = part.getTime();
   }

   public ERefsetCidIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
