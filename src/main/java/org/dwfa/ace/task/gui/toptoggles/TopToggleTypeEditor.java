package org.dwfa.ace.task.gui.toptoggles;

import org.dwfa.bpa.tasks.editor.AbstractComboEditor;

public class TopToggleTypeEditor extends AbstractComboEditor {

   @Override
   public EditorComponent setupEditor() {
      return new EditorComponent(TopToggleTypes.values() );
   }
   
}