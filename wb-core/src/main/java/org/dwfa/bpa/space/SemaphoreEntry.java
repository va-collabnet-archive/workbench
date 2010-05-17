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
/*
 * Created on Apr 18, 2005
 */
package org.dwfa.bpa.space;

import net.jini.core.entry.Entry;

/**
 * @author kec
 *         See page 77 of JavaSpaces Principles, Patterns, and Practice
 * 
 */
public class SemaphoreEntry implements Entry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String resource;

    /**
     * 
     */
    public SemaphoreEntry(String resource) {
        super();
        this.resource = resource;
    }

    public SemaphoreEntry() {
        super();
    }

    /**
     * @return Returns the resource.
     */
    public String getResource() {
        return resource;
    }
}
