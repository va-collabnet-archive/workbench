package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidCidStrRevision extends TkRefexUuidUuidStringRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidCidStrRevision() {
      super();
   }

   public ERefsetCidCidStrRevision(I_ExtendByRefPartCidCidString part) throws IOException {
      uuid1      = Terms.get().nidToUuid(part.getC1id());
      uuid2      = Terms.get().nidToUuid(part.getC2id());
      string1 = part.getStringValue();
      pathUuid    = Terms.get().nidToUuid(part.getPathNid());
      statusUuid  = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time        = part.getTime();
   }

   public ERefsetCidCidStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
