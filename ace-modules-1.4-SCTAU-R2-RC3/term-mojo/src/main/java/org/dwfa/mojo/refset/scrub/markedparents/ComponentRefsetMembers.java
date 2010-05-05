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
package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures information about a <code>ComponentRefsetKey</code>. Stores
 * <code>I_ThinExtByRefVersioned</code> members
 * for this component-refset combination.
 */
public final class ComponentRefsetMembers {

    private final ComponentRefsetKey componentRefsetKey;
    private final List<I_ThinExtByRefVersioned> members;

    public ComponentRefsetMembers(final ComponentRefsetKey componentRefsetKey) {
        this.componentRefsetKey = componentRefsetKey;
        members = new ArrayList<I_ThinExtByRefVersioned>();
    }

    public ComponentRefsetKey getComponentRefsetKey() {
        return componentRefsetKey;
    }

    public void addMember(final I_ThinExtByRefVersioned member) {
        members.add(member);
    }

    public void removeMember(final I_ThinExtByRefVersioned member) {
        members.remove(member);
    }

    public List<I_ThinExtByRefVersioned> getMembers() {
        // return a copy.
        return new ArrayList<I_ThinExtByRefVersioned>(members);
    }

    public int getMemberCount() {
        return members.size();
    }

}
