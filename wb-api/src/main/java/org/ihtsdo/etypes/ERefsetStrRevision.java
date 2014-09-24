package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;

import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;

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
      string1 = part.getString1Value();
      pathUuid    = Terms.get().nidToUuid(part.getPathNid());
      statusUuid  = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time        = part.getTime();
   }

   public ERefsetStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
