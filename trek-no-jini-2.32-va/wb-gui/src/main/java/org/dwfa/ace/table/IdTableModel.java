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



package org.dwfa.ace.table;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;

import javax.swing.table.AbstractTableModel;

public class IdTableModel extends AbstractTableModel implements PropertyChangeListener {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private Set<Integer>                   conceptsToFetch    = new ConcurrentSkipListSet<Integer>();
   private Map<Integer, I_GetConceptData> referencedConcepts = new ConcurrentHashMap<Integer,
                                                                  I_GetConceptData>();
   private SmallProgressPanel          progress = new SmallProgressPanel();
   private List<? extends I_IdVersion> allTuples;
   private ID_FIELD[]                  columns;
   private I_HostConceptPlugins        host;
   private TableChangedSwingWorker     tableChangeWorker;

   //~--- constructors --------------------------------------------------------

   public IdTableModel(ID_FIELD[] columns, I_HostConceptPlugins host) {
      super();
      this.host = host;
      host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
      this.columns = columns;
   }

   //~--- enums ---------------------------------------------------------------

   public enum ID_FIELD {
      LOCAL_ID("local id", 5, 100, 100), STATUS("status", 5, 50, 250), EXT_ID("id", 5, 85, 1550),
      VERSION("time", 5, 140, 140), AUTHOR("author", 5, 90, 150), PATH("path", 5, 90, 150),
      SOURCE("id source", 5, 50, 250);

      private String columnName;
      private int    max;
      private int    min;
      private int    pref;

      //~--- constructors -----------------------------------------------------

      private ID_FIELD(String columnName, int min, int pref, int max) {
         this.columnName = columnName;
         this.min        = min;
         this.pref       = pref;
         this.max        = max;
      }

      //~--- get methods ------------------------------------------------------

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

   //~--- methods -------------------------------------------------------------

   public void propertyChange(PropertyChangeEvent evt) {
      allTuples = null;

      if (tableChangeWorker != null) {
         tableChangeWorker.stop();
      }

      conceptsToFetch    = new ConcurrentSkipListSet<Integer>();
      referencedConcepts = new HashMap<Integer, I_GetConceptData>();

      if (getProgress() != null) {
         getProgress().setVisible(true);
         getProgress().getProgressBar().setValue(0);
         getProgress().getProgressBar().setIndeterminate(true);
      }

      tableChangeWorker = new TableChangedSwingWorker((I_AmTermComponent) evt.getNewValue());
      tableChangeWorker.start();
      fireTableDataChanged();
   }

   //~--- get methods ---------------------------------------------------------

   public Class<?> getColumnClass(int c) {
      switch (columns[c]) {
      case LOCAL_ID :
         return Number.class;

      case EXT_ID :
         return StringWithIdTuple.class;

      case STATUS :
         return StringWithIdTuple.class;

      case VERSION :
         return Number.class;

      case PATH :
         return StringWithIdTuple.class;
      }

      return String.class;
   }

   public int getColumnCount() {
      return columns.length;
   }

   public ID_FIELD[] getColumnEnums() {
      return columns;
   }

   public String getColumnName(int col) {
      return columns[col].getColumnName();
   }

   private I_IdVersion getIdTuple(int rowIndex) throws IOException {
      I_AmTermComponent tc = (I_AmTermComponent) host.getTermComponent();

      if (tc == null) {
         return null;
      }

      if (allTuples == null) {
         int nid = getNidFromTermComponent(tc);

         if (nid != Integer.MIN_VALUE) {
            I_Identify id = Terms.get().getId(nid);

            if (id != null) {
               allTuples = id.getIdVersions();
            }
         }
      }

      return allTuples.get(rowIndex);
   }

