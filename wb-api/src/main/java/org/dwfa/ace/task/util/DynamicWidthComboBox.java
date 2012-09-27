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
package org.dwfa.ace.task.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * This class extends JComboBox by allowing the popup size to be configured
 * dynamically. By default the width of the
 * popup is the same size as the JComboBox. However, in this implementation the
 * width of the popup is calculated based
 * on the combo box contents - the width is set to that of the largest item
 * contained in the drop-down list.
 * Based on code : http://forums.sun.com/thread.jspa?threadID=5386868
 * 
 * @author Christine Hill
 * 
 */
public class DynamicWidthComboBox extends JComboBox {

    private static final long serialVersionUID = 1L;

    public DynamicWidthComboBox() {
        super();
        setUI(new DynamicWidthComboBoxUI());
    }

    public DynamicWidthComboBox(Object[] array) {
        super(array);
        setUI(new DynamicWidthComboBoxUI());
    }

    private class DynamicWidthComboBoxUI extends BasicComboBoxUI {
        private int padding = 10;

        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {

                private static final long serialVersionUID = 1L;

                public void show() {
                    int widestWidth = getWidestItemWidth();
                    if (widestWidth < comboBox.getPreferredSize().width) {
                        widestWidth = comboBox.getPreferredSize().width;
                    }

                    Dimension popupSize = comboBox.getSize();
                    popupSize.setSize(widestWidth + (2 * padding),
                        getPopupHeightForRowCount(comboBox.getMaximumRowCount()));

                    Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width,
                        popupSize.height);

                    scroller.setMaximumSize(popupBounds.getSize());
                    scroller.setPreferredSize(popupBounds.getSize());
                    scroller.setMinimumSize(popupBounds.getSize());

                    list.invalidate();

                    int selectedIndex = comboBox.getSelectedIndex();
                    if (selectedIndex == -1) {
                        list.clearSelection();
                    } else {
                        list.setSelectedIndex(selectedIndex);
                    }

                    list.ensureIndexIsVisible(list.getSelectedIndex());
                    setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
                    show(comboBox, popupBounds.x, popupBounds.y);
                }
            };

            popup.getAccessibleContext().setAccessibleParent(comboBox);

            return popup;

        }

        public int getWidestItemWidth() {
            int numItems = comboBox.getItemCount();
            Font font = comboBox.getFont();
            FontMetrics metrics = comboBox.getFontMetrics(font);
            int widest = 0;

            for (int i = 0; i < numItems; i++) {
                Object item = comboBox.getItemAt(i);
                int lineWidth = metrics.stringWidth(item.toString());
                widest = Math.max(widest, lineWidth);
            }

            return widest;
        }
    }
}
