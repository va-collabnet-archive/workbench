package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartStrStr;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrRevision;

public class ERefsetStrStrRevision extends TkRefsetStrStrRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetStrStrRevision() {
      super();
   }

   public ERefsetStrStrRevision(I_ExtendByRefPartStrStr part) throws IOException {
      string1 = part.getString1Value();
      string2 = part.getString2Value();
      pathUuid    = Terms.get().nidToUuid(part.getPathNid());
      statusUuid  = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time        = part.getTime();
   }

   public ERefsetStrStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
