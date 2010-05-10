/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_DescriptionPart;

import java.io.Serializable;
import java.util.Comparator;

public final class DescriptionPartComparator implements Comparator<I_DescriptionPart>, Serializable {

    private static final long serialVersionUID = -4734608464574630228L;

    @Override public int compare(final I_DescriptionPart d1, final I_DescriptionPart d2) {
        return (int) Math.signum(d1.getVersion() - d2.getVersion());
    }
}
