package org.dwfa.ace.config;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.jini.config.ConfigurationException;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.worker.MasterWorker;

import com.sun.jini.start.LifeCycle;

public class AceFrame extends ComponentFrame {

	private ACE cdePanel;

	public AceFrame(String[] args, LifeCycle lc, I_ConfigAceFrame frameConfig) throws Exception {
		super(args, lc);
		((AceFrameConfig) frameConfig).setAceFrame(this);
		getCdePanel().setup(frameConfig);
		setName(frameConfig.getFrameName());
		setTitle("User: " + frameConfig.getUsername());
		setContentPane(cdePanel);
		setBounds(frameConfig.getBounds());
		
		MasterWorker worker = new MasterWorker(config);
		cdePanel.getAceFrameConfig().setWorker(worker);
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


	public JMenuItem getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem("Ace Viewer");
        newWindow.addActionListener(new NewFrame(this.getArgs(), this.getLc()));
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
	public ACE getCdePanel()  {
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

}
