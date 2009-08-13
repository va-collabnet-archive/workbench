package org.dwfa.mojo.refset;

import org.dwfa.mojo.ConceptDescriptor;

public class RF2Descriptor {

    /**
     * The module that this refset belongs to. e.g. AU pathology module.
     */
    ConceptDescriptor module;

    /**
     * The namespace that this export uses. e.g. 1000036
     */
    String namespace;

    String project;

    /**
     * The content sub type - this is National, Local or Core.
     */
    String contentSubType;

    /**
     * The two digit country code.
     */
    String countryCode;

    public ConceptDescriptor getModule() {
        return module;
    }

    public void setModule(ConceptDescriptor module) {
        this.module = module;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getContentSubType() {
        return contentSubType;
    }

    public void setContentSubType(String contentSubType) {
        this.contentSubType = contentSubType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
