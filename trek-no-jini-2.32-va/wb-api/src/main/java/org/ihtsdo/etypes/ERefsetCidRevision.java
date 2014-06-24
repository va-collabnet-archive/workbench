package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;

import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidRevision extends TkRefexUuidRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidRevision() {
      super();
   }

   public ERefsetCidRevision(I_ExtendByRefPartCid part) throws IOException {
      uuid1     = Terms.get().nidToUuid(part.getC1id());
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();
   }

   public ERefsetCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
