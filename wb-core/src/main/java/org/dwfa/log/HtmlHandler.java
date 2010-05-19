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
 * Created on Apr 23, 2005
 */
package org.dwfa.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;

/**
 * @author kec
 * 
 */
public class HtmlHandler extends Handler implements I_PublishLogRecord {

    private JEditorPane logOut;
    private Writer logFile;
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    private SimpleDateFormat fileName = new SimpleDateFormat("yyyy-MM-dd HH mm");
    private int bufferSize = 60000;
    private Uuid id = UuidFactory.generate();
    private boolean printSourceAndMethodEnabled = false;

    /**
     * @param logOut
     * @throws IOException
     * 
     */
    public HtmlHandler(JEditorPane logOut) throws IOException {
        this(logOut, "logger ");

    }

    public HtmlHandler(JEditorPane logOut, String loggerName) throws IOException {
        super();
        this.logOut = logOut;
        if (loggerName.endsWith(" ") == false) {
            loggerName = loggerName + " ";
        }
        File root = new File("logs");
        root.mkdirs();
        FileWriter fw = new FileWriter(new File(root, loggerName + fileName.format(new Date()) + ".html"));
        this.logFile = new BufferedWriter(fw);
        this.logFile.write("<html>");

    }

    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord record) {
        String htmlRecord = format(record);
        try {
            this.logFile.write(format(record).replaceAll("\n", "<br>"));
            this.logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (logOut != null) {
            SwingUtilities.invokeLater(new AddRecord(htmlRecord));
        }
    }

    private class AddRecord implements Runnable {
        String htmlRecord;

        /**
         * @param htmlRecord
         */
        public AddRecord(String htmlRecord) {
            super();
            this.htmlRecord = htmlRecord;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                if (logOut.getDocument().getLength() > bufferSize) {
                    Document doc = logOut.getDocument();
                    doc.remove(0, bufferSize / 4);
                }
                ((HTMLEditorKit) logOut.getEditorKit()).read(new java.io.StringReader(htmlRecord),
                    logOut.getDocument(), logOut.getDocument().getLength());
                logOut.setCaretPosition(logOut.getDocument().getLength());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

    }

    private class Clear implements Runnable {
        /**
         * @param htmlRecord
         */
        public Clear() {
            super();
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                Document doc = logOut.getDocument();
                doc.remove(0, logOut.getDocument().getLength());
                logOut.setCaretPosition(logOut.getDocument().getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressWarnings("unchecked")
    private String format(LogRecord rec) {
        StringBuffer buf = new StringBuffer(1000);
        buf.append("<font size='3' face='Courier'>");
        if (rec.getLevel().intValue() <= Level.FINEST.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='#8470FF'>"); // Light slate blue
            buf.append("FNST");
        } else if (rec.getLevel().intValue() <= Level.FINER.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='#808000'>"); // Olive
            buf.append("FNER");
        } else if (rec.getLevel().intValue() <= Level.FINE.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='#4682B4'>"); // Steel blue
            buf.append("FINE");
        } else if (rec.getLevel().intValue() <= Level.CONFIG.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='#00BFFF'>"); // DeepSkyBlue
            buf.append("CNFG");
        } else if (rec.getLevel().intValue() <= Level.INFO.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='green'>");
            buf.append(rec.getLevel());
        } else if (rec.getLevel().intValue() <= Level.WARNING.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='#8B0000'>"); // DarkRed
            buf.append("WARN");
        } else if (rec.getLevel().intValue() >= Level.SEVERE.intValue()) {
            buf.append(format.format(new Date(rec.getMillis())));
            buf.append(' ');
            buf.append("<font color='#DC143C'>"); // Crimpson
            buf.append("SEVR");
        }
        buf.append("</font>");

        buf.append(' ');
        buf.append(formatMessage(rec));
        Object[] params = rec.getParameters();
        if ((params != null) && (params.length > 0)) {
            for (int i = 0; i < params.length; i++) {
                buf.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                buf.append("<font color='#8B0000'> param[");
                buf.append(i);
                buf.append("] </font>");
                if (params[i] == null) {
                    buf.append("null");
                } else if (Collection.class.isAssignableFrom(params[i].getClass())) {
                    Iterator<Object> itr = ((Collection<Object>) params[i]).iterator();
                    while (itr.hasNext()) {
                        buf.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                        buf.append(itr.next());
                    }
                } else {
                    buf.append(params[i]);
                }

                // buf.append(" ");
                // buf.append(params[i].getClass().getName());
            }

        }
        if (printSourceAndMethodEnabled) {
            buf.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            buf.append(rec.getSourceClassName());
            buf.append(": ");
            if (rec.getSourceMethodName() != null) {
                buf.append(rec.getSourceMethodName().replaceAll("<", "&lt;"));
            } else {
                buf.append("null");
            }
        }

        if (rec.getThrown() != null) {
            buf.append("<br><font color='#800000'>"); // Maroon
            Throwable thrown = rec.getThrown();
            buf.append("Exception: " + thrown);
            buf.append("<br>");
            StackTraceElement[] elements = thrown.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                buf.append("&nbsp;&nbsp;&nbsp;" + elements[i].toString().replaceAll("<", "&lt;"));
                buf.append("<br>");
            }
            Throwable causedBy = thrown.getCause();
            while (causedBy != null) {
                elements = causedBy.getStackTrace();
                buf.append("Caused by: " + causedBy);
                buf.append("<br>");
                for (int i = 0; i < elements.length; i++) {
                    buf.append("&nbsp;&nbsp;&nbsp;" + elements[i].toString().replaceAll("<", "&lt;"));
                    buf.append("<br>");
                }
                causedBy = causedBy.getCause();
            }

            buf.append("</font><br>");
        }
        buf.append("</font>\n");
        return buf.toString();
    }

    private String formatMessage(LogRecord rec) {
        if (rec.getMessage() != null) {
            return rec.getMessage().replaceAll("<", "&lt;").replaceAll("\n", "<br>");
        }
        return "";
    }

    /**
     * @see java.util.logging.Handler#flush()
     */
    public void flush() {

    }

    /**
     * @see java.util.logging.Handler#close()
     */
    public void close() throws SecurityException {

    }

    /**
     * @return Returns the bufferSize.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize The bufferSize to set.
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @see org.dwfa.log.I_PublishLogRecord#getId()
     */
    public Uuid getId() throws RemoteException {
        return id;
    }

    /**
     * @return Returns the printSourceAndMethodEnabled.
     */
    public boolean isPrintSourceAndMethodEnabled() {
        return printSourceAndMethodEnabled;
    }

    /**
     * @param printSourceAndMethodEnabled The printSourceAndMethodEnabled to
     *            set.
     */
    public void setPrintSourceAndMethodEnabled(boolean printSourceAndMethodEnabled) {
        this.printSourceAndMethodEnabled = printSourceAndMethodEnabled;
    }

    public void clearLog() {
        SwingUtilities.invokeLater(new Clear());
    }
}
