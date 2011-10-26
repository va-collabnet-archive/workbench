/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.tasks;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
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

/**
 * The Class RenderUIOnSignpost.
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class RenderUIOnSignpost extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/** The html prop name. */
	private String htmlPropName = ProcessAttachmentKeys.SIGNPOST_HTML.getAttachmentKey();

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(htmlPropName);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			// nothing to read...
			htmlPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
					.name());
			final String htmlStr = (String) process.readProperty(htmlPropName);
			final JPanel signpostPanel = config.getSignpostPanel();
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					Component[] components = signpostPanel.getComponents();
					for (int i = 0; i < components.length; i++) {
						signpostPanel.remove(components[i]);
					}

					/*signpostPanel.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.weightx = 1.0;
                    c.weighty = 1.0;
                    c.anchor = GridBagConstraints.NORTHWEST;
                    signpostPanel.add(new JLabel(htmlStr), c);*/
					signpostPanel.setLayout(new BoxLayout(signpostPanel, BoxLayout.PAGE_AXIS));
					signpostPanel.add(new JTextArea("Probando text area"));
					signpostPanel.add(new JButton("Dos y tres"));
					signpostPanel.add(new JButton("Probando"));
					signpostPanel.validate();
					Container cont = signpostPanel;
					while (cont != null) {
						cont.validate();
						cont = cont.getParent();
					}
				}
			});
			/*I_PluginToConceptPanel treeEditorPlugin = new TreeEditorPlugin(true,99);
			
			config.addConceptPanelPlugins(I_HostConceptPlugins.HOST_ENUM.CONCEPT_PANEL_R1, 
					I_HostConceptPlugins.TOGGLES.TREE_EDITOR.getPluginId(), treeEditorPlugin);
			//config.getConceptPanelPlugin(I_HostConceptPlugins.HOST_ENUM.CONCEPT_PANEL_R1, 
					//I_HostConceptPlugins.TOGGLES.TREE_EDITOR.getPluginId()).showComponent();	*/
			
			I_ConfigAceDb userConfig = config.getDbConfig();
			String guardado = (String) userConfig.getProperty("termmed");
			if (guardado == null) {
				userConfig.setProperty("termmed", "Valor!!!");
				JOptionPane.showMessageDialog(null, "No estaba, seteando...","Result",JOptionPane.INFORMATION_MESSAGE);
			} else  {
				JOptionPane.showMessageDialog(null, "Sii! guardado = " + guardado,"Result",JOptionPane.INFORMATION_MESSAGE);
			}
			
			AceLog.getAppLog().info("Seteando!!");
			
			final JPanel workflowPanel = config.getWorkflowPanel();
			Component[] components = workflowPanel.getComponents();
			for (int i = 0; i < components.length; i++) {
				workflowPanel.remove(components[i]);
			}
			workflowPanel.setLayout(new BorderLayout());
			workflowPanel.add(new JLabel("Label 1:"), BorderLayout.LINE_START);
			workflowPanel.add(new JTextArea("Contenido...."), BorderLayout.LINE_END);
			
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	/**
	 * Gets the html prop name.
	 * 
	 * @return the html prop name
	 */
	public String getHtmlPropName() {
		return htmlPropName;
	}

	/**
	 * Sets the html prop name.
	 * 
	 * @param instruction the new html prop name
	 */
	public void setHtmlPropName(String instruction) {
		this.htmlPropName = instruction;
	}
}
