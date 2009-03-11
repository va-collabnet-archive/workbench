package org.dwfa.bpa.gui;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;

public class SimpleMessageRenderer implements I_RenderMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String source = "";
	public SimpleMessageRenderer(String source) {
		this.source = source;
	}

	public void renderMessage(I_EncodeBusinessProcess process, I_Work worker) {
		/* Nothing to do for this message, other renderers 
		 * might do variable substution and XSLT for example.
		 */
	}

	public void setRenderSource(String source) {
		this.source = source;
	}
	public void setRenderSource() {

	}

	public String getMessage() {
		return source;
	}

}
