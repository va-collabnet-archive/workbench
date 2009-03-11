package org.dwfa.bpa.process;

import java.io.Serializable;

public interface I_RenderMessage extends Serializable {
	public void renderMessage(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException;
	public void setRenderSource(String source);
	public String getMessage();
}
