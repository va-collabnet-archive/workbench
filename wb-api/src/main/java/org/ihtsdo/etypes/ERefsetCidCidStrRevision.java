package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidCidStrRevision extends TkRefsetCidCidStrRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidStrRevision() {
      super();
   }

   public ERefsetCidCidStrRevision(I_ExtendByRefPartCidCidString part) throws IOException {
      c1Uuid      = Terms.get().nidToUuid(part.getC1id());
      c2Uuid      = Terms.get().nidToUuid(part.getC2id());
      stringValue = part.getStringValue();
      pathUuid    = Terms.get().nidToUuid(part.getPathId());
      statusUuid  = Terms.get().nidToUuid(part.getStatusId());
      time        = part.getTime();
   }

   public ERefsetCidCidStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
