package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;

public class ERefsetCidFloatRevision extends TkRefsetCidFloatRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ERefsetCidFloatRevision() {
      super();
   }

   public ERefsetCidFloatRevision(I_ExtendByRefPartCidFloat part) throws IOException {
      super();
      c1Uuid     = Terms.get().nidToUuid(part.getUnitsOfMeasureId());
      floatValue = (float) part.getMeasurementValue();
      pathUuid   = Terms.get().nidToUuid(part.getPathId());
      statusUuid = Terms.get().nidToUuid(part.getStatusId());
      time       = part.getTime();
   }

   public ERefsetCidFloatRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
   }
}
