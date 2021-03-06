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



package org.dwfa.ace.classifier;

//~--- non-JDK imports --------------------------------------------------------

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import javax.swing.*;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.I_ImplementActiveLabel;
import org.dwfa.ace.LabelForTuple;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.*;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeAnalogBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * Classifier Normal Form (Label Format) Panel
 *
 *
 * @author kazoo
 *
 */

/*
* :NYI:
* 1. clarify minmax !!!
* 2. handle path comparison
* 3.
 */
public class CNFormsLabelPanel extends JPanel implements ActionListener {
   private static final long serialVersionUID = 1L;

   // ** CONFIGURATION PARTICULARS **
   @SuppressWarnings("unused")
   private static final boolean debug = false;    // :DEBUG:

   //~--- fields --------------------------------------------------------------

   boolean           showGroupLabels  = true;    // toggles grouped vs. single label display
   private JCheckBox showStatusCB     = new JCheckBox("show status");
   private JCheckBox showShortFormCB  = new JCheckBox("Short Canonical");
   private JCheckBox showLongFormCB   = new JCheckBox("Long Canonical");
   private JCheckBox showDistFormCB   = new JCheckBox("Distribution");
   private JCheckBox showDetailCB     = new JCheckBox("show detail");
   private JCheckBox showAuthFormCB   = new JCheckBox("Authoring");
   private Dimension minPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 100);

   // AWT: Dimension(int Width, int Height) in pixels(???)
   private Dimension   maxPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 4000);
   private DeltaColors colors           = new DeltaColors();
   List<I_Position>    cClassPathPos;    // Classifier (Inferred) Path I_Positions

   // ** CLASSIFIER PARTICULARS **
   List<I_Position> cEditPathPos;    // Edit (Stated) Path I_Positions
   private SnoTable cSnoTable;

   // ** GUI PARTICULARS **
   private JPanel commonJPanel;

   // JLabel with ActionListener
   private List<I_ImplementActiveLabel> commonLabels;
   private JPanel                       commonPartJPanel;
   private I_ConfigAceFrame             config;
   private JPanel                       deltaJPanel;
   private JPanel                       deltaPartJPanel;
   private JPanel                       formsJPanel;    // sub panels added using tmpJPanel

   // ** WORKBENCH PARTICULARS **
   private I_GetConceptData theCBean;

   //~--- constructors --------------------------------------------------------

   public CNFormsLabelPanel(I_GetConceptData conceptIn, List<I_Position> cEditPathPos,
                            List<I_Position> cClassPathPos, SnoTable cSnoTable) {
      super();
      this.theCBean      = conceptIn;
      this.cEditPathPos  = cEditPathPos;
      this.cClassPathPos = cClassPathPos;
      this.cSnoTable     = cSnoTable;
      setLayout(new GridBagLayout());    // CNFormsLabelPanel LayoutManager

      GridBagConstraints c = new GridBagConstraints();

      c.anchor = GridBagConstraints.NORTHWEST;    // Place

      // CNFormsLabelPanel
      // TOP ROW
      c.gridy     = 0;      // first row
      c.gridx     = 0;      // reset at west side of row
      c.weightx   = 0.0;    // no extra space
      c.weighty   = 0.0;    // no extra space
      c.gridwidth = 1;
      c.fill      = GridBagConstraints.NONE;

      // ADD CHECK BOXES
      c.gridy++;    // next row
      c.gridx     = 0;
      c.gridwidth = 5;

      JLabel label = new JLabel("Normal Forms Expanded View:");

      label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
      add(label, c);
      c.gridx++;
      add(showDistFormCB, c);
      c.gridx++;
      add(showAuthFormCB, c);
      c.gridx++;
      add(showLongFormCB, c);
      c.gridx++;
      add(showShortFormCB, c);

      // FORM SELECTION CHECKBOX ROW
      c.gridy++;    // next row
      c.gridx     = 0;    // first cell in row
      c.gridwidth = 1;
      c.weightx   = 0.0;
      c.fill      = GridBagConstraints.NONE;
      label       = new JLabel("Information:");
      label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
      add(label, c);
      c.gridx++;
      add(showDetailCB, c);
      c.gridx++;
      add(showStatusCB, c);

      // SETUP CHECKBOX VALUES & LISTENER
      showStatusCB.setSelected(false);
      showStatusCB.addActionListener(this);
      showDetailCB.setSelected(false);
      showDetailCB.addActionListener(this);
      showDistFormCB.setSelected(false);
      showDistFormCB.addActionListener(this);
      showAuthFormCB.setSelected(false);
      showAuthFormCB.addActionListener(this);
      showLongFormCB.setSelected(false);
      showLongFormCB.addActionListener(this);
      showShortFormCB.setSelected(false);
      showShortFormCB.addActionListener(this);

      // COMMON & DIFFERENT PANELS ROW
      c.gridy++;
      c.gridx      = 0;
      c.gridwidth  = 2;
      c.fill       = GridBagConstraints.BOTH;
      c.weightx    = 0;
      commonJPanel = newMinMaxJPanel();
      commonJPanel.setLayout(new GridLayout(0, 1));
      commonJPanel.setName("Common Panel");
      commonJPanel.setBorder(BorderFactory.createTitledBorder("Common: "));
      add(commonJPanel, c);
      c.gridx     = c.gridx + 1;
      deltaJPanel = newMinMaxJPanel();
      deltaJPanel.setLayout(new GridLayout(0, 1));
      deltaJPanel.setName("Differences Panel");
      deltaJPanel.setBorder(BorderFactory.createTitledBorder("Different: "));
      add(deltaJPanel, c);

      // FORMS PANEL ROW
      c.gridy++;    // next row
      c.gridx     = 0;    // reset at west side of row
      c.gridwidth = 2;    // number of cells in row
      c.fill      = GridBagConstraints.BOTH;
      formsJPanel = new JPanel(new GridBagLayout());
      formsJPanel.setName("Forms Panel");
      formsJPanel.setBorder(BorderFactory.createTitledBorder("Forms: "));

      JScrollPane formJScrollPane = new JScrollPane(formsJPanel);

      formJScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      add(formJScrollPane, c);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void actionPerformed(ActionEvent e) {
      try {
         setConcept(theCBean, config);
         revalidate();
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLog(this, Level.SEVERE,
                                        "Database Exception: " + e1.getLocalizedMessage(), e1);
      } catch (TerminologyException e1) {
         AceLog.getAppLog().alertAndLog(this, Level.SEVERE,
                                        "Database Exception: " + e1.getLocalizedMessage(), e1);
      }
   }

   private I_ConceptAttributeTuple findSelf(I_GetConceptData cBean, List<I_Position> posList) {
      try {
         I_ConceptAttributeVersioned            cv     = cBean.getConAttrs();
         List<? extends I_ConceptAttributePart> cvList = cv.getMutableParts();
         I_ConceptAttributePart                 cp1    = null;

         for (I_Position pos : posList) {    // !!! <-- NullPointerException
            int tmpCountDupl = 0;

            for (I_ConceptAttributePart cp : cvList) {

               // FIND MOST RECENT
               if (cp.getPathNid() == pos.getPath().getConceptNid()) {
                  if (cp1 == null) {
                     cp1 = cp;               // ... KEEP FIRST_INSTANCE PART
                  } else if (cp1.getTime() < cp.getTime()) {
                     cp1 = cp;               // ... KEEP MORE_RECENT PART
                  } else if (cp1.getTime() == cp.getTime()) {

                     // !!! THIS DUPLICATE SHOULD NEVER HAPPEN
                     tmpCountDupl++;
                  }
               }
            }

            // cp1.getStatusId() == isCURRENT
            if (cp1 != null) {    // IF FOUND ON THIS PATH, STOP SEARCHING
               return new ConceptAttrVersion(cv, cp1);
            }
         }
      } catch (IOException e) {

         // TODO Auto-generated catch block
         e.printStackTrace();

         return null;
      }

      return null;
   }

   /**
    * <b>Authoring Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
    */
   public JPanel newFormAuthJPanel(String label, I_ConfigAceFrame config,
                                   Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
                                   Map<I_DescriptionTuple, Color> desColorMap,
                                   Map<I_RelTuple, Color> relColorMap)
           throws IOException {
      JPanel formJPanel = newMinMaxJPanel();

      formJPanel.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();

      c.fill    = GridBagConstraints.NONE;
      c.anchor  = GridBagConstraints.NORTHWEST;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx   = 0;
      c.gridy   = 0;

      List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();

      c.gridx     = 0;
      c.gridwidth = 2;
      c.anchor    = GridBagConstraints.NORTHWEST;

      // SHOW SELF CONCEPT
      I_ConceptAttributeTuple cTuple    = findSelf(theCBean, cEditPathPos);
      I_ImplementActiveLabel  tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
                                             showStatusCB.isSelected());

      tLabelList.add((LabelForTuple) tmpTLabel);

      Color tmpDeltaColor = conAttrColorMap.get(cTuple);

      setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
      formJPanel.add(tmpTLabel.getLabel(), c);
      c.gridy++;

      // SHOW PROXIMAL ISAs -- as relationships
      SnoGrpList       isaSGList = cSnoTable.getIsaProx();
      List<I_RelTuple> isaList   = new ArrayList<I_RelTuple>();

      for (SnoGrp sg : isaSGList) {
         for (SnoRel sr : sg) {
            isaList.add(new RelVersion(sr.relNid));
         }
      }

      for (I_RelTuple t : isaList) {
         I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, showDetailCB.isSelected(),
                                            showStatusCB.isSelected());

         tLabelList.add((LabelForTuple) tLabel);

         Color deltaColor = relColorMap.get(t);

         setBorder(tLabel.getLabel(), deltaColor);
         formJPanel.add(tLabel.getLabel(), c);
         c.gridy++;
      }

      // FIND NON-REDUNDANT ROLES, DIFFERENTIATED FROM PROXIMATE ISA
      SnoGrpList sgl = cSnoTable.getRoleDiffFromProx();

      // SHOW ROLE SET
      if (sgl.size() > 0) {
         int    i  = 0;
         SnoGrp sg = sgl.get(0);

         // show each of the non-Rels
         if ((sg.size() > 0) && (sg.get(0).group == 0)) {
            for (SnoRel sr : sg) {
               I_RelTuple rTuple = new RelVersion(sr.relNid);

               tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(rTuple);
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            }

            i++;                      // skip past 0 index of the "un-grouped"
         }

         // show each of the groups
         for (; i < sgl.size(); i++) {
            sg = sgl.get(i);

            if (sg.size() == 0) {
               continue;              // :TODO: investigate why empty sets exist
            }

            if (showGroupLabels) {    // true shows one label per group
               List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();

               for (SnoRel sr : sg) {
                  grpTuple.add(new RelVersion(sr.relNid));
               }

               tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(grpTuple.get(0));
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            } else {    // if false, show 1 rel per label
               for (SnoRel sr : sg) {
                  I_RelTuple rTuple = new RelVersion(sr.relNid);

                  tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                          showStatusCB.isSelected());
                  tLabelList.add((LabelForTuple) tmpTLabel);
                  tmpDeltaColor = relColorMap.get(rTuple);
                  setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                  formJPanel.add(tmpTLabel.getLabel(), c);
                  c.gridy++;
               }

               c.gridy++;
            }
         }
      }

      c.weightx   = 1.0;
      c.weighty   = 1.0;
      c.gridwidth = 2;
      formJPanel.add(new JPanel(), c);
      formJPanel.setBorder(BorderFactory.createTitledBorder(label));

      return formJPanel;
   }

   /**
    * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
    */
   public JPanel newFormDistJPanel(String label, I_ConfigAceFrame config,
                                   Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
                                   Map<I_DescriptionTuple, Color> desColorMap,
                                   Map<I_RelTuple, Color> relColorMap)
           throws IOException {
      JPanel formJPanel = newMinMaxJPanel();

      formJPanel.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();

      c.fill    = GridBagConstraints.NONE;
      c.anchor  = GridBagConstraints.NORTHWEST;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx   = 0;
      c.gridy   = 0;

      List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();

      c.gridx     = 0;
      c.gridwidth = 2;
      c.anchor    = GridBagConstraints.NORTHWEST;

      // SHOW SELF CONCEPT
      I_ConceptAttributeTuple cTuple    = findSelf(theCBean, cEditPathPos);
      I_ImplementActiveLabel  tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
                                             showStatusCB.isSelected());

      tLabelList.add((LabelForTuple) tmpTLabel);

      Color tmpDeltaColor = conAttrColorMap.get(cTuple);

      setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
      formJPanel.add(tmpTLabel.getLabel(), c);
      c.gridy++;

      // SHOW PROXIMAL ISAs -- as relationships
      SnoGrpList       isaSGList = cSnoTable.getIsaProx();
      List<I_RelTuple> isaList   = new ArrayList<I_RelTuple>();

      for (SnoGrp sg : isaSGList) {
         for (SnoRel sr : sg) {
            isaList.add(new RelVersion(sr.relNid));
         }
      }

      for (I_RelTuple rTuple : isaList) {
         tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                 showStatusCB.isSelected());
         tLabelList.add((LabelForTuple) tmpTLabel);
         tmpDeltaColor = relColorMap.get(rTuple);
         setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
         formJPanel.add(tmpTLabel.getLabel(), c);
         c.gridy++;
      }

      // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
      SnoGrpList sgl = cSnoTable.getRoleDiffFromRootList();

      if (sgl.size() > 0) {
         int    i  = 0;
         SnoGrp sg = sgl.get(0);

         // show each of the non-Rels
         if ((sg.size() > 0) && (sg.get(0).group == 0)) {
            for (SnoRel sr : sg) {
               I_RelTuple rTuple = new RelVersion(sr.relNid);

               tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(rTuple);
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            }

            i++;                      // skip past 0 index of the "un-grouped"
         }

         // show each of the groups
         for (; i < sgl.size(); i++) {
            sg = sgl.get(i);

            if (sg.size() == 0) {
               continue;
            }

            if (showGroupLabels) {    // true shows one label per group
               List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();

               for (SnoRel sr : sg) {
                  grpTuple.add(new RelVersion(sr.relNid));
               }

               tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(grpTuple.get(0));
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            } else {    // if false, show 1 rel per label
               for (SnoRel sr : sg) {
                  I_RelTuple rTuple = new RelVersion(sr.relNid);

                  tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                          showStatusCB.isSelected());
                  tLabelList.add((LabelForTuple) tmpTLabel);
                  tmpDeltaColor = relColorMap.get(rTuple);
                  setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                  formJPanel.add(tmpTLabel.getLabel(), c);
                  c.gridy++;
               }

               c.gridy++;
            }
         }
      }

      c.weightx   = 1.0;
      c.weighty   = 1.0;
      c.gridwidth = 2;
      formJPanel.add(new JPanel(), c);
      formJPanel.setBorder(BorderFactory.createTitledBorder(label));

      return formJPanel;
   }

   /**
    * <b>Long Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)</li>
    *
    */
   public JPanel newFormLongJPanel(String label, I_ConfigAceFrame config,
                                   Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
                                   Map<I_DescriptionTuple, Color> desColorMap,
                                   Map<I_RelTuple, Color> relColorMap)
           throws IOException {
      JPanel formJPanel = newMinMaxJPanel();

      formJPanel.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();

      c.fill    = GridBagConstraints.NONE;
      c.anchor  = GridBagConstraints.NORTHWEST;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx   = 0;
      c.gridy   = 0;

      List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();

      c.gridx     = 0;
      c.gridwidth = 2;
      c.anchor    = GridBagConstraints.NORTHWEST;

      // SHOW SELF CONCEPT
      I_ConceptAttributeTuple cTuple    = findSelf(theCBean, cEditPathPos);
      I_ImplementActiveLabel  tmpTLabel = TermLabelMaker.newLabel(cTuple, showDetailCB.isSelected(),
                                             showStatusCB.isSelected());

      tLabelList.add((LabelForTuple) tmpTLabel);

      Color tmpDeltaColor = conAttrColorMap.get(cTuple);

      setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
      formJPanel.add(tmpTLabel.getLabel(), c);
      c.gridy++;

      // SHOW PROXIMAL ISAs -- as relationships
      SnoGrpList       isaSGList = cSnoTable.getIsaProxPrim();
      List<I_RelTuple> isaList   = new ArrayList<I_RelTuple>();

      for (SnoGrp sg : isaSGList) {
         for (SnoRel sr : sg) {
            isaList.add(new RelVersion(sr.relNid));
         }
      }

      for (I_RelTuple rTuple : isaList) {
         tmpTLabel = TermLabelMaker.newLabel(rTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
         tLabelList.add((LabelForTuple) tmpTLabel);
         tmpDeltaColor = relColorMap.get(rTuple);
         setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
         formJPanel.add(tmpTLabel.getLabel(), c);
         c.gridy++;
      }

      // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
      SnoGrpList sgl = cSnoTable.getRoleDiffFromRootList();

      if (sgl.size() > 0) {
         int    i  = 0;
         SnoGrp sg = sgl.get(0);

         // show each of the non-Rels
         if ((sg.size() > 0) && (sg.get(0).group == 0)) {
            for (SnoRel sr : sg) {
               I_RelTuple rTuple = new RelVersion(sr.relNid);

               tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(rTuple);
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            }

            i++;                      // skip past 0 index of the "un-grouped"
         }

         // show each of the groups
         for (; i < sgl.size(); i++) {
            sg = sgl.get(i);

            if (sg.size() == 0) {
               continue;
            }

            if (showGroupLabels) {    // set to true to show one label per

               // group
               List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();

               for (SnoRel sr : sg) {
                  grpTuple.add(new RelVersion(sr.relNid));
               }

               tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(grpTuple.get(0));
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            } else {    // if false, show 1 relationship per label
               for (SnoRel sr : sg) {
                  I_RelTuple rTuple = new RelVersion(sr.relNid);

                  tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                          showStatusCB.isSelected());
                  tLabelList.add((LabelForTuple) tmpTLabel);
                  tmpDeltaColor = relColorMap.get(rTuple);
                  setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                  formJPanel.add(tmpTLabel.getLabel(), c);
                  c.gridy++;
               }

               c.gridy++;
            }
         }
      }

      c.weightx   = 1.0;
      c.weighty   = 1.0;
      c.gridwidth = 2;
      formJPanel.add(new JPanel(), c);
      formJPanel.setBorder(BorderFactory.createTitledBorder(label));

      return formJPanel;
   }

   /**
    * <b>Short Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)
    * </li>
    */
   public JPanel newFormShortJPanel(String label, I_ConfigAceFrame config,
                                    Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
                                    Map<I_DescriptionTuple, Color> desColorMap,
                                    Map<I_RelTuple, Color> relColorMap)
           throws IOException {
      JPanel formJPanel = newMinMaxJPanel();

      formJPanel.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();

      c.fill    = GridBagConstraints.NONE;
      c.anchor  = GridBagConstraints.NORTHWEST;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx   = 0;
      c.gridy   = 0;

      List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();

      c.gridx     = 0;
      c.gridwidth = 2;
      c.anchor    = GridBagConstraints.NORTHWEST;

      // SHOW SELF CONCEPT
      I_ConceptAttributeTuple cTuple    = findSelf(theCBean, cEditPathPos);
      I_ImplementActiveLabel  tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
                                             showStatusCB.isSelected());

      tLabelList.add((LabelForTuple) tmpTLabel);

      Color tmpDeltaColor = conAttrColorMap.get(cTuple);

      setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
      formJPanel.add(tmpTLabel.getLabel(), c);
      c.gridy++;

      // SHOW PROXIMAL PRIMITIVE ISAs -- as relationships
      SnoGrpList       isaSGList = cSnoTable.getIsaProxPrim();
      List<I_RelTuple> isaList   = new ArrayList<I_RelTuple>();

      for (SnoGrp sg : isaSGList) {
         for (SnoRel sr : sg) {
            isaList.add(new RelVersion(sr.relNid));
         }
      }

      for (I_RelTuple t : isaList) {
         I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, showDetailCB.isSelected(),
                                            showStatusCB.isSelected());

         tLabelList.add((LabelForTuple) tLabel);

         Color deltaColor = relColorMap.get(t);

         setBorder(tLabel.getLabel(), deltaColor);
         formJPanel.add(tLabel.getLabel(), c);
         c.gridy++;
      }

      // SHOW ROLES
      SnoGrpList sgl = cSnoTable.getRoleDiffFromProxPrim();

      if (sgl.size() > 0) {
         int    i  = 0;
         SnoGrp sg = sgl.get(0);

         // show each of the non-Rels
         if ((sg.size() > 0) && (sg.get(0).group == 0)) {
            for (SnoRel sr : sg) {
               I_RelTuple rTuple = new RelVersion(sr.relNid);

               tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(rTuple);
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            }

            i++;                      // skip past 0 index of the "un-grouped"
         }

         // show each of the groups
         for (; i < sgl.size(); i++) {
            sg = sgl.get(i);

            if (sg.size() == 0) {
               continue;
            }

            if (showGroupLabels) {    // true shows one label per group
               List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();

               for (SnoRel sr : sg) {
                  grpTuple.add(new RelVersion(sr.relNid));
               }

               tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(grpTuple.get(0));
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            } else {    // if false, show 1 rel per label
               for (SnoRel sr : sg) {
                  I_RelTuple rTuple = new RelVersion(sr.relNid);

                  tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                          showStatusCB.isSelected());
                  tLabelList.add((LabelForTuple) tmpTLabel);
                  tmpDeltaColor = relColorMap.get(rTuple);
                  setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                  formJPanel.add(tmpTLabel.getLabel(), c);
                  c.gridy++;
               }

               c.gridy++;
            }
         }
      }

      c.weightx   = 1.0;
      c.weighty   = 1.0;
      c.gridwidth = 2;
      formJPanel.add(new JPanel(), c);
      formJPanel.setBorder(BorderFactory.createTitledBorder(label));

      return formJPanel;
   }

   public JPanel newFormStatedJPanel(String label, I_ConfigAceFrame config,
                                     Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
                                     Map<I_DescriptionTuple, Color> desColorMap,
                                     Map<I_RelTuple, Color> relColorMap)
           throws IOException {
      JPanel formJPanel = newMinMaxJPanel();

      formJPanel.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();

      c.fill    = GridBagConstraints.NONE;
      c.anchor  = GridBagConstraints.NORTHWEST;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx   = 0;
      c.gridy   = 0;

      List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();

      c.gridx     = 0;
      c.gridwidth = 2;
      c.anchor    = GridBagConstraints.NORTHWEST;

      // SHOW SELF CONCEPT
      I_ConceptAttributeTuple cTuple    = findSelf(theCBean, cEditPathPos);
      I_ImplementActiveLabel  tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
                                             showStatusCB.isSelected());

      tLabelList.add((LabelForTuple) tmpTLabel);

      Color tmpDeltaColor = conAttrColorMap.get(cTuple);

      setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
      formJPanel.add(tmpTLabel.getLabel(), c);
      c.gridy++;

      // SHOW PROXIMAL ISAs -- as relationships
      SnoGrpList       isaSGList = cSnoTable.getStatedIsaProx();
      List<I_RelTuple> isaList   = new ArrayList<I_RelTuple>();

      for (SnoGrp sg : isaSGList) {
         for (SnoRel sr : sg) {
            isaList.add(new RelVersion(sr.relNid));
         }
      }

      for (I_RelTuple rTuple : isaList) {
         tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                 showStatusCB.isSelected());
         tLabelList.add((LabelForTuple) tmpTLabel);
         tmpDeltaColor = relColorMap.get(rTuple);
         setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
         formJPanel.add(tmpTLabel.getLabel(), c);
         c.gridy++;
      }

      // GET IMMEDIATE PROXIMAL ROLES & SEPARATE INTO GROUPS
      SnoGrpList sgl = cSnoTable.getStatedRole();

      // !!! :FIXME: UNGROUPED, NOT LONGER JUST THE "0" GROUP
      if (sgl.size() > 0) {
         int    i  = 0;
         SnoGrp sg = sgl.get(0);

         // show each of the non-Rels
         if ((sg.size() > 0) && (sg.get(0).group == 0)) {
            for (SnoRel sr : sg) {
               I_RelTuple rTuple = new RelVersion(sr.relNid);

               tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(rTuple);
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            }

            i++;                      // skip past 0 index of the "un-grouped"
         }

         // show each of the groups
         for (; i < sgl.size(); i++) {
            sg = sgl.get(i);

            if (sg.size() == 0) {
               continue;
            }

            if (showGroupLabels) {    // true shows one label per group
               List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();

               for (SnoRel sr : sg) {
                  grpTuple.add(new RelVersion(sr.relNid));
               }

               tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(),
                       showStatusCB.isSelected());
               tLabelList.add((LabelForTuple) tmpTLabel);
               tmpDeltaColor = relColorMap.get(grpTuple.get(0));
               setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
               formJPanel.add(tmpTLabel.getLabel(), c);
               c.gridy++;
            } else {    // if false, show 1 rel per label
               for (SnoRel sr : sg) {
                  I_RelTuple rTuple = new RelVersion(sr.relNid);

                  tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                          showStatusCB.isSelected());
                  tLabelList.add((LabelForTuple) tmpTLabel);
                  tmpDeltaColor = relColorMap.get(rTuple);
                  setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                  formJPanel.add(tmpTLabel.getLabel(), c);
                  c.gridy++;
               }

               c.gridy++;
            }
         }
      }

      c.weightx   = 1.0;
      c.weighty   = 1.0;
      c.gridwidth = 2;
      formJPanel.add(new JPanel(), c);
      formJPanel.setBorder(BorderFactory.createTitledBorder(label));

      return formJPanel;
   }

   private JPanel newMinMaxJPanel() {
      JPanel p = new JPanel() {
         private static final long serialVersionUID = 1L;
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();

            // DO NOT ALLOW "PREFERRED" WIDTH TO GO BELOW "MIN"
            d.width = Math.max(d.width, minPartPanelSize.width);

            // DO NOT ALLOW "PREFERRED" HEIGHT TO GO BELOW "MIN"
            d.height = Math.max(d.height, minPartPanelSize.height);

            // DO NOT ALLOW "PREFERRED" HEIGHT TO GO ABOVE "MAX"
            d.height = Math.min(d.height, maxPartPanelSize.height);

            return d;
         }
      };

      setMinMaxSize(p);

      return p;
   }

   //~--- get methods ---------------------------------------------------------

   public List<I_ImplementActiveLabel> getCommonLabels(boolean showLongForm, boolean showStatus,
           I_ConfigAceFrame config)
           throws IOException, TerminologyException {
      List<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

      // GET CONCEPT ATTRIBUTES
      Set<? extends I_ConceptAttributeTuple> commonConTuples =
         this.theCBean.getCommonConceptAttributeTuples(config);    // ####

      // COMMON
      // CON
      // CREATE CONCEPT ATTRIBUTE LABELS
      if (commonConTuples != null) {
         for (I_ConceptAttributeTuple t : commonConTuples) {
            I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);

            setBorder(conAttrLabel.getLabel(), null);
            labelList.add(conAttrLabel);
         }
      }

      // GET SOURCE RELATIONSHIPS
      Set<? extends I_RelTuple> commonRelTuples = this.theCBean.getCommonRelTuples(config);    // ####

      // COMMON
      // REL
      // CREATE RELATIONSHIP LABELS
      if (commonRelTuples != null) {
         for (I_RelTuple t : commonRelTuples) {
            I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);

            setBorder(relLabel.getLabel(), null);
            labelList.add(relLabel);
         }
      }

      return labelList;
   }    // getCommonLabels

   public Collection<I_ImplementActiveLabel> getDeltaLabels(boolean showLongForm, boolean showStatus,
           I_ConfigAceFrame config, DeltaColors colors, Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
           Map<I_DescriptionTuple, Color> descColorMap, Map<I_RelTuple, Color> relColorMap)
           throws IOException, TerminologyException {
      Set<I_ConceptAttributeTuple> allConAttrTuples = new HashSet<I_ConceptAttributeTuple>();
      Set<I_RelTuple>              allRelTuples     = new HashSet<I_RelTuple>();

      // FIND ALL...
      for (PositionBI p : config.getViewPositionSet()) {
         Set<PositionBI> pSet = new HashSet<PositionBI>();

         pSet.add(p);

         PositionSetReadOnly posSet = new PositionSetReadOnly(pSet);

         // concept attributes
         List<? extends I_ConceptAttributeTuple> conTuplesForPosition =
            this.theCBean.getConceptAttributeTuples(config.getAllowedStatus(), posSet,
               config.getPrecedence(), config.getConflictResolutionStrategy());    // ####

         // ALL
         // COMMON
         // CON
         allConAttrTuples.addAll(conTuplesForPosition);

         // relationships
         List<? extends I_RelTuple> relTuplesForPosition =
            this.theCBean.getSourceRelTuples(config.getAllowedStatus(), null, posSet, config.getPrecedence(),
               config.getConflictResolutionStrategy());    // ####

         // ALL
         // REL
         allRelTuples.addAll(relTuplesForPosition);
      }

      // FIND & REMOVE COMMON...
      Set<? extends I_ConceptAttributeTuple> commonConAttrTuples =
         this.theCBean.getCommonConceptAttributeTuples(config);    // ####

      // COMMON
      // CON
      allConAttrTuples.removeAll(commonConAttrTuples);

      Set<? extends I_RelTuple> commonRelTuples = this.theCBean.getCommonRelTuples(config);    // ####

      // COMMON
      // REL
      allRelTuples.removeAll(commonRelTuples);

      Collection<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

      // CREATE CONCEPT ATTRIBUTE LABELS
      for (I_ConceptAttributeTuple t : allConAttrTuples) {
         I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
         Color                  deltaColor   = colors.getNextColor();

         conAttrColorMap.put(t, deltaColor);
         setBorder(conAttrLabel.getLabel(), deltaColor);
         labelList.add(conAttrLabel);
      }

      // CREATE RELATIONSHIP LABELS
      for (I_RelTuple t : allRelTuples) {
         I_ImplementActiveLabel relLabel   = TermLabelMaker.newLabel(t, showLongForm, showStatus);
         Color                  deltaColor = colors.getNextColor();

         relColorMap.put(t, deltaColor);
         setBorder(relLabel.getLabel(), deltaColor);
         labelList.add(relLabel);
      }

      return labelList;
   }    // getDeltaLabels

   //~--- set methods ---------------------------------------------------------

   private void setBorder(JLabel tLabel, Color deltaColor) {
      if (deltaColor == null) {
         deltaColor = Color.white;
      }

      Dimension size = tLabel.getSize();

      tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
              BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 5, 1, 5, deltaColor),
                 BorderFactory.createEmptyBorder(1, 3, 1, 3))));
      size.width  = size.width + 18;
      size.height = size.height + 6;
      tLabel.setSize(size);
      tLabel.setPreferredSize(size);
      tLabel.setMaximumSize(size);
      tLabel.setMinimumSize(size);
   }

   public void setConcept(I_GetConceptData conceptIn, I_ConfigAceFrame config)
           throws IOException, TerminologyException {
      this.theCBean = conceptIn;
      this.config   = config;
      commonJPanel.removeAll();
      deltaJPanel.removeAll();
      formsJPanel.removeAll();    // FORMS HAS SUBPANELS: STATED & COMPUTED

      if (conceptIn == null) {
         return;
      }

      // COMMON & DIFFERENT SECTION
      // COMMON PANEL
      commonLabels     = getCommonLabels(showDetailCB.isSelected(), showStatusCB.isSelected(), config);    // ####
      commonPartJPanel = new JPanel();
      setMinMaxSize(commonPartJPanel);
      commonPartJPanel.setLayout(new BoxLayout(commonPartJPanel, BoxLayout.Y_AXIS));

      for (I_ImplementActiveLabel l : commonLabels) {
         commonPartJPanel.add(l.getLabel());
      }

      GridBagConstraints c = new GridBagConstraints();

      c.fill       = GridBagConstraints.NONE;
      c.anchor     = GridBagConstraints.NORTHWEST;
      c.gridheight = 1;
      c.gridwidth  = 1;
      c.weightx    = 0;
      c.weighty    = 0;
      c.gridx      = 0;
      c.gridy      = 0;
      commonJPanel.add(commonPartJPanel, c);

      // DELTA (DIFFERENCES) PANEL
      Map<I_ConceptAttributeTuple, Color> conAttrColorMap = new HashMap<I_ConceptAttributeTuple, Color>();
      Map<I_DescriptionTuple, Color>      desColorMap     = new HashMap<I_DescriptionTuple, Color>();
      Map<I_RelTuple, Color>              relColorMap     = new HashMap<I_RelTuple, Color>();

      colors.reset();

      Collection<I_ImplementActiveLabel> deltaLabels = getDeltaLabels(showDetailCB.isSelected(),
                                                          showStatusCB.isSelected(), config, colors,
                                                          conAttrColorMap, desColorMap, relColorMap);    // ####

      deltaPartJPanel = new JPanel();
      deltaPartJPanel.setLayout(new BoxLayout(deltaPartJPanel, BoxLayout.Y_AXIS));

      for (I_ImplementActiveLabel l : deltaLabels) {
         deltaPartJPanel.add(l.getLabel());
      }

      deltaJPanel.add(deltaPartJPanel);

      // FORM STATED PANEL
      c         = new GridBagConstraints();
      c.fill    = GridBagConstraints.VERTICAL;
      c.anchor  = GridBagConstraints.NORTHWEST;
      c.weightx = 0;    // horizontal free space distribution weight
      c.weighty = 0;    // vertical free space distribution weight
      c.gridx   = 0;
      c.gridy   = 0;

      JPanel tmpJPanel;

      tmpJPanel = newFormStatedJPanel("Stated Form:", config, conAttrColorMap, desColorMap, relColorMap);    // ####
      setMinMaxSize(tmpJPanel);
      formsJPanel.add(tmpJPanel, c);

      // FORM DISTRIBUTION NORMAL PANEL
      if (showDistFormCB.isSelected()) {
         c.gridx++;

         if (c.gridx == 2) {
            c.gridx = 0;
            c.gridy++;
         }

         tmpJPanel = newFormDistJPanel("Distribution Normal Form:", config, conAttrColorMap, desColorMap,
                                       relColorMap);    // ####
         setMinMaxSize(tmpJPanel);
         formsJPanel.add(tmpJPanel, c);
      }

      // AUTHORING NORMAL FORM PANEL
      if (showAuthFormCB.isSelected()) {
         c.gridx++;

         if (c.gridx == 2) {
            c.gridx = 0;
            c.gridy++;
         }

         tmpJPanel = newFormAuthJPanel("Authoring Normal Form:", config, conAttrColorMap, desColorMap,
                                       relColorMap);    // ####
         setMinMaxSize(tmpJPanel);
         formsJPanel.add(tmpJPanel, c);
      }

      // LONG CANONICAL FORM PANEL
      if (showLongFormCB.isSelected()) {
         c.gridx++;

         if (c.gridx == 2) {
            c.gridx = 0;
            c.gridy++;
         }

         tmpJPanel = newFormLongJPanel("Long Canonical Form:", config, conAttrColorMap, desColorMap,
                                       relColorMap);    // ####
         setMinMaxSize(tmpJPanel);
         formsJPanel.add(tmpJPanel, c);
      }

      // FORM SHORT CANONICAL PANEL
      if (showShortFormCB.isSelected()) {
         c.gridx++;

         if (c.gridx == 2) {
            c.gridx = 0;
            c.gridy++;
         }

         tmpJPanel = newFormShortJPanel("Short Canonical Form:", config, conAttrColorMap, desColorMap,
                                        relColorMap);    // ####
         setMinMaxSize(tmpJPanel);
         formsJPanel.add(tmpJPanel, c);
      }
   }

   private void setMinMaxSize(JPanel panel) {
      panel.setMinimumSize(minPartPanelSize);
      panel.setMaximumSize(maxPartPanelSize);
   }

   //~--- inner classes -------------------------------------------------------

   public static class ConceptAttrVersion implements I_ConceptAttributeTuple {
      I_ConceptAttributeVersioned<?> core;
      transient Integer              hash;
      I_ConceptAttributePart         part;

      //~--- constructors -----------------------------------------------------

      public ConceptAttrVersion(I_ConceptAttributeVersioned core, I_ConceptAttributePart part) {
         super();
         this.core = core;
         this.part = part;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean addAnnotation(RefexChronicleBI<?> annotation) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Deprecated
      @Override
      public I_ConceptAttributePart duplicate() {
         return part.duplicate();
      }

      @Override
      public boolean equals(Object obj) {
         ConceptAttrVersion another = (ConceptAttrVersion) obj;

         return core.equals(another.core) && part.equals(another.part);
      }

      @Override
      public int hashCode() {
         if (hash == null) {
            hash = HashFunction.hashCode(new int[] { core.hashCode(), part.hashCode() });
         }

         return hash;
      }

      @Override
      public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public ConceptAttributeAnalogBI makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
         return (ConceptAttributeAnalogBI) part.makeAnalog(statusNid, time, authorNid, moduleNid, pathNid);
      }

      @Override
      public boolean stampIsInRange(int min, int max) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String toString() {
         return "ThinConTuple id: " + getConId() + " status: " + getConceptStatus() + " defined: "
                + isDefined() + " path: " + getPathId() + " version: " + getVersion();
      }

      @Override
      public String toUserString() {
         return core.toUserString();
      }

      @Override
      public String toUserString(TerminologySnapshotDI snapshot) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public Collection<? extends IdBI> getAdditionalIds() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends IdBI> getAllIds() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Set<Integer> getAllNidsForVersion() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Set getAllStampNids() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public int getAuthorNid() {
         return part.getAuthorNid();
      }
      
      @Override
      public ComponentChronicleBI getChronicle() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConId()
       */
      @Override
      public int getConId() {
         return core.getConId();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConVersioned()
       */
      @Override
      public I_ConceptAttributeVersioned getConVersioned() {
         return core;
      }

      @Override
      public int getConceptNid() {
         return part.getConceptNid();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConceptStatus()
       */
      @Deprecated
      @Override
      public int getConceptStatus() {
         return part.getStatusId();
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
              throws IOException {
         return core.getAnnotationsActive(xyz);
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz, int refexNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz)
              throws IOException {
         return core.getRefexesActive(xyz);
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
              throws IOException {
         return core.getActiveRefexes(xyz, refsetNid);
      }

      @Override
      public I_ConceptAttributeVersioned getFixedPart() {
         return core;
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public I_ConceptAttributePart getMutablePart() {
         return part;
      }

      @Override
      public int getNid() {
         return core.getNid();
      }

      @Override
      public ArrayIntList getPartComponentNids() {
         return part.getPartComponentNids();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getPathId()
       */
      @Deprecated
      @Override
      public int getPathId() {
         return part.getPathId();
      }

      @Override
      public int getPathNid() {
         return part.getPathNid();
      }

      @Override
      public PositionBI getPosition() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Set getPositions() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public UUID getPrimUuid() {
         return core.getPrimUuid();
      }

      @Override
      public ConceptAttrVersion getPrimordialVersion() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public int getStampNid() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Deprecated
      @Override
      public int getStatusId() {
         return getConceptStatus();
      }

      @Override
      public int getStatusNid() {
         return part.getStatusNid();
      }
      
      @Override
      public int getModuleNid() {
         return part.getModuleNid();
      }

      @Override
      public long getTime() {
         return part.getTime();
      }

      @Override
      public List<UUID> getUUIDs() {
         return core.getUUIDs();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getVersion()
       */
      @Deprecated
      @Override
      public int getVersion() {
         return part.getVersion();
      }

      @Override
      public ConceptAttributeVersionBI getVersion(ViewCoordinate c) throws ContradictionException {
         return core.getVersion(c);
      }

      @Override
      public Collection<? extends ConceptAttributeVersionBI> getVersions() {
         return core.getVersions();
      }

      @Override
      public Collection<? extends ConceptAttributeVersionBI> getVersions(ViewCoordinate c) {
         return core.getVersions(c);
      }

      @Override
      public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean hasExtensions() {
         return false;
      }

      @Override
      public boolean isActive(NidSetBI allowedStatusNids) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isActive(ViewCoordinate vc) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isBaselineGeneration() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#isDefined()
       */
      @Override
      public boolean isDefined() {
         return part.isDefined();
      }

      @Override
      public boolean isUncommitted() {
         return core.isUncommitted();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setAuthorNid(int authorNid) throws PropertyVetoException {
         part.setAuthorNid(authorNid);
      }

      public void setConceptStatus(int conceptStatus) throws PropertyVetoException {
         part.setStatusNid(conceptStatus);
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#setDefined(boolean)
       */
      @Override
      public void setDefined(boolean defined) throws PropertyVetoException {
         part.setDefined(defined);
      }

      @Deprecated
      @Override
      public void setPathId(int pathId) throws PropertyVetoException {
         part.setPathId(pathId);
      }

      @Override
      public void setPathNid(int pathNid) throws PropertyVetoException {
         part.setPathNid(pathNid);
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.ace.api.I_AmPart#setStatusId(int)
       */
      @Deprecated
      @Override
      public void setStatusId(int statusId) throws PropertyVetoException {
         part.setStatusId(statusId);
      }

      /*
       * (non-Javadoc)
       *
       * @see
       * org.dwfa.vodb.types.I_ConceptAttributeTuple#setStatusId(java.lang.Integer
       * )
       */
      @Deprecated
      public void setStatusId(Integer statusId) throws PropertyVetoException {
         part.setStatusId(statusId);
      }

      @Override
      public void setStatusNid(int statusNid) throws PropertyVetoException {
         part.setStatusNid(statusNid);
      }
      
      @Override
      public void setModuleNid(int moduleNid) throws PropertyVetoException {
         part.setModuleNid(moduleNid);
      }

      @Override
      public void setTime(long value) throws PropertyVetoException {
         part.setTime(value);
      }

      public void setVersion(int version) {
         throw new UnsupportedOperationException();
      }

        @Override
        public ConceptChronicleBI getEnclosingConcept() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConceptAttributeAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCanceled() throws IOException {
            return part.isCanceled();
        }
   }


   /**
    * <b>DeltaColors</b><br>
    * Uses <code>AWT Color</code> object which use some of the following <a
    * href=http://www.w3schools.com/html/html_colornames.asp>color names &
    * values</a>. These colors are used to highlight differences.
    */
   public static class DeltaColors {
      private List<Color> colorList    = new ArrayList<Color>();    // AWT: Color
      int                 currentColor = 0;

      //~--- constructors -----------------------------------------------------

      public DeltaColors() {
         super();

         // Link for colors
         // http://www.w3schools.com/html/html_colornames.asp
         colorList.add(new Color(0x5F9EA0));
         colorList.add(new Color(0x7FFF00));
         colorList.add(new Color(0xD2691E));
         colorList.add(new Color(0x6495ED));
         colorList.add(new Color(0xDC143C));
         colorList.add(new Color(0xB8860B));
         colorList.add(new Color(0xFF8C00));
         colorList.add(new Color(0x8FBC8F));
         colorList.add(new Color(0x483D8B));
         colorList.add(new Color(0x1E90FF));
         colorList.add(new Color(0xFFD700));
         colorList.add(new Color(0xF0E68C));
         colorList.add(new Color(0x90EE90));
         colorList.add(new Color(0x8470FF));    // 14 colors
      }

      //~--- methods ----------------------------------------------------------

      public void reset() {
         currentColor = 0;
      }

      //~--- get methods ------------------------------------------------------

      public Color getNextColor() {
         if (currentColor == colorList.size()) {
            reset();
         }

         return colorList.get(currentColor++);
      }
   }


   private static class RelVersion implements I_RelTuple {
      I_RelVersioned<?> fixedPart;
      transient Integer hash;
      I_RelPart         part;

      //~--- constructors -----------------------------------------------------

      public RelVersion(int relNid) {

         // :NYI: needs to be implemented to construct tuple from rel nid
         throw new UnsupportedOperationException();
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate ec, long time) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void convertIds(I_MapNativeToNative jarToDbNativeMap) {

         // TODO
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#duplicate()
       */
      @Deprecated
      @Override
      public I_RelPart duplicate() {
         return part.duplicate();
      }

      @Override
      public boolean equals(Object obj) {
         RelVersion another = (RelVersion) obj;

         return fixedPart.equals(another.fixedPart) && part.equals(another.part);
      }

      @Override
      public int hashCode() {
         if (hash == null) {
            hash = HashFunction.hashCode(new int[] { fixedPart.hashCode(), part.hashCode() });
         }

         return hash;
      }

      @Override
      public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
         throw new UnsupportedOperationException("Not supported yet.");
      }
      
      @Override
      public RelationshipAnalogBI makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
         return (RelationshipAnalogBI) part.makeAnalog(statusNid, time, authorNid, moduleNid, pathNid);
      }

      @Override
      public boolean stampIsInRange(int min, int max) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String toString() {
         StringBuilder buff = new StringBuilder();

         buff.append("ThinRelTuple: relId: ");
         buff.append(getRelId());
         buff.append(" c1id: ");
         buff.append(getC1Id());
         buff.append(" c2id: ");
         buff.append(getC2Id());
         buff.append(" ");
         buff.append(part.toString());

         return buff.toString();
      }

      @Override
      public String toUserString() {
         return fixedPart.toUserString();
      }

      @Override
      public String toUserString(TerminologySnapshotDI snapshot) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public Collection<? extends IdBI> getAdditionalIds() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends IdBI> getAllIds() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Set<Integer> getAllNidsForVersion() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Set getAllStampNids() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public int getAuthorNid() {
         return part.getAuthorNid();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getC1Id()
       */
      @Override
      public int getC1Id() {
         return fixedPart.getC1Id();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getC2Id()
       */
      @Override
      public int getC2Id() {
         return fixedPart.getC2Id();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getCharacteristicId()
       */
      @Override
      public int getCharacteristicId() {
         return part.getCharacteristicId();
      }

      @Override
      public int getCharacteristicNid() {
         return fixedPart.getCharacteristicNid();
      }

      @Override
      public ComponentChronicleBI getChronicle() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public int getConceptNid() {
         return fixedPart.getSourceNid();
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz, int refexNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public int getTargetNid() {
         return fixedPart.getTargetNid();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getFixedPart()
       */
      @Override
      public I_RelVersioned getFixedPart() {
         return fixedPart;
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getGroup()
       */
      @Override
      public int getGroup() {
         return part.getGroup();
      }

      @Override
      public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz)
              throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public I_RelPart getMutablePart() {
         return part;
      }

      @Override
      public int getNid() {
         return fixedPart.getNid();
      }

      @Override
      public int getSourceNid() {
         return fixedPart.getSourceNid();
      }

      @Override
      public ArrayIntList getPartComponentNids() {
         return part.getPartComponentNids();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getPathId()
       */
      @Deprecated
      @Override
      public int getPathId() {
         return part.getPathId();
      }

      @Override
      public int getPathNid() {
         return part.getPathNid();
      }

      @Override
      public PositionBI getPosition() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Set getPositions() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public UUID getPrimUuid() {
         return fixedPart.getPrimUuid();
      }

      @Override
      public RelVersion getPrimordialVersion() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getRefinabilityId()
       */
      @Override
      public int getRefinabilityId() {
         return part.getRefinabilityId();
      }

      @Override
      public int getRefinabilityNid() {
         return fixedPart.getRefinabilityNid();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getRelId()
       */
      @Override
      public int getRelId() {
         return fixedPart.getRelId();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getRelVersioned()
       */
      @Override
      public I_RelVersioned getRelVersioned() {
         return fixedPart;
      }

      @Override
      public int getStampNid() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getStatusId()
       */
      @Deprecated
      @Override
      public int getStatusId() {
         return part.getStatusId();
      }

      @Override
      public int getStatusNid() {
         return part.getStatusNid();
      }
      
      @Override
      public int getModuleNid() {
         return part.getModuleNid();
      }

      @Override
      public long getTime() {
         return part.getTime();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getTypeId()
       */
      @Deprecated
      @Override
      public int getTypeId() {
         return part.getTypeId();
      }

      @Override
      public int getTypeNid() {
         return part.getTypeNid();
      }

      @Override
      public List<UUID> getUUIDs() {
         return fixedPart.getUUIDs();
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#getVersion()
       */
      @Deprecated
      @Override
      public int getVersion() {
         return part.getVersion();
      }

      @Override
      public RelationshipVersionBI<?> getVersion(ViewCoordinate c) throws ContradictionException {
         return fixedPart.getVersion(c);
      }

      @Override
      public Collection<? extends RelationshipVersionBI> getVersions() {
         return fixedPart.getVersions();
      }

      @Override
      public Collection<? extends RelationshipVersionBI> getVersions(ViewCoordinate c) {
         return fixedPart.getVersions(c);
      }

      @Override
      public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean hasExtensions() {
         return false;
      }

      @Override
      public boolean isActive(NidSetBI allowedStatusNids) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isActive(ViewCoordinate vc) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isBaselineGeneration() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isInferred() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isStated() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isUncommitted() {
         return fixedPart.isUncommitted();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setAuthorNid(int authorNid) throws PropertyVetoException {
         part.setAuthorNid(authorNid);
      }

      public void setC2Id(int destId) {
         fixedPart.setC2Id(destId);
      }

      /*
       * (non-Javadoc)
       *
       * @see
       * org.dwfa.vodb.types.I_RelTuple#setCharacteristicId(java.lang.Integer)
       */
      @Override
      public void setCharacteristicId(int characteristicId) throws PropertyVetoException {
         part.setCharacteristicId(characteristicId);
      }

      @Override
      public void setCharacteristicNid(int nid) throws PropertyVetoException {
         fixedPart.setCharacteristicNid(nid);
      }

      @Override
      public void setTargetNid(int nid) throws PropertyVetoException {
         fixedPart.setTargetNid(nid);
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#setGroup(java.lang.Integer)
       */
      @Override
      public void setGroup(int group) throws PropertyVetoException {
         part.setGroup(group);
      }

      @Override
      public void setNid(int nid) throws PropertyVetoException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Deprecated
      @Override
      public void setPathId(int pathId) throws PropertyVetoException {
         part.setPathId(pathId);
      }

      @Override
      public void setPathNid(int pathNid) throws PropertyVetoException {
         part.setPathNid(pathNid);
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#setRefinabilityId(java.lang.Integer)
       */
      @Override
      public void setRefinabilityId(int refinabilityId) throws PropertyVetoException {
         part.setRefinabilityId(refinabilityId);
      }

      @Override
      public void setRefinabilityNid(int nid) throws PropertyVetoException {
         fixedPart.setRefinabilityNid(nid);
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#setStatusId(java.lang.Integer)
       */
      @Deprecated
      @Override
      public void setStatusId(int statusId) throws PropertyVetoException {
         part.setStatusId(statusId);
      }

      @Override
      public void setStatusNid(int statusNid) throws PropertyVetoException {
         part.setStatusNid(statusNid);
      }
      
      
      @Override
      public void setModuleNid(int moduleNid) throws PropertyVetoException {
         part.setModuleNid(moduleNid);
      }

      @Override
      public void setTime(long value) throws PropertyVetoException {
         part.setTime(value);
      }

      /*
       * (non-Javadoc)
       *
       * @see org.dwfa.vodb.types.I_RelTuple#setTypeId(java.lang.Integer)
       */
      @Deprecated
      @Override
      public void setTypeId(int typeId) throws PropertyVetoException {
         part.setTypeId(typeId);
      }

      @Override
      public void setTypeNid(int typeNid) throws PropertyVetoException {
         part.setTypeNid(typeNid);
      }

        @Override
        public ConceptChronicleBI getEnclosingConcept() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RelationshipCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCanceled() throws IOException {
            return fixedPart.isCanceled();
        }
   }
}
