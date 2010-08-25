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
package org.dwfa.util;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.OpenFrames;

public class LogWithAlerts {

    Logger logger;

    public LogWithAlerts(String logName) {
        super();
        logger = Logger.getLogger(logName);
    }

    public Logger getLogger() {
        return logger;
    }

    public void alertAndLogException(Component parent, Throwable ex) {
        alertAndLog(parent, Level.SEVERE, ex.getLocalizedMessage(), ex);
    }

    public void alertAndLogException(Throwable ex) {
        alertAndLog(null, Level.SEVERE, ex.getLocalizedMessage(), ex);
    }

    public void alertAndLog(Level level, String message, Throwable ex) {
        alertAndLog(null, level, message, ex);
    }

    public void alertAndLog(final Component parent, final Level level, final String message, final Throwable ex) {
        if (DwfaEnv.isHeadless() == false) {
            if (SwingUtilities.isEventDispatchThread()) {
                alertAndLogPrivate(getActiveFrame(parent), level, message, ex);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            alertAndLogPrivate(getActiveFrame(parent), level, message, ex);
                        }

                    });
                } catch (InterruptedException e) {
                    getLogger().log(Level.SEVERE, message, e);
                } catch (InvocationTargetException e) {
                    getLogger().log(Level.SEVERE, message, e);
                }
            }
        } else {
            getLogger().log(level, message, ex);
        }
    }

    private void alertAndLogPrivate(Component parent, Level level, String message, Throwable ex) {
        parent = getActiveFrame(parent);
        getLogger().log(level, message, ex);
        message = "<html>" + message;
        if (level.intValue() <= Level.INFO.intValue()) {
            JOptionPane.showMessageDialog(parent, message, "Information has been logged",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            JOptionPane.showMessageDialog(parent, message, "A warning has been logged", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent, message, "An error has been logged", JOptionPane.ERROR_MESSAGE);
        }
        if (parent != null) {
            SwingUtilities.invokeLater(new FocusParentLater(parent));
        } else {
            getLogger().log(Level.SEVERE, "No parent for alert");
        }
    }

    private static class FocusParentLater implements Runnable {
        Component parent;

        private FocusParentLater(Component parent) {
            super();
            this.parent = parent;
        }

        public void run() {
            parent.requestFocus();
        }

    }

    private static JFrame activeFrame;

    public static Component getActiveFrame(Component parent) {
        if (parent == null) {
            for (Frame f : Frame.getFrames()) {
                if (f.isActive()) {
                    parent = f;
                    break;
                }
            }
            if (parent == null) {
                if (SwingUtilities.isEventDispatchThread()) {
                    setupActiveFrame();
                } else {
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                setupActiveFrame();
                            }
                        });
                    } catch (InterruptedException e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, e.getLocalizedMessage(), e);
                    } catch (InvocationTargetException e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        if (parent != null) {
            return parent;
        } else {
            return activeFrame;
        }
    }

    private static void setupActiveFrame() {
        if (activeFrame == null) {
            activeFrame = new JFrame();
            activeFrame.setContentPane(new JLabel("Startup..."));
            activeFrame.pack();
            activeFrame.setVisible(true);
            activeFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - 10,
                (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - 10);
            OpenFrames.addFrame(activeFrame);
        }
    }

    public void addHandler(Handler arg0) throws SecurityException {
        getLogger().addHandler(arg0);
    }

    public void config(String arg0) {
        getLogger().config(arg0);
    }

    public void entering(String arg0, String arg1, Object arg2) {
        getLogger().entering(arg0, arg1, arg2);
    }

    public void entering(String arg0, String arg1, Object[] arg2) {
        getLogger().entering(arg0, arg1, arg2);
    }

    public void entering(String arg0, String arg1) {
        getLogger().entering(arg0, arg1);
    }

    public void exiting(String arg0, String arg1, Object arg2) {
        getLogger().exiting(arg0, arg1, arg2);
    }

    public void exiting(String arg0, String arg1) {
        getLogger().exiting(arg0, arg1);
    }

    public void fine(String arg0) {
        getLogger().fine(arg0);
    }

    public void finer(String arg0) {
        getLogger().finer(arg0);
    }

    public void finest(String arg0) {
        getLogger().finest(arg0);
    }

    public Filter getFilter() {
        return getLogger().getFilter();
    }

    public Handler[] getHandlers() {
        return getLogger().getHandlers();
    }

    public Level getLevel() {
        return getLogger().getLevel();
    }

    public String getName() {
        return getLogger().getName();
    }

    public Logger getParent() {
        return getLogger().getParent();
    }

    public ResourceBundle getResourceBundle() {
        return getLogger().getResourceBundle();
    }

    public String getResourceBundleName() {
        return getLogger().getResourceBundleName();
    }

    public boolean getUseParentHandlers() {
        return getLogger().getUseParentHandlers();
    }

    public void info(String arg0) {
        getLogger().info(arg0);
    }

    public boolean isLoggable(Level arg0) {
        return getLogger().isLoggable(arg0);
    }

    public void log(Level arg0, String arg1, Object arg2) {
        getLogger().log(arg0, arg1, arg2);
    }

    public void log(Level arg0, String arg1, Object[] arg2) {
        getLogger().log(arg0, arg1, arg2);
    }

    public void log(Level arg0, String arg1, Throwable arg2) {
        getLogger().log(arg0, arg1, arg2);
    }

    public void log(Level arg0, String arg1) {
        getLogger().log(arg0, arg1);
    }

    public void log(LogRecord arg0) {
        getLogger().log(arg0);
    }

    public void logp(Level arg0, String arg1, String arg2, String arg3, Object arg4) {
        getLogger().logp(arg0, arg1, arg2, arg3, arg4);
    }

    public void logp(Level arg0, String arg1, String arg2, String arg3, Object[] arg4) {
        getLogger().logp(arg0, arg1, arg2, arg3, arg4);
    }

    public void logp(Level arg0, String arg1, String arg2, String arg3, Throwable arg4) {
        getLogger().logp(arg0, arg1, arg2, arg3, arg4);
    }

    public void logp(Level arg0, String arg1, String arg2, String arg3) {
        getLogger().logp(arg0, arg1, arg2, arg3);
    }

    public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object arg5) {
        getLogger().logrb(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object[] arg5) {
        getLogger().logrb(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Throwable arg5) {
        getLogger().logrb(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4) {
        getLogger().logrb(arg0, arg1, arg2, arg3, arg4);
    }

    public void removeHandler(Handler arg0) throws SecurityException {
        getLogger().removeHandler(arg0);
    }

    public void setFilter(Filter arg0) throws SecurityException {
        getLogger().setFilter(arg0);
    }

    public void setLevel(Level arg0) throws SecurityException {
        getLogger().setLevel(arg0);
    }

    public void setParent(Logger arg0) {
        getLogger().setParent(arg0);
    }

    public void setUseParentHandlers(boolean arg0) {
        getLogger().setUseParentHandlers(arg0);
    }

    public void severe(String arg0) {
        getLogger().severe(arg0);
    }

    public void severe(String arg0, Throwable t) {
        getLogger().log(Level.SEVERE, arg0, t);
    }

    public void throwing(String arg0, String arg1, Throwable arg2) {
        getLogger().throwing(arg0, arg1, arg2);
    }

    public void warning(String arg0) {
        getLogger().warning(arg0);
    }

}
