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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf1;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf2;
import org.ihtsdo.tk.example.binding.WbDescType;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class ConceptCB extends CreateOrAmendBlueprint {

    public static final UUID conceptSpecNamespace =
            UUID.fromString("620d1f30-5285-11e0-b8af-0800200c9a66");
    private String fullySpecifiedName;
    private String preferredName;
    private boolean initialCaseSensitive = false;
    private String lang;
    private UUID isaType;
    private boolean defined;
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

    public ConceptCB(String fullySpecifiedName,
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

    public boolean isDefined() {
        return defined;
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    public boolean isInitialCaseSensitive() {
        return initialCaseSensitive;
    }

    public void setInitialCaseSensitive(boolean initialCaseSensitive) {
        this.initialCaseSensitive = initialCaseSensitive;
    }

    public DescCAB getFsnCAB() throws IOException {
        //get rf1/rf2 concepts
        UUID fsnUuid = null;
        if (Ts.get().hasUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid())) {
            fsnUuid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid();
        } else {
            fsnUuid = SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid();
        }
        return new DescCAB(
                getComponentUuid(),
                fsnUuid,
                getLang(),
                getFullySpecifiedName(),
                isInitialCaseSensitive());
    }

    public DescCAB getPreferredCAB() throws IOException {
        //get rf1/rf2 concepts
        UUID synUuid = null;
        if (Ts.get().hasUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid())) {
            synUuid = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid();
        } else {
            synUuid = SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid();
        }
        return new DescCAB(
                getComponentUuid(),
                synUuid, //from PREFERRED
                getLang(),
                getPreferredName(),
                isInitialCaseSensitive());
    }

    public List<RelCAB> getParentCABs() throws IOException {
        List<RelCAB> parentCabs =
                new ArrayList<RelCAB>(getParents().size());
        for (UUID parentUuid : parents) {
            RelCAB parent = new RelCAB(
                    getComponentUuid(),
                    isaType,
                    parentUuid,
                    0,
                    TkRelType.STATED_HIERARCHY);
            parentCabs.add(parent);
        }
        return parentCabs;
    }
}
