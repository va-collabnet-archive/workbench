/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api.blueprint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class CreateOrAmendBlueprint contains methods for creating a terminology
 * generic blueprint. This blueprint can be constructed into a type of
 * <code>ComonentChronicleBI</code>. This is the preferred method for updating
 * or creating new components or concepts.
 *
 * @see TerminologyBuilderBI
 * @see ComponentChronicleBI
 */
public abstract class CreateOrAmendBlueprint implements PropertyChangeListener {

    private static UUID currentStatusUuid = null;
    private static UUID retiredStatusUuid = null;
    private UUID componentUuid;
    private UUID statusUuid;
    private ComponentVersionBI cv;
    private ViewCoordinate vc;
    private List<RefexCAB> annotations = new ArrayList<RefexCAB>();
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Long longId;
    private int longAuthorityNid;
    private String stringId;
    private int stringAuthorityNnid;
    private UUID extraUuid;
    private int uuidAuthortiyNid;
    private boolean hasAdditionalIds = false;
    private HashMap<Object, Integer> idMap = new HashMap<>();

    /**
     * Removes the specified property change listener.
     *
     * @param string the string describing the property name
     * @param propertyChangeListener the property change listener
     */
    public synchronized void removePropertyChangeListener(String string,
            PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(string, propertyChangeListener);
    }

    /**
     * Removes the specified property change listener.
     *
     * @param propertyChangeListener the property change listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * Adds the specified property change listener.
     *
     * @param string the string describing the property name
     * @param propertyChangeListener the property change listener
     */
    public synchronized void addPropertyChangeListener(String string, PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(string, propertyChangeListener);
    }

