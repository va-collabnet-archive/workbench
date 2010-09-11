package org.ihtsdo.arena;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;

import org.dwfa.ace.ACE;
import org.ihtsdo.arena.editor.ArenaEditor;

public class Arena extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Arena(ACE ace) throws IOException {
        super(new BorderLayout());
        ArenaEditor editor = new ArenaEditor(ace);
        add(editor, BorderLayout.CENTER);   

    }
    
}
