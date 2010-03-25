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
package org.dwfa.ace.task.signpost;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
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
 * How do I begin tuning performance?<br>
 * <br>
 * 
 * Gathering environment statistics is a useful first step to doing JE
 * performance tuning. Execute the following code snippet periodically to
 * display statistics for the past period and and to reset statistics counters
 * for the next display. <br>
 * <code><br>
StatsConfig config = new StatsConfig();<br>
config.setClear(true);<br>
<br>
System.err.println(env.getStats(config));
</code><br>
 * The Javadoc for com.sleepycat.je.EnvironmentStats describes each field. Cache
 * behavior can have a major effect on performance, and nCacheMiss is an
 * indicator of how hot the cache is. You may want to adjust the cache size,
 * data access pattern, or cache eviction policy and monitor nCacheMiss. <br>
 * <br>
 * Applications which use transactions may want to check nFSyncs to see how many
 * of these costly system calls have been issued. Experimenting with other
 * flavors of commit durability, like TxnWriteNoSync and TxnNoSync can improve
 * performance. <br>
 * <br>
 * nCleanerRuns and cleanerBacklog are indicators of log cleaning activity.
 * Adjusting the property je.cleaner.minUtilization can increase or decrease log
 * cleaning. The user may also elect to do batch log cleaning, as described in
 * the Javadoc for Environment.cleanLog(), to control when log cleaning occurs. <br>
 * <br>
 * High values for nRepeatFaultReads and nRepeatIteratorReads may indicate
 * non-optimal read buffer sizes. See the FAQ entry on configuring read buffers.
 * 
 * @author kec
 * @see http://www.oracle.com/technology/products/berkeley-db/faq/je_faq.html
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/gui/signpost", type = BeanType.TASK_BEAN) })
public class SetSignpostToDbStats extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            final String htmlStr = LocalVersionedTerminology.get().getStats();
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            final JPanel signpostPanel = config.getSignpostPanel();
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = signpostPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        signpostPanel.remove(components[i]);
                    }
                    signpostPanel.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.weightx = 1.0;
                    c.weighty = 1.0;
                    c.anchor = GridBagConstraints.NORTHWEST;
                    JEditorPane htmlPane = new JEditorPane("text/html", htmlStr);
                    htmlPane.setEditable(false);
                    signpostPanel.add(new JScrollPane(htmlPane), c);
                    signpostPanel.validate();
                    Container cont = signpostPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                }
            });
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
