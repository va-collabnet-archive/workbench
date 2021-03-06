/*
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
package org.ihtsdo.qa.gui;

//~--- non-JDK imports --------------------------------------------------------

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.rules.CheckConceptTask;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.tk.helper.ResultsItem;

/**
 * The Class TestListPanel.
 *
 * @author Guillermo Reynoso
 */
public class TestListPanel extends JPanel {
   
   /** The context helper. */
   private RulesContextHelper contextHelper = null;
   
   /** The list1 model. */
   private DefaultListModel   list1Model    = new DefaultListModel();
   
   /** The table1 model. */
   private DefaultTableModel  table1Model   = null;
   
   /** The button1. */
   private JButton            button1;
   
   /** The button2. */
   private JButton            button2;
   
   /** The button3. */
   private JButton            button3;
   
   /** The button4. */
   private JButton            button4;
   
   /** The combo box1. */
   private JComboBox          comboBox1;
   
   /** The config. */
   private I_ConfigAceFrame   config;
   
   /** The label1. */
   private JLabel             label1;
   
   /** The label2. */
   private JLabel             label2;
   
   /** The label3. */
   private JLabel             label3;
   
   /** The label4. */
   private JLabel             label4;
   
   /** The list1. */
   private JList              list1;

   // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
   /** The panel1. */
   private JPanel      panel1;
   
   /** The panel2. */
   private JPanel      panel2;
   
   /** The panel3. */
   private JPanel      panel3;
   
   /** The panel4. */
   private JPanel      panel4;
   
   /** The scroll pane1. */
   private JScrollPane scrollPane1;
   
   /** The scroll pane2. */
   private JScrollPane scrollPane2;
   
   /** The table1. */
   private JTable      table1;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new test list panel.
    *
    * @param config the config
    */
   public TestListPanel(I_ConfigAceFrame config) {
      initComponents();
      this.config        = config;
      this.contextHelper = new RulesContextHelper(config);

      String[]   columnNames = { "Concept", "Alerts" };
      String[][] data        = null;

      table1Model = new DefaultTableModel(data, columnNames) {
         private static final long serialVersionUID = 1L;
         public boolean isCellEditable(int x, int y) {
            return false;
         }
      };
      table1.setModel(table1Model);
      table1.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer());
      table1.repaint();
      updateList1();
      label4.setText("");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Button1 action performed.
    *
    * @param e the e
    */
   private void button1ActionPerformed(ActionEvent e) {
      updateList1();
   }

   /**
    * Button2 action performed.
    *
    * @param e the e
    */
   private void button2ActionPerformed(ActionEvent e) {
      testConcepts();
   }

   /**
    * Button3 action performed.
    *
    * @param e the e
    */
   private void button3ActionPerformed(ActionEvent e) {

      // Clear Cache
      contextHelper.clearCache();
   }

   /**
    * Button4 action performed.
    *
    * @param e the e
    */
   private void button4ActionPerformed(ActionEvent e) {
   }

