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
 * Created on Mar 21, 2005
 */
package org.dwfa.jini;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;

/*
 * ListDialog.java is a 1.4 class meant to be used by programs such as
 * ListDialogRunner. It requires no additional files.
 */

/**
 * Use this modal dialog to let the user choose one string from a long
 * list. See ListDialogRunner.java for an example of using ListDialog.
 * The basics:
 * 
 * <pre>
 * String[] choices = { &quot;A&quot;, &quot;long&quot;, &quot;array&quot;, &quot;of&quot;, &quot;strings&quot; };
 * String selectedName = ListDialog.showDialog(componentInControllingFrame, locatorComponent,
 *     &quot;A description of the list:&quot;, &quot;Dialog Title&quot;, choices, choices[0]);
 * </pre>
 */
public class SelectServiceDialog extends JDialog implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static SelectServiceDialog dialog;
    private static ServiceItem value = null;
    private JList list;

    /**
     * Set up and show the dialog. The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     */
    public static ServiceItem showDialog(Component frameComp, Component locationComp, String labelText, String title,
            ServiceItem[] possibleValues, ServiceItem initialValue, ServiceItem longValue) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new SelectServiceDialog(frame, locationComp, labelText, title, possibleValues, initialValue, longValue);
        dialog.setVisible(true);
        return value;
    }

    private void setValue(ServiceItem newValue) {
        if (newValue == null) {
            newValue = (ServiceItem) list.getModel().getElementAt(0);
        }
        value = newValue;
        list.setSelectedValue(value, true);
    }

    private SelectServiceDialog(Frame frame, Component locationComp, String labelText, String title,
            ServiceItem[] data, ServiceItem initialValue, ServiceItem longValue) {
        super(frame, title, true);

        // Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        //
        final JButton setButton = new JButton("Set");
        setButton.setActionCommand("Set");
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);

        // Sort out services with duplicate service IDs
        Set<ServiceID> serviceIds = new HashSet<ServiceID>();
        Arrays.sort(data, new ServiceListCellRenderer());
        java.util.List<ServiceItem> serviceList = new ArrayList<ServiceItem>();
        for (int i = 0; i < data.length; i++) {
            if (serviceIds.contains(data[i].serviceID)) {
                // do nothing, already have the service.
            } else {
                serviceIds.add(data[i].serviceID);
                serviceList.add(data[i]);
            }
        }

        // main part of the dialog
        list = new JList(serviceList.toArray()) {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            // Subclass JList to workaround bug 4832765, which can cause the
            // scroll pane to not let the user easily scroll up to the beginning
            // of the list. An alternative would be to set the unitIncrement
            // of the JScrollBar to a fixed value. You wouldn't get the nice
            // aligned scrolling, but it should work.
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL && direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0)) {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
            }
        };

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setCellRenderer(new ServiceListCellRenderer());
        if (longValue != null) {
            list.setPrototypeCellValue(longValue); // get extra space
        }
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setButton.doClick(); // emulate button click
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(450, 500));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        // Create a container so that we can add a title around
        // the scroll pane. Can't add a title directly to the
        // scroll pane because its background would be white.
        // Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        // Initialize values.
        setValue(initialValue);
        pack();
        setLocationRelativeTo(locationComp);
    }

    // Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) {
        if ("Set".equals(e.getActionCommand())) {
            SelectServiceDialog.value = (ServiceItem) (list.getSelectedValue());
        } else {
            SelectServiceDialog.value = null;
        }
        SelectServiceDialog.dialog.setVisible(false);
    }
}
