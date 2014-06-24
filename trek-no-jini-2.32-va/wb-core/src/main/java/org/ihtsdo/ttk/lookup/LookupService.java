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



package org.ihtsdo.ttk.lookup;

//~--- non-JDK imports --------------------------------------------------------

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author kec
 */
public class LookupService {
    private static final InstanceContent content = new InstanceContent();
    private static final AbstractLookup  lookup  = new AbstractLookup(content);

    private LookupService() {}

    public static AbstractLookup get() {
        return lookup;
    }
    
    public static void add(InstanceWrapper<?> wrapper)  {
        content.addPair(wrapper);
    }
}
