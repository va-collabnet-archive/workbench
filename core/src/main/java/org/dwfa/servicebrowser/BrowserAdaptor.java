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
 * Created on Apr 27, 2005
 */
package org.dwfa.servicebrowser;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.dwfa.bpa.util.ComponentFrameBean;
import org.dwfa.bpa.util.I_InitComponentMenus;

import com.sun.jini.example.browser.Browser;
import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class BrowserAdaptor extends Browser implements I_InitComponentMenus {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static Logger logger = Logger.getLogger(BrowserAdaptor.class.getName());

    protected JMenu fileMenu;

    private String[] args;
    private LifeCycle lc;

    public static class NewFrame implements ActionListener {
        private String[] args;
        private LifeCycle lc;

        /**
         * @param args
         * @param lc
         */
        public NewFrame(String[] args, LifeCycle lc) {
            super();
            this.args = args;
            this.lc = lc;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                new BrowserAdaptor(this.args, this.lc);
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage());
            }

        }

    }

    /**
     * 
     */
    public BrowserAdaptor(String[] args, LifeCycle lc) throws Exception {
        super(args, lc);
        this.args = args;
        this.lc = lc;
        logger.info("\n*******************\n\n" + "Starting " + this.getClass().getSimpleName() + " with config file: "
            + Arrays.asList(args) + "\n\n******************\n");
        Configuration config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());

        String title = (String) config.getEntry(this.getClass().getName(), "frameName", String.class, "Service Browser");
        if (count == 0) {
            this.setTitle(title);
            count++;
        } else {
            this.setTitle(title + " " + count++);
        }
        new ComponentFrameBean(args, lc, this, this, this.getJMenuBar());

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public void addAppMenus(JMenuBar mainMenuBar) {
        this.fileMenu = mainMenuBar.getMenu(0);
        Component[] menuItems = this.fileMenu.getMenuComponents();
        for (int i = 0; i < menuItems.length; i++) {
            JMenuItem item = (JMenuItem) menuItems[i];
            if (item.getText().equals("Exit")) {
                this.fileMenu.remove(i);
            }
        }

    }

    public JMenu getQuitMenu() {
        return this.fileMenu;
    }

    public void addInternalFrames(JMenu menu) {

    }

    public JMenuItem[] getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem("Service Browser");
        newWindow.addActionListener(new NewFrame(this.args, this.lc));
        return new JMenuItem[] { newWindow };
    }

    private static int count = 0;

}
