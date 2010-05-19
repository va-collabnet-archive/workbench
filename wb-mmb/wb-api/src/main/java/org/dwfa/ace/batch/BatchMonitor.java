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
package org.dwfa.ace.batch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.ComputationCanceled;

/**
 * Used to give statistical reporting (progress, time remaining, etc) during a
 * batch processing operation.
 */
public class BatchMonitor {

    private String description = "Batch process";
    private long startTime = 0;
    private long totalEvents = 0;
    private long reportCycleMs = 0;
    private long eventCount = 0;
    private long lastReportTime = 0;
    private long lastReportEventCount = 0;

    private boolean isCancelled = false;

    private Logger logger = Logger.getLogger(BatchMonitor.class.getName());

    private BatchReportingThread timer;

    private I_ShowActivity activity;
    private JPanel htmlPanel;
    private JEditorPane htmlPane;
    private boolean showMessagePanel = false;
    private JButton finishedButton;

    /**
     * @param description A textual description of the batch
     * @param totalEvents The total number of expected events
     * @param reportCycleMs The time between giving an updated report
     */
    public BatchMonitor(String description, long totalEvents, long reportCycleMs) {
        if (description != null) {
            this.description = description;
        }
        this.totalEvents = totalEvents;
        this.reportCycleMs = reportCycleMs;
    }

    /**
     * @param description A textual description of the batch
     * @param totalEvents The total number of expected events
     * @param reportCycleMs The time between giving an updated report
     * @param showMessagePanelToSet Adds a text panel to the bottom on the
     *            dialog for messages etc.
     */
    public BatchMonitor(String description, long totalEvents, long reportCycleMs, boolean showMessagePanelToSet) {
        this(description, totalEvents, reportCycleMs);

        showMessagePanel = showMessagePanelToSet;
    }

    /**
     * Mark an event
     */
    public void mark() throws BatchCancelledException {
        throwIfCancelled();
        eventCount++;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setText(String text) {
        if (activity != null) {
            if (showMessagePanel) {
                htmlPane.setText(text);
            } else {
                activity.setProgressInfoLower(text);
            }
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (activity != null) {
            activity.setIndeterminate(indeterminate);
        }
        if (indeterminate) {
            totalEvents = 0;
        }
    }

    /**
     * Report updated statistics
     */
    public void report() {
        if (totalEvents != 0) {

            long eventsSinceLastReport = eventCount - lastReportEventCount;
            long timeSinceLastReport = new Date().getTime() - lastReportTime;
            long percentComplete = (eventCount * 100) / totalEvents;

            if (eventsSinceLastReport > 0) {

                double eventsPerMs = eventsSinceLastReport / (double) timeSinceLastReport;
                long timeToCompleteMs = Math.round((totalEvents - eventCount) / eventsPerMs);

                activity.setProgressInfoLower(percentComplete + "% completed (" + eventCount + " of " + totalEvents
                    + "). " + asTimeFormat(timeToCompleteMs, false) + "remaining");
                activity.setValue((int) eventCount);

                logger.info(description + ": " + percentComplete + "% complete (" + eventCount + " of " + totalEvents
                    + "). " + getEventRate(eventsPerMs) + ". Estimated time to complete: "
                    + asTimeFormat(timeToCompleteMs, true));
            } else {
                logger.info(description + ": " + percentComplete + "% complete (" + eventCount + " of " + totalEvents
                    + "). " + "No activity since last report!");
            }

            lastReportEventCount = eventCount;
            lastReportTime = new Date().getTime();
        }
    }

    /**
     * Start this monitor. Batch processing has commenced.
     */
    public void start() throws Exception {
        I_TermFactory termFactory = Terms.get();
        activity = termFactory.newActivityPanel(false, termFactory.getActiveAceFrameConfig(),
            description, true);
        activity.setProgressInfoUpper(description);
        activity.setProgressInfoLower("Commencing...");
        activity.setMaximum((int) totalEvents);
        activity.setValue(0);
        activity.setIndeterminate(false);

        timer = new BatchReportingThread(this, reportCycleMs, activity);
        if (showMessagePanel) {
            addMessagePanel();
        }
        addFinishedButton();
        timer.activity.getViewPanel(false).validate();

        timer.start();
    }

    /**
     * Add a JEditorPane to the bottom on the dialog.
     */
    private void addMessagePanel() {
        htmlPanel = new JPanel();
        htmlPane = new JEditorPane("text/html", "");

        htmlPane.setEditable(false);
        htmlPane.validate();

        htmlPanel.setLayout(new GridLayout(1, 1));
        htmlPanel.setPreferredSize(new Dimension(100, 100));
        htmlPanel.add(new JScrollPane(htmlPane), BorderLayout.CENTER);
        htmlPanel.validate();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.SOUTH;

        startTime = new Date().getTime();
        lastReportTime = startTime;
        timer.activity.getViewPanel(false).add(htmlPanel, c);
    }

    /**
     * Add a JEditorPane to the bottom on the dialog.
     */
    private void addFinishedButton() {
        finishedButton = new JButton(new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/flag_green.png")));
        finishedButton.setVisible(false);
        finishedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                finished();
            };
        });
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 4;
        c.gridy = 0;
        c.gridheight = 1;
        c.weighty = 0;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        timer.activity.getViewPanel(false).add(finishedButton, c);
    }