   private int getNidFromTermComponent(I_AmTermComponent tc) {
      int nid = Integer.MIN_VALUE;

      if (I_DescriptionVersioned.class.isAssignableFrom(tc.getClass())) {
         I_DescriptionVersioned dv = (I_DescriptionVersioned) tc;

         nid = dv.getDescId();
      } else if (I_GetConceptData.class.isAssignableFrom(tc.getClass())) {
         I_GetConceptData cb = (I_GetConceptData) tc;

         nid = cb.getConceptNid();
      } else if (I_RelVersioned.class.isAssignableFrom(tc.getClass())) {
         I_RelVersioned rel = (I_RelVersioned) tc;

         nid = rel.getRelId();
      }

      return nid;
   }

   private String getPrefText(int id) throws IOException {
      I_GetConceptData   cb   = referencedConcepts.get(id);
      I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(),
                                   host.getConfig());

      if (desc != null) {
         String text = desc.getText();

         return text;
      }

      return "null desc for " + id;
   }

   public SmallProgressPanel getProgress() {
      return progress;
   }

   public int getRowCount() {
      if (allTuples == null) {
         try {
            getIdTuple(0);
         } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }

      if (allTuples == null) {
         return 0;
      }

      return allTuples.size();
   }

   public Object getValueAt(int rowIndex, int columnIndex) {
      try {
         if (rowIndex >= getRowCount()) {
            return null;
         }

         I_IdVersion idTuple = getIdTuple(rowIndex);

         if (idTuple == null) {
            return null;
         }

         I_ConfigAceFrame config     = host.getConfig();
         boolean          inConflict = false;

         switch (columns[columnIndex]) {
         case LOCAL_ID :
            return new StringWithIdTuple(Integer.toString(host.getTermComponent().getNid()), idTuple,
                                         inConflict);

         case STATUS :
            if (referencedConcepts.containsKey(idTuple.getStatusId())) {
               return new StringWithIdTuple(getPrefText(idTuple.getStatusId()), idTuple, inConflict);
            }

            return new StringWithIdTuple(Integer.toString(idTuple.getStatusId()), idTuple, inConflict);

         case EXT_ID :
            return new StringWithIdTuple(idTuple.getDenotation().toString(), idTuple, inConflict);

         case VERSION :
            if (idTuple.getVersion() == Integer.MAX_VALUE) {
               return new StringWithIdTuple(ThinVersionHelper.uncommittedHtml(), idTuple, inConflict);
            }

            return new StringWithIdTuple(ThinVersionHelper.format(idTuple.getVersion()), idTuple, inConflict);

         case PATH :
            if (referencedConcepts.containsKey(idTuple.getPathId())) {
               return new StringWithIdTuple(getPrefText(idTuple.getPathId()), idTuple, inConflict);
            }

            return new StringWithIdTuple(Integer.toString(idTuple.getPathId()), idTuple, inConflict);

         case SOURCE :
            if (referencedConcepts.containsKey(idTuple.getAuthorityNid())) {
               return new StringWithIdTuple(getPrefText(idTuple.getAuthorityNid()), idTuple, inConflict);
            }

            return new StringWithIdTuple(Integer.toString(idTuple.getAuthorityNid()), idTuple, inConflict);

         case AUTHOR :
            if (referencedConcepts.containsKey(idTuple.getAuthorNid())) {
               return new StringWithIdTuple(getPrefText(idTuple.getAuthorNid()), idTuple, inConflict);
            }

            return new StringWithIdTuple(Integer.toString(idTuple.getAuthorNid()), idTuple, inConflict);
         }
      } catch (Exception e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      return null;
   }

   //~--- set methods ---------------------------------------------------------

   public void setColumns(ID_FIELD[] columns) {
      if (this.columns.length != columns.length) {
         this.columns = columns;
         fireTableStructureChanged();

         return;
      }

      for (int i = 0; i < columns.length; i++) {
         if (columns[i].equals(this.columns[i]) == false) {
            this.columns = columns;
            fireTableStructureChanged();

            return;
         }
      }
   }

   public void setProgress(SmallProgressPanel progress) {
      this.progress = progress;
   }

   //~--- inner classes -------------------------------------------------------

   public static class IdStatusFieldEditor extends AbstractPopupFieldEditor {
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public IdStatusFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
         super(config);
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int[] getPopupValues() {
         return config.getEditStatusTypePopup().getListArray();
      }

      @Override
      public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
         StringWithIdTuple swdt = (StringWithIdTuple) value;

         return Terms.get().getConcept(swdt.getTuple().getStatusId());
      }
   }


