package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

import java.util.ArrayList;

public class ERefsetBooleanMember extends TkRefexBooleanMember {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetBooleanMember() {
      super();
   }

   public ERefsetBooleanMember(I_ExtendByRef m) throws IOException {
      if (I_Identify.class.isAssignableFrom(m.getClass())) {
         EConcept.convertId((I_Identify) m, this);
      } else {
         EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
      }

      int partCount = m.getMutableParts().size();

      refsetUuid    = Terms.get().nidToUuid(m.getRefsetId());
      componentUuid = Terms.get().nidToUuid(m.getComponentNid());

      I_ExtendByRefPartBoolean part = (I_ExtendByRefPartBoolean) m.getMutableParts().get(0);

      boolean1 = part.getBooleanValue();
      pathUuid     = Terms.get().nidToUuid(part.getPathNid());
      statusUuid   = Terms.get().nidToUuid(part.getStatusNid());
      authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
      moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
      time         = part.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefexBooleanRevision>(partCount - 1);

         for (int i = 1; i < partCount; i++) {
            revisions.add(new ERefsetBooleanRevision((I_ExtendByRefPartBoolean) m.getMutableParts().get(i)));
         }
      }
   }

   public ERefsetBooleanMember(I_ExtendByRefVersion m) throws IOException {
       super(m);
   }

   public ERefsetBooleanMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
