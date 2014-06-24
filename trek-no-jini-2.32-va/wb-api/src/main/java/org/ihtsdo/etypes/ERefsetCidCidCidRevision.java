package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;

import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidCidCidRevision extends TkRefexUuidUuidUuidRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidCidRevision() {
      super();
   }

   public ERefsetCidCidCidRevision(I_ExtendByRefPartCidCidCid part) throws IOException {
      uuid1     = Terms.get().nidToUuid(part.getC1id());
      uuid2     = Terms.get().nidToUuid(part.getC2id());
      uuid3     = Terms.get().nidToUuid(part.getC3id());
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();
   }

   public ERefsetCidCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
