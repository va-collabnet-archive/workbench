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
package org.dwfa.ace.config;

import java.util.ArrayList;

import org.dwfa.config.CoreServices;
import org.dwfa.config.ServiceConfigOption;

public class AceReadOnlyServices extends ArrayList<ServiceConfigOption> {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    public AceReadOnlyServices() {
        super();

        // add(AceLocalServices.CO_LocalTransactionManager);
        // add(CoreServices.CO_TransactionAggregator);
        add(CO_AceViewer);

    }

    public static String ACE_READONLY_SERVICE = "Ace Viewer";
    public static String ACE_READONLY_SERVICE_PROP = "org.dwfa.ACE_READONLY_SERVICE";
    public static ServiceConfigOption CO_AceViewer = new ServiceConfigOption(ACE_READONLY_SERVICE,
        "config${/}ace.policy", "config${/}aceSecure.policy", ACE_READONLY_SERVICE_PROP, "Starts the Ace Viewer. ",
        true, "", null, CoreServices.dwaPath, "org.dwfa.ace.config.AceReadOnlyRunner",
        new String[] { "config${/}aceViewer.config" }, new String[] { "config${/}aceSecure.config" }, true, false,
        false, "");

}