    /**
     * Show or hide the finish button.
     * 
     * @param visible boolean
     */
    public void setFinishedButtonVisible(boolean visible) {
        finishedButton.setVisible(visible);
    }

    /**
     * Stop the monitor. Batch processing has completed.
     */
    public void complete() throws BatchCancelledException {
        throwIfCancelled();
        long duration = new Date().getTime() - startTime;
        report();
        String timeElapsed = asTimeFormat(duration, true);
        activity.setProgressInfoLower("Completed processing " + eventCount + " items in " + timeElapsed);
        logger.info("Batch completed: " + description + ". " + eventCount + " items processed in " + timeElapsed);
        totalEvents = 0;
        try {
            activity.complete();
        } catch (ComputationCanceled e) {
            throw new BatchCancelledException(e);
        }
        if (!finishedButton.isVisible()) {
            timer.interrupt();
        }
    }

    /**
     * Cancel the monitor. Batch has been aborted.
     * Any further calls to {@link #mark()} or {@link #complete()} will raise a
     * {@link BatchCancelledException}
     */
    public void cancel() {
        isCancelled = true;
        timer.interrupt();
        try {
            activity.complete();
        } catch (ComputationCanceled e) {
           // Nothing to do...;
        }
        activity.setProgressInfoLower("Cancelled by user");
    }

    public void finished() {
        timer.interrupt();
        activity.setProgressInfoLower("Finished processing");
    }

    private void throwIfCancelled() throws BatchCancelledException {
        if (isCancelled) {
            throw new BatchCancelledException();
        }
    }

    private String getEventRate(double eventsPerMs) {
        DecimalFormat format = new DecimalFormat("0.00");
        if (eventsPerMs < 0.001) {
            return "Events per minute: " + format.format(eventsPerMs * 60000);
        } else {
            return "Events per second: " + format.format(eventsPerMs * 1000);
        }
    }

    private String asTimeFormat(long durationMs, boolean showMillis) {
        StringBuffer result = new StringBuffer();
        long hours = durationMs / 3600000;
        if (hours != 0) {
            result.append(hours).append(" hours, ");
        }
        long minutes = (durationMs % 3600000) / 60000;
        if (minutes != 0) {
            result.append(minutes).append(" minutes, ");
        }
        long seconds = ((durationMs % 3600000) % 60000) / 1000;
        result.append(seconds);
        if (showMillis) {
            String millis = String.format("%03d", (durationMs % 1000));
            result.append(".").append(millis);
        }
        result.append(" seconds ");
        return result.toString();
    }

    public class BatchReportingThread extends Thread {

        private long reportCycle = 0;

        private BatchMonitor batch;
        private MonitorDialog dialog;
        private I_ShowActivity activity;

        public BatchReportingThread(BatchMonitor batch, long reportCycleMs, I_ShowActivity activity) {
            this.batch = batch;
            this.reportCycle = reportCycleMs;
            this.activity = activity;

            this.activity.addRefreshActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel();
                };
            });
        }

        @Override
        public void run() {
            try {
                showDialog();
                while (true) {
                    sleep(reportCycle);
                    batch.report();
                }
            } catch (InterruptedException e) {
            } finally {
                dialog.dispose();
            }
        }

        private void showDialog() {
            dialog = new MonitorDialog(activity);
            dialog.pack();
            dialog.setTitle("Operation in progress");
            dialog.setResizable(false);
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true);

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();

            // centre on the screen
            int x = (screenSize.width - dialog.getWidth()) / 2;
            int y = (screenSize.height - dialog.getHeight()) / 2;
            dialog.setLocation(x, y);

            // beef it up a little
            dialog.setSize((int) (1.25 * dialog.getWidth()), (int) (1.5 * dialog.getHeight()));

            dialog.setVisible(true);
        }
    }

    public class MonitorDialog extends JDialog {

        private static final long serialVersionUID = 8408703041409497746L;

        public MonitorDialog(I_ShowActivity activity) {
            setContentPane(activity.getViewPanel(false));
        }
    }
}