   /**
    * Inits the components.
    */
   private void initComponents() {

      // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
      panel1      = new JPanel();
      label1      = new JLabel();
      button1     = new JButton();
      scrollPane1 = new JScrollPane();
      list1       = new JList();
      panel2      = new JPanel();
      label2      = new JLabel();
      scrollPane2 = new JScrollPane();
      table1      = new JTable();
      panel3      = new JPanel();
      label4      = new JLabel();
      label3      = new JLabel();
      comboBox1   = new JComboBox();
      button2     = new JButton();
      panel4      = new JPanel();
      button4     = new JButton();
      button3     = new JButton();

      // ======== this ========
      setLayout(new GridBagLayout());
      ((GridBagLayout) getLayout()).columnWidths  = new int[] { 0, 0 };
      ((GridBagLayout) getLayout()).rowHeights    = new int[] {
         0, 0, 0, 0, 0, 0, 0
      };
      ((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
      ((GridBagLayout) getLayout()).rowWeights    = new double[] {
         0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4
      };

      // ======== panel1 ========
      {
         panel1.setLayout(new GridBagLayout());
         ((GridBagLayout) panel1.getLayout()).columnWidths  = new int[] { 0, 0, 0, 0, 0 };
         ((GridBagLayout) panel1.getLayout()).rowHeights    = new int[] { 0, 0 };
         ((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
         ((GridBagLayout) panel1.getLayout()).rowWeights    = new double[] { 0.0, 1.0E-4 };

         // ---- label1 ----
         label1.setText("Test concepts in Workbench \"List\"");
         panel1.add(label1,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

         // ---- button1 ----
         button1.setText("Refresh content from list");
         button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
         button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               button1ActionPerformed(e);
            }
         });
         panel1.add(button1,
                    new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      }
      add(panel1,
          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                 new Insets(0, 0, 5, 0), 0, 0));

      // ======== scrollPane1 ========
      {
         scrollPane1.setViewportView(list1);
      }
      add(scrollPane1,
          new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                 new Insets(0, 0, 5, 0), 0, 0));

      // ======== panel2 ========
      {
         panel2.setLayout(new GridBagLayout());
         ((GridBagLayout) panel2.getLayout()).columnWidths  = new int[] { 0, 0, 0, 0 };
         ((GridBagLayout) panel2.getLayout()).rowHeights    = new int[] { 0, 0 };
         ((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
         ((GridBagLayout) panel2.getLayout()).rowWeights    = new double[] { 0.0, 1.0E-4 };

         // ---- label2 ----
         label2.setText("Results:");
         panel2.add(label2,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
      }
      add(panel2,
          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                 new Insets(0, 0, 5, 0), 0, 0));

      // ======== scrollPane2 ========
      {
         scrollPane2.setViewportView(table1);
      }
      add(scrollPane2,
          new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                 new Insets(0, 0, 5, 0), 0, 0));

      // ======== panel3 ========
      {
         panel3.setLayout(new GridBagLayout());
         ((GridBagLayout) panel3.getLayout()).columnWidths  = new int[] {
            0, 0, 0, 0, 0, 0, 0, 0
         };
         ((GridBagLayout) panel3.getLayout()).rowHeights    = new int[] { 0, 0 };
         ((GridBagLayout) panel3.getLayout()).columnWeights = new double[] {
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4
         };
         ((GridBagLayout) panel3.getLayout()).rowWeights    = new double[] { 0.0, 1.0E-4 };

         // ---- label4 ----
         label4.setText("text");
         label4.setForeground(Color.red);
         panel3.add(label4,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

         // ---- label3 ----
         label3.setText("Use context:");
         panel3.add(label3,
                    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
         panel3.add(comboBox1,
                    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

         // ---- button2 ----
         button2.setText("Test concepts");
         button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
         button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               button2ActionPerformed(e);
            }
         });
         panel3.add(button2,
                    new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      }
      add(panel3,
          new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                                 new Insets(0, 0, 5, 0), 0, 0));

      // ======== panel4 ========
      {
         panel4.setLayout(new GridBagLayout());
         ((GridBagLayout) panel4.getLayout()).columnWidths  = new int[] { 0, 0, 0, 0 };
         ((GridBagLayout) panel4.getLayout()).rowHeights    = new int[] { 0, 0 };
         ((GridBagLayout) panel4.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
         ((GridBagLayout) panel4.getLayout()).rowWeights    = new double[] { 0.0, 1.0E-4 };

         // ---- button4 ----
         button4.setText("Create ISA cache for Config");
         button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
         button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               button4ActionPerformed(e);
            }
         });
         panel4.add(button4,
                    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

         // ---- button3 ----
         button3.setText("Clear KB File Cache");
         button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
         button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               button3ActionPerformed(e);
            }
         });
         panel4.add(button3,
                    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      }
      add(panel4,
          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                                 new Insets(0, 0, 0, 0), 0, 0));

      // JFormDesigner - End of component initialization  //GEN-END:initComponents
   }

   /**
    * Test concepts.
    */
   private void testConcepts() {
      label4.setText("");
      label4.revalidate();

      String[]   columnNames = { "Concept", "Alerts" };
      String[][] data        = null;

      table1Model = new DefaultTableModel(data, columnNames) {
         private static final long serialVersionUID = 1L;
         public boolean isCellEditable(int x, int y) {
            return false;
         }
      };

      long start = Calendar.getInstance().getTimeInMillis();

      for (int i = 0; i < list1Model.getSize(); i++) {
         final I_GetConceptData loopConcept = (I_GetConceptData) list1Model.getElementAt(i);
         I_GetConceptData       context     = (I_GetConceptData) comboBox1.getSelectedItem();

         try {
            final CheckConceptTask checkTask = new CheckConceptTask();

            // set fields
            checkTask.setContext(context);
            checkTask.setConcept(loopConcept);
            checkTask.setConfig(config);
            checkTask.setContextHelper(contextHelper);
            checkTask.setInferredOrigin(INFERRED_VIEW_ORIGIN.INFERRED);
            checkTask.setOnlyUncommittedContent(false);

            PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
               public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                  String property = propertyChangeEvent.getPropertyName();

                  if ("state".equals(property)) {
                     StateValue value = (StateValue) propertyChangeEvent.getNewValue();

                     if (value.equals(StateValue.DONE)) {
                        try {
                           for (ResultsItem resultsItem : checkTask.get().getResultsItems()) {
                              table1Model.addRow(new String[] { loopConcept.toString(),
                                                                "[" + resultsItem.getErrorCode() + "] "
                                                                + resultsItem.getMessage() });
                           }
                        } catch (InterruptedException e) {
                           AceLog.getAppLog().alertAndLogException(e);
                        } catch (ExecutionException e) {
                           AceLog.getAppLog().alertAndLogException(e);
                        }
                     }
                  }
               }
            };

            checkTask.addPropertyChangeListener(propertyChangeListener);
            checkTask.execute();

            // ResultsCollectorWorkBench results = RulesLibrary.checkConcept(loopConcept, context, false, config, contextHelper, INFERRED_VIEW_ORIGIN.FULL);
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }

      // long end = Calendar.getInstance().getTimeInMillis();
      // label4.setText("Total test time: " + (end - start) + " milliseconds...");
      // label4.revalidate();
      table1.setModel(table1Model);
      table1.repaint();
   }

   /**
    * Update list1.
    */
   private void updateList1() {
      list1Model.removeAllElements();

      JList                  conceptList = config.getBatchConceptList();
      I_ModelTerminologyList model       = (I_ModelTerminologyList) conceptList.getModel();

      for (int i = 0; i < model.getSize(); i++) {
         list1Model.addElement(model.getElementAt(i));
      }

      list1.setModel(list1Model);
      list1.repaint();

      for (int row = 0; row < table1Model.getRowCount(); row++) {
         table1Model.removeRow(row);
      }

      table1.setModel(table1Model);
      table1.repaint();
      comboBox1.removeAllItems();

      try {
         for (I_GetConceptData context : contextHelper.getAllContexts()) {
            comboBox1.addItem(context);
         }
      } catch (Exception e) {
         AceLog.getAppLog().alertAndLogException(e);
      }
   }

   // JFormDesigner - End of variables declaration  //GEN-END:variables
}
