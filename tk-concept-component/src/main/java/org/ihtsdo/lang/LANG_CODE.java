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
package org.ihtsdo.lang;

/**
 *
 * @author AKF
 */
public enum LANG_CODE {
    EN, EN_AU, EN_BZ, EN_CA, EN_GB, EN_IE, EN_JM, EN_NZ, EN_TT, EN_US, EN_ZA,
    LIT, ES, ES_AR, ES_BO, ES_CL, ES_CO, ES_CR, ES_DO, ES_EC, ES_ES, ES_GT,
    ES_HN, EX_MX, ES_NI, ES_PA, ES_PE, ES_ER, ES_PY, ES_SV, ES_UY, ES_VE, FR,
    FR_BE, FR_CA, FR_FR, FR_CH, FR_LU, FR_MC, DA, DA_DK, PL, SV, SV_FI, SV_SE,
    LT, LT_LT, ZH, ZH_CN, ZH_HK, ZH_CHS, ZH_CHT, ZH_MO, ZH_SG, ZH_TW, EN_ZZ;

    public String getFormatedLanguageCode() {
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

    public static LANG_CODE getLangCode(String name) {
        String result = null;
        String[] nameArray = name.split("-");
        if (nameArray.length == 1) {
            result = nameArray[0].toUpperCase();
        } else if (nameArray.length == 2) {
            result = nameArray[0].toUpperCase() + "_" + nameArray[1].toUpperCase();
        }
        return LANG_CODE.valueOf(result);
    }

    @Override
    public String toString() {
        return this.name();
    }
    
}
