/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.contradiction;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.MarshalledObject;
import java.util.Set;
import javax.naming.ConfigurationException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.LINK_TYPE;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.util.OpenFramesWindowListener;
import org.ihtsdo.arena.Arena;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class ContradictionEditorFrame extends ComponentFrame {

    private static final long serialVersionUID = 1L;
    protected JMenu adjudicatorMenu;
    private I_ConfigAceFrame newFrameConfig = null;
    private JSplitPane topSplit = new JSplitPane();
    private JTabbedPane conceptTabs = new JTabbedPane();
    private ConceptPanel c1Panel;
    private final TerminologyList batchConceptList;
	private ViewCoordinate viewCoord;

    public ConceptPanel getC1Panel() {
        return c1Panel;
    }
    private Arena arena;

    public ContradictionEditorFrame(AceFrameConfig origConfig) throws Exception {
        super(new String[]{}, null);
        // Set the title for the frame
        setTitle(getNextFrameName());
        this.newFrameConfig = new ContradictionConfig(this, origConfig);
        this.viewCoord = this.newFrameConfig.getViewCoordinate();
        
        // Set the position and size of frame
        setBounds(10, 10, 500, 500);

        // Add the panel and editor pane to the frame
        Container cp = getContentPane();

        // Add the panel to the north
        cp.add(topSplit);
        topSplit.setRightComponent(conceptTabs);


        c1Panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R1,
                newFrameConfig, LINK_TYPE.UNLINKED,
                conceptTabs, 1, "plugins/contradiction");
        conceptTabs.add(c1Panel);

        arena = new Arena(newFrameConfig);
        conceptTabs.addTab("arena",
                new ImageIcon(ACE.class.getResource("/16x16/plain/eye.png")), arena);
        conceptTabs.setSelectedIndex(1);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new OpenFramesWindowListener(this, this.cfb));
        this.setBounds(getDefaultFrameSize());

        TerminologyListModel batchListModel =
                new TerminologyListModel(newFrameConfig.getTabHistoryMap().get("batchList"));
        batchConceptList = new TerminologyList(batchListModel, true, true, newFrameConfig);
        topSplit.setLeftComponent(new JScrollPane(batchConceptList));
        batchConceptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        topSplit.setDividerLocation(350);

        batchConceptList.addListSelectionListener(new SelectionListener());
    }

    private class FindContradictionAction extends AbstractAction {

        public FindContradictionAction() {
            super("Run Contradiction Finder");
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
        ContradictionFinderSwingWorker worker =
                new ContradictionFinderSwingWorker(viewCoord,
                (TerminologyListModel) batchConceptList.getModel());
        }
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    @Override
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        mainMenuBar.add(adjudicatorMenu = new JMenu("Adjudicator"));
        adjudicatorMenu.add(new FindContradictionAction());
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    @Override
    public JMenu getQuitMenu() {
        return this.adjudicatorMenu;
    }

    /**
     * @see org.dwfa.bpa.util.I_InitComponentMenus#addInternalFrames(javax.swing.JMenu)
     */
    @Override
    public void addInternalFrames(JMenu menu) {
    }

    void selectTab(int hostIndex) {
        conceptTabs.setSelectedIndex(0);
    }

    private class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            int selectedIndex = batchConceptList.getSelectedIndex();
            if (selectedIndex >= 0) {
                TerminologyListModel tm = (TerminologyListModel) batchConceptList.getModel();
                I_GetConceptData concept = tm.getElementAt(selectedIndex);
                c1Panel.setTermComponent(concept);
            }
        }
    }

    public class NewAdjudicatorFrame implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                MarshalledObject<I_ConfigAceFrame> marshalledFrame =
                        new MarshalledObject<I_ConfigAceFrame>(AceConfig.config.getActiveConfig());
                AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();


                ContradictionEditorFrame newFrame = new ContradictionEditorFrame(newFrameConfig);
                newFrame.setVisible(true);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

        }
    }

    @Override
    public JMenuItem[] getNewWindowMenu() {
        if (ACE.editMode) {
            JMenuItem newViewer = new JMenuItem("Contradiction Adjudicator");
            newViewer.addActionListener(new NewAdjudicatorFrame());
            return new JMenuItem[]{newViewer,};
        }
        return new JMenuItem[]{};
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    @Override
    public String getNextFrameName() {
        String title = "Contradiction Adjudicator";
        if (count > 0) {
            return title + " " + count++;
        }
        count++;
        return title;
    }
    private static int count = 0;

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getCount()
     */
    @Override
    public int getCount() {
        return count;
    }
}
