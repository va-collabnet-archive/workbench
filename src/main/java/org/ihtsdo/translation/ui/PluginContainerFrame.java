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
package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.lucene.index.Term;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class PluginContainerFrame.
 */
public class PluginContainerFrame extends JPanel implements I_ContainTermComponent {

	/** The panel. */
    private JPanel panel;
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new plugin container frame.
	 * 
	 * @param guestPanel the guest panel
	 * @param panelTitle the panel title
	 */
	public PluginContainerFrame(JPanel guestPanel,String panelTitle) {
		initComponents(guestPanel,panelTitle);
	}

	/**
	 * Inits the components.
	 * 
	 * @param guestPanel the guest panel
	 * @param panelTitle the panel title
	 */
	private void initComponents(JPanel guestPanel,String panelTitle) {
		this.setLayout(new BorderLayout());

		panel= new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = 17;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = 0;
        c.gridwidth = 2;
        JLabel descLabel = new JLabel(panelTitle + ":");
        descLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        panel.add(descLabel, c);
		I_TermFactory tf = Terms.get();
		
			
        
//        SmallProgressPanel descProgress = new SmallProgressPanel();
//        descProgress.setVisible(false);
//        c.gridwidth = 1;
//        c.anchor = 14;
//        c.gridx++;
//        descPanel.add(descProgress, c);
//        descTableModel.setProgress(descProgress);
        c.anchor = 17;
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        c.gridwidth = 1;
        c.gridheight = 2;
//        if(ACE.editMode)
//        {
//            JButton rowAddAfter = new JButton(new ImageIcon(org/dwfa/ace/ACE.getResource("/24x24/plain/row_add_after.png")));
//            descPanel.add(rowAddAfter, c);
//            rowAddAfter.addActionListener(new AddDescription(host, host.getConfig()));
//        } else
//        {
            JPanel filler = new JPanel();
            filler.setMaximumSize(new Dimension(35, 32));
            filler.setMinimumSize(new Dimension(35, 32));
            filler.setPreferredSize(new Dimension(35, 32));
            panel.add(filler, c);
//        }
        c.gridheight = 1;
        c.gridx++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        panel.add(guestPanel, c);
        
		this.add(panel, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3), BorderFactory.createLineBorder(Color.GRAY)));
       

		
	}

	/**
	 * Instantiates a new plugin container frame.
	 * 
	 * @param arg0 the arg0
	 */
	public PluginContainerFrame(LayoutManager arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new plugin container frame.
	 * 
	 * @param arg0 the arg0
	 */
	public PluginContainerFrame(boolean arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new plugin container frame.
	 * 
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 */
	public PluginContainerFrame(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ContainTermComponent#getConfig()
	 */
	public I_ConfigAceFrame getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ContainTermComponent#getTermComponent()
	 */
	public I_AmTermComponent getTermComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ContainTermComponent#setTermComponent(org.dwfa.ace.api.I_AmTermComponent)
	 */
	public void setTermComponent(I_AmTermComponent termComponent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unlink() {
		// TODO Auto-generated method stub
		
	}

}
