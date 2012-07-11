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

/**
 *
 * @author kec
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

    public synchronized void removePropertyChangeListener(String string,
            PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(string, propertyChangeListener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(propertyChangeListener);
    }

    public synchronized void addPropertyChangeListener(String string, PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(string, propertyChangeListener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }

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

    public abstract void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException;

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

    public UUID getComponentUuid() {
        return componentUuid;
    }

    public void setComponentUuid(UUID componentUuid) {
        UUID oldUuid = this.componentUuid;
        this.componentUuid = componentUuid;
        pcs.firePropertyChange("componentUuid", oldUuid, this.componentUuid);
    }
    
    public void setComponentUuidNoRecompute(UUID componentUuid) {
        this.componentUuid = componentUuid;
    }

    public int getComponentNid() throws IOException {
        return Ts.get().getNidForUuids(componentUuid);
    }
    /**
     * Returns list of annotation blueprints, gets list from original component if null.
     * @return
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException 
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
    
    public List<RefexCAB> getAnnotationBlueprints() throws IOException, InvalidCAB, ContradictionException {
        return annotations;
    }
    
    public void addAnnotationBlueprint(RefexCAB annotationBlueprint){
        annotations.add(annotationBlueprint);
    }
    
    public void replaceAnnotationBlueprints(List<RefexCAB> annotationBlueprints){
        this.annotations = annotationBlueprints;
    }

    public UUID getStatusUuid() {
        return statusUuid;
    }

    public int getStatusNid() throws IOException {
        return Ts.get().getNidForUuids(statusUuid);
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    public void setCurrent() {
        this.statusUuid = currentStatusUuid;
    }

    public void setRetired() {
        this.statusUuid = retiredStatusUuid;
    }
}
