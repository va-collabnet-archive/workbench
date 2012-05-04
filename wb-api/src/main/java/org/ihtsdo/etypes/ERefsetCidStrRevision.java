package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;

import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidStrRevision extends TkRefsetCidStrRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidStrRevision() {
      super();
   }

   public ERefsetCidStrRevision(I_ExtendByRefPartCidString part) throws IOException {
      c1Uuid     = Terms.get().nidToUuid(part.getC1id());
      strValue   = part.getStringValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();
   }

   public ERefsetCidStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
