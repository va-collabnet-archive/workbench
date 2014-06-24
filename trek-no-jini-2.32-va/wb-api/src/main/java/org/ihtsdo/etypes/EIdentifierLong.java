package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;

import org.ihtsdo.tk.dto.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class EIdentifierLong extends TkIdentifierLong {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public EIdentifierLong() {
      super();
   }

   public EIdentifierLong(I_IdPart idp) throws IOException {
      super();
      denotation    = (Long) idp.getDenotation();
      authorityUuid = Terms.get().nidToUuid(idp.getAuthorityNid());
      pathUuid      = Terms.get().nidToUuid(idp.getPathNid());
      statusUuid    = Terms.get().nidToUuid(idp.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(idp.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(idp.getModuleNid());
      time          = idp.getTime();
   }

   public EIdentifierLong(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
      denotation = in.readLong();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.LONG;
   }
}
