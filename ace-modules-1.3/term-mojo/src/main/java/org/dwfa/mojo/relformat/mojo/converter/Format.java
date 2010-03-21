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
package org.dwfa.mojo.relformat.mojo.converter;

import java.util.List;

// TODO: This is a duplicate class created to allow manven to automagically pick
// up the format configuration.
public final class Format {

    private String type;

    private boolean addfilename;

    private List<String> filters;

    public Format() {
        // for maven.
    }

    public Format(final String type, final List<String> filters, boolean addfilename) {
        this.type = type;
        this.addfilename = addfilename;
        this.filters = filters;
    }

    public String getType() {
        return type;
    }

    public boolean getaddfilename() {
        return addfilename;
    }

    public List<String> getFilters() {
        return filters;
    }

    public String toString() {
        return "[Format type=" + type + ", addfilename=" + addfilename + ", filters=" + filters + " ]";
    }
}
