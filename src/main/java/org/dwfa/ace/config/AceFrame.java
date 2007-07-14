package org.dwfa.ace.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.jini.config.ConfigurationException;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;

import com.sun.jini.start.LifeCycle;

public class AceFrame extends ComponentFrame {

	private ACE cdePanel;

	private class AceWindowActionListener implements WindowListener {

		public void windowActivated(WindowEvent e) {
			doWindowActivation();
		}

		public void windowClosed(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowOpened(WindowEvent e) {
			doWindowActivation();
		}

	}

	public AceFrame(String[] args, LifeCycle lc, I_ConfigAceFrame frameConfig,
			boolean executeStartupProcesses) throws Exception {
		super(args, lc);
		((AceFrameConfig) frameConfig).setAceFrame(this);
		getCdePanel().setup(frameConfig);
		setName(frameConfig.getFrameName());
		setTitle("User: " + frameConfig.getUsername());
		setContentPane(cdePanel);
		setBounds(frameConfig.getBounds());
		MasterWorker worker = new MasterWorker(config);
		cdePanel.getAceFrameConfig().setWorker(worker);
		doWindowActivation();
		getQuitList().add(cdePanel);
		this.addWindowListener(new AceWindowActionListener());
		if (executeStartupProcesses) {
			File configFile = ((AceFrameConfig) frameConfig).getMasterConfig()
					.getConfigFile();
			File startupFolder = new File(configFile.getParentFile().getParentFile(), "startup");
			executeStartupProcesses(worker, startupFolder);
			startupFolder = new File(configFile.getParentFile(), "startup");
			executeStartupProcesses(worker, startupFolder);
		}

	}

	private void executeStartupProcesses(MasterWorker worker, File startupFolder) {
		if (startupFolder.exists()) {
			AceLog.getAppLog().info("Startup folder exists: " + startupFolder);
			File[] startupFiles = startupFolder
					.listFiles(new FilenameFilter() {

						public boolean accept(File dir, String name) {
							return name.endsWith(".bp");
						}
					});
			if (startupFiles != null) {
				for (int i = 0; i < startupFiles.length; i++) {
					try {
						AceLog.getAppLog().info(
								"Executing startup business process: "
										+ startupFiles[i]);
						FileInputStream fis = new FileInputStream(
								startupFiles[i]);
						BufferedInputStream bis = new BufferedInputStream(
								fis);
						ObjectInputStream ois = new ObjectInputStream(bis);
						I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
								.readObject();
						worker.execute(process);
						AceLog.getAppLog().info(
								"Finished startup business process: "
										+ startupFiles[i]);
					} catch (Throwable e1) {
						AceLog.getAppLog().alertAndLog(
								Level.SEVERE,
								e1.getMessage() + " thrown by "
										+ startupFiles[i], e1);
					}
				}
			} else {
				AceLog.getAppLog().info(
						"No startup processes found. Folder exists: "
								+ startupFolder.exists());
			}
		} else {
			AceLog.getAppLog().info("NO startup folder exists: " + startupFolder);
		}
	}

	private void doWindowActivation() {
		try {
			AceConfig.getVodb().setActiveAceFrameConfig(
					getCdePanel().getAceFrameConfig());
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
		getCdePanel().addToMenuBar(mainMenuBar, cfb.getEditMenu());
	}

	/**
	 * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
	 */
	public JMenu getQuitMenu() {
		return getCdePanel().getFileMenu();
	}

	public void addInternalFrames(JMenu menu) {

	}

	/**
	 * @see org.dwfa.bpa.util.ComponentFrame#getCount()
	 */
	public int getCount() {
		return count;
	}

	private static int count = 0;

	public class NewAceFrame implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				AceFrame newFrame = new AceFrame(getArgs(), getLc(), cdePanel
						.getAceFrameConfig(), false);
				newFrame.setTitle(getNextFrameName());
				newFrame.setVisible(true);
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}

		}

	}

	public JMenuItem getNewWindowMenu() {
		JMenuItem newWindow = new JMenuItem("Ace Viewer");
		newWindow.addActionListener(new NewAceFrame());
		return newWindow;
	}

	public String getNextFrameName() throws ConfigurationException {
		String title = (String) config.getEntry(this.getClass().getName(),
				"frameName", String.class, "Ace Viewer");
		if (count > 0) {
			return title + " " + count++;
		}
		count++;
		return title;
	}

	public ACE getCdePanel() {
		if (cdePanel == null) {
			cdePanel = new ACE(config);
		}
		return cdePanel;
	}

	public JList getBatchConceptList() {
		return getCdePanel().getBatchConceptList();
	}

	public void performLuceneSearch(String query, I_GetConceptData root) {
		getCdePanel().performLuceneSearch(query, root);

	}

	public void setShowAddresses(boolean show) {
		getCdePanel().setShowAddresses(show);

	}

	public void setShowComponentView(boolean show) {
		getCdePanel().setShowComponentView(show);
	}

	public void setShowHierarchyView(boolean show) {
		getCdePanel().setShowHierarchyView(show);

	}

	public void setShowHistory(boolean show) {
		getCdePanel().setShowHistory(show);
	}

	public void setShowPreferences(boolean show) {
		getCdePanel().setShowPreferences(show);

	}

	public void setShowSearch(boolean show) {
		getCdePanel().setShowSearch(show);

	}

	public void showListView() {
		getCdePanel().showListView();
	}

	public void setupSvn() {
		getCdePanel().setupSvn();
	}

	public void setShowProcessBuilder(boolean show) {
		getCdePanel().setShowProcessBuilder(show);
	}

	public void setShowQueueViewer(boolean show) {
		getCdePanel().setShowQueueViewer(show);
	}

	public JPanel getSignpostPanel() {
		return getCdePanel().getSignpostPanel();
	}

	public void setShowSignpostPanel(boolean show) {
		getCdePanel().setShowSignpostPanel(show);
	}

	public void setShowSignpostToggleVisible(boolean visible) {
		getCdePanel().setShowSignpostToggleVisible(visible);
	}

	public void setShowSignpostToggleEnabled(boolean enabled) {
		getCdePanel().setShowSignpostToggleEnabled(enabled);
	}

	public void setSignpostToggleIcon(ImageIcon icon) {
		getCdePanel().setSignpostToggleIcon(icon);
	}

}
