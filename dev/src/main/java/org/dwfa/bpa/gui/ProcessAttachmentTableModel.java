/*
 * Created on Mar 6, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;

public class ProcessAttachmentTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ProcessAttachmentTableModel.class
            .getName());

    private I_EncodeBusinessProcess process;

    public static final int CLASS = 0;

    public static final int NAME = 1;

    public static final int VALUE = 2;

    public static final int SIZE = 3;

   private String[] columnNames = { "class", "key name", "attachment value", "size" };

    public ProcessAttachmentTableModel(I_EncodeBusinessProcess process) {
        super();
        this.process = process;
    }

    public int getRowCount() {
        return process.getAttachmentKeys().size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        int size = process.getAttachmentKeys().size();
        String[] keys = process.getAttachmentKeys().toArray(new String[size]);
        Object attachment = process.readAttachement(keys[rowIndex]);
        switch (columnIndex) {

        case NAME:
            return keys[rowIndex];
        case SIZE:
            int objSize = getObjectSize(attachment);
            if (objSize < 1000) {
                return objSize + " b";
            } else if (objSize < 1000000) {
                return objSize / 1000 + " kb";
            } else {
                return objSize / 1000000 + " mb";
            }

        case VALUE:
            if (attachment == null) {
                return "null value";
            }
            if (attachment.toString().length() < 20) {
                return attachment.toString();
            }
            return "<html><font color='green'>Use popup to view";
        case CLASS:
            if (attachment == null) {
                return "null class";
            }
            return attachment.getClass().getSimpleName();
        }
        return null;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    public boolean isCellEditable(int row, int col) {
        if (col == NAME) {
            return true;
        }
        if (col == VALUE) {
            String name = (String) this.getValueAt(row, NAME);
            Object obj = this.process.readAttachement(name);
            try {
                if (obj == null) {
                    return false;
                }
                obj.getClass().getConstructor(new Class[] { String.class });
                return true;
            } catch (SecurityException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Not editable: " + obj);
                }
                // No worries. Editable is false...
                return false;
            }
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == NAME) {
            String oldName = (String) this.getValueAt(row, NAME);
            String newName = (String) value;
            try {
				this.process.renameAttachment(oldName, newName);
			} catch (PropertyVetoException e) {
				//rename failed, nothing to do. ;
			}
        } else if (col == VALUE) {
            String name = (String) this.getValueAt(row, NAME);
            Object obj = this.process.takeAttachment(name);
            try {
                Constructor<?> c = obj.getClass().getConstructor(new Class[] { String.class });
                Object newObj = c.newInstance(new Object[] { value });
                this.process.writeAttachment(name, newObj);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } 
        }
        this.fireTableDataChanged();
    }

 	public static int getObjectSize(Object object) {
        if (object == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Object is null. Cannot measure.");
            }
            return -1;
        }
        try {
        	Boolean canSerialize = false;
        	if (Serializable.class.isAssignableFrom(object.getClass())) {
        		canSerialize = true;
        		if (Collection.class.isAssignableFrom(object.getClass())) {
        			Collection<?> c = (Collection<?>) object;
        			for (Object item: c) {
        				if (Serializable.class.isAssignableFrom(item.getClass()) == false) {
        					canSerialize = false;
        					break;
        				}
        			}
        		}
        	} 
        	if (canSerialize) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                byte[] bytes = baos.toByteArray();
                oos.close();
                baos.close();
                return bytes.length;
        	}
        	return -2;
         } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return -3;
    }

    public void setWidths(JTable table) {
        TableColumn column = null;
        for (int i = 0; i < this.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            switch (i) {
            case CLASS:
                column.setPreferredWidth(140);
                break;
            case NAME:
                column.setPreferredWidth(300);
                break;
            case SIZE:
                column.setPreferredWidth(50);
                break;
            case VALUE:
                column.setPreferredWidth(140);
                break;
            }

        }
    }
}
