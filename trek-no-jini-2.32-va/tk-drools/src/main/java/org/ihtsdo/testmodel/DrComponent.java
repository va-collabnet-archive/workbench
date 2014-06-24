package org.ihtsdo.testmodel;

import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public abstract class DrComponent {

	private String statusUuid;
	private String pathUuid;
	private String authorUuid;
	private Long time;

	private String factContextName;

	// Inferred properties
	private boolean published = false;
	private boolean extensionComponent = false;
	private String extensionId = "";
	private boolean active = false;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		try {
			try {
				ConceptChronicleBI status = Ts.get().getConcept(UUID.fromString(statusUuid));
				sb.append(" Status: " + status + " (" + statusUuid + "),");
			} catch (Exception ex) {
			}

			try {
				ConceptChronicleBI path = Ts.get().getConcept(UUID.fromString(pathUuid));
				sb.append(" Path: " + path + " (" + pathUuid + "),");
			} catch (Exception ex) {
			}

			try {
				ConceptChronicleBI author = Ts.get().getConcept(UUID.fromString(authorUuid));
				sb.append(" Author: " + author + " (" + authorUuid + "),");
			} catch (Exception ex) {
			}

			sb.append(" Time: " + time + ",");
			sb.append(" Fact Context Name: " + factContextName + ",");
			sb.append(" Published: " + published + ",");
			sb.append(" Extension Component: " + extensionComponent + ",");
			sb.append(" Active: " + active);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public String getStatusUuid() {
		return statusUuid;
	}

	public void setStatusUuid(String statusUuid) {
		this.statusUuid = statusUuid;
	}

	public String getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(String pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getAuthorUuid() {
		return authorUuid;
	}

	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getFactContextName() {
		return factContextName;
	}

	public void setFactContextName(String factContextName) {
		this.factContextName = factContextName;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isExtensionComponent() {
		return extensionComponent;
	}

	public void setExtensionComponent(boolean extensionComponent) {
		this.extensionComponent = extensionComponent;
	}

	public String getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(String extensionId) {
		this.extensionId = extensionId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
