/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.blueprint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateOrAmendBlueprint.
 *
 * @author kec
 */
public abstract class CreateOrAmendBlueprint implements PropertyChangeListener {

    /** The current status uuid. */
    private static UUID currentStatusUuid = null;
    
    /** The retired status uuid. */
    private static UUID retiredStatusUuid = null;
    
    /** The component uuid. */
    private UUID componentUuid;
    
    /** The status uuid. */
    private UUID statusUuid;
    
    /** The cv. */
    private ComponentVersionBI cv;
    
    /** The vc. */
    private ViewCoordinate vc;
    
    /** The annotations. */
    private List<RefexCAB> annotations = new ArrayList<RefexCAB>();
    
    /** The pcs. */
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Removes the property change listener.
     *
     * @param string the string
     * @param propertyChangeListener the property change listener
     */
    public synchronized void removePropertyChangeListener(String string,
            PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(string, propertyChangeListener);
    }

    /**
     * Removes the property change listener.
     *
     * @param propertyChangeListener the property change listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * Adds the property change listener.
     *
     * @param string the string
     * @param propertyChangeListener the property change listener
     */
    public synchronized void addPropertyChangeListener(String string, PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(string, propertyChangeListener);
    }

    /**
     * Adds the property change listener.
     *
     * @param propertyChangeListener the property change listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Instantiates a new creates the or amend blueprint.
     *
     * @param componentUuid the component uuid
     * @param componentVersion the component version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
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
     * Recompute uuid.
     *
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public abstract void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException;

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
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
     * Gets the primoridal uuid string.
     *
     * @param nid the nid
     * @return the primoridal uuid string
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     */
    protected String getPrimoridalUuidString(int nid)
            throws IOException, InvalidCAB {
        ComponentBI component = Ts.get().getComponent(nid);
        if (component != null) {
            return component.getPrimUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(nid);
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCAB("Can't find primordialUuid for: " + component);
    }

    /**
     * Gets the primoridal uuid string.
     *
     * @param uuid the uuid
     * @return the primoridal uuid string
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     */
    protected String getPrimoridalUuidString(UUID uuid)
            throws IOException, InvalidCAB {
        if (Ts.get().hasUuid(uuid)) {
            ComponentChronicleBI<?> component = Ts.get().getComponent(uuid);
            if(component == null){
                return uuid.toString();
            }
            return Ts.get().getComponent(uuid).getPrimUuid().toString();
        }
        return uuid.toString();
    }

    /**
     * Gets the component uuid.
     *
     * @return the component uuid
     */
    public UUID getComponentUuid() {
        return componentUuid;
    }

    /**
     * Sets the component uuid.
     *
     * @param componentUuid the new component uuid
     */
    public void setComponentUuid(UUID componentUuid) {
        UUID oldUuid = this.componentUuid;
        this.componentUuid = componentUuid;
        pcs.firePropertyChange("componentUuid", oldUuid, this.componentUuid);
    }
    
    /**
     * Sets the component uuid no recompute.
     *
     * @param componentUuid the new component uuid no recompute
     */
    public void setComponentUuidNoRecompute(UUID componentUuid) {
        this.componentUuid = componentUuid;
    }

    /**
     * Gets the component nid.
     *
     * @return the component nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getComponentNid() throws IOException {
        return Ts.get().getNidForUuids(componentUuid);
    }
    
    /**
     * Returns list of annotation blueprints, gets list from original component if null.
     *
     * @return the annotation blueprints from original
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
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
     * Gets the annotation blueprints.
     *
     * @return the annotation blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public List<RefexCAB> getAnnotationBlueprints() throws IOException, InvalidCAB, ContradictionException {
        return annotations;
    }
    
    /**
     * Adds the annotation blueprint.
     *
     * @param annotationBlueprint the annotation blueprint
     */
    public void addAnnotationBlueprint(RefexCAB annotationBlueprint){
        annotations.add(annotationBlueprint);
    }
    
    /**
     * Replace annotation blueprints.
     *
     * @param annotationBlueprints the annotation blueprints
     */
    public void replaceAnnotationBlueprints(List<RefexCAB> annotationBlueprints){
        this.annotations = annotationBlueprints;
    }

    /**
     * Gets the status uuid.
     *
     * @return the status uuid
     */
    public UUID getStatusUuid() {
        return statusUuid;
    }

    /**
     * Gets the status nid.
     *
     * @return the status nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getStatusNid() throws IOException {
        return Ts.get().getNidForUuids(statusUuid);
    }

    /**
     * Sets the status uuid.
     *
     * @param statusUuid the new status uuid
     */
    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    /**
     * Sets the current.
     */
    public void setCurrent() {
        this.statusUuid = currentStatusUuid;
    }

    /**
     * Sets the retired.
     */
    public void setRetired() {
        this.statusUuid = retiredStatusUuid;
    }
}
