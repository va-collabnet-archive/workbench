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
package org.dwfa.mojo.epicexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;

/**
 * A load file builder is a class that is used to determines which items will
 * appear in an export, and how they will appear, and in the case multiple
 * export files
 * are needed for a single master file, which export file will be written to.
 * 
 * @author Steven Neiner
 * @parameter exportFactory - The factory to produce new writers
 * @parameter exportManager - The export manager
 */
public abstract class AbstractEpicExportBuilder {
    EpicLoadFileFactory exportFactory;
    EpicExportManager exportManager;
    String masterfile;
    I_GetConceptData parentConcept;
    List<EpicItem> epicItems = new ArrayList<EpicItem>();
    private I_EpicExportRecordWriter writer;
    private String[] exportIfTheseItemsChanged;

    public AbstractEpicExportBuilder(EpicLoadFileFactory exportFactory, EpicExportManager em) {
        this.exportFactory = exportFactory;
        this.exportManager = em;
    }

    public EpicLoadFileFactory getExportFactory() {
        return exportFactory;
    }

    public void setExportFactory(EpicLoadFileFactory exportManager) {
        this.exportFactory = exportManager;
    }

    public I_EpicExportRecordWriter getWriter() {
        return writer;
    }

    public void setWriter(I_EpicExportRecordWriter writer) {
        this.writer = writer;
    }

    public I_GetConceptData getParentConcept() {
        return parentConcept;
    }

    public void setParentConcept(I_GetConceptData parentConcept) {
        this.parentConcept = parentConcept;
    }

    public String getParentUUID() throws IOException {
        String ret = null;
        for (UUID u : this.parentConcept.getUids()) {
            ret = u.toString();
        }
        return ret;
    }

    /**
     * Populates an Epic item number value pair for export into loadfiles
     * 
     * @param refsetId - The field number from the refest which identifies what
     *            the
     *            element is
     * @param value - The Object that contains the value
     * @param previousValue - The object that contains the previous value
     */
    public void sendItemForExport(String item, Object value, Object previousValue) {
        epicItems.add(new EpicItem(item, value, previousValue));
    }

    /**
     * Prepares the writer for receiving values for a record; initializes the
     * buffer of values.
     */
    public void newExportRecord() {
        epicItems = new ArrayList<EpicItem>();
    }

    public abstract String getEpicItemNumber(int refsetNumber);

    /**
     * Abstract method to determine which items export and how.
     * 
     * @param version - Passed to determine which load file to write to
     * @throws Exception
     */
    public abstract void writeRecord(String version) throws Exception;

