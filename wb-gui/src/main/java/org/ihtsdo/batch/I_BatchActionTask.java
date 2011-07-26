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
package org.ihtsdo.batch;

import java.util.List;
import javax.swing.JPanel;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
public interface I_BatchActionTask {

    JPanel getPanel();

    /**
     * getTask(EditCoordinate ec, ViewCoordinate vc) is responsible for:<br>
     * (1) updates all the values from the user GUI selections and<br>
     * (2) updates any UUID and NID cache values for efficient execution.
     *
     * @param ec
     * @param vc
     * @return
     */
    BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc) throws Exception;

    /**
     * updateExisting() updates the GUI objects based information from the current concept list.<br>
     * Call this routine each time the concept list content is changed.
     *
     * @param existingParents
     * @param existingRefsets
     * @param existingRoles
     */
    void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages);
}
