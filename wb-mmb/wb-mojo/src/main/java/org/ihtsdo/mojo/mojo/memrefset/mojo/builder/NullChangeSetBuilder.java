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
package org.ihtsdo.mojo.mojo.memrefset.mojo.builder;

import java.util.UUID;

import org.ihtsdo.mojo.mojo.memrefset.mojo.ChangeSet;

/**
 * This class follows the null object pattern. We can use this class whereever a
 * ChangeSetBuilder is required
 * without the use of null values.
 */
public final class NullChangeSetBuilder implements ChangeSetBuilder {

    public CmrscsChangeSetBuilder withPathUUID(final UUID uuid) {
        return null;
    }

    public CmrscsChangeSetBuilder withRefsetUUID(final UUID uuid) {
        return null;
    }

    public CmrscsRefSetBuilder openRefset() {
        return null;
    }

    public CmrscsResultBuilder closeChangeSet() {
        return null;
    }

    public CmrscsChangeSetBuilder withTime(final Long time) {
        return null;
    }

    public ChangeSet build() {
        return null;
    }
}
