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
 * Created on Feb 28, 2006
 */
package org.dwfa.bpa.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import com.sun.jini.start.LifeCycle;

public class NewWindowGenerator implements I_InitComponentMenus {

    protected static final Logger logger = Logger.getLogger(NewWindowGenerator.class.getName());

    public class NewFrame implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Constructor<?> c = classToMake.getConstructor(new Class[] { String[].class, LifeCycle.class });
                c.newInstance(new Object[] { frameArgs, null });
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
            }

        }

    }

    private String[] frameArgs;
    private Class<?> classToMake;
    private String title;

    public NewWindowGenerator(String[] args, LifeCycle lc) throws ConfigurationException {
        super();
        Configuration config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        this.title = (String) config.getEntry(this.getClass().getName(), "frameName", String.class);
        this.classToMake = (Class<?>) config.getEntry(this.getClass().getName(), "frameClass", Class.class);
        this.frameArgs = (String[]) config.getEntry(this.getClass().getName(), "frameArgs", String[].class);
        OpenFrames.addNewWindowMenuItemGenerator(this);
    }

    @Override
    public JMenuItem[] getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem(this.title);
        newWindow.addActionListener(new NewFrame());
        return new JMenuItem[] { newWindow };
    }

    @Override
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMenu getQuitMenu() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addInternalFrames(JMenu menu) {
        throw new UnsupportedOperationException();
    }
}
