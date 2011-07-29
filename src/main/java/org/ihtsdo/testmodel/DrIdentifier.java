package org.ihtsdo.testmodel;

import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class DrIdentifier extends DrComponent {

	private String primordialUuid;

	private String componentUuid;

	private String authorityUuid;
	private String denotation;

	// Inferred properties
	// none yet

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		try {
			sb.append("primordialUuid: " + primordialUuid + ",");

			try {
				ConceptChronicleBI component = Ts.get().getConcept(UUID.fromString(componentUuid));
				sb.append(" Source Rel: " + component + " (" + componentUuid + "),");
			} catch (IllegalArgumentException ex) {
			}

			try {
				ConceptChronicleBI authority = Ts.get().getConcept(UUID.fromString(authorityUuid));
				sb.append(" Type: " + authority + " (" + authorityUuid + "),");
			} catch (IllegalArgumentException ex) {
			}

			sb.append(" Denotation: " + denotation + ",");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public DrIdentifier() {
		// TODO Auto-generated constructor stub
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid) {
		this.componentUuid = componentUuid;
	}

	public String getAuthorityUuid() {
		return authorityUuid;
	}

	public void setAuthorityUuid(String authorityUuid) {
		this.authorityUuid = authorityUuid;
	}

	public String getDenotation() {
		return denotation;
	}

	public void setDenotation(String denotation) {
		this.denotation = denotation;
	}
}
