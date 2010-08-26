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

import java.util.ArrayList;
import java.util.Iterator;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.epicexport.AbstractEpicExportBuilder.EpicItem;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;

public interface I_EpicLoadFileBuilder {

    public EpicLoadFileFactory getExportFactory();

    public void setExportFactory(EpicLoadFileFactory exportManager);

    public I_EpicExportRecordWriter getWriter();

    public void setWriter(I_EpicExportRecordWriter writer);

    /**
     * Populates an Epic item number value pair for export into loadfiles
     * 
     * @param refsetId - The field number from the refest which identifies what
     *            the
     *            element is
     * @param value - The Object that contains the value
     * @param previousValue - The object that contains the previous value
     */
    public void sendItemForExport(String item, Object value, Object previousValue);

    /**
     * Prepares the writer for receiving values for a record; initializes the
     * buffer of values.
     */
    public void newExportRecord();

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
    public void writeItem(String epicItemNumber, String aliasItemNumber);

    /**
     * Writes an item to the Epic load file
     * 
     * @param epicItemNumber - The item number to write to the load file
     */
    public void writeItem(String epicItemNumber);

    public String getMasterfile();

    public void setMasterfile(String masterfile);

    public I_GetConceptData getParentConcept();

    public void setParentConcept(I_GetConceptData parentConcept);

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
    public void writeItemWithDefault(String epicItemNumber, String defaultItem);

    /**
     * If an item has changed value since the last version, will populate the
     * load file,
     * otherwise will leave it out of the load file.
     * 
     * @param epicItemNumber - The item number of the value to populate
     * @param aliasItemNumber - The item number that will appear in the load
     *            file.
     */
    public void writeItemIfChanged(String epicItemNumber, String aliasItemNumber);

    /**
     * If an item has changed value since the last version, will populate the
     * load file,
     * otherwise will leave it out of the load file.
     * 
     * @param epicItemNumber - The item number of the value to populate
     */
    public void writeItemIfChanged(String epicItemNumber);

    /**
     * Write an item number / literal value combination to the load file
     * 
     * @param epicItemNumber
     * @param literal
     */
    public void writeLiteralItem(String epicItemNumber, String literal);

    /**
     * Determines whether an item has any changes between the current value
     * and previous values
     * 
     * @param epicItemNumber - The item number to check for changes
     * @return - true if an item has any changes
     */
    public boolean itemHasChanges(String epicItemNumber);

    /**
     * Looks for the first occurrence of the item in the exported items cache.
     * 
     * @param epicItemNumber
     * @return the first item found
     */
    public EpicItem getFirstItem(String epicItemNumber);

}
