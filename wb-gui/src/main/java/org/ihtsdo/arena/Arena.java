package org.ihtsdo.arena;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.arena.editor.ArenaEditor;

public class Arena extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    ArenaEditor editor;

    public ArenaEditor getEditor() {
        return editor;
    }
    
    public Arena(I_ConfigAceFrame config, File defaultLayout) throws IOException {
        super(new BorderLayout());
        
        editor = new ArenaEditor(config, defaultLayout);
        add(editor, BorderLayout.CENTER);   
    }
    
}
