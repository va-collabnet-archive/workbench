package org.dwfa.bpa.gui;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;

public interface I_HandleDoubleClickInTaskProcess {

	public void handle(I_EncodeBusinessProcess process, I_Work worker, I_EncodeBusinessProcess parentProcess);

}
