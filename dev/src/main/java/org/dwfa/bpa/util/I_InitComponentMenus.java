/*
 * Created on May 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * @author kec
 *
 */
public interface I_InitComponentMenus {
	public void addAppMenus(JMenuBar mainMenuBar) throws Exception;

	public JMenu getQuitMenu();
    
    public void addInternalFrames(JMenu menu);
    
    public JMenuItem getNewWindowMenu();
}