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

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.AceTableRenderer;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;

public class RefsetSpecTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private I_ConfigAceFrame configAceFrame;
    private IntSet viewerImageTypes = new IntSet();

    /**
     * @param configAceFrame
     * @throws IOException
     * @throws TerminologyException
     * @todo Get the AND/OR statements indented so it is clear that they are not
     *       ANDing/ORing the siblings, just the descendants Perhaps this should
     *       be handled by deliberately "ORing" all descendants, or by sorting
     *       the AND/OR statements to the end of the list...
     * 
     *       Put Spec concepts in a different color than the query input
     *       concepts...
     * 
     */
    public RefsetSpecTreeCellRenderer(I_ConfigAceFrame configAceFrame) throws TerminologyException, IOException {
        super();
        this.configAceFrame = configAceFrame;
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
        ConceptBean viewerImageType = ConceptBean.get(ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getUids());
        viewerImageTypes.add(viewerImageType.getConceptId());
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        this.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        generateHtmlRendering(value);
        return this;
    }

    public String getHtmlRendering(Object value) {
        generateHtmlRendering(new DefaultMutableTreeNode(value));
        return this.getText();
    }

    private void generateHtmlRendering(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() != null
            && (I_ThinExtByRefVersioned.class.isAssignableFrom(node.getUserObject().getClass()) || I_ThinExtByRefTuple.class.isAssignableFrom(node.getUserObject()
                .getClass()))) {
            I_ThinExtByRefTuple firstTuple = null;
            I_ThinExtByRefTuple lastTuple = null;
            EXT_TYPE extType = null;
            List<I_ThinExtByRefTuple> tuples = null;
            if (I_ThinExtByRefVersioned.class.isAssignableFrom(node.getUserObject().getClass())) {
                I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) node.getUserObject();
                tuples = ext.getTuples(configAceFrame.getAllowedStatus(), configAceFrame.getViewPositionSet(), true);
                if (tuples != null && tuples.size() > 0) {
                    firstTuple = tuples.get(0);
                }
                if (tuples != null && tuples.size() > 0) {
                    lastTuple = tuples.get(tuples.size() - 1);
                    if (lastTuple.getVersion() == Integer.MAX_VALUE) {
                        this.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, AceTableRenderer.UNCOMMITTED_COLOR));
                    }
                }
                if (firstTuple != null) {
                    extType = ThinExtBinder.getExtensionType(firstTuple.getCore());
                }
            } else {
                firstTuple = (I_ThinExtByRefTuple) node.getUserObject();
                extType = ThinExtBinder.getExtensionType(firstTuple.getCore());
                tuples = new ArrayList<I_ThinExtByRefTuple>();
                tuples.add(firstTuple);
            }

            boolean indent = false;

            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            if (parent != null && parent.getUserObject() != null) {
                if (I_GetConceptData.class.isAssignableFrom(parent.getUserObject().getClass())) {
                    indent = false;
                } else {
                    indent = true;
                }
            }
            if (firstTuple != null) {
                setOpaque(false);
                try {
                    switch (extType) {
                    case CONCEPT_CONCEPT:
                        renderBranchingClause(firstTuple);
                        break;
                    case CONCEPT_CONCEPT_CONCEPT:
                        renderStructuralQueryClause(firstTuple, indent);
                        break;
                    case CONCEPT_CONCEPT_STRING:
                        renderTextQueryClause(firstTuple, indent);
                        break;
                    }
                } catch (Exception ex) {
                    setText(ex.getLocalizedMessage());
                    AceLog.getAppLog().log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            } else {
                setBackground(Color.LIGHT_GRAY);
                setOpaque(true);
                I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) node.getUserObject();
                tuples = ext.getTuples(null, configAceFrame.getViewPositionSet(), true);
                firstTuple = null;
                if (tuples != null && tuples.size() > 0) {
                    firstTuple = tuples.get(0);
                }
                if (firstTuple != null) {
                    try {
                        extType = ThinExtBinder.getExtensionType(firstTuple.getCore());
                        switch (extType) {
                        case CONCEPT_CONCEPT:
                            renderBranchingClause(firstTuple);
                            break;
                        case CONCEPT_CONCEPT_CONCEPT:
                            renderStructuralQueryClause(firstTuple, indent);
                            break;
                        case CONCEPT_CONCEPT_STRING:
                            renderTextQueryClause(firstTuple, indent);
                            break;
                        }
                    } catch (Exception ex) {
                        setText(ex.getLocalizedMessage());
                        AceLog.getAppLog().log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                } else {
                    if (extType != null) {
                        switch (extType) {
                        case CONCEPT_CONCEPT:
                            this.setText("Branching clause is Primoridal or Extinct");
                            break;
                        case CONCEPT_CONCEPT_CONCEPT:
                            this.setText("Structural clause is Primoridal or Extinct");
                            break;
                        case CONCEPT_CONCEPT_STRING:
                            this.setText("Text clause is Primoridal or Extinct");
                            break;
                        default:
                            this.setText("Can't handle extinct type: " + extType);
                        }
                    } else {
                        this.setText("Clause is Primoridal or Extinct");
                    }

                }
            }
        }
    }

    private void renderTextQueryClause(I_ThinExtByRefTuple firstTuple, boolean indent) throws IOException {
        List<String> htmlParts = new ArrayList<String>();
        I_ThinExtByRefPartConceptConceptString ccsPart = (I_ThinExtByRefPartConceptConceptString) firstTuple.getPart();

        if (indent) {
            htmlParts.add("&nbsp;&nbsp;");
        }
        addPrefixImage(htmlParts, ccsPart.getC1id());
        htmlParts.add("&nbsp;&nbsp;");
        addConceptDescription(htmlParts, ccsPart.getC2id(), "#483D8B");
        htmlParts.add("<font color='#483D8B'>:&nbsp;&nbsp;\"</font>");
        String text = ccsPart.getStr();
        if (text.toLowerCase().startsWith("<html>")) {
            htmlParts.add(text.substring(5));
        } else {
            htmlParts.add(text);
        }
        htmlParts.add("<font color='#483D8B'>\"&nbsp;&nbsp;</font>");
        setTextToHtml(htmlParts);
    }

    private void renderStructuralQueryClause(I_ThinExtByRefTuple firstTuple, boolean indent) throws IOException {
        List<String> htmlParts = new ArrayList<String>();
        I_ThinExtByRefPartConceptConceptConcept cccPart = (I_ThinExtByRefPartConceptConceptConcept) firstTuple.getPart();
        if (indent) {
            htmlParts.add("&nbsp;&nbsp;");
        }
        addPrefixImage(htmlParts, cccPart.getC1id());
        htmlParts.add("&nbsp;&nbsp;");
        addConceptDescription(htmlParts, cccPart.getC2id(), "#191970");
        htmlParts.add("<font color='#191970'>:&nbsp;&nbsp;</font>");
        addConceptDescription(htmlParts, cccPart.getC3id(), "black");
        htmlParts.add("&nbsp;&nbsp;");
        setTextToHtml(htmlParts);
    }

    private void renderBranchingClause(I_ThinExtByRefTuple firstTuple) throws IOException {
        List<String> htmlParts = new ArrayList<String>();
        I_ThinExtByRefPartConceptConcept ccPart = (I_ThinExtByRefPartConceptConcept) firstTuple.getPart();
        htmlParts.add("&nbsp;&nbsp;&nbsp;");
        addPrefixImage(htmlParts, ccPart.getC1id());
        htmlParts.add("&nbsp;");
        addConceptDescription(htmlParts, ccPart.getC2id(), "black");
        htmlParts.add("&nbsp;&nbsp;");
        setTextToHtml(htmlParts);
    }

    private void addConceptDescription(List<String> htmlParts, int cid, String color) throws IOException {
        htmlParts.add("<font color='" + color + "'>");
        addConceptDescription(htmlParts, cid);
        htmlParts.add("</font>");
    }

    private void addConceptDescription(List<String> htmlParts, int cid) throws IOException {
        ConceptBean cb = ConceptBean.get(cid);
        I_DescriptionTuple desc = cb.getDescTuple(configAceFrame.getTreeDescPreferenceList(), configAceFrame);
        if (desc != null) {
            String text = desc.getText();
            if (text.toLowerCase().startsWith("<html>")) {
                htmlParts.add(text.substring(5));
            } else {
                htmlParts.add(text);
            }
        } else {
            htmlParts.add(cb.toString());
        }
    }

    private void addPrefixImage(List<String> htmlParts, int prefixConceptId) throws IOException {
        ConceptBean prefixConcept = ConceptBean.get(prefixConceptId);
        for (I_ImageTuple imageTuple : prefixConcept.getImageTuples(configAceFrame.getAllowedStatus(),
            viewerImageTypes, configAceFrame.getViewPositionSet())) {
            htmlParts.add("<img src='ace:" + imageTuple.getImageId() + "$" + imageTuple.getConceptId()
                + "' align=absbottom>");
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
