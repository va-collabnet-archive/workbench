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
package org.dwfa.ace.task.profile.cap;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile/cap", type = BeanType.TASK_BEAN) })
public class SetNewCapUserReleaseDate extends PreviousNextOrCancel {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    protected transient JTextField releaseDate;
    protected transient JLabel instruction;
    private String releaseDatePropName = ProcessAttachmentKeys.RELEASE_DATE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(releaseDatePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	releaseDatePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    protected void setupInput(I_EncodeBusinessProcess process) {
        instruction = new JLabel("Enter the release date for the paths: ");
        releaseDate = new JTextField("yyyymmdd");
        releaseDate.selectAll();
    }

	protected void readInput(I_EncodeBusinessProcess process) {

		try {
			process.setProperty(releaseDatePropName, releaseDate.getText().trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	
    private boolean testDateValidity(String s) {
		String date = s.substring(6);
    	return date.equalsIgnoreCase("31");
	}

	private boolean testMonthValidity(String s) {
		String date = s.substring(4, 6);
    	return date.equalsIgnoreCase("01") || date.equalsIgnoreCase("07");
	}

	private boolean testYearValidity(String date) {
    	return date.substring(0, 2).equalsIgnoreCase("20");
	}

	private boolean isIntegerString(String date) {
		try {
			Integer yy = Integer.parseInt(date);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

    @Override
    protected boolean showPrevious() {
        return true;
    }

	@Override
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
	}

    private class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected Boolean construct() throws Exception {
            setup(process);
            setupInput(process);
            return true;
        }

        @Override
        protected void finished() {
            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                workflowPanel.remove(components[i]);
            }
            workflowPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            workflowPanel.add(instruction, c);
            c.weightx = 0.0;
            c.gridx++;
            c.weightx = 1.0;
            workflowPanel.add(releaseDate, c);
            c.gridx++;
            c.weightx = 0.0;
            setupPreviousNextOrCancelButtons(workflowPanel, c);
            workflowPanel.setVisible(true);
            releaseDate.requestFocusInWindow();
        }

    }

    @Override
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
        try {
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }

            readInput(process);
            restore();

        } catch (Exception e) {
            throw new TaskFailedException(e);
        } 
        
        return returnCondition;	
    }
    
    public String getReleaseDatePropName() {
        return releaseDatePropName;
    }

    public void setReleaseDatePropName(String prop) {
        this.releaseDatePropName = prop;
    }

    @Override
    public String getInvalidInputMessage() {
        return "Release Date must be in yyyymmdd format.";
    }

    @Override
    public boolean hasValidInput() {
		String date = releaseDate.getText().trim();
		if (date.length() != 8 ||
			!isIntegerString(date) || 
			!testYearValidity(date) ||
			!testMonthValidity(date) ||
			!testDateValidity(date)) {
			return false;
		} else {
			return true;
		}
	}
}
