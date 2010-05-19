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
package org.dwfa.bpa.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dwfa.bpa.process.I_ContainData;

public class DataContainerTransferable implements Transferable {
    @SuppressWarnings("unused")
    private static boolean initialized = initStaticFields();

    private I_ContainData data;

    private static DataFlavor localDataFlavor;

    private static DataFlavor serialDataFlavor;

    private static Set<DataFlavor> exportFlavors;

    private static Set<DataFlavor> importFlavors;

    /**
     * @return Returns the localDataFlavor.
     */
    public static DataFlavor getLocalDataFlavor() {
        return localDataFlavor;
    }

    /**
     * @return Returns the serialDataFlavor.
     */
    public static DataFlavor getSerialDataFlavor() {
        return serialDataFlavor;
    }

    public DataContainerTransferable(I_ContainData data) throws ClassNotFoundException {
        this.data = data;
    }

    public static DataFlavor[] getImportFlavors() {
        return new DataFlavor[] { DataContainerTransferable.localDataFlavor, DataContainerTransferable.serialDataFlavor };

    }

    public static DataFlavor[] getExportFlavors() {
        return new DataFlavor[] { DataContainerTransferable.localDataFlavor, DataContainerTransferable.serialDataFlavor };

    }

    /**
     * @param flavor
     * @param supportedFlavors
     * @return
     */
    public static boolean isFlavorSupported(DataFlavor flavor, DataFlavor[] supportedFlavors) {
        for (int i = 0; i < supportedFlavors.length; i++) {
            if (supportedFlavors[i].getPrimaryType().equals(flavor.getPrimaryType())
                && supportedFlavors[i].getSubType().equals(flavor.getSubType())
                && flavor.getRepresentationClass().isAssignableFrom(supportedFlavors[i].getRepresentationClass())) {
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
            DataContainerTransferable.exportFlavors = new HashSet<DataFlavor>();
            DataContainerTransferable.importFlavors = new HashSet<DataFlavor>();
            serialDataFlavor = new DataFlavor(TaskInputStream.class, "Serial I_ContainData");

            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + I_ContainData.class.getName();
                localDataFlavor = new DataFlavor(mimeType);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            DataContainerTransferable.importFlavors.add(localDataFlavor);
            DataContainerTransferable.importFlavors.add(serialDataFlavor);
            DataContainerTransferable.exportFlavors.add(localDataFlavor);
            DataContainerTransferable.exportFlavors.add(serialDataFlavor);
        }
        return false;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_TransferData#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataContainerTransferable.localDataFlavor, DataContainerTransferable.serialDataFlavor };
        // return new DataFlavor[] {this.localDataFlavor};
    }

    /**
     * @see org.dwfa.bpa.dnd.I_TransferData#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        /*
         * System.out.println("isDataFlavorSupported: " + flavor.getMimeType()
         * + " " + flavor.getHumanPresentableName());
         */
        if (flavor == null) {
            return false;
        }
        for (Iterator<DataFlavor> i = exportFlavors.iterator(); i.hasNext();) {
            DataFlavor supportedFlavor = i.next();
            if (supportedFlavor.getPrimaryType().equals(flavor.getPrimaryType())
                && supportedFlavor.getSubType().equals(flavor.getSubType())
                && flavor.getRepresentationClass().isAssignableFrom(supportedFlavor.getRepresentationClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_TransferData#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        /*
         * System.out.println(" # getTransferData "
         * + flavor.getHumanPresentableName());
         */
        if (this.isDataFlavorSupported(flavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        if (flavor.equals(serialDataFlavor)) {
            return DataContainerInputStream.create(this.data);
        }
        return this.data;
    }

}
