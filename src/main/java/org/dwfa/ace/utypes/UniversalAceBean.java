package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UniversalAceBean implements I_AmChangeSetObject, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int dataVersion = 1;

	private UniversalAceIdentification id;

	private UniversalAceConceptAttributes conceptAttributes;

	private List<UniversalAceDescription> descriptions = new ArrayList<UniversalAceDescription>();

	private List<UniversalAceRelationship> sourceRels = new ArrayList<UniversalAceRelationship>();

	private List<UniversalAceImage> images = new ArrayList<UniversalAceImage>();

	private List<UniversalAceIdentification> uncommittedIds = new ArrayList<UniversalAceIdentification>();

	private UniversalAceConceptAttributes uncommittedConceptAttributes;

	private List<UniversalAceRelationship> uncommittedSourceRels = new ArrayList<UniversalAceRelationship>();

	private List<UniversalAceDescription> uncommittedDescriptions = new ArrayList<UniversalAceDescription>();

	private List<UniversalAceImage> uncommittedImages = new ArrayList<UniversalAceImage>();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(id);
		out.writeObject(conceptAttributes);
		out.writeObject(descriptions);
		out.writeObject(sourceRels);
		out.writeObject(images);
		out.writeObject(uncommittedIds);
		out.writeObject(uncommittedConceptAttributes);
		out.writeObject(uncommittedSourceRels);
		out.writeObject(uncommittedDescriptions);
		out.writeObject(uncommittedImages);
	}
	
	public String toString() {
		return "UniversalAceBean: " + id +
				"\n  attributes: " + conceptAttributes + 
				"\n descriptions: " + descriptions + 
				"\n sourceRels: " + sourceRels + 
				"\n images: " + images + 
				"\n uncommittedIds: " + uncommittedIds + 
				"\n uncommittedConceptAttributes: " + uncommittedConceptAttributes + 
				"\n uncommittedSourceRels: " + uncommittedSourceRels + 
				"\n uncommittedDescriptions: " + uncommittedDescriptions + 
				"\n uncommittedImages: " + uncommittedImages; 
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			id = (UniversalAceIdentification) in.readObject();
			conceptAttributes = (UniversalAceConceptAttributes) in.readObject();
			descriptions = (List<UniversalAceDescription>) in.readObject();
			sourceRels = (List<UniversalAceRelationship>) in.readObject();
			images = (List<UniversalAceImage>) in.readObject();
			uncommittedIds = (List<UniversalAceIdentification>) in.readObject();
			uncommittedConceptAttributes = (UniversalAceConceptAttributes) in.readObject();
			uncommittedSourceRels = (List<UniversalAceRelationship>) in.readObject();
			uncommittedDescriptions = (List<UniversalAceDescription>) in.readObject();
			uncommittedImages = (List<UniversalAceImage>) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public UniversalAceConceptAttributes getConceptAttributes() {
		return conceptAttributes;
	}

	public void setConceptAttributes(UniversalAceConceptAttributes conceptAttributes) {
		this.conceptAttributes = conceptAttributes;
	}

	public List<UniversalAceDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<UniversalAceDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public UniversalAceIdentification getId() {
		return id;
	}

	public void setId(UniversalAceIdentification id) {
		this.id = id;
	}

	public List<UniversalAceImage> getImages() {
		return images;
	}

	public void setImages(List<UniversalAceImage> images) {
		this.images = images;
	}

	public List<UniversalAceRelationship> getSourceRels() {
		return sourceRels;
	}

	public void setSourceRels(List<UniversalAceRelationship> sourceRels) {
		this.sourceRels = sourceRels;
	}

	public UniversalAceConceptAttributes getUncommittedConceptAttributes() {
		return uncommittedConceptAttributes;
	}

	public void setUncommittedConceptAttributes(
			UniversalAceConceptAttributes uncommittedConceptAttributes) {
		this.uncommittedConceptAttributes = uncommittedConceptAttributes;
	}

	public List<UniversalAceDescription> getUncommittedDescriptions() {
		return uncommittedDescriptions;
	}

	public void setUncommittedDescriptions(
			List<UniversalAceDescription> uncommittedDescriptions) {
		this.uncommittedDescriptions = uncommittedDescriptions;
	}

	public List<UniversalAceIdentification> getUncommittedIds() {
		return uncommittedIds;
	}

	public void setUncommittedIds(List<UniversalAceIdentification> uncommittedIds) {
		this.uncommittedIds = uncommittedIds;
	}

	public List<UniversalAceImage> getUncommittedImages() {
		return uncommittedImages;
	}

	public void setUncommittedImages(List<UniversalAceImage> uncommittedImages) {
		this.uncommittedImages = uncommittedImages;
	}

	public List<UniversalAceRelationship> getUncommittedSourceRels() {
		return uncommittedSourceRels;
	}

	public void setUncommittedSourceRels(
			List<UniversalAceRelationship> uncommittedSourceRels) {
		this.uncommittedSourceRels = uncommittedSourceRels;
	}


}
