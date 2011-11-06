/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.contradiction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionManagerBI;

/**
 *
 * @author kec
 */
public class InteractiveAdjudicator implements ContradictionManagerBI {

    Map<Integer, ButtonGroup> nidGroupMap;
    Map<JRadioButton, ComponentVersionBI> buttonVersionMap;
    
    public InteractiveAdjudicator(Map<Integer, ButtonGroup> nidGroupMap, Map<JRadioButton, ComponentVersionBI> buttonVersionMap) {
        this.nidGroupMap = nidGroupMap;
        this.buttonVersionMap = buttonVersionMap;
    }

    @Override
    public String getDisplayName() {
        return "interactive adjudicator";
    }

    @Override
    public String getDescription() {
        return "This interactive adjudicator presents contradicting versions to the user for adjudication. ";
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {

        if (versions.size() > 1) {
            List<T> resolvedVersions = new ArrayList<T>(1);
            int nid = versions.get(0).getNid();
            ButtonGroup group = nidGroupMap.get(nid);
            AbstractButton activeButton = null;

            for (AbstractButton b : Collections.list(group.getElements())) {
                if (b.isSelected()) {
                    activeButton = b;
                    break;
                }
            }
            if (activeButton != null) {
                resolvedVersions.add((T) buttonVersionMap.get((JRadioButton) activeButton));
                return resolvedVersions;
            }

            throw new UnsupportedOperationException("Not supported yet.");
        }


        return versions;
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        ArrayList<T> versions = new ArrayList<T>(2);
        versions.add(part1);
        versions.add(part2);
        return resolveVersions(versions);
    }
}
