package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;

import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.UUID;

public class EIdentifierUuid extends TkIdentifierUuid {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public EIdentifierUuid() {
      super();
   }

   public EIdentifierUuid(I_IdPart idp) throws IOException {
      denotation    = (UUID) idp.getDenotation();
      authorityUuid = Terms.get().nidToUuid(idp.getAuthorityNid());
      pathUuid      = Terms.get().nidToUuid(idp.getPathNid());
      statusUuid    = Terms.get().nidToUuid(idp.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(idp.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(idp.getModuleNid());
      time          = idp.getTime();
   }

   public EIdentifierUuid(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
