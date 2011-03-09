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
package org.dwfa.tapi.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ExtendLocally;
import org.dwfa.tapi.I_ExtendUniversally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.TaxonomySpec;

public class MemoryTermServer implements I_StoreLocalFixedTerminology {

    public enum FILE_FORMAT {
        ACE, SNOMED
    };

    private static int nextNid = Integer.MIN_VALUE;

    private boolean generateIds = false;

    private List<I_ConceptualizeLocally> roots = new ArrayList<I_ConceptualizeLocally>();

    private Map<UUID, Integer> uuidIntMap = new HashMap<UUID, Integer>();

    private Map<Integer, Collection<UUID>> intUuidMap = new HashMap<Integer, Collection<UUID>>();

    private Map<Integer, I_ConceptualizeLocally> conceptMap = new HashMap<Integer, I_ConceptualizeLocally>();

    private Map<Integer, I_DescribeConceptLocally> descMap = new HashMap<Integer, I_DescribeConceptLocally>();

    private Map<Integer, Collection<I_DescribeConceptLocally>> conNidDescMap = new HashMap<Integer, Collection<I_DescribeConceptLocally>>();

    private Map<Integer, I_RelateConceptsLocally> relMap = new HashMap<Integer, I_RelateConceptsLocally>();

    private Map<Integer, Collection<I_RelateConceptsLocally>> srcRelMap = new HashMap<Integer, Collection<I_RelateConceptsLocally>>();

    private Map<Integer, Collection<I_RelateConceptsLocally>> destRelMap = new HashMap<Integer, Collection<I_RelateConceptsLocally>>();

    private Map<Integer, Map<Integer, I_ExtendLocally>> extensions = new HashMap<Integer, Map<Integer, I_ExtendLocally>>();

    private Set<I_ConceptualizeLocally> extensionTypes = new HashSet<I_ConceptualizeLocally>();

    private UUID pathUuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next();

    private Date effectiveDate = new Date();

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    @Override
    public Collection<I_ConceptualizeLocally> doConceptSearch(String[] words) throws IOException, TerminologyException {
        return doConceptSearch(Arrays.asList(words));
    }

    @Override
    public Collection<I_ConceptualizeLocally> doConceptSearch(List<String> words) throws IOException,
            TerminologyException {
        Set<I_ConceptualizeLocally> concepts = new HashSet<I_ConceptualizeLocally>();
        for (I_DescribeConceptLocally d : doDescriptionSearch(words)) {
            concepts.add(d.getConcept());
        }
        return concepts;
    }

    @Override
    public Collection<I_DescribeConceptLocally> doDescriptionSearch(String[] words) throws IOException,
            TerminologyException {
        return doDescriptionSearch(Arrays.asList(words));
    }

