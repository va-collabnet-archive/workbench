package org.ihtsdo.rules.context;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.ihtsdo.rules.RulesLibrary;

public class RulesDeploymentPackageReference {

	private String name;
	private String url;
	private Collection<UUID> uuids;

	public RulesDeploymentPackageReference() {
	}

	public RulesDeploymentPackageReference(String name, String url, List<UUID> uuids) {
		super();
		this.name = name;
		this.url = url;
		this.uuids = uuids;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Collection<UUID> getUuids() {
		return uuids;
	}

	public void setUuids(Collection<UUID> uuids) {
		this.uuids = uuids;
	}

	public byte[] getChangeSetXmlBytes() {
		StringBuffer buff = new StringBuffer();
		buff.append("<change-set xmlns='http://drools.org/drools-5.0/change-set'");
		buff.append("		xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'");
		buff.append("		xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >");
		buff.append("		<add>");
		buff.append("			<resource source='");
		buff.append(url);
		//TODO: implement full authentication
		buff.append("' type='PKG' basicAuthentication='enabled' username='empty' password='empty'/>");
		buff.append("		</add>");
		buff.append("</change-set>");

		return buff.toString().getBytes();
	}

	public byte[] getChangeSetXmlBytesForFile() throws FileNotFoundException {
		StringBuffer buff = new StringBuffer();
		buff.append("<change-set xmlns='http://drools.org/drools-5.0/change-set'");
		buff.append("		xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'");
		buff.append("		xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >");
		buff.append("		<add>");
		buff.append("			<resource source='");
		String[] parts = url.split("/");
		File file = new File("rules/" + parts[parts.length-2] + "_" + parts[parts.length-1] + ".guvnor");
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		try {
			buff.append(file.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		//TODO: implement full authentication
		buff.append("' type='PKG' basicAuthentication='enabled' username='empty' password='empty'/>");
		buff.append("		</add>");
		buff.append("</change-set>");

		return buff.toString().getBytes();
	}

	public String toString() {
		return name;
	}

	public KnowledgeBase updateKnowledgeBase() throws Exception {
		return getKnowledgeBase(true);
	}

	public KnowledgeBase getKnowledgeBase(boolean recreate) throws Exception {
		if (!recreate) {
			KnowledgeBase fileBased = null;
			try {
				fileBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
						getChangeSetXmlBytesForFile(), recreate);
			} catch (Exception e) {
				// not found
			}
			if (fileBased != null) {
				return fileBased;
			} else {
				KnowledgeBase guvnorBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
						getChangeSetXmlBytes(), recreate);
				if (guvnorBased != null) {
					return guvnorBased;
				}
			}
		} else {
			KnowledgeBase guvnorBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
					getChangeSetXmlBytes(), recreate);
			if (guvnorBased != null) {
				return guvnorBased;
			} else {
				System.out.println("WARNING: KB Recreation failed.");
				KnowledgeBase fileBased = null;
				try {
					fileBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
							getChangeSetXmlBytesForFile(), recreate);
				} catch (Exception e) {
					// not found
				}
				if (fileBased != null) return fileBased;
			}
		}

		return null;

	}

	public Collection<Rule> getRules() throws Exception {
		Collection<Rule> rules = new ArrayList<Rule>();
		KnowledgeBase kbase = getKnowledgeBase(false);
		if (kbase != null) {
			for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
				//System.out.println("** pkg: " + kpackg.getName());
				for (Rule rule : kpackg.getRules()) {
					rules.add(rule);
				}
			}
		}
		return rules;
	}

	public boolean validate() {
		return RulesLibrary.validateDeploymentPackage(uuids.iterator().next(), getChangeSetXmlBytes());
	}

}
