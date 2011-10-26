package org.ihtsdo.project;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;

public class FileLink implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5088978032170296841L;
	private String name;
	private UUID uuid;
	private List<UUID> categoryUUIDs;
	private File file;
	private String description;
	private boolean foundOnDisk;

	public FileLink() {
		super();
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public List<UUID> getCategoryUUIDs() {
		return categoryUUIDs;
	}

	public void setCategoryUUIDs(List<UUID> categoryUUIDs) {
		this.categoryUUIDs = categoryUUIDs;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) throws Exception {
		if (file.isFile() && !file.isHidden()) {
			this.file = file;
			this.foundOnDisk = file.exists();
		} else {
			throw new Exception("Only files must be indexed");
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isFoundOnDisk() {
		return file.exists();
	}

	public void setFoundOnDisk(boolean foundOnDisk) throws Exception {
		throw new Exception("Unsupported method");
	}
	
	public String toString() {
		return file.getName();
	}
}
