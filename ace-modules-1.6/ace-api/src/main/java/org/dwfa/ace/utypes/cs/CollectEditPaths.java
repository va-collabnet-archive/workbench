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
package org.dwfa.ace.utypes.cs;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;

public class CollectEditPaths extends AbstractUncommittedProcessor {

    Set<UUID> pathSet = new HashSet<UUID>();

    public Set<UUID> getPathSet() {
        return pathSet;
    }

    @Override
    protected void processNewUniversalAcePath(UniversalAcePath path) {
        // Nothing to do...

    }

    @Override
    protected void processUncommittedUniversalAceConceptAttributesPart(UniversalAceConceptAttributesPart part) {
        pathSet.addAll(part.getPathId());

    }

    @Override
    protected void processUncommittedUniversalAceDescriptionPart(UniversalAceDescriptionPart part) {
        pathSet.addAll(part.getPathId());
    }

    @Override
    protected void processUncommittedUniversalAceExtByRefPart(UniversalAceExtByRefPart part) {
        pathSet.addAll(part.getPathUid());
    }

    @Override
    protected void processUncommittedUniversalAceIdentificationPart(UniversalAceIdentificationPart part) {
        pathSet.addAll(part.getPathId());

    }

    @Override
    protected void processUncommittedUniversalAceImagePart(UniversalAceImagePart part) {
        pathSet.addAll(part.getPathId());

    }

    @Override
    protected void processUncommittedUniversalAceRelationshipPart(UniversalAceRelationshipPart part) {
        pathSet.addAll(part.getPathId());
    }

}
