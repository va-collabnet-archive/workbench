package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;

import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetBooleanRevision extends TkRefexBooleanRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetBooleanRevision() {
      super();
   }

   public ERefsetBooleanRevision(I_ExtendByRefPartBoolean part) throws IOException {
      boolean1 = part.getBooleanValue();
      pathUuid     = Terms.get().nidToUuid(part.getPathNid());
      statusUuid   = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time         = part.getTime();
   }

   public ERefsetBooleanRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
