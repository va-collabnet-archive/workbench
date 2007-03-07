/*
 * Created on Mar 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dwfa.bpa.process.I_DefineTask;


public class TaskTransferable implements Transferable {
    @SuppressWarnings("unused")
	private static boolean initialized = initStaticFields();
    private I_DefineTask task;

    private static DataFlavor stringFlavor;

    private static DataFlavor localTaskFlavor;

    private static DataFlavor serialTaskFlavor;

    private static Set<DataFlavor> exportFlavors;

    private static Set<DataFlavor> importFlavors;

    /**
     * @return Returns the localTaskFlavor.
     */
    public static DataFlavor getLocalTaskFlavor() {
        return localTaskFlavor;
    }

    /**
     * @return Returns the serialTaskFlavor.
     */
    public static DataFlavor getSerialTaskFlavor() {
        return serialTaskFlavor;
    }

    public TaskTransferable(I_DefineTask task)
            throws ClassNotFoundException {
        this.task = task;
     }

    public static DataFlavor[] getImportFlavors() {
        return new DataFlavor[] {
                TaskTransferable.localTaskFlavor,
                TaskTransferable.serialTaskFlavor };

    }

    public static DataFlavor[] getExportFlavors() {
        return new DataFlavor[] {
                TaskTransferable.localTaskFlavor,
                TaskTransferable.serialTaskFlavor,
                TaskTransferable.stringFlavor };

    }
    /**
     * @param flavor
     * @param supportedFlavors
     * @return
     */
    public static boolean isFlavorSupported(DataFlavor flavor, DataFlavor[] supportedFlavors) {
        for (int i = 0; i < supportedFlavors.length; i++) {
            if (supportedFlavors[i].getPrimaryType()
                    .equals(flavor.getPrimaryType())
                    && supportedFlavors[i].getSubType().equals(flavor.getSubType())
                    && flavor.getRepresentationClass().isAssignableFrom(
                            supportedFlavors[i].getRepresentationClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     *  
     */
    private static boolean initStaticFields() {
        if (exportFlavors == null) {
            TaskTransferable.exportFlavors = new HashSet<DataFlavor>();
            TaskTransferable.importFlavors = new HashSet<DataFlavor>();
            serialTaskFlavor = new DataFlavor(
                    TaskInputStream.class, "Serial I_DefineTask");

            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                        + ";class=" + I_DefineTask.class.getName();
                localTaskFlavor = new DataFlavor(mimeType);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            stringFlavor = DataFlavor.stringFlavor;
            TaskTransferable.importFlavors
                    .add(localTaskFlavor);
            TaskTransferable.importFlavors
                    .add(serialTaskFlavor);
            TaskTransferable.exportFlavors
                    .add(localTaskFlavor);
            TaskTransferable.exportFlavors
                    .add(serialTaskFlavor);
            TaskTransferable.exportFlavors.add(stringFlavor);
        }
        return true;
    }

    /**
     * <em>The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).</em>
     * 
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {
                TaskTransferable.localTaskFlavor,
                TaskTransferable.serialTaskFlavor,
                TaskTransferable.stringFlavor };
        //return new DataFlavor[] {this.localTaskFlavor};
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        /*System.out.println("isDataFlavorSupported: " + flavor.getMimeType()
                + " " + flavor.getHumanPresentableName());*/
        for (Iterator<DataFlavor> i = exportFlavors.iterator(); i.hasNext();) {
            DataFlavor supportedFlavor = i.next();
            if (supportedFlavor.getPrimaryType()
                    .equals(flavor.getPrimaryType())
                    && supportedFlavor.getSubType().equals(flavor.getSubType())
                    && flavor.getRepresentationClass().isAssignableFrom(
                            supportedFlavor.getRepresentationClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        /*System.out.println(" # getTransferData "
                + flavor.getHumanPresentableName());*/
        if (this.isDataFlavorSupported(flavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        if (flavor.equals(stringFlavor)) {
            return this.task.toString();
        } else if (flavor.equals(serialTaskFlavor)) {
            return TaskInputStream.create(this.task);
        }
        return this.task;
    }

}