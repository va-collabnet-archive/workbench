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
package org.dwfa.ace.refset;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

public class RefsetMemberTreeRenderer extends DefaultTreeCellRenderer {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private I_ConfigAceFrame configAceFrame;
    private IntSet viewerImageTypes = new IntSet();

    RefsetMemberTreeRenderer(I_ConfigAceFrame configAceFrame) throws TerminologyException, IOException {
        super();
        this.configAceFrame = configAceFrame;
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
        I_GetConceptData viewerImageType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getUids());
        viewerImageTypes.add(viewerImageType.getConceptId());
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() != null) {
            try {
                renderMember(node.getUserObject());
            } catch (Exception ex) {
                setText(ex.getLocalizedMessage());
                AceLog.getAppLog().log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

        }
        return this;
    }

    private void renderMember(Object userObject) throws IOException {
        List<String> htmlParts = new ArrayList<String>();
        if (String.class.isAssignableFrom(userObject.getClass())) {
            htmlParts.add(userObject.toString());
        } else if (I_GetConceptData.class.isAssignableFrom(userObject.getClass())) {
            I_GetConceptData concept = (I_GetConceptData) userObject;
            addConceptDescription(htmlParts, concept, "black");
        } else {
            htmlParts.add(userObject.toString());
        }
        setTextToHtml(htmlParts);
    }

    private void addConceptDescription(List<String> htmlParts, I_GetConceptData concept, String color)
            throws IOException {
        htmlParts.add("<font color='" + color + "'>");
        addConceptDescription(htmlParts, concept);
        htmlParts.add("</font>");
    }

    private void addConceptDescription(List<String> htmlParts, I_GetConceptData concept) throws IOException {
        I_DescriptionTuple desc = concept.getDescTuple(configAceFrame.getTreeDescPreferenceList(), configAceFrame);
        if (desc != null) {
            String text = desc.getText();
            if (text.toLowerCase().startsWith("<html>")) {
                htmlParts.add(text.substring(5));
            } else {
                htmlParts.add(text);
            }
        } else {
            htmlParts.add(concept.toString());
        }
    }

    private void setTextToHtml(List<String> htmlParts) {
        StringBuffer buff = new StringBuffer();
        if (htmlParts.size() > 0) {
            buff.append("<html>");
            for (String prefix : htmlParts) {
                buff.append(prefix);
            }
        }
        setText(buff.toString());
    }
}
