/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.country;

/**
 * TODO Add country codes from: http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#ZZ
 * @author kec
 */
public enum COUNTRY_CODE {
    AU, CA, CY, DK, EE, ES, GB,
    IS, LT, MT, NL, NZ,
    PL, SE, SG, SK, SI, US, ZZ;
    
    public String getFormatedCountryCode() {
        String result = "";
        String name = this.name();
        String[] nameArray = name.split("_");
        if (nameArray.length == 1) {
            result = nameArray[0].toLowerCase();
        } else if (nameArray.length == 2) {
            result = nameArray[0].toLowerCase() + "-" + nameArray[1].toUpperCase();
        }
        return result;
    }
}
