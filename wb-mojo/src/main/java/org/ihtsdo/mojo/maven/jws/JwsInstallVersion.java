/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.jws;

import java.util.ArrayList;

/**
 *
 * @author Marc Campbell
 */
public class JwsInstallVersion {

    String jre_version;  // "jre_7u60"
    String manifest_date; // "2014.07.03_14:30:20"
    String manifest_sha1; // "asdf1234"
    ArrayList<String> skip_always; // "jre"
    ArrayList<String> skip_on_update; // user-profiles/*, database/*, logfiles
    String update_priority; // "optional" | "required"    

    public JwsInstallVersion(String jre_version, 
            String manifest_date, 
            String manifest_sha1, 
            ArrayList<String> skip_always, 
            ArrayList<String> skip_on_update, 
            String update_priority) {
        this.jre_version = jre_version;
        this.manifest_date = manifest_date;
        this.manifest_sha1 = manifest_sha1;
        this.skip_always = skip_always;
        this.skip_on_update = skip_on_update;
        this.update_priority = update_priority;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\njre_version = ");
        sb.append(jre_version);
        sb.append("\nmanifest_date = ");
        sb.append(manifest_date);
        sb.append("\nmanifest_sha1 = ");
        sb.append(manifest_sha1);
        sb.append("\nskip_always = ");
        for (String str : skip_always) {
            sb.append(str);
            sb.append(":");
        }
        sb.append("\nskip_on_update = ");
        for (String str : skip_on_update) {
            sb.append(str);
            sb.append(":");
        }
        sb.append("\nupdate_priority = ");
        sb.append(update_priority);
        return sb.toString();
    }
    
}
