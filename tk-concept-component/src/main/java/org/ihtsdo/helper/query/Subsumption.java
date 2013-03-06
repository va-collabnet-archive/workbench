/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.helper.query;

/**
 * The Enumeration <code>Subsumption</code> represents the different types of subsumption used in a query.
 */
public enum Subsumption {
    /**
     *Represents "is".
     */
    IS,
    /**
     *Represents "is kind of".
     */
    IS_KIND_OF,
    /**
     *Represents "is child of".
     */
    IS_CHILD_OF,
    /**
     *Represents "is descendant of".
     */
    IS_DESCENDANT_OF
}
