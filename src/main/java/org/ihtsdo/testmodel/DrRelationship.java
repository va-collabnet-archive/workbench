package org.ihtsdo.testmodel;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class DrRelationship extends DrComponent {
	private String primordialUuid;

	private String sourceUuid;
	private String typeUuid;
	private String targetUuid;
	private String characteristicUuid;
	private String modifierUuid;
	private int relGroup;

	private List<DrIdentifier> identifiers;

	// Inferred properties
	// none yet

	@Override
	public String toString() {
		StringBuffer relSb = new StringBuffer("");
		try {
			relSb.append("primordialUuid: " + primordialUuid + ",");
			
			try {
				ConceptChronicleBI source = Ts.get().getConcept(UUID.fromString(sourceUuid));
				relSb.append(" Source Rel: " + source + " (" + sourceUuid + "),");
			} catch (IllegalArgumentException ex) {}
			
			try {
				ConceptChronicleBI type = Ts.get().getConcept(UUID.fromString(typeUuid));
				relSb.append(" Type: " + type + " (" + typeUuid + "),");
			} catch (IllegalArgumentException ex) {}
			
			try {
				ConceptChronicleBI target = Ts.get().getConcept(UUID.fromString(targetUuid));
				relSb.append(" Target Rel: " + target + " (" + targetUuid + "),");
			} catch (IllegalArgumentException ex) {}

			try {
				ConceptChronicleBI characteristic = Ts.get().getConcept(UUID.fromString(characteristicUuid));
				relSb.append(" Characteristic: " + characteristic + " (" + characteristicUuid + "),");
			} catch (IllegalArgumentException ex) {}
			
			try {
				ConceptChronicleBI modifier = Ts.get().getConcept(UUID.fromString(modifierUuid));
				relSb.append(" Modifier: " + modifier + " (" + modifierUuid + "),");
			} catch (IllegalArgumentException ex) {}

			relSb.append(" Relationship group: " + relGroup + ",");
			relSb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");
			relSb.append("\nIdentifiers: [");
			if (identifiers != null) {
				for (DrIdentifier identifier : identifiers) {
					int i = 0;
					relSb.append(identifier.toString() + (i == identifiers.size() - 1 ? "" : ","));
					i++;
				}
			}
			relSb.append("]");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return relSb.toString();
	}

	public DrRelationship() {
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getCharacteristicUuid() {
		return characteristicUuid;
	}

	public void setCharacteristicUuid(String characteristicUuid) {
		this.characteristicUuid = characteristicUuid;
	}

	public int getRelGroup() {
		return relGroup;
	}

	public void setRelGroup(int relGroup) {
		this.relGroup = relGroup;
	}

	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public List<DrIdentifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public String getModifierUuid() {
		return modifierUuid;
	}

	public void setModifierUuid(String modifierUuid) {
		this.modifierUuid = modifierUuid;
	}

	public String getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid) {
		this.sourceUuid = sourceUuid;
	}

	public String getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(String targetUuid) {
		this.targetUuid = targetUuid;
	}

}
