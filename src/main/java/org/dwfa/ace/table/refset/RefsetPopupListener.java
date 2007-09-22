package org.dwfa.ace.table.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.ThinExtByRefPart;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;

public class RefsetPopupListener extends MouseAdapter {

   private RefsetMemberTableModel model;

   private class ChangeActionListener implements ActionListener {

      public ChangeActionListener() {
         super();
      }

      public void actionPerformed(ActionEvent e) {
         try {
            for (I_Path p : config.getEditingPathSet()) {
               ThinExtByRefPart newPart = selectedObject.getTuple().getPart().duplicatePart();
               newPart.setPathId(p.getConceptId());
               newPart.setVersion(Integer.MAX_VALUE);
               setProperStatus(newPart);
               model.referencedConcepts.put(newPart.getStatus(), ConceptBean.get(newPart.getStatus()));
               selectedObject.getTuple().addVersion(newPart);
            }
            ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
            model.allTuples = null;
            model.fireTableDataChanged();
            model.propertyChange(null);
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }

   private class ChangeFieldActionListener implements ActionListener {
      private Collection<UUID> ids;

      private REFSET_FIELDS field;

      public ChangeFieldActionListener(Collection<UUID> ids, REFSET_FIELDS field) {
         super();
         this.ids = ids;
         this.field = field;
      }

      public void actionPerformed(ActionEvent e) {
         try {
            for (I_Path p : config.getEditingPathSet()) {
               ThinExtByRefPart newPart = selectedObject.getTuple().getPart().duplicatePart();
               newPart.setPathId(p.getConceptId());
               newPart.setVersion(Integer.MAX_VALUE);
               setProperStatus(newPart);
               switch (field) {
               case STATUS:
                  newPart.setStatus((AceConfig.getVodb().uuidToNative(ids)));
                  model.referencedConcepts.put(newPart.getStatus(), ConceptBean.get(newPart.getStatus()));
                  break;
               case CONCEPT_ID:
                  ((ThinExtByRefPartConcept) newPart).setConceptId((AceConfig.getVodb().uuidToNative(ids)));
                  break;
               case ACCEPTABILITY:
                  ((ThinExtByRefPartLanguage) newPart).setAcceptabilityId((AceConfig.getVodb().uuidToNative(ids)));
                  break;
               case CORRECTNESS:
                  ((ThinExtByRefPartLanguage) newPart).setCorrectnessId((AceConfig.getVodb().uuidToNative(ids)));
                  break;
               case DEGREE_OF_SYNONYMY:
                  ((ThinExtByRefPartLanguage) newPart).setDegreeOfSynonymyId((AceConfig.getVodb().uuidToNative(ids)));
                  break;
               case TAG:
                  ((ThinExtByRefPartLanguageScoped) newPart).setTagId((AceConfig.getVodb().uuidToNative(ids)));
                  break;
               case SCOPE:
                  ((ThinExtByRefPartLanguageScoped) newPart).setScopeId((AceConfig.getVodb().uuidToNative(ids)));
                  break;

               case INTEGER_VALUE:
               case PRIORITY:
               case BOOLEAN_VALUE:
               case MEASUREMENT_VALUE:
               case MEASUREMENT_UNITS_ID:
               default:
                  throw new Exception("Don't know how to handle: " + field);
               }

               model.referencedConcepts.put(AceConfig.getVodb().uuidToNative(ids), ConceptBean.get((AceConfig.getVodb()
                     .uuidToNative(ids))));
               model.referencedConcepts.put(newPart.getStatus(), ConceptBean.get(newPart.getStatus()));

               selectedObject.getTuple().addVersion(newPart);
            }
            ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
            model.allTuples = null;
            model.fireTableDataChanged();
            model.propertyChange(null);
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }

   }

   private JPopupMenu popup;

   private JTable table;

   private ActionListener change;

   private StringWithExtTuple selectedObject;

   private I_ConfigAceFrame config;

   private I_RefsetDefaults preferences;

   public RefsetPopupListener(JTable table, I_ConfigAceFrame config, I_RefsetDefaults defaults,
                              RefsetMemberTableModel model) {
      super();
      this.table = table;
      this.config = config;
      this.model = model;
      this.preferences = defaults;
      
      change = new ChangeActionListener();
   }

   private void makePopup(MouseEvent e) {
      try {
         popup = null;
         int column = table.columnAtPoint(e.getPoint());
         int row = table.rowAtPoint(e.getPoint());
         if ((row != -1) && (column != -1)) {
            popup = new JPopupMenu();
            JMenuItem noActionItem = new JMenuItem("");
            popup.add(noActionItem);
            selectedObject = (StringWithExtTuple) table.getValueAt(row, column);
            JMenuItem changeItem = new JMenuItem("Change");
            popup.add(changeItem);
            changeItem.addActionListener(change);
            for (REFSET_FIELDS field: model.getPopupFields()) {
               JMenu changeMenu = new JMenu("Change " + field.getColumnName());
               popup.add(changeMenu);

               switch (field) {
               case STATUS:
                  addSubmenuItems(changeMenu, field, preferences.getStatusPopupIds());
                  break;
               case CONCEPT_ID:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsConcept)preferences).getConceptPopupIds());
                  break;
               case ACCEPTABILITY:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsLanguage)preferences).getAcceptabilityPopupIds());
                  break;
               case CORRECTNESS:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsLanguage)preferences).getCorrectnessPopupIds());
                  break;
               case DEGREE_OF_SYNONYMY:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsLanguage)preferences).getDegreeOfSynonymyPopupIds());
                  break;
               case TAG:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsLanguageScoped)preferences).getTagPopupIds());
                  break;
               case SCOPE:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsLanguageScoped)preferences).getScopePopupIds());
                  break;
               case MEASUREMENT_UNITS_ID:
                  addSubmenuItems(changeMenu, field, ((RefsetDefaultsMeasurement)preferences).getUnitsOfMeasurePopupIds());

               case INTEGER_VALUE:
               case PRIORITY:
               case BOOLEAN_VALUE:
               case MEASUREMENT_VALUE:
               default:
                  AceLog.getAppLog().alertAndLogException(new Exception("Don't know how to handle: " + field));
               }
               
               
            }
         }
      } catch (TerminologyException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      }
   }
   private void setProperStatus(ThinExtByRefPart newPart) throws Exception {
      newPart.setStatus(preferences.getDefaultStatusForRefset().getConceptId());
   }

   private void addSubmenuItems(JMenu menu, REFSET_FIELDS field, I_IntList possibleValues) throws TerminologyException,
         IOException {
      for (int id : possibleValues.getListValues()) {
         I_GetConceptData possibleValue = LocalVersionedTerminology.get().getConcept(id);
         JMenuItem changeStatusItem = new JMenuItem(possibleValue.toString());
         changeStatusItem.addActionListener(new ChangeFieldActionListener(possibleValue.getUids(), field));
         menu.add(changeStatusItem);
      }
   }

   public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
   }

   public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
   }

   private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
         if (config.getEditingPathSet().size() > 0) {
            int column = table.columnAtPoint(e.getPoint());
            int row = table.rowAtPoint(e.getPoint());
            selectedObject = (StringWithExtTuple) table.getValueAt(row, column);
            if (selectedObject.getTuple().getVersion() == Integer.MAX_VALUE) {
               JOptionPane
                     .showMessageDialog(
                           table.getTopLevelAncestor(),
                           "<html>To change an uncommitted relationship, <br>use the cancel button, or change the value<br>directly on the uncommitted concept...");
            } else {
               makePopup(e);
               if (popup != null) {
                  popup.show(e.getComponent(), e.getX(), e.getY());
               }
            }
         } else {
            JOptionPane.showMessageDialog(table.getTopLevelAncestor(),
                  "You must select at least one path to edit on...");
         }
      }
   }
}
