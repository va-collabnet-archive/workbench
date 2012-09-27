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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.tapi.NoMappingException;

import sun.awt.dnd.SunDragSourceContextPeer;

public class BpaGestureListener implements DragGestureListener {

    private static Logger logger = Logger.getLogger(BpaGestureListener.class.getName());
    private I_DoDragAndDrop sourceComponent;

    private DragSourceListener dragSource;

    /**
     * @param dragSource
     * @param sourceComponent
     *            TODO
     */
    public BpaGestureListener(I_DoDragAndDrop sourceComponent, DragSourceListener dragSource) {
        super();
        this.sourceComponent = sourceComponent;
        this.dragSource = dragSource;
    }

    /**
     * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
     */
    public void dragGestureRecognized(DragGestureEvent ev) {
        logger.fine("BpaGestureListener dragGestureRecognized" + ev);
        try {
            try {
                if (this.sourceComponent.isDragging()) {
                    return;
                }
                Transferable transferable = this.sourceComponent.getTransferable();
                String transferableString;
                if (TaskTransferable.class.isAssignableFrom(transferable.getClass())) {
                    I_DefineTask task = (I_DefineTask) transferable.getTransferData(TaskTransferable.getLocalTaskFlavor());
                    try {
                        BeanInfo info = task.getBeanInfo();
                        transferableString = info.getBeanDescriptor().getDisplayName();
                    } catch (IntrospectionException e1) {
                        logger.log(Level.WARNING, e1.getMessage(), e1);
                        transferableString = task.getName();
                    }
                } else if (DataContainerTransferable.class.isAssignableFrom(transferable.getClass())) {
                    I_ContainData data = (I_ContainData) transferable.getTransferData(DataContainerTransferable.getLocalDataFlavor());
                    transferableString = data.getDescription();
                } else {
                    transferableString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                }

                Image dragImage = this.getDragImage(transferableString);
                Point imageOffset = new Point(0, 0);
                try {
                    ev.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, transferable, dragSource);
                } catch (InvalidDnDOperationException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    logger.log(Level.INFO, "Resetting SunDragSourceContextPeer [2]");
                    SunDragSourceContextPeer.setDragDropInProgress(false);
                }
            } catch (UnsupportedFlavorException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }

        } catch (InvalidDnDOperationException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            SunDragSourceContextPeer.setDragDropInProgress(false);
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    /**
     * @return
     * @throws IOException
     * @throws UnsupportedFlavorException
     * @throws InvalidComponentException
     * @throws ValidationException
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws NoMappingException
     * @throws RemoteException
     */
    public Image getDragImage(String transferableString) throws UnsupportedFlavorException, IOException,
            RemoteException {
        JLabel component = new JLabel(transferableString);
        component.setBorder(BorderFactory.createLineBorder(Color.black));
        Image dragImage = sourceComponent.createImage(component.getPreferredSize().width,
            component.getPreferredSize().height);
        component.setVisible(true);
        component.setBounds(0, 0, component.getPreferredSize().width, component.getPreferredSize().height);
        Graphics og = dragImage.getGraphics();
        og.setClip(0, 0, component.getPreferredSize().width, component.getPreferredSize().height);
        component.paint(og);
        og.dispose();

        ImageFilter filter = new TransparencyFilter();
        FilteredImageSource filteredSrc = new FilteredImageSource(dragImage.getSource(), filter);

        // Create the filtered image
        dragImage = Toolkit.getDefaultToolkit().createImage(filteredSrc);
        return dragImage;
    }

    private class TransparencyFilter extends RGBImageFilter {
        public TransparencyFilter() {
            // When this is set to true, the filter will work with images
            // whose pixels are indices into a color table (IndexColorModel).
            // In such a case, the color values in the color table are filtered.
            canFilterIndexColorModel = true;
        }

        // This method is called for every pixel in the image
        public int filterRGB(int x, int y, int rgb) {
            if (x == -1) {
                // The pixel value is from the image's color table rather than
                // the image itself
            }
            return rgb & 0xAAFFFFFF;
        }
    }
}
