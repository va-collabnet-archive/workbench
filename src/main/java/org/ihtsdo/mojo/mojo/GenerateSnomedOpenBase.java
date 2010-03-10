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
package org.ihtsdo.mojo.mojo;

import java.sql.DriverManager;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal openbase-snomed
 * 
 * @phase generate-resources
 */
public class GenerateSnomedOpenBase extends GenerateSnomedJDBC {

    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;

    public void setup() throws Exception {

        Class.forName("com.openbase.jdbc.ObDriver");
        String url = "jdbc:openbase://g5-1.informatics.com/snomed"; // Set here
        // your
        // hostname
        // and the
        // database
        // name
        getLog().info("OpenBase url: " + url);
        setConn(DriverManager.getConnection(url, "snomed", "sdo"));
        getConn().setAutoCommit(false);
        createTables();
        getConn().commit();
        getLog().info("Opened OpenBase database");
    }

    public String longDataType() {
        return "longlong";
    }
}
