package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;

import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetStrRevision extends TkRefsetStrRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetStrRevision() {
      super();
   }

   public ERefsetStrRevision(I_ExtendByRefPartStr part) throws IOException {
      stringValue = part.getStringValue();
      pathUuid    = Terms.get().nidToUuid(part.getPathId());
      statusUuid  = Terms.get().nidToUuid(part.getStatusId());
      time        = part.getTime();
   }

   public ERefsetStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
