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
package org.ihtsdo.tk.api.blueprint;

/**
 *
 * @author kec
 */
public enum IdDirective {
    PRESERVE, GENERATE_RANDOM, GENERATE_HASH, 
    GENERATE_REFEX_CONTENT_HASH, GENERATE_RANDOM_CONCEPT_REST_HASH,
    PRESERVE_CONCEPT_REST_HASH
}