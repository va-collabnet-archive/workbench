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
 * Created on Jun 8, 2005
 */
package org.dwfa.bpa.gui;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.PropertySpec;

public class PropertyTableModel extends AbstractTableModel {
    /**
	 *  
	 */
    private static final long serialVersionUID = 1L;

    private String[] columnNames = { "Task/Property", "Export" };

    private Vector<PropertyDescriptorWithTarget> rowData = new Vector<PropertyDescriptorWithTarget>();

    private I_EncodeBusinessProcess process;

    /**
     * @throws RemoteException
     * @throws IOException
     * @throws IntrospectionException
     * 
     */
    public PropertyTableModel(I_EncodeBusinessProcess process) throws RemoteException, IOException,
            IntrospectionException {
        super();
        this.process = process;
        PropertyDescriptorWithTarget properties[] = (PropertyDescriptorWithTarget[]) this.process.getAllPropertiesBeanInfo()
            .getPropertyDescriptors();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                this.rowData.add(properties[i]);
            }
        }

    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
        return rowData.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col) {
        PropertyDescriptorWithTarget property = (PropertyDescriptorWithTarget) this.rowData.get(row);
        switch (col) {
        case 0:
            return property.getLabel();
        case 1:
            PropertySpec spec = property.getSpec();
            return new Boolean(this.process.isPropertyExternal(spec));
        }
        throw new UnsupportedOperationException("don't know how to get value for col: " + col);
    }

    public boolean isCellEditable(int row, int col) {
        if (col == 1) {
            return true;
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        PropertyDescriptorWithTarget property = (PropertyDescriptorWithTarget) this.rowData.get(row);
        switch (col) {
        case 1:
            PropertySpec spec = property.getSpec();
            Boolean external = (Boolean) value;
            this.process.setPropertyExternal(spec, external.booleanValue());
            break;
        default:
            throw new UnsupportedOperationException("Cannot edit col: " + col);

        }
        fireTableCellUpdated(row, col);
    }

    public PropertyDescriptorWithTarget getPropertyDescriptor(int row) {
        return rowData.get(row);
    }

    public Class<?> getColumnClass(int c) {
        switch (c) {
        case 0:
            return String.class;
        case 1:
            return Boolean.class;
        }
        return String.class;
    }

    /**
     * @return Returns the process.
     */
    public I_EncodeBusinessProcess getProcess() {
        return process;
    }
}
