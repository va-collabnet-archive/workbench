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
/*
 * Created on Apr 19, 2005
 */
package org.dwfa.bpa.tasks.pwdbreaker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.gui.FieldInputPanel;
import org.dwfa.bpa.gui.InstructionPanel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.ComponentFrame;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/grid/pwdbreaker", type = BeanType.TASK_BEAN) })
public class ConfigureCryptBreakerWorkspace extends AbstractTask {

    public static UUID WORKSPACE_ID = UUID.fromString("f6013206-04e6-4bad-bb19-3733dba84b0b");

    public static final String INPUT = "Input parameters";

    public static final String INSTRUCTION = "Instruction";

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * 
     */
    public ConfigureCryptBreakerWorkspace() {
        super();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(ConfigureCryptBreakerWorkspace.dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            if (worker.isWorkspaceActive(WORKSPACE_ID) == false) {
                I_Workspace ws = worker.createWorkspace(WORKSPACE_ID, "Password Breaker Workspace", null);
                FieldInputPanel fip = new FieldInputPanel(INPUT, ws);
                fip.setGridx(0);
                fip.setGridheight(4);
                fip.setTitle(INPUT);
                fip.setName(INPUT);
                String[] labels = new String[] { "Salt:", "Word:", "Tries per task:", "High mark:", "Low mark:" };
                String[] defaults = new String[] { "js", "duke", "10000", "20", "10" };
                fip.setFields(1, labels, defaults);
                fip.setCompleteLabel("execute");
                ws.addGridBagPanel(fip);

                ws.setStatusMessage("<HTML><font color='blue'>Welcome to the password breaker workspace...");

                /**/
                InstructionPanel instruction = new InstructionPanel(INSTRUCTION, ws);
                instruction.setTitle(INSTRUCTION);
                instruction.setName(INSTRUCTION);
                instruction.showButtons(false);
                instruction.setGridx(1);
                instruction.setGridy(0);
                instruction.setGridheight(4);
                instruction.setGridwidth(2);

                StringBuffer buff = new StringBuffer();
                buff.append("<html><h1><font color='blue'>Parallel Algorithm Execution on a Distributed Grid</font></h1>");
                buff.append("This process demonstrates that workflow process can utilize a distributed computing grid");
                buff.append(" for computations that may overwhelm a single resource (or for which faster parallel execution is desired).");
                buff.append("<p>This jini-based workflow architecture");
                buff.append(" utilizes a <i>replicated-worker pattern</i> (sometimes referred to as the \"master-worker pattern\")");
                buff.append(" where workflow tasks are executed by a master worker. A specific <i>master task</i> may incorporate a parallel");
                buff.append(" algorithm, and cause that algorithm's execution on a distributed compute grid. The master task acquires a reference to a JavaSpace");
                buff.append(" from the master worker, and then populates that JavaSpace with <i>generic tasks</i> that are then executed by generic workers on the computing grid.");
                buff.append(" These generic workers deposit results back into the java space for collection by the master task. ");
                buff.append("<p>The master task controls the size and number of generic tasks placed on the grid to minimize the communications overhead ");
                buff.append(" associated with parallel execution, and to prevent the JavaSpace from being overwhelmed by generic tasks. In this example, ");
                buff.append(" the master task uses the following user-specified input parameters: ");

                buff.append("<dl><dt><b>Tries per task</b><dd><i>A paramater that controls the size (number of computations) of each generic task.</i>");
                buff.append("<dt><b>High mark</b><dd><i>The maximum number of generic tasks to write to the JavaSpace.</i>");
                buff.append("<dt><b>Low mark</b><dd><i>The point at which more tasks should be added to the JavaSpace. ");
                buff.append("The low mark should be greater than the number of available workers to ensure maximum utilization of the computing grid.</i>");
                buff.append("</dl>In this example, the tries per task, high mark, and low mark are statically specified before the grid computation is");
                buff.append(" initiated, but a master task could manage these parameters dynamically based on performance of the grid. ");
                buff.append("<p>This process uses a computing grid to break encrypted passwords that where encrypted using a <i>one-way encryption algorithm</i>, ");
                buff.append(" The parallel algorithm will break passwords (up to 4 characters) using a brute force technique of trying every possible combination of");
                buff.append(" ASCII characters that makes up a password. ");
                buff.append("<p>The encryption scheme is based on the UNIX password encryption scheme that takes a password and then prepends two characters of ");
                buff.append(" \"salt\"--an arbitrary sequence that is prepended to prohibit simple dictionary schemes of password breaking. These data elements are");
                buff.append(" input with the following user-specified input parameters: ");
                buff.append("<dl><dt><b>Salt</b><dd><i>A two character sequence that will be prepended to the password prior to encryption.</i>");
                buff.append("<dt><b>Word</b><dd><i>The password to encrypt and subsequenctly try to break using a parallel brute-force algorithm.</i></dl>");
                buff.append("<p>When you have input the desired parameters, press the \"complete\" button on the \"Input parameters\" window to initiate the ");
                buff.append(" grid computation. Progress (and eventually the results) of the computation will be displayed in this window. ");
                buff.append("");

                instruction.setInstruction(buff.toString());

                ws.addGridBagPanel(instruction);
                ws.setWorkspaceBounds(0, 0, ComponentFrame.getDefaultFrameSize().width,
                    ComponentFrame.getDefaultFrameSize().height);

            }
            I_Workspace ws = worker.getWorkspace(WORKSPACE_ID);
            ws.setWorkspaceVisible(true);
            ws.bringToFront();
            return Condition.CONTINUE;
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }
}
