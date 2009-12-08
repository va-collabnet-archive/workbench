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
package org.dwfa.mojo.relformat.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ReleaseFormat")
public class ReleaseFormat {

    @XStreamAlias("Type")
    private String type;

    @XStreamAlias("Schema")
    private String schema;

    public ReleaseFormat(final String type, final String schema) {
        this.type = type;
        this.schema = schema;
    }

    public String getType() {
        return type;
    }

    public String getSchema() {
        return schema;
    }
}
