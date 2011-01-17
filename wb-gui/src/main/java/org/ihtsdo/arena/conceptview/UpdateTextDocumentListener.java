/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;

/**
 *
 * @author kec
 */
public class UpdateTextDocumentListener implements DocumentListener, ActionListener {
      
      FixedWidthJEditorPane editorPane;
      DescriptionAnalogBI desc;
      Timer t;
      I_GetConceptData c;
      boolean update = false;
      
      public UpdateTextDocumentListener(FixedWidthJEditorPane editorPane, 
              DescriptionAnalogBI desc) throws TerminologyException, IOException {
         super();
         this.editorPane = editorPane;
         this.desc = desc;
         t = new Timer(1000, this);
         t.start();
         c = Terms.get().getConcept(desc.getConceptNid());
      }
      
      @Override
      public void insertUpdate(DocumentEvent e) {
         update = true;
      }
      
      @Override
      public void removeUpdate(DocumentEvent e) {
         update = true;
      }
      
      @Override
      public void changedUpdate(DocumentEvent e) {
         update = true;
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         
         try {
            if (update) {
               update = false;
               desc.setText(editorPane.extractText());
            }
         } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }