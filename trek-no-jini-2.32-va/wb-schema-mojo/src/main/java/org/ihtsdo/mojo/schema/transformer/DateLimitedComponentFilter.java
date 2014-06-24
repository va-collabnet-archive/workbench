package org.ihtsdo.mojo.schema.transformer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

public class DateLimitedComponentFilter extends AbstractTransformer {

	/** The id. */
	private final String id = "time-and-optional-rels-filter";

	/** Filters out version older than this, but always leaves at least one. */
	Long dateLimit;

	/** Whether to include retired rels or not. */
	Boolean includeRetiredRels;

	UUID activeStatus; 

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		activeStatus = Terms.get().nidToUuid(SnomedMetadataRfx.getSTATUS_CURRENT_NID());
		TransformersConfigApi api = new TransformersConfigApi(xmlFile);

		String strDateLimit = api.getValueAt(api.getIntId(id), "parameters.dateLimit");
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date date = formatter.parse(strDateLimit);
		dateLimit = date.getTime();
		includeRetiredRels = new Boolean(api.getValueAt(api.getIntId(id), "parameters.includeRetiredRels"));
	}

	@Override
	public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
		if (attributes.revisions != null) {
			List<TkConceptAttributesRevision> attributeRevisions = new ArrayList<>();
			attributeRevisions.addAll(attributes.revisions);
			for (TkConceptAttributesRevision loopRevision : attributeRevisions) {
				if (loopRevision.getTime() < dateLimit) {
					attributes.revisions.remove(loopRevision);
				}
			}
		}

	}

    @Override
	public void transformDescription(TkDescription description, TkConcept concept) {
		if (description.revisions != null) {
			List<TkDescriptionRevision> descriptionRevisions = new ArrayList<>();
			descriptionRevisions.addAll(description.revisions);
			for (TkDescriptionRevision loopRevision : descriptionRevisions) {
				if (loopRevision.getTime() < dateLimit) {
					description.revisions.remove(loopRevision);
				}
			}
		}
	}

	@Override
	public void transformRelationship(TkRelationship relationship, TkConcept concept) {
		TkRelationshipRevision lastRevision = null;
		if (relationship.revisions != null) {
			List<TkRelationshipRevision> relationshipRevisions = new ArrayList<>();
			relationshipRevisions.addAll(relationship.revisions);
			long lastestTime = Long.MIN_VALUE;
			for (TkRelationshipRevision loopRevision : relationshipRevisions) {
				if (loopRevision.getTime() < dateLimit) {
					relationship.revisions.remove(loopRevision);
				} else {
					if (loopRevision.getTime() > lastestTime) {
						lastestTime = loopRevision.getTime();
						lastRevision = loopRevision;
					}
				}
			}
		}

		if ((lastRevision == null && !relationship.statusUuid.equals(activeStatus)) ||
				lastRevision != null && !lastRevision.statusUuid.equals(activeStatus)) {
			concept.relationships.remove(relationship);
		}
	}

	@Override
	public void transformAnnotation(TkRefexAbstractMember<?> annotation,
			TkComponent<?> component) {
		if (annotation.revisions != null) {
			List<TkRevision> annotationRevisions = new ArrayList<>();
			annotationRevisions.addAll(annotation.revisions);
			for (TkRevision loopRevision : annotationRevisions) {
				if (loopRevision.getTime() < dateLimit) {
					annotation.revisions.remove(loopRevision);
				}
			}
		}
	}

	@Override
	public void transformMember(TkRefexAbstractMember<?> member,
			TkConcept concept) {
		if (member.revisions != null) {
			List<TkRevision> annotationRevisions = new ArrayList<>();
			annotationRevisions.addAll(member.revisions);
			for (TkRevision loopRevision : annotationRevisions) {
				if (loopRevision.getTime() < dateLimit) {
					member.revisions.remove(loopRevision);
				}
			}
		}
	}

	@Override
	public boolean postProcessConcept(TkConcept eConcept) {
		return true;
	}

	@Override
	public void preProcessIteration() {
		// Nothing

	}

	@Override
	public List<TkConcept> postProcessIteration() {
		return null;
	}

	public Boolean getIncludeRetiredRels() {
		return includeRetiredRels;
	}

	public void setIncludeRetiredRels(Boolean includeRetiredRels) {
		this.includeRetiredRels = includeRetiredRels;
	}

	public Long getDateLimit() {
		return dateLimit;
	}

	public void setDateLimit(Long dateLimit) {
		this.dateLimit = dateLimit;
	}

}
