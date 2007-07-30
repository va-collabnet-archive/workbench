package org.dwfa.ace.task.signpost;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/gui/signpost", type = BeanType.TASK_BEAN) })
public class SetSignpostDetailAndInstruction extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String detailHtmlPropName = ProcessAttachmentKeys.HTML_DETAIL.getAttachmentKey();

    private String instructionHtmlPropName = ProcessAttachmentKeys.HTML_INSTRUCTION.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(detailHtmlPropName);
        out.writeObject(instructionHtmlPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
            detailHtmlPropName = (String) in.readObject();
            instructionHtmlPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
                    .name());
            final String detailHtmlStr = (String) process.readProperty(detailHtmlPropName);
            final String instructionHtmlStr = (String) process.readProperty(instructionHtmlPropName);
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
                    JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
                    tabs.addTab("instruction", new JScrollPane(new JLabel(instructionHtmlStr)));
                    tabs.addTab("detail", new JScrollPane(new JLabel(detailHtmlStr)));
                    signpostPanel.add(tabs, c);
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
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
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

	public String getInstructionHtmlPropName() {
		return instructionHtmlPropName;
	}

	public void setInstructionHtmlPropName(String instructionHtmlPropName) {
		this.instructionHtmlPropName = instructionHtmlPropName;
	}

	public String getDetailHtmlPropName() {
		return detailHtmlPropName;
	}

	public void setDetailHtmlPropName(String detailHtmlPropName) {
		this.detailHtmlPropName = detailHtmlPropName;
	}
}
