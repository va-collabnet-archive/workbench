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
/*
 * Created on May 20, 2005
 */
package org.dwfa.bpa.util;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

/**
 * @author kec
 * 
 */
public abstract class ComponentFrame extends JFrame implements I_InitComponentMenus {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    protected static final Logger logger = Logger.getLogger(ComponentFrame.class.getName());
    protected ComponentFrameBean cfb;
    protected final static int MENU_MASK = getMenuMask();

    public static int getMenuMask() {
        try {
            return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (HeadlessException e) {
            //
        }
        return 0;
    }

    private boolean sendToBackInsteadOfClose = false;
    public boolean sendToBackInsteadOfClose() {
		return sendToBackInsteadOfClose;
	}

    public void setSendToBackInsteadOfClose(boolean sendToBackInsteadOfClose) {
        this.sendToBackInsteadOfClose = sendToBackInsteadOfClose;
    }

    private String[] args;
    protected File menuDir;

    public static Rectangle getDefaultFrameSize() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        if (screenSize.width < 1400) {
            return new Rectangle(0, 0, screenSize.width - 50, screenSize.height - 50);
        }
        return new Rectangle(0, 0, 1400, 1028);
    }

    public class NewFrame implements ActionListener {

        private String[] args;

        /**
         * @param args
         * @param lc
         */
        public NewFrame(String[] args) {
            super();
            this.args = args;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Class<? extends ComponentFrame> classToMake = ComponentFrame.this.getClass();
                Constructor<? extends ComponentFrame> c = classToMake.getConstructor(new Class[] { String[].class});
                c.newInstance(new Object[] { args });
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage());
            }

        }

    }

    /**
     * @param title
     * @throws Exception
     */
    public ComponentFrame(String[] args) throws Exception {
        this(args, false);
    }

    public ComponentFrame(String[] args, File menuDir) throws Exception {
        this(args, false, menuDir);
    }

    public ComponentFrame(String[] args, boolean hiddenFrame) throws Exception {
        this(args, hiddenFrame, null);
    }

    public ComponentFrame(String[] args, boolean hiddenFrame, File menuDir) throws Exception {
        super();
        this.args = args;
         this.menuDir = menuDir;
        this.setTitle(this.getNextFrameName());
        this.cfb = new ComponentFrameBean(args, this, this, hiddenFrame);
        this.cfb.setup();
    }

    @Override
    public abstract void addAppMenus(JMenuBar mainMenuBar) throws Exception;

    @Override
    public abstract JMenu getQuitMenu();

    @Override
    public abstract JMenuItem[] getNewWindowMenu();

    public abstract String getNextFrameName();

    /**
     * @return Returns the args.
     */
    public String[] getArgs() {
        return args;
    }

    public boolean okToClose() {
        return getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE;
    }

    public abstract int getCount();

    public List<I_DoQuitActions> getQuitList() {
        return cfb.getQuitList();
    }

    public JMenuItem getQuitMI() {
        return cfb.getQuitMI();
    }
}
