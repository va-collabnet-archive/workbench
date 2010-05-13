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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;

public class TermLabelMaker {

    public static final int LABEL_WIDTH = 400;

    public static I_ImplementActiveLabel newLabel(I_ConceptAttributeTuple conAttribute, boolean showLongForm,
            boolean showStatus) throws IOException {
        I_GetConceptData statusBean = null;
        String text = "null characteristic";
        if (conAttribute != null) {
            if (conAttribute.isDefined()) {
                text = "defined";
            } else {
                text = "primitive";
            }
            try {
                statusBean = LocalVersionedTerminology.get().getConcept(conAttribute.getConceptStatus());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        }
        StringBuffer labelBuff = new StringBuffer();
        StringBuffer toolTipBuff = new StringBuffer();
        if (BasicHTML.isHTMLString(text)) {
            text = text.substring(6);
        }
        labelBuff.append("<html>");
        labelBuff.append("<font face='Dialog' size='3'>");
        labelBuff.append(text);
        labelBuff.append(" &nbsp;</font>");
        labelBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
        if (statusBean != null) {
            labelBuff.append(statusBean.getInitialText());
        }
        labelBuff.append("</font>&nbsp;");

        toolTipBuff.append("<html>");
        StringBuffer writeBuff = labelBuff;
        if (showLongForm) {
            writeBuff = labelBuff;
        } else {
            writeBuff = toolTipBuff;
        }

        if (showStatus) {
            writeBuff = labelBuff;
            writeBuff.append("<br>");
        } else {
            writeBuff = toolTipBuff;
            if (showLongForm == false) {
                writeBuff.append("<br>");
            }
        }

        String labelHtml = labelBuff.toString();
        String toolTipHtml = toolTipBuff.toString();
        LabelForTuple ldt = new LabelForConceptAttributeTuple(conAttribute, showLongForm, showStatus);
        return makeLabel(ldt, labelHtml, toolTipHtml);
    }

    public static I_ImplementActiveLabel newLabel(I_DescriptionTuple desc, boolean showLongForm, boolean showStatus)
            throws IOException {
        I_GetConceptData typeBean = null;
        I_GetConceptData statusBean = null;
        String text = "null desc";
        if (desc != null) {
            text = desc.getText();
            try {
                typeBean = LocalVersionedTerminology.get().getConcept(desc.getTypeId());
                statusBean = LocalVersionedTerminology.get().getConcept(desc.getStatusId());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        }
        StringBuffer labelBuff = new StringBuffer();
        StringBuffer toolTipBuff = new StringBuffer();
        if (BasicHTML.isHTMLString(text)) {
            text = text.substring(6);
        }
        labelBuff.append("<html>");
        labelBuff.append("<font face='Dialog' size='3'>");
        labelBuff.append(text);
        labelBuff.append(" &nbsp;</font>");
        toolTipBuff.append("<html>");
        StringBuffer writeBuff = labelBuff;
        if (showLongForm) {
            writeBuff = labelBuff;
            writeBuff.append("<br>");
        } else {
            writeBuff = toolTipBuff;
        }
        writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
        if (typeBean != null) {
            writeBuff.append(typeBean.getInitialText());
        }
        writeBuff.append(" (");
        if (desc != null) {
            writeBuff.append(desc.getLang());
        } else {
            writeBuff.append("null lang");
        }
        writeBuff.append(")</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
        if (desc != null) {
            if (desc.getInitialCaseSignificant()) {
                writeBuff.append("fixed case&nbsp;");
            } else {
                writeBuff.append("changable case&nbsp;");
            }
        }
        writeBuff.append("</font>");

        if (showStatus) {
            writeBuff = labelBuff;
            writeBuff.append("<br>");
        } else {
            writeBuff = toolTipBuff;
            if (showLongForm == false) {
                writeBuff.append("<br>");
            }
        }
        writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
        if (statusBean != null) {
            writeBuff.append(statusBean.getInitialText());
        }
        writeBuff.append("</font>&nbsp;");

        String labelHtml = labelBuff.toString();
        String toolTipHtml = toolTipBuff.toString();
        if (desc == null) {

        }
        LabelForTuple ldt = new LabelForDescriptionTuple(desc, showLongForm, showStatus);
        return makeLabel(ldt, labelHtml, toolTipHtml);
    }

    public static I_ImplementActiveLabel newLabel(I_RelTuple rel, boolean showLongForm, boolean showStatus)
            throws IOException {
        try {
            I_GetConceptData typeBean = LocalVersionedTerminology.get().getConcept(rel.getRelTypeId());
            I_GetConceptData destBean = LocalVersionedTerminology.get().getConcept(rel.getC2Id());
            I_GetConceptData refinabilityBean = LocalVersionedTerminology.get().getConcept(rel.getRefinabilityId());
            I_GetConceptData characteristicBean = LocalVersionedTerminology.get().getConcept(rel.getCharacteristicId());
            I_GetConceptData statusBean = LocalVersionedTerminology.get().getConcept(rel.getStatusId());

            StringBuffer labelBuff = new StringBuffer();
            StringBuffer toolTipBuff = new StringBuffer();
            labelBuff.append("<html>");
            toolTipBuff.append("<html>");
            labelBuff.append("<font face='Dialog' size='3' color='blue'>");
            labelBuff.append(typeBean.getInitialText());
            labelBuff.append(" &nbsp;</font><font face='Dialog' size='3' color='green'>");
            labelBuff.append(destBean.getInitialText());
            labelBuff.append(" &nbsp;</font>");
            StringBuffer writeBuff = labelBuff;
            if (showLongForm) {
                writeBuff = labelBuff;
                writeBuff.append("<br>");
            } else {
                writeBuff = toolTipBuff;
            }
            writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
            writeBuff.append(characteristicBean.getInitialText());
            writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
            writeBuff.append(refinabilityBean.getInitialText());
            writeBuff.append("</font>");
            writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
            writeBuff.append(rel.getGroup());
            writeBuff.append("&nbsp;</font>");
            if (showStatus) {
                writeBuff = labelBuff;
                writeBuff.append("<br>");
            } else {
                writeBuff = toolTipBuff;
                if (showLongForm == false) {
                    writeBuff.append("<br>");
                }
            }
            writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
            writeBuff.append(statusBean.getInitialText());
            writeBuff.append("&nbsp;</font>");

            String labelHtml = labelBuff.toString();
            String toolTipHtml = toolTipBuff.toString();
            LabelForRelationshipTuple lrt = new LabelForRelationshipTuple(rel, showLongForm, showStatus);
            return makeLabel(lrt, labelHtml, toolTipHtml);
        } catch (TerminologyException e) {
            throw new IOException();
        }
    }

    public static I_ImplementActiveLabel makeLabel(I_ImplementActiveLabel activeLabel, String labelHtml,
            String toolTipHtml) {
        setupLabel(activeLabel.getLabel(), labelHtml, toolTipHtml);

        return activeLabel;
    }

    private static JLabel setupLabel(JLabel label, String labelHtml, String toolTipHtml) {
        label.setText(labelHtml);
        label.setBackground(Color.white);
        label.setOpaque(true);

        View v = BasicHTML.createHTMLView(label, labelHtml);
        v.setSize(LABEL_WIDTH, 0);
        float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
        float firstYSpan = prefYSpan;
        float prefXSpan = v.getPreferredSpan(View.X_AXIS);
        Dimension prefSize = new Dimension((int) prefXSpan, (int) prefYSpan);

        v.setSize(prefSize.width - 10, prefSize.height);
        prefYSpan = v.getPreferredSpan(View.Y_AXIS);
        prefXSpan = v.getPreferredSpan(View.X_AXIS);

        while ((firstYSpan == prefYSpan) && (prefSize.width > 40)) {
            prefSize = new Dimension((int) prefXSpan, (int) prefYSpan);
            v.setSize(prefSize.width - 10, prefSize.height);
            prefYSpan = v.getPreferredSpan(View.Y_AXIS);
            prefXSpan = v.getPreferredSpan(View.X_AXIS);
        }

        prefSize = new Dimension(prefSize.width + 10, prefSize.height + 4);
        label.setMaximumSize(prefSize);
        label.setSize(prefSize);
        label.setMinimumSize(prefSize);
        label.setPreferredSize(prefSize);
        label.setToolTipText(toolTipHtml);
        return label;
    }

    // CONCEPT LABEL FOR NORMALIZE FORM
    public static I_ImplementActiveLabel newLabelForm(I_ConceptAttributeTuple conAttribute, boolean showLongForm,
            boolean showStatus) throws IOException {
        I_GetConceptData statusBean = null;
        String text = "null characteristic";
        if (conAttribute != null) {
            if (conAttribute.isDefined()) {
                text = "defined";
            } else {
                text = "primitive";
            }
            try {
                statusBean = LocalVersionedTerminology.get().getConcept(conAttribute.getConceptStatus());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        }
        StringBuffer labelBuff = new StringBuffer();
        StringBuffer toolTipBuff = new StringBuffer();
        if (BasicHTML.isHTMLString(text)) {
            text = text.substring(6);
        }
        labelBuff.append("<html>");
        labelBuff.append("<font face='Dialog' size='3'>");
        labelBuff.append(text);
        labelBuff.append(" &nbsp;</font>");
        labelBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
        if (statusBean != null) {
            labelBuff.append(statusBean.getInitialText());
        }
        labelBuff.append("</font>&nbsp;");

        toolTipBuff.append("<html>");
        StringBuffer writeBuff = labelBuff;
        if (showLongForm) {
            writeBuff = labelBuff;
        } else {
            writeBuff = toolTipBuff;
        }

        if (showStatus) {
            writeBuff = labelBuff;
            writeBuff.append("<br>");
        } else {
            writeBuff = toolTipBuff;
            if (showLongForm == false) {
                writeBuff.append("<br>");
            }
        }

        String labelHtml = labelBuff.toString();
        String toolTipHtml = toolTipBuff.toString();
        LabelForTuple ldt = new LabelForConceptAttributeTuple(conAttribute, showLongForm, showStatus);
        return makeLabel(ldt, labelHtml, toolTipHtml);
    }

    // RELATIONSIP LABEL FOR NORMALIZED FORMS
    public static I_ImplementActiveLabel newLabelForm(I_RelTuple rel, boolean showLongForm, boolean showStatus)
            throws IOException {
        try {
            I_GetConceptData typeBean = LocalVersionedTerminology.get().getConcept(rel.getTypeId());
            I_GetConceptData destBean = LocalVersionedTerminology.get().getConcept(rel.getC2Id());
            I_GetConceptData refinabilityBean = LocalVersionedTerminology.get().getConcept(rel.getRefinabilityId());
            I_GetConceptData characteristicBean = LocalVersionedTerminology.get().getConcept(rel.getCharacteristicId());
            I_GetConceptData statusBean = LocalVersionedTerminology.get().getConcept(rel.getStatusId());

            StringBuffer labelBuff = new StringBuffer();
            StringBuffer toolTipBuff = new StringBuffer();
            labelBuff.append("<html>");
            toolTipBuff.append("<html>");
            labelBuff.append("<font face='Dialog' size='3' color='blue'>");
            labelBuff.append(typeBean.getInitialText());
            labelBuff.append(" &nbsp;</font><br><font face='Dialog' size='3' color='green'>");
            labelBuff.append(destBean.getInitialText());
            labelBuff.append(" &nbsp;</font>");
            StringBuffer writeBuff = labelBuff;
            if (showLongForm) {
                writeBuff = labelBuff;
                writeBuff.append("<br>");
            } else {
                writeBuff = toolTipBuff;
            }
            writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
            writeBuff.append(characteristicBean.getInitialText());
            writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
            writeBuff.append(refinabilityBean.getInitialText());
            writeBuff.append("</font>");
            writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
            writeBuff.append(rel.getGroup());
            writeBuff.append("&nbsp;</font>");
            if (showStatus) {
                writeBuff = labelBuff;
                writeBuff.append("<br>");
            } else {
                writeBuff = toolTipBuff;
                if (showLongForm == false) {
                    writeBuff.append("<br>");
                }
            }
            writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
            writeBuff.append(statusBean.getInitialText());
            writeBuff.append("&nbsp;</font>");

            String labelHtml = labelBuff.toString();
            String toolTipHtml = toolTipBuff.toString();
            LabelForRelationshipTuple lrt = new LabelForRelationshipTuple(rel, showLongForm, showStatus);
            return makeLabel(lrt, labelHtml, toolTipHtml);
        } catch (TerminologyException e) {
            throw new IOException(e);
        }
    }

    // RELATIONSIP LABEL FOR NORMALIZED FORMS
    public static I_ImplementActiveLabel newLabel(List<I_RelTuple> relList, boolean showLongForm, boolean showStatus)
            throws IOException {

        StringBuffer labelBuff = new StringBuffer();
        StringBuffer toolTipBuff = new StringBuffer();
        labelBuff.append("<html>");
        toolTipBuff.append("<html>");
        I_TermFactory tf = LocalVersionedTerminology.get();
        boolean addBreak = false;
        for (I_RelTuple rel : relList) {
            try {
                I_GetConceptData typeBean = tf.getConcept(rel.getTypeId());
                I_GetConceptData destBean = tf.getConcept(rel.getC2Id());
                I_GetConceptData refinabilityBean = tf.getConcept(rel.getRefinabilityId());
                I_GetConceptData characteristicBean = tf.getConcept(rel.getCharacteristicId());
                I_GetConceptData statusBean = tf.getConcept(rel.getStatusId());

                if (addBreak)
                    labelBuff.append("<br>");
                else
                    addBreak = true;

                labelBuff.append("<font face='Dialog' size='3' color='blue'>");
                labelBuff.append(typeBean.getInitialText());
                labelBuff.append(" &nbsp;</font><br><font face='Dialog' size='3' color='green'>");
                labelBuff.append(destBean.getInitialText());
                labelBuff.append(" &nbsp;</font>");
                StringBuffer writeBuff = labelBuff;
                if (showLongForm) {
                    writeBuff = labelBuff;
                    writeBuff.append("<br>");
                } else {
                    writeBuff = toolTipBuff;
                }
                writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
                writeBuff.append(characteristicBean.getInitialText());
                writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
                writeBuff.append(refinabilityBean.getInitialText());
                writeBuff.append("</font>");
                writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
                writeBuff.append(rel.getGroup());
                writeBuff.append("&nbsp;</font>");
                if (showStatus) {
                    writeBuff = labelBuff;
                    writeBuff.append("<br>");
                } else {
                    writeBuff = toolTipBuff;
                    if (showLongForm == false) {
                        writeBuff.append("<br>");
                    }
                }
                writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
                writeBuff.append(statusBean.getInitialText());
                writeBuff.append("&nbsp;</font>");
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        }

        String labelHtml = labelBuff.toString();
        String toolTipHtml = toolTipBuff.toString();

        LabelForGroupTuple lrt = new LabelForGroupTuple(relList, showLongForm, showStatus);
        return makeLabel(lrt, labelHtml, toolTipHtml);
    }

    public static JLabel makeLabel(String labelHtml) {
        if (BasicHTML.isHTMLString(labelHtml) == false) {
            labelHtml = "<html>" + labelHtml + " &nbsp;";
        }
        if (labelHtml.endsWith(" &nbsp;") == false) {
            labelHtml = labelHtml + " &nbsp;";
        }
        return setupLabel(new JLabel(), labelHtml, null);
    }

    public static ImageFilter getTransparentFilter() {
        return transparentFilter;
    }

    private static TransparencyFilter transparentFilter = new TransparencyFilter();

    private static class TransparencyFilter extends RGBImageFilter {
        public TransparencyFilter() {
            // When this is set to true, the filter will work with images
            // whose pixels are indices into a color table (IndexColorModel).
            // In such a case, the color values in the color table are filtered.
            canFilterIndexColorModel = true;
        }

        // This method is called for every pixel in the image
        public int filterRGB(int x, int y, int rgb) {
            if (x == -1) {
                // The pixel value is from the image's color table rather than
                // the image itself
            }
            return rgb & 0xAAFFFFFF;
        }
    }

}
