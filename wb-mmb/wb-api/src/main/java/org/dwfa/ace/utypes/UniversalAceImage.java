/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceImage implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Collection<UUID> imageId;
    private String format;
    private byte[] image;
    private Collection<UUID> conceptId;
    private List<UniversalAceImagePart> versions;

    public UniversalAceImage() {
        super();
    }

    // START: ADDED TO IMPLEMENT JAVABEANS SPEC
    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setImageId(Collection<UUID> imageId) {
        this.imageId = imageId;
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setConceptId(Collection<UUID> conceptId) {
        this.conceptId = conceptId;
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setVersions(List<UniversalAceImagePart> versions) {
        this.versions = versions;
    }

    // END: ADDED TO IMPLEMENT JAVABEANS SPEC

    public UniversalAceImage(Collection<UUID> imageId, byte[] image, List<UniversalAceImagePart> versions,
            String format, Collection<UUID> conceptId) {
        super();
        this.imageId = imageId;
        this.image = image;
        this.versions = versions;
        this.format = format;
        this.conceptId = conceptId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getImage()
     */
    public byte[] getImage() {
        return image;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getImageId()
     */
    public Collection<UUID> getImageId() {
        return imageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getVersions()
     */
    public List<UniversalAceImagePart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ImageVersioned#addVersion(org.dwfa.vodb.types.
     * ThinImagePart)
     */
    public boolean addVersion(UniversalAceImagePart part) {
        int index = versions.size() - 1;
        if (index == -1) {
            return versions.add(part);
        } else if (index >= 0) {
            return versions.add(part);

        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
     */
    public String getFormat() {
        return format;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptId()
     */
    public Collection<UUID> getConceptId() {
        return conceptId;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName());
        buff.append(": ");
        buff.append(imageId);
        buff.append(" conid: ");
        buff.append(conceptId);
        buff.append(" ");
        buff.append(format);
        buff.append("\n");
        for (UniversalAceImagePart part : versions) {
            buff.append("     ");
            buff.append(part.toString());
            buff.append("\n");
        }

        return buff.toString();
    }

}
