/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.reporting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Updates dialect variant file based on text file.
 * 
 * @author akf
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class CreateDescriptionsDiffRefsets extends AbstractTask {

	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	/** The done. */
	private transient boolean done = false;
	protected DescriptionDiffAttrDialog dialog;
	private boolean canceled;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do...
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		done = false;
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					try {
						dialog = new DescriptionDiffAttrDialog(CreateDescriptionsDiffRefsets.this);
						dialog.createAndShowGui();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			synchronized (this) {
				this.waitTillDone(worker.getLogger());
			}
			if (!canceled) {
				Calendar c = new GregorianCalendar();
				String refsetNamePrefix = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " "  +c.get(Calendar.YEAR);
				String initialTime1 = dialog.getInitTime();// "2011.01.31 23:59:59 CST";
				String laterTime2 = dialog.getLaterTime();// "2011.07.31 23:59:59 CST";
				UUID path1UUID = dialog.getUUID1();// UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
				UUID path2UUID = dialog.getUUID2();// .fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
				DescriptionsDiffComputer diff = new DescriptionsDiffComputer(Terms.get().getActiveAceFrameConfig(), refsetNamePrefix, initialTime1, laterTime2, path1UUID, path2UUID);
				diff.setup();
				diff.run();
			}
			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public void ok() {
		canceled = false;
		done = true;
		synchronized (CreateDescriptionsDiffRefsets.this) {
			CreateDescriptionsDiffRefsets.this.notifyAll();
		}
	}

	public void cancel() {
		canceled = false;
		done = true;
		synchronized (CreateDescriptionsDiffRefsets.this) {
			CreateDescriptionsDiffRefsets.this.notifyAll();
		}
	}

	/**
	 * Checks if is done.
	 * 
	 * @return true, if is done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Wait till done.
	 * 
	 * @param l
	 *            the l
	 */
	private void waitTillDone(Logger l) {
		while (!this.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				l.log(Level.SEVERE, e.getMessage(), e);
			}
		}

	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

}
