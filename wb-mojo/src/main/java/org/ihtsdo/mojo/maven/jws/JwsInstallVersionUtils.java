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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 *
 * @author Marc Campbell
 */
public class JwsInstallVersionUtils {
    
    public static JwsInstallVersion convertJsonToInstallVersion(JsonObject json) {
        String jreVersion = json.getString("jre_version");
        String manifestDate = json.getString("manifest_date");
        String manifestSha1 = json.getString("manifest_sha1");
        // 
        ArrayList<String> skipAlways = new ArrayList<>();
        JsonArray skipAlwaysJson = json.getJsonArray("skip_always");
        for (int i = 0; i < skipAlwaysJson.size(); i++) {
            skipAlways.add(skipAlwaysJson.getString(i));
        }
        // 
        ArrayList<String> skipOnUpdate = new ArrayList<>();
        JsonArray skipOnUpdateJson = json.getJsonArray("skip_on_update");
        for (int i = 0; i < skipOnUpdateJson.size(); i++) {
            skipOnUpdate.add(skipOnUpdateJson.getString(i));
    }
        // 
        String updatePriority = json.getString("update_priority");
        //
        JwsInstallVersion iVersion = new JwsInstallVersion(jreVersion,
                manifestDate,
                manifestSha1,
                skipAlways,
                skipOnUpdate,
                updatePriority);
    
        return iVersion;
    }
        
    public static JsonObject convertInstallVersionToJson(JwsInstallVersion iVersion) {
        JsonObjectBuilder objBuilder = Json.createObjectBuilder();
        objBuilder.add("jre_version", iVersion.jre_version);
        objBuilder.add("manifest_date", iVersion.manifest_date);
        objBuilder.add("manifest_sha1", iVersion.manifest_sha1);
        //
        JsonArrayBuilder skipAlwaysBuilder = Json.createArrayBuilder();
        for (String str : iVersion.skip_always) {
            skipAlwaysBuilder.add(str);
        }
        objBuilder.add("skip_always", skipAlwaysBuilder);
        //
        JsonArrayBuilder skipOnUpdateBuilder = Json.createArrayBuilder();
        for (String str : iVersion.skip_on_update) {
            skipOnUpdateBuilder.add(str);
        }
        objBuilder.add("skip_on_update", skipOnUpdateBuilder);
        //
        objBuilder.add("update_priority", iVersion.update_priority);
        
        JsonObject jsonResult = objBuilder.build();
        return jsonResult;
    }

    public static void writeJsonFile(JsonObject jsonObject, String outPath)
            throws FileNotFoundException {
        File fpOut = new File(outPath);
        fpOut.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(fpOut);
        try (JsonWriter jsonWriter = Json.createWriter(os)) {
            jsonWriter.writeObject(jsonObject);
        }
    }

}
