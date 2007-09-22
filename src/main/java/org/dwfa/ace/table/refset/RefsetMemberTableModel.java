package org.dwfa.ace.table.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.ThinExtByRefPart;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefTuple;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

public class RefsetMemberTableModel extends AbstractTableModel implements PropertyChangeListener, I_HoldRefsetData,
      ActionListener {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public enum REFSET_FIELDS {
      // All extensions
      REFSET_ID("refset", 5, 75, 1000), MEMBER_ID("member id", 5, 100, 100), COMPONENT_ID("component id", 5, 100, 100), STATUS(
            "status", 5, 50, 250), VERSION("version", 5, 140, 140), BRANCH("path", 5, 90, 180),

      // Boolean extension
      BOOLEAN_VALUE("boolean value", 5, 100, 500),

      // Concept extension
      CONCEPT_ID("concept", 5, 300, 1000),

      // Integer extension
      INTEGER_VALUE("integer value", 5, 100, 500),

      // Language extension
      ACCEPTABILITY("acceptability", 5, 125, 1000), CORRECTNESS("correctness", 5, 125, 1000), DEGREE_OF_SYNONYMY(
            "synonymy", 5, 125, 1000),

      // Scoped language extension
      TAG("tag", 5, 100, 1000), SCOPE("scope", 5, 100, 1000), PRIORITY("priority", 5, 50, 100),

      // Measurement extension
      MEASUREMENT_VALUE("measurement value", 75, 100, 1000), MEASUREMENT_UNITS_ID("units of measure", 75, 100, 1000);

      private String columnName;

      private int min;

      private int pref;

      private int max;

      private REFSET_FIELDS(String columnName, int min, int pref, int max) {
         this.columnName = columnName;
         this.min = min;
         this.pref = pref;
         this.max = max;
      }

      public String getColumnName() {
         return columnName;
      }

      public int getMax() {
         return max;
      }

      public int getMin() {
         return min;
      }

      public int getPref() {
         return pref;
      }

   }

   private static REFSET_FIELDS[] booleanRefsetFields = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.BOOLEAN_VALUE, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH };

   private static REFSET_FIELDS[] booleanRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.BOOLEAN_VALUE, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
   // REFSET_FIELDS.BRANCH
   };

   private static REFSET_FIELDS[] conceptRefsetFields = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.CONCEPT_ID, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH };

   private static REFSET_FIELDS[] conceptRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.CONCEPT_ID, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
   // REFSET_FIELDS.BRANCH
   };

   private static REFSET_FIELDS[] measurementRefsetFields = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
         // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.MEASUREMENT_VALUE, REFSET_FIELDS.MEASUREMENT_UNITS_ID, REFSET_FIELDS.STATUS,
         REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH };

   private static REFSET_FIELDS[] measurementRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.MEASUREMENT_VALUE, REFSET_FIELDS.MEASUREMENT_UNITS_ID, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
   // REFSET_FIELDS.BRANCH
   };

   private static REFSET_FIELDS[] integerRefsetFields = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.INTEGER_VALUE, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH };

   private static REFSET_FIELDS[] integerRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
   // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.INTEGER_VALUE, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
   // REFSET_FIELDS.BRANCH
   };

   private static REFSET_FIELDS[] languageRefsetFields = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
         // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.ACCEPTABILITY, REFSET_FIELDS.CORRECTNESS, REFSET_FIELDS.DEGREE_OF_SYNONYMY,
         REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH };

   private static REFSET_FIELDS[] languageRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
         // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.ACCEPTABILITY, REFSET_FIELDS.CORRECTNESS, REFSET_FIELDS.DEGREE_OF_SYNONYMY,
         REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH
   };

   private static REFSET_FIELDS[] scopedLanguageRefsetFields = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
         // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.ACCEPTABILITY, REFSET_FIELDS.CORRECTNESS, REFSET_FIELDS.DEGREE_OF_SYNONYMY, REFSET_FIELDS.TAG,
         REFSET_FIELDS.SCOPE, REFSET_FIELDS.PRIORITY, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION, REFSET_FIELDS.BRANCH };

   private static REFSET_FIELDS[] scopedLanguageRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
         // REFSET_FIELDS.MEMBER_ID, REFSET_FIELDS.COMPONENT_ID,
         REFSET_FIELDS.ACCEPTABILITY, REFSET_FIELDS.CORRECTNESS, REFSET_FIELDS.DEGREE_OF_SYNONYMY, REFSET_FIELDS.TAG,
         REFSET_FIELDS.SCOPE, REFSET_FIELDS.PRIORITY, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
   // REFSET_FIELDS.BRANCH
   };

   public static REFSET_FIELDS[] getRefsetColumns(I_HostConceptPlugins host, EXT_TYPE type) {
      if (host.getShowHistory()) {
         switch (type) {
         case BOOLEAN:
            return booleanRefsetFields;
         case CONCEPT:
            return conceptRefsetFields;
         case INTEGER:
            return integerRefsetFields;
         case LANGUAGE:
            return languageRefsetFields;
         case SCOPED_LANGUAGE:
            return scopedLanguageRefsetFields;
         case MEASUREMENT:
            return measurementRefsetFields;
         default:
            throw new UnsupportedOperationException("Can't handle type: " + type);
         }
      } else {
         switch (type) {
         case BOOLEAN:
            return booleanRefsetFieldsNoHistory;
         case CONCEPT:
            return conceptRefsetFieldsNoHistory;
         case INTEGER:
            return integerRefsetFieldsNoHistory;
         case LANGUAGE:
            return languageRefsetFieldsNoHistory;
         case SCOPED_LANGUAGE:
            return scopedLanguageRefsetFieldsNoHistory;
         case MEASUREMENT:
            return measurementRefsetFieldsNoHistory;
         default:
            throw new UnsupportedOperationException("Can't handle type: " + type);
         }
      }
   }

   private REFSET_FIELDS[] columns;

   private SmallProgressPanel progress = new SmallProgressPanel();

   I_HostConceptPlugins host;

   List<ThinExtByRefTuple> allTuples;

   ArrayList<ThinExtByRefVersioned> allExtensions;

   Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

   private Set<Integer> conceptsToFetch = new HashSet<Integer>();

   private TableChangedSwingWorker tableChangeWorker;

   private ReferencedConceptsSwingWorker refConWorker;

   private int tableComponentId = Integer.MIN_VALUE;

   private JButton addButton;

   private EXT_TYPE refsetType;

   private TOGGLES toggle;

   protected Class getExtPartClass() {
      switch (refsetType) {
      case BOOLEAN:
         return ThinExtByRefPartBoolean.class;
      case CONCEPT:
         return ThinExtByRefPartConcept.class;
      case INTEGER:
         return ThinExtByRefPartInteger.class;
      case LANGUAGE:
         return ThinExtByRefPartLanguage.class;
      case SCOPED_LANGUAGE:
         return ThinExtByRefPartLanguageScoped.class;
      case MEASUREMENT:
         return ThinExtByRefPartMeasurement.class;
      default:
         throw new UnsupportedOperationException("Can't handle type: " + refsetType);
      }
   }

   public class ReferencedConceptsSwingWorker extends SwingWorker<Map<Integer, ConceptBean>> {
      private boolean stopWork = false;

      @Override
      protected Map<Integer, ConceptBean> construct() throws Exception {
         getProgress().setActive(true);
         Map<Integer, ConceptBean> concepts = new HashMap<Integer, ConceptBean>();
         for (Integer id : new HashSet<Integer>(conceptsToFetch)) {
            if (stopWork) {
               break;
            }
            ConceptBean b = ConceptBean.get(id);
            b.getDescriptions();
            concepts.put(id, b);
         }
         return concepts;
      }

      @Override
      protected void finished() {
         super.finished();
         if (getProgress() != null) {
            getProgress().getProgressBar().setIndeterminate(false);
            if (conceptsToFetch.size() == 0) {
               getProgress().getProgressBar().setValue(1);
            } else {
               getProgress().getProgressBar().setValue(conceptsToFetch.size());
            }
         }
         if (stopWork) {
            return;
         }
         try {
            referencedConcepts = get();
         } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
         fireTableDataChanged();
         if (getProgress() != null) {
            getProgress().setProgressInfo("   " + getRowCount() + "   ");
            getProgress().setActive(false);
         }

      }

      public void stop() {
         stopWork = true;
      }

   }

   public class TableChangedSwingWorker extends SwingWorker<Integer> {
      Integer componentId;

      private boolean stopWork = false;

      public TableChangedSwingWorker(Integer componentId) {
         super();
         this.componentId = componentId;
      }

      @Override
      protected Integer construct() throws Exception {
         if (refConWorker != null) {
            refConWorker.stop();
         }
         conceptsToFetch.clear();
         referencedConcepts.clear();
         if (componentId == null || componentId == Integer.MIN_VALUE) {
            return 0;
         }
         List<ExtensionByReferenceBean> extensions = AceConfig.getVodb().getExtensionsForComponent(componentId);
         extensions.addAll(ExtensionByReferenceBean.getNewExtensions(componentId));
         for (ExtensionByReferenceBean ebrBean : extensions) {
            if (stopWork) {
               return -1;
            }
            conceptsToFetch.add(ebrBean.getExtension().getRefsetId());
            for (ThinExtByRefPart part : ebrBean.getExtension().getVersions()) {
               if (getExtPartClass().equals(part.getClass()) == false) {
                  break;
               }
               if (ThinExtByRefPartBoolean.class.equals(part.getClass())) {
                  conceptsToFetch.add(part.getStatus());
                  conceptsToFetch.add(part.getPathId());
               } else if (ThinExtByRefPartConcept.class.equals(part.getClass())) {
                  ThinExtByRefPartConcept conceptPart = (ThinExtByRefPartConcept) part;
                  conceptsToFetch.add(conceptPart.getConceptId());
                  conceptsToFetch.add(part.getStatus());
                  conceptsToFetch.add(part.getPathId());
               } else if (ThinExtByRefPartMeasurement.class.equals(part.getClass())) {
                  ThinExtByRefPartMeasurement conceptPart = (ThinExtByRefPartMeasurement) part;
                  conceptsToFetch.add(conceptPart.getUnitsOfMeasureId());
                  conceptsToFetch.add(part.getStatus());
                  conceptsToFetch.add(part.getPathId());
               } else if (ThinExtByRefPartInteger.class.equals(part.getClass())) {
                  conceptsToFetch.add(part.getStatus());
                  conceptsToFetch.add(part.getPathId());
               } else if (ThinExtByRefPartLanguage.class.equals(part.getClass())) {
                  ThinExtByRefPartLanguage conceptPart = (ThinExtByRefPartLanguage) part;
                  conceptsToFetch.add(conceptPart.getAcceptabilityId());
                  conceptsToFetch.add(conceptPart.getCorrectnessId());
                  conceptsToFetch.add(conceptPart.getDegreeOfSynonymyId());
                  conceptsToFetch.add(part.getStatus());
                  conceptsToFetch.add(part.getPathId());
               } else if (ThinExtByRefPartLanguageScoped.class.equals(part.getClass())) {
                  ThinExtByRefPartLanguageScoped conceptPart = (ThinExtByRefPartLanguageScoped) part;
                  conceptsToFetch.add(conceptPart.getScopeId());
                  conceptsToFetch.add(conceptPart.getTagId());
                  conceptsToFetch.add(conceptPart.getAcceptabilityId());
                  conceptsToFetch.add(conceptPart.getCorrectnessId());
                  conceptsToFetch.add(conceptPart.getDegreeOfSynonymyId());
                  conceptsToFetch.add(part.getStatus());
                  conceptsToFetch.add(part.getPathId());
               }
               allTuples.add(new ThinExtByRefTuple(ebrBean.getExtension(), part));
            }
         }

         refConWorker = new ReferencedConceptsSwingWorker();
         refConWorker.start();
         return extensions.size();
      }

      @Override
      protected void finished() {
         super.finished();
         if (getProgress() != null) {
            getProgress().getProgressBar().setIndeterminate(false);
            if (conceptsToFetch.size() == 0) {
               getProgress().getProgressBar().setValue(1);
               getProgress().getProgressBar().setMaximum(1);
            } else {
               getProgress().getProgressBar().setValue(1);
               getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
            }
         }
         if (stopWork) {
            return;
         }
         try {
            get();
         } catch (InterruptedException e) {
            ;
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
         tableComponentId = componentId;
         fireTableDataChanged();

      }

      public void stop() {
         stopWork = true;
      }

   }

   public static class StringWithExtTuple implements Comparable, I_CellTextWithTuple {
      String cellText;

      ThinExtByRefTuple tuple;

      public StringWithExtTuple(String cellText, ThinExtByRefTuple tuple) {
         super();
         this.cellText = cellText;
         this.tuple = tuple;
      }

      public String getCellText() {
         return cellText;
      }

      public ThinExtByRefTuple getTuple() {
         return tuple;
      }

      public String toString() {
         return cellText;
      }

      public int compareTo(Object o) {
         StringWithExtTuple another = (StringWithExtTuple) o;
         return cellText.compareTo(another.cellText);
      }
   }

   public RefsetMemberTableModel(I_HostConceptPlugins host, REFSET_FIELDS[] columns, EXT_TYPE refsetType,
                                 I_HostConceptPlugins.TOGGLES toggle) {
      super();
      this.columns = columns;
      this.host = host;
      this.refsetType = refsetType;
      this.toggle = toggle;
      this.host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
   }

   public I_RefsetDefaults getRefsetPreferences() throws Exception {
      switch (refsetType) {
      case BOOLEAN:
         return host.getConfig().getRefsetPreferencesForToggle(toggle).getBooleanPreferences();
      case CONCEPT:
         return host.getConfig().getRefsetPreferencesForToggle(toggle).getConceptPreferences();
      case INTEGER:
         return host.getConfig().getRefsetPreferencesForToggle(toggle).getIntegerPreferences();
      case LANGUAGE:
         return host.getConfig().getRefsetPreferencesForToggle(toggle).getLanguagePreferences();
      case SCOPED_LANGUAGE:
         return host.getConfig().getRefsetPreferencesForToggle(toggle).getLanguageScopedPreferences();
      case MEASUREMENT:
         return host.getConfig().getRefsetPreferencesForToggle(toggle).getMeasurementPreferences();
      default:
         throw new Exception("Can't handle refset type: " + refsetType);
      }
   }

   public SmallProgressPanel getProgress() {
      return progress;
   }

   public void setProgress(SmallProgressPanel progress) {
      this.progress = progress;
   }

   public int getColumnCount() {
      return columns.length;
   }

   public String getColumnName(int col) {
      return columns[col].getColumnName();
   }

   public void propertyChange(PropertyChangeEvent arg0) {
      allTuples = null;
      allExtensions = null;
      if (getProgress() != null) {
         getProgress().setVisible(true);
         getProgress().getProgressBar().setValue(0);
         getProgress().getProgressBar().setIndeterminate(true);
      }
      fireTableDataChanged();
   }

   public void setComponentId(int componentId) throws Exception {
      this.tableComponentId = componentId;
      if (ACE.editMode) {
         this.addButton.setEnabled(this.tableComponentId != Integer.MIN_VALUE);
      }
      propertyChange(null);
   }

   public int getRowCount() {
      if (tableComponentId == Integer.MIN_VALUE) {
         return 0;
      }
      if (allTuples == null) {
         allTuples = new ArrayList<ThinExtByRefTuple>();
         tableChangeWorker = new TableChangedSwingWorker(tableComponentId);
         SwingUtilities.invokeLater(new Runnable() {

            public void run() {
               tableChangeWorker.start();
            }

         });
         return 0;
      }
      return allTuples.size();
   }

   public Object getValueAt(int rowIndex, int columnIndex) {
      try {
         ThinExtByRefTuple tuple = allTuples.get(rowIndex);
         switch (columns[columnIndex]) {
         case REFSET_ID:
            if (referencedConcepts.containsKey(tuple.getRefsetId())) {
               return new StringWithExtTuple(getPrefText(tuple.getRefsetId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(tuple.getRefsetId()), tuple);
         case MEMBER_ID:
            return new StringWithExtTuple(Integer.toString(tuple.getMemberId()), tuple);
         case COMPONENT_ID:
            return new StringWithExtTuple(Integer.toString(tuple.getComponentId()), tuple);

         case STATUS:
            if (referencedConcepts.containsKey(tuple.getStatus())) {
               return new StringWithExtTuple(getPrefText(tuple.getStatus()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(tuple.getStatus()), tuple);
         case VERSION:
            if (tuple.getVersion() == Integer.MAX_VALUE) {
               return new StringWithExtTuple(ThinVersionHelper.uncommittedHtml(), tuple);
            }
            return new StringWithExtTuple(ThinVersionHelper.format(tuple.getVersion()), tuple);
         case BRANCH:
            if (referencedConcepts.containsKey(tuple.getPathId())) {
               return new StringWithExtTuple(getPrefText(tuple.getPathId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(tuple.getPathId()), tuple);

            // Boolean extension
         case BOOLEAN_VALUE:
            return new StringWithExtTuple(Boolean.toString(((ThinExtByRefPartBoolean) tuple.getPart()).getValue()),
                  tuple);
            // Concept extension
         case CONCEPT_ID:
            if (referencedConcepts.containsKey(((ThinExtByRefPartConcept) tuple.getPart()).getConceptId())) {
               return new StringWithExtTuple(getPrefText(((ThinExtByRefPartConcept) tuple.getPart()).getConceptId()),
                     tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartConcept) tuple.getPart()).getConceptId()),
                  tuple);

            // Integer extension
         case INTEGER_VALUE:
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartInteger) tuple.getPart()).getValue()),
                  tuple);

            // Language extension
         case ACCEPTABILITY:
            if (referencedConcepts.containsKey(((ThinExtByRefPartLanguage) tuple.getPart()).getAcceptabilityId())) {
               return new StringWithExtTuple(getPrefText(((ThinExtByRefPartLanguage) tuple.getPart())
                     .getAcceptabilityId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartLanguage) tuple.getPart())
                  .getAcceptabilityId()), tuple);
         case CORRECTNESS:
            if (referencedConcepts.containsKey(((ThinExtByRefPartLanguage) tuple.getPart()).getCorrectnessId())) {
               return new StringWithExtTuple(getPrefText(((ThinExtByRefPartLanguage) tuple.getPart())
                     .getCorrectnessId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartLanguage) tuple.getPart())
                  .getCorrectnessId()), tuple);
         case DEGREE_OF_SYNONYMY:
            if (referencedConcepts.containsKey(((ThinExtByRefPartLanguage) tuple.getPart()).getDegreeOfSynonymyId())) {
               return new StringWithExtTuple(getPrefText(((ThinExtByRefPartLanguage) tuple.getPart())
                     .getDegreeOfSynonymyId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartLanguage) tuple.getPart())
                  .getDegreeOfSynonymyId()), tuple);

            // Scoped language extension
         case TAG:
            if (referencedConcepts.containsKey(((ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId())) {
               return new StringWithExtTuple(
                     getPrefText(((ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartLanguageScoped) tuple.getPart())
                  .getTagId()), tuple);
         case SCOPE:
            if (referencedConcepts.containsKey(((ThinExtByRefPartLanguageScoped) tuple.getPart()).getScopeId())) {
               return new StringWithExtTuple(getPrefText(((ThinExtByRefPartLanguageScoped) tuple.getPart())
                     .getScopeId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartLanguageScoped) tuple.getPart())
                  .getScopeId()), tuple);
         case PRIORITY:
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartLanguageScoped) tuple.getPart())
                  .getPriority()), tuple);
         case MEASUREMENT_UNITS_ID:
            if (referencedConcepts.containsKey(((ThinExtByRefPartMeasurement) tuple.getPart()).getUnitsOfMeasureId())) {
               return new StringWithExtTuple(getPrefText(((ThinExtByRefPartMeasurement) tuple.getPart())
                     .getUnitsOfMeasureId()), tuple);
            }
            return new StringWithExtTuple(Integer.toString(((ThinExtByRefPartMeasurement) tuple.getPart())
                  .getUnitsOfMeasureId()), tuple);
         case MEASUREMENT_VALUE:
            return new StringWithExtTuple(Double.toString(((ThinExtByRefPartMeasurement) tuple.getPart())
                  .getMeasurementValue()), tuple);
         }
         AceLog.getAppLog().alertAndLogException(new Exception("Can't handle column type: " + columns[columnIndex]));
      } catch (IOException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }
      return null;
   }

   private String getPrefText(int id) throws IOException {
      ConceptBean cb = referencedConcepts.get(id);
      I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
      if (desc != null) {
         return desc.getText();
      }
      cb = referencedConcepts.get(id);
      desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
      return "null pref desc: " + cb.getInitialText();
   }

   public REFSET_FIELDS[] getColumns() {
      return columns;
   }

   public REFSET_FIELDS[] getFieldsForPopup() {
      return columns;
   }

   public void setAddButton(JButton addButton) {
      if (this.addButton != null) {
         this.addButton.removeActionListener(this);
      }
      this.addButton = addButton;
      if (this.addButton != null) {
         this.addButton.addActionListener(this);
      }
   }

   public void actionPerformed(ActionEvent arg0) {
      try {
         I_HoldRefsetPreferences preferences = host.getConfig().getRefsetPreferencesForToggle(toggle);
         I_RefsetDefaults refsetDefaults = null;
         switch (refsetType) {
         case BOOLEAN:
            refsetDefaults = preferences.getBooleanPreferences();
            break;
         case CONCEPT:
            refsetDefaults = preferences.getBooleanPreferences();
            break;
         case INTEGER:
            refsetDefaults = preferences.getBooleanPreferences();
            break;
         case MEASUREMENT:
            refsetDefaults = preferences.getBooleanPreferences();
            break;
         case LANGUAGE:
            refsetDefaults = preferences.getBooleanPreferences();
            break;
         case SCOPED_LANGUAGE:
            refsetDefaults = preferences.getBooleanPreferences();
            break;
         default:
            throw new UnsupportedOperationException("Can't handle ref set type: " + refsetType);
         }
         int refsetId = refsetDefaults.getDefaultRefset().getConceptId();
         int memberId = LocalVersionedTerminology.get().uuidToNativeWithGeneration(UUID.randomUUID(),
               ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
               host.getConfig().getEditingPathSet(), Integer.MAX_VALUE);
         ThinExtByRefVersioned extension = new ThinExtByRefVersioned(refsetId, memberId, tableComponentId,
               ThinExtBinder.getExtensionType(refsetType));
         switch (refsetType) {
         case BOOLEAN:
            for (I_Path editPath : host.getConfig().getEditingPathSet()) {
               ThinExtByRefPartBoolean booleanPart = new ThinExtByRefPartBoolean();
               booleanPart.setPathId(editPath.getConceptId());
               booleanPart.setStatus(refsetDefaults.getDefaultStatusForRefset().getConceptId());
               booleanPart.setValue(preferences.getBooleanPreferences().getDefaultForBooleanRefset());
               booleanPart.setVersion(Integer.MAX_VALUE);
               extension.addVersion(booleanPart);
            }
            break;
         case CONCEPT:
            for (I_Path editPath : host.getConfig().getEditingPathSet()) {
               ThinExtByRefPartConcept conceptPart = new ThinExtByRefPartConcept();
               conceptPart.setPathId(editPath.getConceptId());
               conceptPart.setStatus(refsetDefaults.getDefaultStatusForRefset().getConceptId());
               conceptPart
                     .setConceptId(preferences.getConceptPreferences().getDefaultForConceptRefset().getConceptId());
               conceptPart.setVersion(Integer.MAX_VALUE);
               extension.addVersion(conceptPart);
            }
            break;
         case INTEGER:
            for (I_Path editPath : host.getConfig().getEditingPathSet()) {
               ThinExtByRefPartInteger integerPart = new ThinExtByRefPartInteger();
               integerPart.setPathId(editPath.getConceptId());
               integerPart.setStatus(refsetDefaults.getDefaultStatusForRefset().getConceptId());
               integerPart.setValue(preferences.getIntegerPreferences().getDefaultForIntegerRefset());
               integerPart.setVersion(Integer.MAX_VALUE);
               extension.addVersion(integerPart);
            }
            break;
         case MEASUREMENT:
            for (I_Path editPath : host.getConfig().getEditingPathSet()) {
               ThinExtByRefPartMeasurement measurementPart = new ThinExtByRefPartMeasurement();
               measurementPart.setPathId(editPath.getConceptId());
               measurementPart.setStatus(refsetDefaults.getDefaultStatusForRefset().getConceptId());
               measurementPart.setUnitsOfMeasureId(preferences.getMeasurementPreferences()
                     .getDefaultUnitsOfMeasureForMeasurementRefset().getConceptId());
               measurementPart.setMeasurementValue(preferences.getMeasurementPreferences()
                     .getDefaultMeasurementValueForMeasurementRefset());
               measurementPart.setVersion(Integer.MAX_VALUE);
               extension.addVersion(measurementPart);
            }
            break;
         case LANGUAGE:
            for (I_Path editPath : host.getConfig().getEditingPathSet()) {
               ThinExtByRefPartLanguage laguagePart = new ThinExtByRefPartLanguage();
               laguagePart.setPathId(editPath.getConceptId());
               laguagePart.setStatus(refsetDefaults.getDefaultStatusForRefset().getConceptId());
               laguagePart.setAcceptabilityId(preferences.getLanguagePreferences()
                     .getDefaultAcceptabilityForLanguageRefset().getConceptId());
               laguagePart.setCorrectnessId(preferences.getLanguagePreferences()
                     .getDefaultCorrectnessForLanguageRefset().getConceptId());
               laguagePart.setDegreeOfSynonymyId(preferences.getLanguagePreferences()
                     .getDefaultDegreeOfSynonymyForLanguageRefset().getConceptId());
               laguagePart.setVersion(Integer.MAX_VALUE);
               extension.addVersion(laguagePart);
            }
            break;
         case SCOPED_LANGUAGE:
            for (I_Path editPath : host.getConfig().getEditingPathSet()) {
               ThinExtByRefPartLanguageScoped scopedLaguagePart = new ThinExtByRefPartLanguageScoped();
               scopedLaguagePart.setPathId(editPath.getConceptId());
               scopedLaguagePart.setStatus(refsetDefaults.getDefaultStatusForRefset().getConceptId());
               scopedLaguagePart.setAcceptabilityId(preferences.getLanguageScopedPreferences()
                     .getDefaultAcceptabilityForLanguageRefset().getConceptId());
               scopedLaguagePart.setCorrectnessId(preferences.getLanguageScopedPreferences()
                     .getDefaultCorrectnessForLanguageRefset().getConceptId());
               scopedLaguagePart.setDegreeOfSynonymyId(preferences.getLanguageScopedPreferences()
                     .getDefaultDegreeOfSynonymyForLanguageRefset().getConceptId());
               scopedLaguagePart.setScopeId(preferences.getLanguageScopedPreferences()
                     .getDefaultScopeForScopedLanguageRefset().getConceptId());
               scopedLaguagePart.setTagId(preferences.getLanguageScopedPreferences()
                     .getDefaultTagForScopedLanguageRefset().getConceptId());
               scopedLaguagePart.setPriority(preferences.getLanguageScopedPreferences()
                     .getDefaultPriorityForScopedLanguageRefset());
               scopedLaguagePart.setVersion(Integer.MAX_VALUE);
               extension.addVersion(scopedLaguagePart);
            }
            break;
         default:
            throw new UnsupportedOperationException("Can't handle ref set type: " + refsetType);
         }
         ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(extension.getMemberId(), extension);
         ACE.addUncommitted(ebrBean);
         propertyChange(null);
      } catch (TerminologyException e) {
         AceLog.getAppLog().alertAndLogException(e);
      } catch (IOException e) {
         AceLog.getAppLog().alertAndLogException(e);
      } catch (RuntimeException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }
   }

   public boolean isCellEditable(int row, int col) {
      if (ACE.editMode == false) {
         return false;
      }
      if (allTuples.get(row).getVersion() == Integer.MAX_VALUE) {
         if (AceLog.getAppLog().isLoggable(Level.FINER)) {
            AceLog.getAppLog().finer("Cell is editable: " + row + " " + col);
         }
         return true;
      }
      return false;
   }

   public void setValueAt(Object value, int row, int col) {
      ThinExtByRefTuple extTuple = allTuples.get(row);
      if (extTuple.getVersion() == Integer.MAX_VALUE) {
         switch (columns[col]) {
         case REFSET_ID:
            Integer refsetId = (Integer) value;
            extTuple.getCore().setRefsetId(refsetId);
            referencedConcepts.put(refsetId, ConceptBean.get(refsetId));
            break;
         case MEMBER_ID:
            break;
         case COMPONENT_ID:
            break;
         case STATUS:
            Integer statusId = (Integer) value;
            extTuple.setStatus(statusId);
            referencedConcepts.put(statusId, ConceptBean.get(statusId));
         case VERSION:
            break;
         case BRANCH:
            break;
         case BOOLEAN_VALUE:
            Boolean booleanValue = (Boolean) value;
            ((ThinExtByRefPartBoolean) extTuple.getPart()).setValue(booleanValue);
            break;
         case CONCEPT_ID:
            Integer conceptId = (Integer) value;
            ((ThinExtByRefPartConcept) extTuple.getPart()).setConceptId(conceptId);
            referencedConcepts.put(conceptId, ConceptBean.get(conceptId));
            break;
         case INTEGER_VALUE:
            Integer intValue = (Integer) value;
            ((ThinExtByRefPartInteger) extTuple.getPart()).setValue(intValue);
            break;
         case ACCEPTABILITY:
            Integer acceptabilityId = (Integer) value;
            ((ThinExtByRefPartLanguage) extTuple.getPart()).setAcceptabilityId(acceptabilityId);
            referencedConcepts.put(acceptabilityId, ConceptBean.get(acceptabilityId));
            break;
         case CORRECTNESS:
            Integer correctnessId = (Integer) value;
            ((ThinExtByRefPartLanguage) extTuple.getPart()).setCorrectnessId(correctnessId);
            referencedConcepts.put(correctnessId, ConceptBean.get(correctnessId));
            break;
         case DEGREE_OF_SYNONYMY:
            Integer dosId = (Integer) value;
            ((ThinExtByRefPartLanguage) extTuple.getPart()).setDegreeOfSynonymyId(dosId);
            referencedConcepts.put(dosId, ConceptBean.get(dosId));
            break;
         case TAG:
            Integer tagId = (Integer) value;
            ((ThinExtByRefPartLanguageScoped) extTuple.getPart()).setTagId(tagId);
            referencedConcepts.put(tagId, ConceptBean.get(tagId));
            break;
         case SCOPE:
            Integer scopeId = (Integer) value;
            ((ThinExtByRefPartLanguageScoped) extTuple.getPart()).setScopeId(scopeId);
            referencedConcepts.put(scopeId, ConceptBean.get(scopeId));
            break;
         case PRIORITY:
            Integer priority = (Integer) value;
            ((ThinExtByRefPartLanguageScoped) extTuple.getPart()).setPriority(priority);
            break;
         case MEASUREMENT_UNITS_ID:
            Integer unitsId = (Integer) value;
            ((ThinExtByRefPartMeasurement) extTuple.getPart()).setUnitsOfMeasureId(unitsId);
            referencedConcepts.put(unitsId, ConceptBean.get(unitsId));
            break;
         case MEASUREMENT_VALUE:
            Double measurementValue = (Double) value;
            ((ThinExtByRefPartMeasurement) extTuple.getPart()).setMeasurementValue(measurementValue);
            break;
         }
         fireTableDataChanged();
      }
   }

   public Class<?> getColumnClass(int c) {
      switch (columns[c]) {
      case REFSET_ID:
         return Number.class;
      case MEMBER_ID:
         return Number.class;
      case COMPONENT_ID:
         return Number.class;
      case STATUS:
         return Number.class;
      case VERSION:
         return Number.class;
      case BRANCH:
         return Number.class;
      case BOOLEAN_VALUE:
         return Boolean.class;
      case CONCEPT_ID:
         return Number.class;
      case INTEGER_VALUE:
         return Integer.class;
      case ACCEPTABILITY:
         return Number.class;
      case CORRECTNESS:
         return Number.class;
      case DEGREE_OF_SYNONYMY:
         return Number.class;
      case TAG:
         return Number.class;
      case SCOPE:
         return Number.class;
      case PRIORITY:
         return Integer.class;
      case MEASUREMENT_UNITS_ID:
         return Number.class;
      case MEASUREMENT_VALUE:
         return Double.class;
      }
      return String.class;
   }

   public RefsetPopupListener makePopupListener(JTable table, I_ConfigAceFrame config) throws Exception {
      return new RefsetPopupListener(table, config, this.getRefsetPreferences(), this);
   }

   public List<REFSET_FIELDS> getPopupFields() {
      ArrayList<REFSET_FIELDS> returnValues = new ArrayList<REFSET_FIELDS>();
      for (REFSET_FIELDS f : columns) {
         switch (f) {
         case MEMBER_ID:
         case COMPONENT_ID:
         case INTEGER_VALUE:
         case BRANCH:
         case BOOLEAN_VALUE:
         case MEASUREMENT_VALUE:
         case PRIORITY:
            break;

         default:
            returnValues.add(f);
         }
      }

      return returnValues;
   }

}
