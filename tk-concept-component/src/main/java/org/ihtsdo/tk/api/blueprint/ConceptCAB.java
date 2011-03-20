/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class ConceptCAB extends CreateOrAmendBlueprint {

    public static final UUID conceptSpecNamespace =
            UUID.fromString("620d1f30-5285-11e0-b8af-0800200c9a66");
    private String fullySpecifiedName;

    private String preferredName;
    private String lang;
    private UUID isaType;
    
    private Collection<UUID> parents = new TreeSet<UUID>() {

        @Override
        public boolean add(UUID e) {
            boolean result = super.add(e);
            comupteComponentUuid();
            return result;
        }

        @Override
        public boolean addAll(Collection<? extends UUID> clctn) {
            boolean result = super.addAll(clctn);
            comupteComponentUuid();
            return result;
        }

        @Override
        public boolean remove(Object o) {
            boolean result = super.remove(o);
            comupteComponentUuid();
            return result;
        }

        @Override
        public boolean removeAll(Collection<?> clctn) {
            boolean result = super.removeAll(clctn);
            comupteComponentUuid();
            return result;
        }
    
    };

    public Collection<UUID> getParents() {
        return parents;
    }

    public ConceptCAB(String fullySpecifiedName,
            String preferredName,
            String lang,
            UUID isaType,
            UUID... parents) {
        super(null);
        this.fullySpecifiedName = fullySpecifiedName;
        this.preferredName = preferredName;
        this.lang = lang;
        this.isaType = isaType;
        if (parents != null) {
            this.parents.addAll(Arrays.asList(parents));
        }
        comupteComponentUuid();

    }

    public final void comupteComponentUuid() throws RuntimeException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(preferredName);
            sb.append(isaType.toString());
            for (UUID p : parents) {
                sb.append(p.toString());
            }

            setComponentUuid(
                    UuidT5Generator.get(conceptSpecNamespace,
                    sb.toString()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
        public String getFullySpecifiedName() {
        return fullySpecifiedName;
    }

    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
        comupteComponentUuid();
    }

    public UUID getIsaType() {
        return isaType;
    }

    public void setIsaType(UUID isaType) {
        this.isaType = isaType;
        comupteComponentUuid();
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        comupteComponentUuid();
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        comupteComponentUuid();
    }

}
