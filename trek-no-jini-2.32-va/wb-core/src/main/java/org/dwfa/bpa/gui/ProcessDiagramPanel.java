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
package org.dwfa.bpa.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputListener;

import org.dwfa.bpa.Branch;
import org.dwfa.bpa.I_Branch;
import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.DataContainerTransferable;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.dnd.TaskTransferable;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.tapi.NoMappingException;

/**
 * @author kec
 * 
 */
public class ProcessDiagramPanel extends JPanel implements I_DoDragAndDrop, MouseInputListener, PropertyChangeListener {

    private static boolean colorForDebug = false;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(ProcessBuilderPanel.class.getName());
    private I_SupportDragAndDrop dndBean;

    private Point offset;

    private Component draggedComponent;

    private I_EncodeBusinessProcess process;

    private Map<Integer, TaskPanel> taskPanels = new HashMap<Integer, TaskPanel>();

    private I_Work worker;
    private Set<ActionListener> taskAddedActionListeners = new HashSet<ActionListener>();

    private int indexSpacer = 10;

    I_HandleDoubleClickInTaskProcess doubleClickHandler;

    /**
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     * @throws QueryException
     * @throws PropertyVetoException
     * @throws InvalidComponentException
     * @throws ValidationException
     * @throws IdentifierIsNotNativeException
     * @throws NoMappingException
     * @throws RemoteException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * 
     */
    public ProcessDiagramPanel(I_EncodeBusinessProcess process, I_Work worker,
            I_HandleDoubleClickInTaskProcess doubleClickHandler) throws PropertyVetoException, Exception {
        super();
        this.doubleClickHandler = doubleClickHandler;
        this.setLayout(null);
        this.dndBean = new BpaDragAndDropBean("ProcessDiagram", this, true, false);
        this.process = process;
        this.worker = worker;
        this.process.addPropertyChangeListener("currentTaskId", this);
        this.process.addPropertyChangeListener("lastTaskRemoved", this);
        layoutTasks(this.worker);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    /**
     * @param worker
     * @throws ClassNotFoundException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws RemoteException
     * @throws NoMappingException
     * @throws IdentifierIsNotNativeException
     * @throws ValidationException
     * @throws InvalidComponentException
     * @throws PropertyVetoException
     * @throws QueryException
     * @throws NoSuchMethodException
     */
    private void layoutTasks(I_Work worker) throws PropertyVetoException, Exception {
        this.removeAll();
        Iterator<I_DefineTask> taskItr = this.process.getTasks().iterator();
        while (taskItr.hasNext()) {
            I_DefineTask task = taskItr.next();
            if (task != null) {
                addTaskPanel(task, doubleClickHandler);
            }
        }
        updatePreferredSize();
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
        Map<Integer, Map<MinAndMaxPoint, Point>> verticalRunXs = new HashMap<Integer, Map<MinAndMaxPoint, Point>>();
        Shape clip = g.getClip();
        g.setClip(this.getVisibleRect());
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(getForeground());
        g.setClip(clip);
        super.paintComponent(g);
        g.setClip(this.getVisibleRect());
        Graphics2D g2 = (Graphics2D) g;
        ArrayList<TaskPanel> taskPanelList = new ArrayList<TaskPanel>(this.taskPanels.values());
        Collections.sort(taskPanelList, new Comparator<TaskPanel>() {

            public int compare(TaskPanel tp0, TaskPanel tp1) {
                return tp0.getId() - tp1.getId();
            }
        });
        Iterator<TaskPanel> panelItr = taskPanelList.iterator();
        while (panelItr.hasNext()) {
            TaskPanel originTask = panelItr.next();
            Iterator<Branch> branchItr = this.process.getBranches(originTask.getTask()).iterator();
            while (branchItr.hasNext()) {
                drawBranch(g2, originTask, branchItr, verticalRunXs);
            }
        }

    }

    private class MinAndMaxPoint {
        int minPt;

        int maxPt;

        /**
         * @return Returns the maxPt.
         */
        public int getMaxPt() {
            return maxPt;
        }

        /**
         * @return Returns the minPt.
         */
        public int getMinPt() {
            return minPt;
        }

        /**
         * @param minPt
         * @param maxPt
         */
        public MinAndMaxPoint(int minPt, int maxPt) {
            super();
            this.minPt = minPt;
            this.maxPt = maxPt;
        }

        public boolean crosses(int minPt, int maxPt) {
            if ((minPt >= this.minPt) && (minPt <= this.maxPt)) {
                return true;
            }
            if ((maxPt >= this.minPt) && (maxPt <= this.maxPt)) {
                return true;
            }
            if ((minPt < this.minPt) && (maxPt > this.maxPt)) {
                return true;
            }

            return false;
        }

        public boolean crosses(MinAndMaxPoint span) {
            return crosses(span.minPt, span.maxPt);
        }
    }

    /**
     * @param g2
     * @param originTask
     * @param branchItr
     */
    private void drawBranch(Graphics2D g2, TaskPanel originTask, Iterator<Branch> branchItr,
            Map<Integer, Map<MinAndMaxPoint, Point>> verticalRuns) {
        I_Branch branch = branchItr.next();
        TaskPanel destinationTask = this.taskPanels.get(new Integer(branch.getDestinationId()));
        if (destinationTask != null) {
            g2.setColor(Color.BLUE);
            // g2.drawRect(x, y, width, height);
            Point originPt = originTask.getBranchExitPoint(branch);
            Point destinationPt = destinationTask.getBranchEntrancePoint();
            if (destinationTask.getLocation().y > originPt.y) {
                drawBranchBelow(g2, verticalRuns, originPt, destinationPt);
            } else {
                if (destinationTask.equals(originTask)) {
                    drawBranchBelow(g2, verticalRuns, originPt, destinationPt);
                } else if (destinationTask.getLocation().x > originPt.x) {
                    drawBranchAboveRight(g2, verticalRuns, originPt, destinationPt, destinationTask);

                } else {
                    if (originPt.x < destinationTask.getLocation().x + destinationTask.getWidth() + indexSpacer) {
                        drawBranchAboveMiddle(g2, verticalRuns, originPt, destinationPt, destinationTask);

                    } else if ((originPt.x > destinationTask.getBranchEntrancePoint().x - (indexSpacer * 2) && (destinationTask.getBranchEntrancePoint().x > originTask.getLocation().x))) {
                        drawBranchAboveMiddle(g2, verticalRuns, originPt, destinationPt, destinationTask);

                    } else {
                        drawBranchAboveLeft(g2, verticalRuns, originPt, destinationPt, originTask);
                    }

                }
            }

        }
    }

    /**
     * @param g2
     * @param verticalRuns
     * @param originPt
     * @param destinationPt
     */
    private void drawBranchAboveRight(Graphics2D g2, Map<Integer, Map<MinAndMaxPoint, Point>> verticalRuns,
            Point originPt, Point destinationPt, TaskPanel destinationTask) {
        Stroke origStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));
        if (colorForDebug) {
            g2.setColor(Color.GRAY);
        }