    /**
     * Writes an item to the Epic load file
     * 
     * @param @param epicItemNumber - The item number of the value to write to
     *        the load file
     * @param aliasItemNumber - The item number to use in the load file
     */
    public void writeItem(String epicItemNumber, String aliasItemNumber) {
        for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
            EpicItem ei = (EpicItem) i.next();
            if (ei.itemNumber.equalsIgnoreCase(epicItemNumber))
                writer.addItemValue(aliasItemNumber, ei.value);
        }
    }

    /**
     * Writes an item to the Epic load file
     * 
     * @param epicItemNumber - The item number to write to the load file
     */
    public void writeItem(String epicItemNumber) {
        writeItem(epicItemNumber, epicItemNumber);
    }

    public String getMasterfile() {
        return masterfile;
    }

    public void setMasterfile(String masterfile) {
        this.masterfile = masterfile;
    }

    public EpicExportManager getExportManager() {
        return exportManager;
    }

    /**
     * If an item has no value set, will populate the the file with the value of
     * another
     * item. If the value of epicItemNumber is set, will populate with that
     * item.
     * 
     * @param epicItemNumber - The item number to check for if populated. I
     * @param defaultItem - If not populated, will use this item number to
     *            populate.
     */
    public void writeItemWithDefault(String epicItemNumber, String defaultItem) {
        if (getFirstItem(epicItemNumber) == null) {
            EpicItem d = getFirstItem(defaultItem);
            if (d != null)
                writer.addItemValue(epicItemNumber, d.value.toString());
        } else {
            writeItem(epicItemNumber);
        }
    }

    /**
     * If an item has changed value since the last version, will populate the
     * load file,
     * otherwise will leave it out of the load file.
     * 
     * @param epicItemNumber - The item number of the value to populate
     * @param aliasItemNumber - The item number that will appear in the load
     *            file.
     */
    public void writeItemIfChanged(String epicItemNumber, String aliasItemNumber) {
        if (itemHasChanges(epicItemNumber)) {
            writeItem(epicItemNumber, aliasItemNumber);
        }
    }

    /**
     * If an item has changed value since the last version, will populate the
     * load file,
     * otherwise will leave it out of the load file.
     * 
     * @param epicItemNumber - The item number of the value to populate
     */
    public void writeItemIfChanged(String epicItemNumber) {
        writeItemIfChanged(epicItemNumber, epicItemNumber);
    }

    /**
     * Write an item number / literal value combination to the load file
     * 
     * @param epicItemNumber
     * @param literal
     */
    public void writeLiteralItem(String epicItemNumber, String literal) {
        writer.addItemValue(epicItemNumber, literal);
    }

    /**
     * Determines whether an item has any changes between the current value
     * and previous values
     * 
     * @param epicItemNumber - The item number to check for changes
     * @return - true if an item has any changes
     */
    public boolean itemHasChanges(String epicItemNumber) {
        boolean ret = false;
        for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
            EpicItem ei = (EpicItem) i.next();
            if (ei.itemNumber.equalsIgnoreCase(epicItemNumber)) {
                ret = ret || ei.hasChanges();
                if (ret)
                    break;
            }
        }
        return ret;
    }

    /**
     * Looks for the first occurrence of the item in the exported items cache.
     * 
     * @param epicItemNumber
     * @return the first item found
     */
    public EpicItem getFirstItem(String epicItemNumber) {
        EpicItem ret = null;

        for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
            EpicItem ei = (EpicItem) i.next();
            if (ei.itemNumber.equalsIgnoreCase(epicItemNumber)) {
                ret = ei;
                break;
            }
        }
        return ret;
    }

    public void setExportIfTheseItemsChanged(String[] exportIfTheseItemsChanged) {
        this.exportIfTheseItemsChanged = exportIfTheseItemsChanged;
    }

    public boolean itemIsPopulated(String epicItemNumber) {
        return !(getFirstItem(epicItemNumber) == null);
    }

    public boolean allItemsArePopulated(String[] itemList) {
        boolean ret = true;
        for (String i : itemList) {
            ret = ret && this.itemIsPopulated(i);
            if (!ret)
                System.out.println("WARNING: Missing item " + i);
        }
        return ret;
    }

    public boolean anyItemsHaveChanges(String[] itemList) {
        boolean ret = false;
        for (String i : itemList) {
            ret = ret | this.itemHasChanges(i);
            if (ret)
                break;
        }
        return ret;
    }

    public boolean isChangedRecord() {
        return this.anyItemsHaveChanges(this.exportIfTheseItemsChanged);
    }

    public boolean onlyHasChangesIn(String[] itemList) {
        boolean ret = anyItemsHaveChanges(itemList);
        List<String> checkFor = new ArrayList<String>();
        for (String i : this.exportIfTheseItemsChanged) {
            boolean doAdd = true;
            for (String j : itemList) {
                if (j.equals(i))
                    doAdd = false;
            }
            if (doAdd)
                checkFor.add(i);
        }
        ret = ret || anyItemsHaveChanges((String[]) checkFor.toArray());
        return ret;
    }

    public class EpicItem {
        String itemNumber;
        Object value;
        Object previousValue;

        public EpicItem(String itemNumber, Object value, Object previousValue) {
            this.itemNumber = itemNumber;
            this.value = value;
            this.previousValue = previousValue;
        }

        public Object getValue() {
            return value;
        }

        public Object getPreviousValue() {
            return previousValue;
        }

        /**
         * Returns true if there is a difference between the previous value and
         * the
         * current value.
         * 
         * @return true if there is a difference
         */
        public boolean hasChanges() {
            if (previousValue == null && value == null)
                return false;
            else if (previousValue == null && value != null)
                return true;
            else if (previousValue != null && value == null)
                return true;
            else
                return !value.equals(previousValue);
        }
    }

}