    /**
     * Adds the specified property change listener.
     *
     * @param propertyChangeListener the property change listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Instantiates a new create or amend blueprint.
     *
     * @param componentUuid the uuid of the component specified by this
     * blueprint
     * @param componentVersion the component version to create this blueprint
     * from
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is returned for
     * the view coordinate
     */
    public CreateOrAmendBlueprint(UUID componentUuid, ComponentVersionBI componentVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        try {
            if (Ts.get().usesRf2Metadata()) {
                currentStatusUuid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid();
                retiredStatusUuid = SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid();
            } else {
                currentStatusUuid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid();
                retiredStatusUuid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        statusUuid = currentStatusUuid;
        this.componentUuid = componentUuid;
        this.cv = componentVersion;
        this.vc = viewCoordinate;
        getAnnotationBlueprintsFromOriginal();
        pcs.addPropertyChangeListener(this);
    }

    /**
     * This method is implemented by the other component blueprint classes. This
     * allows the components to recompute their uuids if a dependent component
     * changes.
     *
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * give position or view coordinate
     */
    public abstract void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException;

    /**
     * Listens for a property change event in any of the component blueprint
     * classes and recomputes the blueprints' computed uuid if a dependent
     * component has changed.
     *
     * @param propertyChangeEvent the property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        try {
            recomputeUuid();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidCAB ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContradictionException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Gets a string representing the primordial uuid for the component
     * specified by the
     * <code>componentNid</code>. This is uuid associated with the first version
     * of the component.
     *
     * @param componentNid the nid associated with the component in question
     * @return a string representing the primordial uuid of the component in
     * question
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     */
    protected String getPrimoridalUuidString(int componentNid)
            throws IOException, InvalidCAB {
        ComponentBI component = Ts.get().getComponent(componentNid);
        if (component != null) {
            return component.getPrimUuid().toString();
        }
        throw new InvalidCAB("Can't find primordialUuid for: " + component);
    }

    /**
     * Gets a string representing the primordial uuid for the component
     * specified by the
     * <code>uuid/code>. This is uuid associated with the first version of the component.
     *
     * @param uuid the uuid of the component in question
     * @return a string representing the primordial uuid of the component in
     * question
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     */
    protected String getPrimoridalUuidString(UUID uuid)
            throws IOException, InvalidCAB {
        if (Ts.get().hasUuid(uuid)) {
            ComponentChronicleBI<?> component = Ts.get().getComponent(uuid);
            if (component == null) {
                return uuid.toString();
            }
            return Ts.get().getComponent(uuid).getPrimUuid().toString();
        }
        return uuid.toString();
    }

    /**
     * Gets the uuid of the component specified by this blueprint.
     *
     * @return the uuid of the component specified by this blueprint
     */
    public UUID getComponentUuid() {
        return componentUuid;
    }

    /**
     * Sets the uuid of the component specified by this blueprint.
     *
     * @param componentUuid the uuid of the component specified by this
     * blueprint
     */
    public void setComponentUuid(UUID componentUuid) {
        UUID oldUuid = this.componentUuid;
        this.componentUuid = componentUuid;
        pcs.firePropertyChange("componentUuid", oldUuid, this.componentUuid);
    }

    /**
     * Sets the component uuid. Does not fire a property change event for the
     * changed uuid. No dependent component uuids will be recomputed. This is
     * useful when setting the component uuid to a pre-determined uuid, and
     * purposefully not use a re-computable uuid.
     *
     * @param componentUuid the new component uuid no recompute
     */
    public void setComponentUuidNoRecompute(UUID componentUuid) {
        this.componentUuid = componentUuid;
    }

    /**
     * Gets the nid of the component specified by this blueprint.
     *
     * @return the nid of the component specified by this blueprint.
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getComponentNid() throws IOException {
        return Ts.get().getNidForUuids(componentUuid);
    }

    /**
     * Returns list of annotation blueprints associated with this component
     * blueprint. Gets a list from the original component if null.
     *
     * @return a list of annotation blueprints associated with this component
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more then one version is found for a
     * particular view coordinate
     */
    public List<RefexCAB> getAnnotationBlueprintsFromOriginal() throws IOException, InvalidCAB, ContradictionException {
        if (annotations.isEmpty() && cv != null) {
            if (cv.getRefexesActive(vc) != null) {
                Collection<? extends RefexVersionBI<?>> originalRefexes = cv.getRefexesActive(vc);
                if (!originalRefexes.isEmpty()) {
                    for (RefexVersionBI refex : originalRefexes) {
                        annotations.add(refex.makeBlueprint(vc));
                    }
                }
            }
        }
        return annotations;
    }

    /**
     * Returns list of annotation blueprints associated with this component
     * blueprint.
     *
     * @return a list of annotation blueprints associated with this component
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more then one version is found for a
     * particular view coordinate
     */
    public List<RefexCAB> getAnnotationBlueprints() throws IOException, InvalidCAB, ContradictionException {
        return annotations;
    }

    /**
     * Adds an annotation blueprint to be associated with this component
     * blueprint.
     *
     * @param annotationBlueprint the annotation blueprint to associate with
     * this component blueprint
     */
    public void addAnnotationBlueprint(RefexCAB annotationBlueprint) {
        annotations.add(annotationBlueprint);
    }

    /**
     * Replace the annotation blueprints associated with this blueprint with the
     * given list of
     * <code>annoationBlueprints</code>.
     *
     * @param annotationBlueprints the annotation blueprints to associate with
     * this component blueprint
     */
    public void replaceAnnotationBlueprints(List<RefexCAB> annotationBlueprints) {
        this.annotations = annotationBlueprints;
    }

    /**
     * Gets the uuid of the status associated with this component blueprint.
     *
     * @return the uuid of the status associated with this component blueprint
     */
    public UUID getStatusUuid() {
        return statusUuid;
    }

    /**
     * Gets the nid of the status associated with this component blueprint.
     *
     * @return the nid of the status associated with this component blueprint
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getStatusNid() throws IOException {
        return Ts.get().getNidForUuids(statusUuid);
    }

    /**
     * Sets the uuid of the status associated with this component blueprint.
     *
     * @param statusUuid the uuid of the status associated with this component
     * blueprint
     */
    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    /**
     * Sets this component blueprint's status to active.
     */
    public void setCurrent() {
        this.statusUuid = currentStatusUuid;
    }

    /**
     * Sets this component blueprint's status to retired.
     */
    public void setRetired() {
        this.statusUuid = retiredStatusUuid;
    }

    /**
     * Adds an additional
     * <code>long</code> ID to the component specified by this component
     * blueprint. Any SCT IDs to add to this concept should be added using this
     * method.
     *
     * @param longId the long identifier to add
     * @param authorityNid the authority associated with the identifier
     */
    public void addLongId(Long longId, int authorityNid) {
        this.longId = longId;
        this.longAuthorityNid = authorityNid;
        idMap.put(longId, authorityNid);
        hasAdditionalIds = true;
    }

    /**
     * Adds an additional
     * <code>String</code> ID to the component specified by this component
     * blueprint.
     *
     * @param stringId the string identifier to add
     * @param authorityNid the authority associated with the identifier
     */
    public void addStringId(String stringId, int authorityNid) {
        this.stringId = stringId;
        this.stringAuthorityNnid = authorityNid;
        idMap.put(stringId, authorityNid);
        hasAdditionalIds = true;
    }

    /**
     * Adds an additional
     * <code>UUID</code> ID to the component specified by this component
     * blueprint. This is a UUID in addition to the primordial uuid associated
     * with this concept. Use setComponentUuid to set the primordial uuid.
     *
     * @param extraUuid the uuid identifier to add
     * @param authorityNid the authority associated with the identifier
     * @see CreateOrAmendBlueprint#setComponentUuid(java.util.UUID)
     */
    public void addExtraUuid(UUID extraUuid, int authorityNid) {
        this.extraUuid = extraUuid;
        this.uuidAuthortiyNid = authorityNid;
        idMap.put(extraUuid, authorityNid);
        hasAdditionalIds = true;
    }

    /**
     * Returns true if any additional ids have been added to this blueprint.
     *
     * @return <code>true</code> if this blueprint has any additional ids
     */
    public boolean hasAdditionalIds() {
        return hasAdditionalIds;
    }

    /**
     * Returns a map of IDs as
     * <code>Objects</code> and their associated authority nids. The supported
     * IDs are currently: long, string, and uuid.
     *
     * @return a map of IDs as <code>Objects</code> and their associated
     * authority nids
     */
    public HashMap<Object, Integer> getIdMap() {
        return idMap;
    }
}
