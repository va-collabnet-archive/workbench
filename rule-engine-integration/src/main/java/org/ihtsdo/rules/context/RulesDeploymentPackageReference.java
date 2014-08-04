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
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.rules.RulesLibrary;

/**
 * The Class RulesDeploymentPackageReference.
 */
public class RulesDeploymentPackageReference {

	/** The name. */
	private String name;

	/** The url. */
	private String url;

	/** The uuids. */
	private Collection<UUID> uuids;

	/**
	 * Instantiates a new rules deployment package reference.
	 */
	public RulesDeploymentPackageReference() {
	}

	/**
	 * Instantiates a new rules deployment package reference.
	 *
	 * @param name the name
	 * @param url the url
	 * @param uuids the uuids
	 */
	public RulesDeploymentPackageReference(String name, String url, List<UUID> uuids) {
		super();
		this.name = name;
		this.url = url;
		this.uuids = uuids;
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
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the uuids.
	 *
	 * @return the uuids
	 */
	public Collection<UUID> getUuids() {
		return uuids;
	}

	/**
	 * Sets the uuids.
	 *
	 * @param uuids the new uuids
	 */
	public void setUuids(Collection<UUID> uuids) {
		this.uuids = uuids;
	}

	/**
	 * Gets the change set xml bytes.
	 *
	 * @return the change set xml bytes
	 */
	public byte[] getChangeSetXmlBytes() {
		StringBuffer buff = new StringBuffer();
		buff.append("<change-set xmlns='http://drools.org/drools-5.0/change-set'");
		buff.append("		xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'");
		buff.append("		xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >");
		buff.append("		<add>");
		buff.append("			<resource source='");
		buff.append(url);
		//TODO: implement full authentication
		buff.append("' type='PKG' basicAuthentication='enabled' username='ihtsdo' password='ihtsdo'/>");
		buff.append("		</add>");
		buff.append("</change-set>");
		//AceLog.getAppLog().info(buff.toString());
		return buff.toString().getBytes();
	}

	/**
	 * Gets the change set xml bytes for file.
	 *
	 * @return the change set xml bytes for file
	 * @throws FileNotFoundException the file not found exception
	 */
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
			AceLog.getAppLog().info(file.getName());
			throw new FileNotFoundException();
		}
		try {
			buff.append(file.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		//TODO: implement full authentication
		buff.append("' type='PKG' basicAuthentication='enabled' username='ihtsdo' password='ihtsdo'/>");
		buff.append("		</add>");
		buff.append("</change-set>");
		//AceLog.getAppLog().info(buff.toString());
		return buff.toString().getBytes();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	/**
	 * Update knowledge base.
	 *
	 * @return the knowledge base
	 * @throws Exception the exception
	 */
	public KnowledgeBase updateKnowledgeBase() throws Exception {
		return getKnowledgeBase(true);
	}

	/**
	 * Gets the knowledge base.
	 *
	 * @param recreate the recreate
	 * @return the knowledge base
	 * @throws Exception the exception
	 */
	public KnowledgeBase getKnowledgeBase(boolean recreate) throws Exception {
		if (url.startsWith("http")) {
			if (!recreate) {
				KnowledgeBase fileBased = null;
				try {
					fileBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
							getChangeSetXmlBytesForFile(), recreate);
				} catch (Exception e) {
					AceLog.getAppLog().info("File Package not accessible: " + getName() + " Exception: " + e.getMessage());
				}
				if (fileBased != null && fileBased.getKnowledgePackages().size() > 0) {
					return fileBased;
				} else {
					KnowledgeBase guvnorBased = null;
					try {
						guvnorBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
								getChangeSetXmlBytes(), recreate);
					} catch (Exception e1) {
						AceLog.getAppLog().info("Web Package not accessible: " + getName() + " Exception: " + e1.getMessage());
					}
					if (guvnorBased != null && guvnorBased.getKnowledgePackages().size() > 0) {
						return guvnorBased;
					}
				}
			} else {
				KnowledgeBase guvnorBased = null;

				try {
					guvnorBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
							getChangeSetXmlBytes(), recreate);
				} catch (Exception e1) {
					AceLog.getAppLog().info("Web Package not accessible: " + getName() + " Exception: " + e1.getMessage());
				}
				if (guvnorBased != null && guvnorBased.getKnowledgePackages().size() > 0) {
					return guvnorBased;
				} else {
					KnowledgeBase fileBased = null;
					try {
						fileBased = RulesLibrary.getKnowledgeBase(uuids.iterator().next(), 
								getChangeSetXmlBytesForFile(), recreate);
					} catch (Exception e) {
						AceLog.getAppLog().info("File Package not accessible: " + getName() + " Exception: " + e.getMessage());
					}
					if (fileBased != null && fileBased.getKnowledgePackages().size() > 0) return fileBased;
				}
			}
		} else {
			File localFolder = new File(url);
			if (localFolder != null && localFolder.exists() && localFolder.isDirectory()) {
				KnowledgeBase folderBased = RulesLibrary.getKnowledgeBaseFromFolder(uuids.iterator().next(), localFolder);
				return folderBased;
			} else {
				AceLog.getAppLog().info("Folder not accessible: " + url + " | " + getName());
			}
		}
		AceLog.getAppLog().info("WARNING: KB Recreation failed.");
		return null;

	}

	/**
	 * Gets the rules.
	 *
	 * @return the rules
	 * @throws Exception the exception
	 */
	public Collection<Rule> getRules() throws Exception {
		Collection<Rule> rules = new ArrayList<Rule>();
		KnowledgeBase kbase = getKnowledgeBase(false);
		if (kbase != null) {
			for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
				//AceLog.getAppLog().info("** pkg: " + kpackg.getName());
				for (Rule rule : kpackg.getRules()) {
					rules.add(rule);
				}
			}
		}
		return rules;
	}

	/**
	 * Validate.
	 *
	 * @return true, if successful
	 */
	public boolean validate() {
		boolean result = false;
		try {
			KnowledgeBase kb = getKnowledgeBase(true);
			if (kb != null && !kb.getKnowledgePackages().isEmpty()) {
				result = true;
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

}
