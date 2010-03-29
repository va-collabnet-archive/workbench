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
 * Created on Feb 25, 2005
 */
package org.dwfa.jini;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.entry.Name;
import net.jini.lookup.entry.ServiceInfo;

import com.sun.jini.lookup.entry.BasicServiceType;

/**
 * @author kec
 * 
 */
public class ServiceListCellRenderer extends JLabel implements ListCellRenderer, Comparator<ServiceItem> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected static Border noFocusBorder;

    /**
	 *  
	 */
    public ServiceListCellRenderer() {
        super();
        if (noFocusBorder == null) {
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }
        this.setOpaque(true);
        setBorder(noFocusBorder);
    }

    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        ServiceItem service = (ServiceItem) value;
        this.setText(getText(service));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
        return this;
    }

    /**
     * @param service
     * @return
     */
    private String getText(ServiceItem service) {
        StringBuffer textBuff = new StringBuffer();
        List<Entry> serviceAttributes = new ArrayList<Entry>();
        serviceAttributes.addAll(Arrays.asList(service.attributeSets));
        textBuff.append("<html>");
        boolean addBreak = false;
        boolean userName = false;
        boolean electronicAddress = false;
        for (ListIterator<Entry> itr = serviceAttributes.listIterator(); itr.hasNext();) {
            Entry entry = itr.next();
            if (Name.class.isAssignableFrom(entry.getClass())) {
                Name name = (Name) entry;
                textBuff.append("<font color='blue'><u>");
                textBuff.append(name.name);
                textBuff.append("</u>");
                textBuff.append("</font>");
                itr.remove();
                addBreak = true;
                userName = true;
            }
        }
        for (ListIterator<Entry> itr = serviceAttributes.listIterator(); itr.hasNext();) {
            Entry entry = itr.next();
            if (ElectronicAddress.class.isAssignableFrom(entry.getClass())) {
                ElectronicAddress eAddress = (ElectronicAddress) entry;
                if (addBreak) {
                    textBuff.append("<p>");
                    addBreak = false;
                }
                textBuff.append("<font color='green'>");
                textBuff.append(eAddress.address);
                textBuff.append("</font>");
                itr.remove();
                addBreak = true;
                electronicAddress = true;
            }
        }
        if (userName == false && electronicAddress == false) {
            boolean serviceInfo = false;
            for (ListIterator<Entry> itr = serviceAttributes.listIterator(); itr.hasNext();) {
                Entry entry = itr.next();
                if (ServiceInfo.class.isAssignableFrom(entry.getClass())) {
                    ServiceInfo info = (ServiceInfo) entry;
                    if (addBreak) {
                        textBuff.append("<p>");
                        addBreak = false;
                    }
                    textBuff.append("<font color='blue'><u>");
                    textBuff.append(info.name);
                    textBuff.append("</u><p><font color='green'>");
                    textBuff.append(info.manufacturer);
                    textBuff.append("</font><p><font color='red'>Version: ");
                    textBuff.append(info.version);
                    textBuff.append("</font>");
                    itr.remove();
                    addBreak = true;
                    serviceInfo = true;
                }

            }
            for (ListIterator<Entry> itr = serviceAttributes.listIterator(); itr.hasNext();) {
                Entry entry = itr.next();
                if (BasicServiceType.class.isAssignableFrom(entry.getClass())) {
                    BasicServiceType serviceType = (BasicServiceType) entry;
                    if (serviceInfo == false) {
                        if (addBreak) {
                            textBuff.append("<p>");
                            addBreak = false;
                        }
                        textBuff.append("Service Type: " + serviceType.type);
                        addBreak = true;
                    }
                } else {
                    if (addBreak) {
                        textBuff.append("<p>");
                        addBreak = false;
                    }
                    textBuff.append(entry);
                    addBreak = true;
                }
            }
            if (service.attributeSets.length == 0) {
                textBuff.append(service.service.getClass().getName());
            }
        }
        return textBuff.toString();
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void validate() {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName == "text")
            super.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note </a> for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    public int compare(ServiceItem s1, ServiceItem s2) {
        return this.getText(s1).toLowerCase().compareTo(this.getText(s2).toLowerCase());
    }

}