   public class ReferencedConceptsSwingWorker extends SwingWorker<Boolean> {
      private boolean                stopWork = false;
      Map<Integer, I_GetConceptData> concepts;

      //~--- methods ----------------------------------------------------------

      @Override
      protected Boolean construct() throws Exception {
         getProgress().setActive(true);
         concepts = new HashMap<Integer, I_GetConceptData>();

         Set<Integer> idSetToFetch = null;

         synchronized (conceptsToFetch) {
            idSetToFetch = new ConcurrentSkipListSet<Integer>(conceptsToFetch);
         }

         for (Integer id : idSetToFetch) {
            if (stopWork) {
               return false;
            }

            I_GetConceptData b = Terms.get().getConcept(id);

            b.getDescs();
            concepts.put(id, b);
         }

         return true;
      }

      @Override
      protected void finished() {
         super.finished();

         try {
            if (get()) {
               if (stopWork) {
                  return;
               }

               if (getProgress() != null) {
                  getProgress().getProgressBar().setIndeterminate(false);

                  if (conceptsToFetch.isEmpty()) {
                     getProgress().getProgressBar().setValue(1);
                  } else {
                     getProgress().getProgressBar().setValue(conceptsToFetch.size());
                  }
               }

               referencedConcepts = concepts;
               fireTableDataChanged();

               if (getProgress() != null) {
                  getProgress().setProgressInfo("   " + getRowCount() + "   ");
                  getProgress().setActive(false);
               }
            }
         } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }

      public void stop() {
         stopWork = true;
      }

      public String toString() {
         return "ReferencedConceptsSwingWorker stopWork: " + stopWork + " concepts: " + concepts;
      }
   }


   public static class StringWithIdTuple extends StringWithTuple<StringWithIdTuple> {
      String      cellText;
      I_IdVersion tuple;

      //~--- constructors -----------------------------------------------------

      public StringWithIdTuple(String cellText, I_IdVersion tuple, boolean isInConflict) {
         super(cellText, isInConflict);
         this.tuple = tuple;
      }

      //~--- get methods ------------------------------------------------------

      public I_IdVersion getTuple() {
         return tuple;
      }
   }


   public class TableChangedSwingWorker extends SwingWorker<Boolean> {
      private boolean               workStopped = false;
      ReferencedConceptsSwingWorker refConWorker;
      I_AmTermComponent             tc;

      //~--- constructors -----------------------------------------------------

      public TableChangedSwingWorker(I_AmTermComponent tc) {
         super();
         this.tc = tc;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      protected Boolean construct() throws Exception {
         if (tc == null) {
            return false;
         }

         int        nid = getNidFromTermComponent(tc);
         I_Identify id  = Terms.get().getId(nid);
         
         if ((id != null) && (id.getMutableIdParts() != null) && (id.getMutableIdParts().size() > 0)) {
            for (I_IdPart part : id.getMutableIdParts()) {
               if (workStopped) {
                  return false;
               }

               conceptsToFetch.add(part.getStatusNid());
               conceptsToFetch.add(part.getPathNid());
               conceptsToFetch.add(part.getAuthorityNid());
               conceptsToFetch.add(part.getAuthorNid());
            }
         }

         if (workStopped) {
            return false;
         }

         refConWorker = new ReferencedConceptsSwingWorker();
         refConWorker.start();

         return true;
      }

      @Override
      protected void finished() {
         super.finished();

         try {
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

            get();
            fireTableDataChanged();
         } catch (InterruptedException e) {
            ;
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }

      public void stop() {
         workStopped = true;

         if (refConWorker != null) {
            refConWorker.stop();
         }
      }

      public String toString() {
         return "TableChangedSwingWorker: " + tc + " workStopped: " + workStopped + "\n" + refConWorker;
      }
   }
}
