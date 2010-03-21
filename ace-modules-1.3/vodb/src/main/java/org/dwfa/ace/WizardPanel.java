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
package org.dwfa.ace;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;

public class WizardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JPanel wfPanel = new JPanel();
    private JPanel wfDetailsPanel = new JPanel();
    private ACE acePanel;

    public WizardPanel(ACE acePanel) {
        super();        
        this.acePanel = acePanel;
        
        setLayout(null);
        setOpaque(false);
        setSize(2000, 2000);
        setVisible(true);
        setEnabled(true);
        
        initComponents();
    }

    private void initComponents() {
        
        wfPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(true);
            }
        };
        
        wfDetailsPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void setVisible(boolean aFlag) { 
                super.setVisible(true);
            }
        };

        JScrollPane wfScrollPane = new JScrollPane();
        JScrollPane wfDetailsScrollPane = new JScrollPane();

        GroupLayout wfDetailPanelLayout = new GroupLayout(wfDetailsPanel);
        wfDetailsPanel.setLayout(wfDetailPanelLayout);
        wfDetailPanelLayout.setHorizontalGroup(
            wfDetailPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        wfDetailPanelLayout.setVerticalGroup(
            wfDetailPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 219, Short.MAX_VALUE)
        );

        wfDetailsScrollPane.setViewportView(wfDetailsPanel);

        GroupLayout wfPanelLayout = new GroupLayout(wfPanel);
        wfPanel.setLayout(wfPanelLayout);
        wfPanelLayout.setHorizontalGroup(
            wfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 374, Short.MAX_VALUE)
        );
        wfPanelLayout.setVerticalGroup(
            wfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        wfScrollPane.setViewportView(wfPanel);
        wfScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(wfDetailsScrollPane, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(wfScrollPane, GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wfScrollPane, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wfDetailsScrollPane, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addContainerGap())
        );
        
    }

    public JPanel getWfPanel() {
        return wfPanel;
    }

    public JPanel getWfDetailsPanel() {
        return wfDetailsPanel;
    }
}
