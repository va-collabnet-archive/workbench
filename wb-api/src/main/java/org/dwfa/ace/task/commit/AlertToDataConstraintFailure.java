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
package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class AlertToDataConstraintFailure {

    public enum ALERT_TYPE {
        INFORMATIONAL, WARNING, ERROR, RESOLVED, OMG
    };

    private ALERT_TYPE alertType;

    private String alertMessage;

    private List<I_Fixup> fixOptions = new ArrayList<I_Fixup>();

    private transient JComponent rendererComponent;

    private ConceptChronicleBI conceptWithAlert;

    public AlertToDataConstraintFailure(ALERT_TYPE alertType, String alertMessage, ConceptChronicleBI conceptWithAlert) {
        super();
        this.alertType = alertType;
        this.alertMessage = alertMessage;
        this.conceptWithAlert = conceptWithAlert;
    }

    public ALERT_TYPE getAlertType() {
        return alertType;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public List<I_Fixup> getFixOptions() {
        return fixOptions;
    }

    public JComponent getRendererComponent() {
        return rendererComponent;
    }

    public void setRendererComponent(JComponent rendererComponent) {
        this.rendererComponent = rendererComponent;
    }

    public ConceptChronicleBI getConceptWithAlert() {
        return conceptWithAlert;
    }
    
    public I_GetConceptData getConceptDataWithAlert() {
        I_GetConceptData concept = null;
        try {
            concept = Terms.get().getConcept(conceptWithAlert.getNid());
        } catch (TerminologyException ex) {
            Logger.getLogger(AlertToDataConstraintFailure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AlertToDataConstraintFailure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return concept;
    }

    @Override
    public boolean equals(Object obj) {
        if (AlertToDataConstraintFailure.class.isAssignableFrom(obj.getClass())) {
            AlertToDataConstraintFailure another = (AlertToDataConstraintFailure) obj;
            if (!alertMessage.equals(another.alertMessage)) {
                return false;
            }
            if (!conceptWithAlert.equals(another.conceptWithAlert)) {
                return false;
            }
            if (!alertType.equals(another.alertType)) {
                return false;
            }
            if (fixOptions.size() != another.fixOptions.size()) {
                return false;
            }
            return true;
        }
        return false;
     }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] {alertMessage.hashCode(), conceptWithAlert.getNid() });
    }



}
