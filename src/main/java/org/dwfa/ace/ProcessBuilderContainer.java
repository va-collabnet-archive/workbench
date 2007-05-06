package org.dwfa.ace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;

import javax.security.auth.login.LoginException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.bpa.gui.ProcessBuilderPanel;
import org.dwfa.bpa.worker.MasterWorker;

public class ProcessBuilderContainer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ProcessBuilderContainer(Configuration config)
			throws ConfigurationException, LoginException, IOException,
			PrivilegedActionException, IntrospectionException,
			InvocationTargetException, IllegalAccessException,
			PropertyVetoException, ClassNotFoundException,
			NoSuchMethodException {
		super(new GridBagLayout());
		MasterWorker processWorker = new MasterWorker(config);
		ProcessBuilderPanel processBuilderPanel = new ProcessBuilderPanel(
				config, processWorker);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(getProcessBuilderTopPanel(processBuilderPanel), c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(processBuilderPanel, c);

	}
	private static JPanel getProcessBuilderTopPanel(ProcessBuilderPanel processBuilderPanel) {
		JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		addActionButton(processBuilderPanel.getNewProcessActionListener(), 
				"/32x32/plain/bullet_triangle_blue.png",
				"new process",
				listEditorTopPanel, c);
		addActionButton(processBuilderPanel.getReadProcessActionListener(), 
				"/32x32/plain/bullet_triangle_blue.png",
				"read process",
				listEditorTopPanel, c);
		
		addActionButton(processBuilderPanel.getTakeNoTranProcessActionListener(), 
				"/32x32/plain/bullet_triangle_blue.png",
				"take process (no transaction)",
				listEditorTopPanel, c);
		
		addActionButton(processBuilderPanel.getSaveProcessActionListener(), 
				"/32x32/plain/bullet_triangle_blue.png",
				"save process",
				listEditorTopPanel, c);
		
		addActionButton(processBuilderPanel.getSaveForLauncherQueueActionListener(), 
				"/32x32/plain/bullet_triangle_blue.png",
				"save for queue",
				listEditorTopPanel, c);
		
		addActionButton(processBuilderPanel.getSaveAsXmlActionListener(), 
				"/32x32/plain/bullet_triangle_blue.png",
				"save as XML",
				listEditorTopPanel, c);
		c.weightx = 1.0;
		listEditorTopPanel.add(new JLabel(" "), c); //filler
		c.gridx++;
		c.weightx = 0.0;
		listEditorTopPanel.add(new JLabel(" "), c); //right sided buttons
		return listEditorTopPanel;

	}
	private static void addActionButton(ActionListener actionListener, 
			String resource,
			String tooltipText,
			JPanel topPanel, GridBagConstraints c) {
		JButton newProcess = new JButton(new ImageIcon(
				ACE.class.getResource(resource)));
		newProcess.setToolTipText(tooltipText);
		newProcess.addActionListener(actionListener);
		topPanel.add(newProcess, c);
		c.gridx++;
	}

}
