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
package org.dwfa.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Configuration {
    List<ServiceConfigOption> options;
    boolean rmiSecure = false;
    boolean localOnly = false;

    public Configuration(List<ServiceConfigOption> options, boolean rmiSecure, boolean localOnly) {
        super();
        this.options = options;
        this.rmiSecure = rmiSecure;
        this.localOnly = localOnly;
    }

    public Configuration(ServiceConfigOption[] options, boolean rmiSecure, boolean localOnly) {
        this(Arrays.asList(options), rmiSecure, localOnly);
    }

    public Configuration(ServiceConfigOption[] options, boolean rmiSecure) {
        this(Arrays.asList(options), rmiSecure, false);
    }

    public Configuration(List<ServiceConfigOption> options, boolean rmiSecure) {
        this(options, rmiSecure, false);
    }

    public void writeToFile(String fileName) throws IOException {
        writeToFile(new File(fileName));
    }

    /**
     * @param file
     */
    public void writeToFile(File file) throws IOException {
        file.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(file);
        fw.write(this.generateConfigString());
        fw.close();
    }

    public String generateConfigString() {
        StringBuffer buff = new StringBuffer();
        buff.append("import com.sun.jini.start.NonActivatableServiceDescriptor;\n");
        buff.append("import com.sun.jini.start.ServiceDescriptor;\n");
        buff.append("import com.sun.jini.config.ConfigUtil;\n");
        buff.append("import net.jini.url.httpmd.HttpmdUtil;\n\n");
        buff.append("import org.dwfa.jini.VHelp;\n\n");

        buff.append("com.sun.jini.start {\n");

        if (localOnly) {
            buff.append("   private static host = \"localhost\";\n");
        } else {
            buff.append("   private static host = ConfigUtil.getHostAddress();\n");
        }
        buff.append("   private static jiniPort = org.dwfa.jini.ConfigUtil.getJiniPort();\n");
        buff.append("   private static jiniPortUrlPart = org.dwfa.jini.ConfigUtil.getJiniPortUrlPart();\n");

        buff.append('\n');
        buff.append("   private static jskCodebase = ConfigUtil.concat(new String[] { \"http://\", host, jiniPortUrlPart, VHelp.addDlVersion(\"jsk-dl\")});\n");
        buff.append("   private static jskMdURL = ConfigUtil.concat(new String[] { \"httpmd://\", host, jiniPortUrlPart,  VHelp.addDlVersion(\"jsk-dl\"), \";sha=0\"});\n");
        buff.append("   private static jskCodebaseMd = HttpmdUtil.computeDigestCodebase(\"lib-dl\", jskMdURL);\n\n");
        for (ServiceConfigOption option : options) {
            if (option.isEnabled()) {
                String[] setupStrings = option.getSetupStrings(rmiSecure);
                for (int j = 0; j < setupStrings.length; j++) {
                    buff.append("   ");
                    buff.append(setupStrings[j]);
                    buff.append("\n");
                }
                buff.append("\n");
            }
        }

        buff.append("   static serviceDescriptors = new ServiceDescriptor[] {\n");
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).isEnabled()) {
                buff.append("     ");
                buff.append(options.get(i).getNonActivatableServiceDescriptor());
                if (i < options.size() - 1) {
                    buff.append(",");
                }
                buff.append("\n");
            }
        }

        buff.append("   };\n");
        buff.append("}\n");
        return buff.toString();
    }

    public boolean isRmiSecure() {
        return rmiSecure;
    }

    public void setRmiSecure(boolean rmiSecure) {
        this.rmiSecure = rmiSecure;
    }

    public List<ServiceConfigOption> getOptions() {
        return options;
    }

}
