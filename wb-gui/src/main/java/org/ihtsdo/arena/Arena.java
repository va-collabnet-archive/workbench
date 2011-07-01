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

    public Arena(I_ConfigAceFrame config, File defaultLayout) throws IOException {
        super(new BorderLayout());
        
        ArenaEditor editor = new ArenaEditor(config, defaultLayout);
        add(editor, BorderLayout.CENTER);   

    }
    
}
