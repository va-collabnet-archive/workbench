/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;

/**
 * The Class FileLink.
 */
public class FileLink implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 5088978032170296841L;
    /**
     * The name.
     */
    private String name;
    /**
     * The uuid.
     */
    private UUID uuid;
    /**
     * The category uui ds.
     */
    private List<UUID> categoryUUIDs;
    /**
     * The file.
     */
    private File file;
    /**
     * The description.
     */
    private String description;
    /**
     * The found on disk.
     */
    private boolean foundOnDisk;

    /**
     * Instantiates a new file link.
     */
    public FileLink() {
        super();
    }

    /**
     * Instantiates a new file link.
     *
     * @param file the file
     * @param category the category
     * @throws Exception the exception
     */
    public FileLink(File file, I_GetConceptData category) throws Exception {
        super();
        if (file.isFile() && !file.isHidden()) {
            this.name = file.getName();
            this.uuid = UUID.randomUUID();
            this.categoryUUIDs = category.getUids();
            this.file = file;
            this.description = "";
            this.foundOnDisk = file.exists();
        } else {
            throw new Exception("Only files must be indexed");
        }
    }

    /**
     * Instantiates a new file link.
     *
     * @param name the name
     * @param uuid the uuid
     * @param categoryUUIDs the category uui ds
     * @param file the file
     * @param description the description
     * @throws Exception the exception
     */
    public FileLink(String name, UUID uuid, List<UUID> categoryUUIDs,
            File file, String description) throws Exception {
        super();
        if (file.isFile() && !file.isHidden()) {
            this.name = name;
            this.uuid = uuid;
            this.categoryUUIDs = categoryUUIDs;
            this.file = file;
            this.description = description;
            this.foundOnDisk = file.exists();
        } else {
            throw new Exception("Only files must be indexed");
        }
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid the new uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the category uui ds.
     *
     * @return the category uui ds
     */
    public List<UUID> getCategoryUUIDs() {
        return categoryUUIDs;
    }

    /**
     * Sets the category uui ds.
     *
     * @param categoryUUIDs the new category uui ds
     */
    public void setCategoryUUIDs(List<UUID> categoryUUIDs) {
        this.categoryUUIDs = categoryUUIDs;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file the new file
     * @throws Exception the exception
     */
    public void setFile(File file) throws Exception {
        if (file.isFile() && !file.isHidden()) {
            this.file = file;
            this.foundOnDisk = file.exists();
        } else {
            throw new Exception("Only files must be indexed");
        }
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks if is found on disk.
     *
     * @return true, if is found on disk
     */
    public boolean isFoundOnDisk() {
        return file.exists();
    }

    /**
     * Sets the found on disk.
     *
     * @param foundOnDisk the new found on disk
     * @throws Exception the exception
     */
    public void setFoundOnDisk(boolean foundOnDisk) throws Exception {
        throw new Exception("Unsupported method");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return file.getName();
    }
}