    @Override
    public Collection<I_DescribeConceptLocally> doDescriptionSearch(List<String> words) throws IOException,
            TerminologyException {
        List<Pattern> patterns = new ArrayList<Pattern>(words.size());
        for (String word : words) {
            patterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE));
        }
        ArrayList<I_DescribeConceptLocally> matchList = new ArrayList<I_DescribeConceptLocally>();
        for (I_DescribeConceptLocally d : descMap.values()) {
            boolean matchedAll = true;
            for (Pattern p : patterns) {
                Matcher matcher = p.matcher(d.getText());
                if (matcher.find() == false) {
                    matchedAll = false;
                    break;
                }
            }
            if (matchedAll) {
                matchList.add(d);
            }
        }
        return matchList;
    }

    @Override
    public I_ConceptualizeLocally getConcept(int conceptNid) throws IOException, TerminologyException {
        return conceptMap.get(conceptNid);
    }

    @Override
    public I_DescribeConceptLocally getDescription(int descriptionNid, int conceptNid) throws IOException,
            TerminologyException {
        return descMap.get(descriptionNid);
    }

    @Override
    public Collection<I_DescribeConceptLocally> getDescriptionsForConcept(I_ConceptualizeLocally concept)
            throws IOException, TerminologyException {
        return conNidDescMap.get(concept.getNid());
    }

    @Override
    public Collection<I_RelateConceptsLocally> getDestRels(I_ConceptualizeLocally dest) throws IOException,
            TerminologyException {
        Collection<I_RelateConceptsLocally> rels = destRelMap.get(dest.getNid());
        if (rels == null) {
            rels = new ArrayList<I_RelateConceptsLocally>();
        }
        return rels;
    }

    @Override
    public int getNid(UUID uid) throws NoMappingException {
        if (uuidIntMap.containsKey(uid) == false) {
            if (generateIds) {
                int nid = nextNid++;
                uuidIntMap.put(uid, nid);
                List<UUID> uuids = new ArrayList<UUID>();
                intUuidMap.put(nid, uuids);
            } else {
                throw new NoMappingException("No nid found for: " + uid);
            }
        }
        return uuidIntMap.get(uid);
    }

    @Override
    public int getNid(Collection<UUID> uids) throws NoMappingException {
        for (UUID uid : uids) {
            if (uuidIntMap.containsKey(uid)) {
                return uuidIntMap.get(uid);
            }
        }
        if (generateIds) {
            int nid = nextNid++;
            intUuidMap.put(nid, uids);
            for (UUID uid : uids) {
                uuidIntMap.put(uid, nid);
            }
            return nid;
        }
        throw new NoMappingException("No nid found for: " + uids);
    }

    @Override
    public I_RelateConceptsLocally getRel(int relNid) {
        return relMap.get(relNid);
    }

    @Override
    public Collection<I_ConceptualizeLocally> getRoots() {
        return roots;
    }

    @Override
    public Collection<I_RelateConceptsLocally> getSourceRels(I_ConceptualizeLocally source) {
        Collection<I_RelateConceptsLocally> rels = srcRelMap.get(source.getNid());
        if (rels == null) {
            rels = new ArrayList<I_RelateConceptsLocally>();
        }
        return rels;
    }

    @Override
    public Collection<UUID> getUids(int nid) {
        return intUuidMap.get(nid);
    }

    public synchronized void add(I_ConceptualizeUniversally icu) throws IOException, TerminologyException {
        I_ConceptualizeLocally icl = (I_ConceptualizeLocally) icu.localize();
        conceptMap.put(icl.getNid(), icl);
        intUuidMap.put(icl.getNid(), icu.getUids());
    }

    public synchronized void addRoot(I_ConceptualizeUniversally icu) throws IOException, TerminologyException {
        I_ConceptualizeLocally icl = (I_ConceptualizeLocally) icu.localize();
        roots.add(icl);
    }

    public synchronized void add(I_RelateConceptsUniversally iru) throws IOException, TerminologyException {
        I_RelateConceptsLocally irl = (I_RelateConceptsLocally) iru.localize();
        relMap.put(irl.getNid(), irl);
        if (srcRelMap.containsKey(irl.getC1().getNid()) == false) {
            srcRelMap.put(irl.getC1().getNid(), new ArrayList<I_RelateConceptsLocally>());
        }
        srcRelMap.get(irl.getC1().getNid()).add(irl);
        if (destRelMap.containsKey(irl.getC2().getNid()) == false) {
            destRelMap.put(irl.getC2().getNid(), new ArrayList<I_RelateConceptsLocally>());
        }
        destRelMap.get(irl.getC2().getNid()).add(irl);
        intUuidMap.put(irl.getNid(), iru.getUids());
    }

    public synchronized void add(I_DescribeConceptUniversally idu) throws IOException, TerminologyException {
        I_DescribeConceptLocally idl = (I_DescribeConceptLocally) idu.localize();
        descMap.put(idl.getNid(), idl);
        if (conNidDescMap.containsKey(idl.getConcept().getNid()) == false) {
            conNidDescMap.put(idl.getConcept().getNid(), new ArrayList<I_DescribeConceptLocally>());
        }
        conNidDescMap.get(idl.getConcept().getNid()).add(idl);
        intUuidMap.put(idl.getNid(), idu.getUids());
    }

    @Override
    public I_ExtendLocally getExtension(I_ManifestLocally component, I_ConceptualizeLocally extensionType) {
        if (extensions.containsKey(component.getNid())) {
            Map<Integer, I_ExtendLocally> types = extensions.get(component.getNid());
            if (types.containsKey(extensionType.getNid())) {
                return types.get(extensionType.getNid());
            }
        }
        return null;
    }

    int extensionCount = 0;

    /**
     * Contains the concept hierarchies to be excluded when exporting the cement
     * taxonomies
     */
    private TaxonomySpec[] exclusions;

    public synchronized void addExtension(I_ManifestLocally component, I_ConceptualizeLocally extensionType,
            I_ExtendLocally extension) {
        if (extensions.containsKey(component.getNid()) == false) {
            extensions.put(component.getNid(), new HashMap<Integer, I_ExtendLocally>());
        }
        extensions.get(component.getNid()).put(extensionType.getNid(), extension);
        extensionTypes.add(extensionType);
        // System.out.println(extensionCount++ + " componentUids: " +
        // component.getUids() + " component: " + component + " extension: " +
        // extension);
    }

    public Collection<I_ConceptualizeLocally> getExtensionTypes() throws IOException, TerminologyException {
        return extensionTypes;
    }

    public void writeConcepts(Writer conceptWriter, Writer altIdWriter, FILE_FORMAT format) throws IOException,
            TerminologyException {

        UUID currentId = getFirstUid(ArchitectonicAuxiliary.Concept.CURRENT);
        I_ConceptualizeLocally[] descTypeOrder = new I_ConceptualizeLocally[] {
                                                                               this.getConcept(this.getNid(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())),
                                                                               this.getConcept(this.getNid(ArchitectonicAuxiliary.Concept.XHTML_DEF.getUids())) };
        List<I_ConceptualizeLocally> descTypePriorityList = Arrays.asList(descTypeOrder);
        for (I_ConceptualizeLocally c : conceptMap.values()) {
            if (inExclusionList(exclusions, c)) {
                continue;
            }

            UUID firstComponentId = processUids(altIdWriter, c.getUids());
            switch (format) {
            case SNOMED:
                I_DescribeConceptLocally desc = c.getDescription(descTypePriorityList);
                conceptWriter.append(firstComponentId.toString());
                conceptWriter.append('\t');
                conceptWriter.append(currentId.toString());
                conceptWriter.append('\t');
                conceptWriter.append(desc.getText());
                conceptWriter.append('\t');
                conceptWriter.append("CTV3ID");
                conceptWriter.append('\t');
                conceptWriter.append("SNOMEDID");
                conceptWriter.append('\t');
                conceptWriter.append(new Boolean(c.isPrimitive()).toString());
                conceptWriter.append('\n');
                break;

            case ACE:
                // Concept UUID
                conceptWriter.append(firstComponentId.toString());
                conceptWriter.append('\t');

                // Status UUID
                conceptWriter.append(currentId.toString());
                conceptWriter.append('\t');

                // Primitive
                conceptWriter.append(new Boolean(c.isPrimitive()).toString());
                conceptWriter.append('\t');

                // Effective Date
                conceptWriter.append(dateFormatter.format(getEffectiveDate()));
                conceptWriter.append('\t');

                // Path UUID
                conceptWriter.append(getPathUuid().toString());
                conceptWriter.append('\n');
                break;
            default:
                throw new IOException("Can't handle format: " + format);
            }
        }
    }

    private boolean inExclusionList(TaxonomySpec[] exclusions, I_ConceptualizeLocally c) throws IOException,
            TerminologyException {
        if (exclusions == null) {
            return false;
        }
        for (TaxonomySpec exclusion : exclusions) {
            if (exclusion.contains(c)) {
                return true;
            }
        }
        return false;
    }

    private UUID processUids(Writer altIdWriter, Collection<UUID> componentIds) throws IOException {
        Iterator<UUID> componentIdItr = componentIds.iterator();
        UUID firstComponentId = componentIdItr.next();
        while (componentIdItr.hasNext()) {
            UUID altId = componentIdItr.next();
            altIdWriter.append(altId.toString());
            altIdWriter.append('\t');
            altIdWriter.append(firstComponentId.toString());
            altIdWriter.append('\n');
        }
        return firstComponentId;
    }

    public void writeDescriptions(Writer descWriter, Writer altIdWriter, FILE_FORMAT format) throws IOException,
            TerminologyException {
        UUID currentId = getFirstUid(ArchitectonicAuxiliary.Concept.CURRENT);
        for (I_DescribeConceptLocally d : descMap.values()) {
            if (inExclusionList(exclusions, d.getConcept())) {
                continue;
            }

            UUID firstComponentId = processUids(altIdWriter, d.getUids());
            switch (format) {
            case SNOMED:
                descWriter.append(firstComponentId.toString());
                descWriter.append('\t');
                descWriter.append(currentId.toString());
                descWriter.append('\t');
                descWriter.append(d.getConcept().getUids().iterator().next().toString());
                descWriter.append('\t');
                descWriter.append(d.getText());
                descWriter.append('\t');
                descWriter.append(new Boolean(d.isInitialCapSig()).toString());
                descWriter.append('\t');
                descWriter.append(d.getDescType().getUids().iterator().next().toString());
                descWriter.append('\t');
                descWriter.append(d.getLangCode());
                descWriter.append('\n');
                break;

            case ACE:
                descWriter.append(firstComponentId.toString());
                descWriter.append('\t');
                descWriter.append(currentId.toString());
                descWriter.append('\t');
                descWriter.append(d.getConcept().getUids().iterator().next().toString());
                descWriter.append('\t');
                descWriter.append(d.getText());
                descWriter.append('\t');
                descWriter.append(new Boolean(d.isInitialCapSig()).toString());
                descWriter.append('\t');
                descWriter.append(d.getDescType().getUids().iterator().next().toString());
                descWriter.append('\t');
                descWriter.append(d.getLangCode());
                descWriter.append('\t');
                // Effective Date
                descWriter.append(dateFormatter.format(getEffectiveDate()));
                descWriter.append('\t');

                // Path UUID
                descWriter.append(getPathUuid().toString());
                descWriter.append('\n');
                break;

            default:
                throw new IOException("Can't handle format: " + format);
            }
        }
    }

    public void writeRelationships(Writer relWriter, Writer altIdWriter, FILE_FORMAT format) throws IOException,
            Exception {
        UUID currentId = getFirstUid(ArchitectonicAuxiliary.Concept.CURRENT);
        for (I_RelateConceptsLocally r : relMap.values()) {
            if (inExclusionList(exclusions, r.getC1()) || inExclusionList(exclusions, r.getC2())
                || inExclusionList(exclusions, r.getCharacteristic())
                || inExclusionList(exclusions, r.getRefinability()) || inExclusionList(exclusions, r.getRelType())) {
                continue;
            }

            UUID firstComponentId = processUids(altIdWriter, r.getUids());
            switch (format) {
            case SNOMED:
                relWriter.append(firstComponentId.toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getC1()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getRelType()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getC2()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getCharacteristic()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getRefinability()).toString());
                relWriter.append('\t');
                relWriter.append(new Integer(r.getRelGrp()).toString());
                relWriter.append('\n');
                break;

            case ACE:
                relWriter.append(firstComponentId.toString());
                relWriter.append('\t');
                relWriter.append(currentId.toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getC1()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getRelType()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getC2()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getCharacteristic()).toString());
                relWriter.append('\t');
                relWriter.append(getFirstUid(r.getRefinability()).toString());
                relWriter.append('\t');
                relWriter.append(new Integer(r.getRelGrp()).toString());
                relWriter.append('\t');

                // Effective Date
                relWriter.append(dateFormatter.format(getEffectiveDate()));
                relWriter.append('\t');

                // Path UUID
                relWriter.append(getPathUuid().toString());
                relWriter.append('\n');
                break;

            default:
                throw new IOException("Can't handle format: " + format);
            }
        }
    }

    private UUID getFirstUid(I_ManifestLocally component) throws IOException, TerminologyException {
        return component.getUids().iterator().next();
    }

    private UUID getFirstUid(I_ManifestUniversally component) throws IOException, TerminologyException {
        return component.getUids().iterator().next();
    }

    private UUID getFirstUid(Collection<UUID> ids) {
        return ids.iterator().next();
    }

    public void writeRoots(Writer rootsWriter, FILE_FORMAT format) throws IOException, TerminologyException {
        for (I_ConceptualizeLocally r : roots) {
            if (inExclusionList(exclusions, r)) {
                continue;
            }
            rootsWriter.append(getFirstUid(r).toString());
            rootsWriter.append('\n');
        }
    }

    public void writeExtensionTypes(Writer extensionTypeWriter, Writer altIdWriter, FILE_FORMAT format)
            throws IOException, TerminologyException {
        for (I_ConceptualizeLocally extensionType : getExtensionTypes()) {
            if (inExclusionList(exclusions, extensionType)) {
                continue;
            }
            UUID extensionTypeId = processUids(altIdWriter, extensionType.getUids());
            extensionTypeWriter.append(extensionTypeId.toString());
            extensionTypeWriter.append('\n');
        }
    }

    public void writeExtension(I_ConceptualizeLocally extensionType, Writer extensionWriter, Writer altIdWriter,
            FILE_FORMAT format) throws IOException, TerminologyException, IllegalArgumentException,
            IntrospectionException, IllegalAccessException, InvocationTargetException {
        for (Map.Entry<Integer, Map<Integer, I_ExtendLocally>> extEntry : extensions.entrySet()) {
            UUID componentUuid = getFirstUid(getUids(extEntry.getKey()));
            I_ExtendLocally extension = extEntry.getValue().get(extensionType.getNid());
            if (inExclusionList(exclusions, extensionType)) {
                continue;
            }
            if (extension != null) {
                UUID extensionId = processUids(altIdWriter, extension.getUids());
                I_ExtendUniversally universalExtension = extension.universalize();
                extensionWriter.append(extensionId.toString());
                extensionWriter.append('\t');
                extensionWriter.append(componentUuid.toString());
                for (PropertyDescriptor d : universalExtension.getDataDescriptors()) {
                    extensionWriter.append('\t');
                    Object datum = d.getReadMethod().invoke(universalExtension, (Object[]) null);
                    extensionWriter.append(datum.toString());
                }
                extensionWriter.append('\n');
            }
        }
    }

    public boolean isGenerateIds() {
        return generateIds;
    }

    public void setGenerateIds(boolean generateIds) {
        this.generateIds = generateIds;
    }

    public UUID getPathUuid() {
        return pathUuid;
    }

    public void setPathUuid(UUID pathUuid) {
        this.pathUuid = pathUuid;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * Sets the array of hierarchies that should be excluded when any of the
     * write methods are called to export the content of this TermServer
     */
    public void setExclusions(TaxonomySpec[] exclusions) {
        this.exclusions = exclusions;
    }

	@Override
	public Collection<I_ConceptualizeLocally> getConcepts() throws IOException,
			TerminologyException {
		return conceptMap.values();
	}
}
