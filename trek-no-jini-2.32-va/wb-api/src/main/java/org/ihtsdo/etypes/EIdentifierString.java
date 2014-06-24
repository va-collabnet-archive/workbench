package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;

import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierString;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class EIdentifierString extends TkIdentifierString {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public EIdentifierString() {
      super();
   }

   public EIdentifierString(I_IdPart idp) throws IOException {
      denotation    = (String) idp.getDenotation();
      authorityUuid = Terms.get().nidToUuid(idp.getAuthorityNid());
      pathUuid      = Terms.get().nidToUuid(idp.getPathNid());
      statusUuid    = Terms.get().nidToUuid(idp.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(idp.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(idp.getModuleNid());
      time          = idp.getTime();
   }

   public EIdentifierString(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
