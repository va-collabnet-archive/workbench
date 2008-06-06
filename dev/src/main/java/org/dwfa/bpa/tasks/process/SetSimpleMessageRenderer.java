package org.dwfa.bpa.tasks.process;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.gui.SimpleMessageRenderer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

@BeanList(specs = 
{ @Spec(directory = "tasks/set tasks", type = BeanType.TASK_BEAN)})
public class SetSimpleMessageRenderer extends AbstractTask {

	private String source = "renderSource";

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(source);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			source = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public SetSimpleMessageRenderer() {
		super();
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		I_RenderMessage renderer = new SimpleMessageRenderer(source);
		process.setMessageRenderer(renderer);
		renderer.renderMessage(process, worker);
		return Condition.CONTINUE;
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	/**
	 * @return Returns the processTaskId.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param processTaskId
	 *            The processTaskId to set.
	 */
	public void setSource(String newName) {
		this.source = newName;
	}
}
