package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetIntRevision extends TkRefexIntRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetIntRevision() {
      super();
   }

   public ERefsetIntRevision(I_ExtendByRefPartInt part) throws IOException {
      int1   = part.getIntValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathNid());
      statusUuid = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time       = part.getTime();
   }

   public ERefsetIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