        Point intermediatePoint = destinationTask.getLocation();
        intermediatePoint.x = intermediatePoint.x - indexSpacer;
        intermediatePoint.y = intermediatePoint.y - indexSpacer;

        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);

        int verticalRunX = Math.max(originPt.x - indexSpacer, intermediatePoint.x - indexSpacer);
        int minY = Math.min(originPt.y, intermediatePoint.y);
        int maxY = Math.max(originPt.y, intermediatePoint.y);
        int pointsPerLine = 6;
        MinAndMaxPoint ySpan = new MinAndMaxPoint(minY, maxY);
        Integer key = new Integer(verticalRunX / pointsPerLine);
        while (verticalRuns.containsKey(key)) {
            // will they cross?
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            boolean crosses = false;
            for (Map.Entry<MinAndMaxPoint, Point> entry : entries.entrySet()) {
                MinAndMaxPoint existingSpan = entry.getKey();
                Point existingDest = entry.getValue();
                if (existingDest.equals(destinationPt)) {
                    break;
                } else if (existingSpan.crosses(ySpan)) {
                    crosses = true;
                    break;
                }
            }
            if (crosses) {
                verticalRunX = verticalRunX - indexSpacer;
                key = new Integer(verticalRunX / pointsPerLine);
            } else {
                break;
            }
        }

        if (verticalRuns.containsKey(key)) {
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            entries.put(ySpan, destinationPt);
        } else {
            Map<MinAndMaxPoint, Point> entries = new HashMap<MinAndMaxPoint, Point>();
            entries.put(ySpan, destinationPt);
            verticalRuns.put(key, entries);
        }
        polyline.moveTo(originPt.x, originPt.y);
        polyline.lineTo(verticalRunX, originPt.y);
        polyline.lineTo(verticalRunX, intermediatePoint.y);
        polyline.lineTo(intermediatePoint.x, intermediatePoint.y);

        g2.draw(polyline);

        g2.setStroke(origStroke);
        drawBranchBelow(g2, verticalRuns, intermediatePoint, destinationPt);
    }

    /**
     * @param g2
     * @param verticalRuns
     * @param originPt
     * @param destinationPt
     */
    private void drawBranchAboveLeft(Graphics2D g2, Map<Integer, Map<MinAndMaxPoint, Point>> verticalRuns,
            Point originPt, Point destinationPt, TaskPanel originTask) {
        Stroke origStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));
        if (colorForDebug) {
            g2.setColor(Color.RED);
        }

        Point intermediatePoint = originTask.getLocation();
        intermediatePoint.x = intermediatePoint.x - indexSpacer;
        intermediatePoint.y = intermediatePoint.y + originTask.getHeight() + indexSpacer;

        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);

        int verticalRunX = Math.max(originPt.x + indexSpacer, intermediatePoint.x + indexSpacer);
        int minY = Math.min(originPt.y, intermediatePoint.y);
        int maxY = Math.max(originPt.y, intermediatePoint.y);
        int pointsPerLine = 6;
        MinAndMaxPoint ySpan = new MinAndMaxPoint(minY, maxY);
        Integer key = new Integer(verticalRunX / pointsPerLine);
        while (verticalRuns.containsKey(key)) {
            // will they cross?
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            boolean crosses = false;
            for (Map.Entry<MinAndMaxPoint, Point> entry : entries.entrySet()) {
                MinAndMaxPoint existingSpan = entry.getKey();
                Point existingDest = entry.getValue();
                if (existingDest.equals(destinationPt)) {
                    break;
                } else if (existingSpan.crosses(ySpan)) {
                    crosses = true;
                    break;
                }
            }
            if (crosses) {
                verticalRunX = verticalRunX + indexSpacer;
                key = new Integer(verticalRunX / pointsPerLine);
            } else {
                break;
            }
        }

        if (verticalRuns.containsKey(key)) {
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            entries.put(ySpan, destinationPt);
        } else {
            Map<MinAndMaxPoint, Point> entries = new HashMap<MinAndMaxPoint, Point>();
            entries.put(ySpan, destinationPt);
            verticalRuns.put(key, entries);
        }
        polyline.moveTo(originPt.x, originPt.y);
        polyline.lineTo(verticalRunX, originPt.y);
        polyline.lineTo(verticalRunX, intermediatePoint.y);
        polyline.lineTo(intermediatePoint.x, intermediatePoint.y);

        g2.draw(polyline);

        g2.setStroke(origStroke);
        drawBranchAbove(g2, verticalRuns, intermediatePoint, destinationPt);
    }

    /**
     * @param g2
     * @param verticalRuns
     * @param originPt
     * @param destinationPt
     */
    private void drawBranchAboveMiddle(Graphics2D g2, Map<Integer, Map<MinAndMaxPoint, Point>> verticalRuns,
            Point originPt, Point destinationPt, TaskPanel originTask) {
        Stroke origStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));
        if (colorForDebug) {
            g2.setColor(Color.ORANGE);
        }

        Point intermediatePoint = originTask.getLocation();
        intermediatePoint.x = intermediatePoint.x - indexSpacer;
        intermediatePoint.y = intermediatePoint.y + originTask.getHeight() + indexSpacer;

        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);

        int verticalRunX = Math.max(originPt.x + indexSpacer, intermediatePoint.x + indexSpacer);
        int minY = Math.min(originPt.y, intermediatePoint.y);
        int maxY = Math.max(originPt.y, intermediatePoint.y);
        int pointsPerLine = 6;
        MinAndMaxPoint ySpan = new MinAndMaxPoint(minY, maxY);
        Integer key = new Integer(verticalRunX / pointsPerLine);
        while (verticalRuns.containsKey(key)) {
            // will they cross?
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            boolean crosses = false;
            for (Map.Entry<MinAndMaxPoint, Point> entry : entries.entrySet()) {
                MinAndMaxPoint existingSpan = entry.getKey();
                Point existingDest = entry.getValue();
                if (existingDest.equals(destinationPt)) {
                    break;
                } else if (existingSpan.crosses(ySpan)) {
                    crosses = true;
                    break;
                }
            }
            if (crosses) {
                verticalRunX = verticalRunX + indexSpacer;
                key = new Integer(verticalRunX / pointsPerLine);
            } else {
                break;
            }
        }

        if (verticalRuns.containsKey(key)) {
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            entries.put(ySpan, destinationPt);
        } else {
            Map<MinAndMaxPoint, Point> entries = new HashMap<MinAndMaxPoint, Point>();
            entries.put(ySpan, destinationPt);
            verticalRuns.put(key, entries);
        }
        polyline.moveTo(originPt.x, originPt.y);
        polyline.lineTo(verticalRunX, originPt.y);
        // polyline.lineTo(verticalRunX, intermediatePoint.y);
        intermediatePoint.x = verticalRunX;
        intermediatePoint.y = originPt.y;
        // polyline.lineTo(intermediatePoint.x, intermediatePoint.y);

        g2.draw(polyline);

        g2.setStroke(origStroke);
        drawBranchBelow(g2, verticalRuns, intermediatePoint, destinationPt);
    }

    /**
     * @param g2
     * @param verticalRuns
     * @param originPt
     * @param destinationPt
     */
    private void drawBranchBelow(Graphics2D g2, Map<Integer, Map<MinAndMaxPoint, Point>> verticalRuns, Point originPt,
            Point destinationPt) {
        Stroke origStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));
        if (colorForDebug) {
            g2.setColor(Color.BLACK);
        }

        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);

        int verticalRunX = Math.max(originPt.x + 10, destinationPt.x + 10);
        int minY = Math.min(originPt.y, destinationPt.y);
        int maxY = Math.max(originPt.y, destinationPt.y);
        int pointsPerLine = 6;
        MinAndMaxPoint ySpan = new MinAndMaxPoint(minY, maxY);
        Integer key = new Integer(verticalRunX / pointsPerLine);
        while (verticalRuns.containsKey(key)) {
            // will they cross?
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            boolean crosses = false;
            for (Map.Entry<MinAndMaxPoint, Point> entry : entries.entrySet()) {
                MinAndMaxPoint existingSpan = entry.getKey();
                Point existingDest = entry.getValue();
                if (existingDest.equals(destinationPt)) {
                    break;
                } else if (existingSpan.crosses(ySpan)) {
                    crosses = true;
                    break;
                }
            }
            if (crosses) {
                verticalRunX = verticalRunX + indexSpacer;
                key = new Integer(verticalRunX / pointsPerLine);
            } else {
                break;
            }
        }

        if (verticalRuns.containsKey(key)) {
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            entries.put(ySpan, destinationPt);
        } else {
            Map<MinAndMaxPoint, Point> entries = new HashMap<MinAndMaxPoint, Point>();
            entries.put(ySpan, destinationPt);
            verticalRuns.put(key, entries);
        }
        polyline.moveTo(originPt.x, originPt.y);
        polyline.lineTo(verticalRunX, originPt.y);
        polyline.lineTo(verticalRunX, destinationPt.y);
        polyline.lineTo(destinationPt.x, destinationPt.y);

        g2.draw(polyline);
        g2.setStroke(new BasicStroke(1.0f));
        int arrowLength = 5;
        int arrowWidth = 5;
        polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        polyline.moveTo(destinationPt.x - 2, destinationPt.y);
        polyline.lineTo(destinationPt.x + arrowLength, destinationPt.y - arrowWidth);
        polyline.lineTo(destinationPt.x + arrowWidth, destinationPt.y);
        polyline.lineTo(destinationPt.x + arrowLength, destinationPt.y + arrowWidth);
        polyline.lineTo(destinationPt.x - 2, destinationPt.y);
        g2.fill(polyline);

        g2.setStroke(origStroke);
    }

    /**
     * @param g2
     * @param verticalRuns
     * @param originPt
     * @param destinationPt
     */
    private void drawBranchAbove(Graphics2D g2, Map<Integer, Map<MinAndMaxPoint, Point>> verticalRuns, Point originPt,
            Point destinationPt) {
        Stroke origStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));
        if (colorForDebug) {
            g2.setColor(Color.GREEN);
        }

        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);

        int verticalRunX = Math.max(originPt.x - indexSpacer, destinationPt.x - indexSpacer);
        int minY = Math.min(originPt.y, destinationPt.y);
        int maxY = Math.max(originPt.y, destinationPt.y);
        int pointsPerLine = 6;
        MinAndMaxPoint ySpan = new MinAndMaxPoint(minY, maxY);
        Integer key = new Integer(verticalRunX / pointsPerLine);
        while (verticalRuns.containsKey(key)) {
            // will they cross?
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            boolean crosses = false;
            for (Map.Entry<MinAndMaxPoint, Point> entry : entries.entrySet()) {
                MinAndMaxPoint existingSpan = entry.getKey();
                Point existingDest = entry.getValue();
                if (existingDest.equals(destinationPt)) {
                    break;
                } else if (existingSpan.crosses(ySpan)) {
                    crosses = true;
                    break;
                }
            }
            if (crosses) {
                verticalRunX = verticalRunX - indexSpacer;
                key = new Integer(verticalRunX / pointsPerLine);
            } else {
                break;
            }
        }

        if (verticalRuns.containsKey(key)) {
            Map<MinAndMaxPoint, Point> entries = verticalRuns.get(key);
            entries.put(ySpan, destinationPt);
        } else {
            Map<MinAndMaxPoint, Point> entries = new HashMap<MinAndMaxPoint, Point>();
            entries.put(ySpan, originPt);
            verticalRuns.put(key, entries);
        }
        polyline.moveTo(originPt.x, originPt.y);
        polyline.lineTo(verticalRunX, originPt.y);
        polyline.lineTo(verticalRunX, destinationPt.y);
        polyline.lineTo(destinationPt.x, destinationPt.y);

        g2.draw(polyline);
        g2.setStroke(new BasicStroke(1.0f));
        int arrowLength = 5;
        int arrowWidth = 5;
        polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        polyline.moveTo(destinationPt.x - 2, destinationPt.y);
        polyline.lineTo(destinationPt.x + arrowLength, destinationPt.y - arrowWidth);
        polyline.lineTo(destinationPt.x + arrowWidth, destinationPt.y);
        polyline.lineTo(destinationPt.x + arrowLength, destinationPt.y + arrowWidth);
        polyline.lineTo(destinationPt.x - 2, destinationPt.y);
        g2.fill(polyline);

        g2.setStroke(origStroke);
    }

    /**
     * @param g2
     * @param originTask
     * @param branchItr
     */
    private void invalidateBranchRegions(TaskPanel originTask, TaskPanel destinationTask) {
        int x = Math.min(originTask.getX(), destinationTask.getX());
        int y = Math.min(originTask.getY(), destinationTask.getY());
        int w = Math.max(originTask.getX() + originTask.getWidth() + 50, destinationTask.getX()
            + destinationTask.getWidth() + 50);
        int h = Math.max(originTask.getY() + originTask.getHeight() + 50, destinationTask.getY()
            + destinationTask.getHeight() + 50);

        this.repaint(x, y, w, h);

    }

    /**
     * @return
     */
    public int getAcceptableActions() {
        return dndBean.getAcceptableActions();
    }

    /**
     * @param highlight
     */
    public void highlightForDrag(boolean highlight) {
        dndBean.highlightForDrag(highlight);
        this.invalidate();
    }

    /**
     * @param highlight
     */
    public void highlightForDrop(boolean highlight) {
        dndBean.highlightForDrop(highlight);
        this.invalidate();
    }

    /**
     * @return
     */
    public boolean isDragging() {
        return dndBean.isDragging();
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#getImportDataFlavors()
     */
    public DataFlavor[] getImportDataFlavors() {
        List<DataFlavor> taskflavors = Arrays.asList(TaskTransferable.getImportFlavors());
        List<DataFlavor> dataflavors = Arrays.asList(DataContainerTransferable.getImportFlavors());
        ArrayList<DataFlavor> flavors = new ArrayList<DataFlavor>(taskflavors);
        flavors.addAll(dataflavors);
        return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForImport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForImport(DataFlavor flavor) {
        if (TaskTransferable.isFlavorSupported(flavor, TaskTransferable.getImportFlavors())) {
            return true;
        }
        return DataContainerTransferable.isFlavorSupported(flavor, TaskTransferable.getImportFlavors());
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForExport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForExport(DataFlavor flavor) {
        if (TaskTransferable.isFlavorSupported(flavor, TaskTransferable.getExportFlavors())) {
            return true;
        }
        return DataContainerTransferable.isFlavorSupported(flavor, TaskTransferable.getExportFlavors());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object obj, DropTargetDropEvent ev) {
        setDroppedObject(obj, ev.getLocation());
    }

    /**
     * @param obj
     * @param location
     */
    private void setDroppedObject(Object obj, Point location) {
        try {
            if (I_DefineTask.class.isAssignableFrom(obj.getClass())) {
                I_DefineTask task = (I_DefineTask) obj;
                this.process.addTask(task);
                TaskPanel tp = addTaskPanel(task, doubleClickHandler);
                tp.setLocation(location);
                this.process.setTaskBounds(tp.getId(), tp.getBounds());
            }
            updatePreferredSize();
            notifyTaskAdded();
        } catch (PropertyVetoException e) {
            System.out.println(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object obj) {
        setDroppedObject(obj, new Point(30, 30));
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getTaskToDrop()
     */
    public I_DefineTask getTaskToDrop() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getJComponent()
     */
    public JComponent getJComponent() {
        return this;
    }

    public Transferable getTransferable() throws ClassNotFoundException {
        return new TaskTransferable(this.getTaskToDrop());
    }

    /**
     * @param task
     * @return
     * @throws ClassNotFoundException
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private TaskPanel addTaskPanel(I_DefineTask task, I_HandleDoubleClickInTaskProcess doubleClickHandler)
            throws ClassNotFoundException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        TaskPanel tp = new TaskPanel(task, true, false, this.process, null, worker, doubleClickHandler);
        tp.addPropertyChangeListener("branch", this);
        tp.addPropertyChangeListener("taskLocation", this);
        this.taskPanels.put(new Integer(tp.getId()), tp);
        if (tp.getLocation().equals(new Point(0, 0))) {
            if (this.taskPanels.get(new Integer(tp.getId() - 1)) != null) {
                TaskPanel prevPanel = this.taskPanels.get(new Integer(tp.getId() - 1));
                Rectangle position = prevPanel.getBounds();
                tp.setLocation(position.x, position.y + position.height + 4);
            }
        }
        this.add(tp);
        tp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        tp.setSize(tp.getPreferredSize());
        BeanInfo taskInfo = task.getBeanInfo();
        PropertyDescriptor[] properties = taskInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            task.addPropertyChangeListener(properties[i].getName(), this);
        }
        return tp;
    }

    /**
     *  
     */
    private void updatePreferredSize() {
        int maxX = 0;
        int maxY = 0;
        Iterator<TaskPanel> panelItr = this.taskPanels.values().iterator();
        while (panelItr.hasNext()) {
            TaskPanel tp = panelItr.next();
            maxX = Math.max(maxX, tp.getX() + tp.getWidth());
            maxY = Math.max(maxY, tp.getY() + tp.getHeight());
        }
        maxX = maxX + 40;
        maxY = maxY + 40;
        this.setMinimumSize(new Dimension(200, 200));
        this.setPreferredSize(new Dimension(maxX, maxY));
        this.revalidate();
    }

    public void mouseMoved(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        if ((this.draggedComponent != null) && (e.isShiftDown())) {
            if (TaskPanel.class.isAssignableFrom(this.draggedComponent.getClass()))
                ;
            TaskPanel tp = (TaskPanel) this.draggedComponent;
            tp.setLocation(e.getX() - offset.x, e.getY() - offset.y);
            this.process.setTaskBounds(tp.getId(), tp.getBounds());
            this.updatePreferredSize();
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        Component c = this.getComponentAt(e.getPoint());
        if ((c != null) && (c != this)) {
            this.draggedComponent = c;
            this.offset = new Point(e.getX() - c.getX(), e.getY() - c.getY());
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        if (this.draggedComponent != null) {
            updatePreferredSize();
        }
        this.draggedComponent = null;
        this.offset = null;
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ProcessDiagramPanel got property change: " + evt.getPropertyName());
        }
        if (evt.getPropertyName().equals("branch")) {
            this.repaint();
        } else if (evt.getPropertyName().equals("taskLocation")) {
            TaskPanel tp = (TaskPanel) evt.getSource();
            repaintConnectors(tp);

            this.updatePreferredSize();
        } else if (evt.getPropertyName().equals("currentTaskId")) {
            TaskPanel oldCurrentTaskPanel = this.taskPanels.get(evt.getOldValue());
            TaskPanel currentTaskPanel = this.taskPanels.get(evt.getNewValue());
            if (oldCurrentTaskPanel != null) {
                oldCurrentTaskPanel.setCurrentTask(false);
            }
            if (currentTaskPanel != null) {
                currentTaskPanel.setCurrentTask(true);
            }

        } else if (evt.getPropertyName().endsWith("Id")) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("An id changed. Repainting. ");
            }
            this.repaint();
        } else if (evt.getPropertyName().equals("queueType")) {
            // No need to do anything with this property change.
        } else if (evt.getPropertyName().equals("relativeTimeInMins")) {
            // No need to do anything with this property change.
        } else if (evt.getPropertyName().equals("visible")) {
            // No need to do anything with this property change.
        } else if (evt.getPropertyName().equals("lastTaskRemoved")) {
            try {
                I_DefineTask task = (I_DefineTask) evt.getNewValue();
                this.taskPanels.remove(new Integer(task.getId()));
                layoutTasks(this.worker);
                this.repaint();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } else if (evt.getPropertyName().equals("lastTaskAdded")) {
            // No need to do anything with this property change.
        } else {
            logger.info("Don't know what to do with change: " + evt.getPropertyName());
        }

    }

    /**
     * @param tp
     */
    private void repaintConnectors(TaskPanel tp) {
        // Find size of rectangle to invalidate.
        int movedTask = tp.getTask().getId();
        for (Iterator<Branch> branchItr = this.process.getBranches(tp.getTask()).iterator(); branchItr.hasNext();) {
            I_Branch branch = branchItr.next();
            TaskPanel destinationTask = this.taskPanels.get(new Integer(branch.getDestinationId()));
            // System.out.println("Invalidating sourceId: " + movedTask + ", " +
            // branch.getDestinationId());
            invalidateBranchRegions(tp, destinationTask);
        }
        // Find all the tasks that branch to tp...
        Iterator<TaskPanel> taskPanelItr = this.taskPanels.values().iterator();
        while (taskPanelItr.hasNext()) {
            TaskPanel aTask = taskPanelItr.next();
            for (Iterator<Branch> branchItr = this.process.getBranches(aTask.getTask()).iterator(); branchItr.hasNext();) {
                I_Branch branch = branchItr.next();
                TaskPanel destinationTask = this.taskPanels.get(new Integer(branch.getDestinationId()));
                if (destinationTask.getTask().getId() == movedTask) {
                    // System.out.println("Invalidating destination: " +
                    // aTask.getTask().getId() + ", " +
                    // destinationTask.getTask().getId());
                    invalidateBranchRegions(aTask, destinationTask);
                }
            }
        }
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getLocalFlavors()
     */
    public Collection<DataFlavor> getLocalFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(TaskTransferable.getLocalTaskFlavor());
        flavorList.add(DataContainerTransferable.getLocalDataFlavor());
        return flavorList;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getSerialFlavors()
     */
    public Collection<DataFlavor> getSerialFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(TaskTransferable.getSerialTaskFlavor());
        flavorList.add(DataContainerTransferable.getSerialDataFlavor());
        return flavorList;
    }

    public void addTaskAddedActionListener(ActionListener l) {
        taskAddedActionListeners.add(l);
    }

    public void removeTaskAddedActionListener(ActionListener l) {
        taskAddedActionListeners.remove(l);
    }

    private void notifyTaskAdded() {
        Iterator<ActionListener> listenerItr = this.taskAddedActionListeners.iterator();
        ActionEvent evt = new ActionEvent(this, 1, "taskAdded");
        while (listenerItr.hasNext()) {
            ActionListener l = listenerItr.next();
            l.actionPerformed(evt);
        }
    }

    public Logger getLogger() {
        return logger;
    }

}
