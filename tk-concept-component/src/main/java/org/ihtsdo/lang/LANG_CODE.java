/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.lang;

// TODO: Auto-generated Javadoc
/**
 * The Enum LANG_CODE.
 *
 * @see http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
 */
public enum LANG_CODE {
    
    /** The en. */
    EN, 
 /** The en au. */
 EN_AU, 
 /** The en bz. */
 EN_BZ, 
 /** The en ca. */
 EN_CA, 
 /** The en gb. */
 EN_GB, 
 /** The en ie. */
 EN_IE, 
 /** The en jm. */
 EN_JM, 
 /** The en nz. */
 EN_NZ, 
 /** The en tt. */
 EN_TT, 
 /** The en us. */
 EN_US, 
 /** The en za. */
 EN_ZA,
    
    /** The lit. */
    LIT, 
 /** The es. */
 ES, 
 /** The es ar. */
 ES_AR, 
 /** The es bo. */
 ES_BO, 
 /** The es cl. */
 ES_CL, 
 /** The es co. */
 ES_CO, 
 /** The es cr. */
 ES_CR, 
 /** The es do. */
 ES_DO, 
 /** The es ec. */
 ES_EC, 
 /** The es es. */
 ES_ES, 
 /** The es gt. */
 ES_GT,
    
    /** The es hn. */
    ES_HN, 
 /** The ex mx. */
 EX_MX, 
 /** The es ni. */
 ES_NI, 
 /** The es pa. */
 ES_PA, 
 /** The es pe. */
 ES_PE, 
 /** The es er. */
 ES_ER, 
 /** The es py. */
 ES_PY, 
 /** The es sv. */
 ES_SV, 
 /** The es uy. */
 ES_UY, 
 /** The es ve. */
 ES_VE, 
 /** The fr. */
 FR,
    
    /** The fr be. */
    FR_BE, 
 /** The fr ca. */
 FR_CA, 
 /** The fr fr. */
 FR_FR, 
 /** The fr ch. */
 FR_CH, 
 /** The fr lu. */
 FR_LU, 
 /** The fr mc. */
 FR_MC, 
 /** The da. */
 DA, 
 /** The da dk. */
 DA_DK, 
 /** The pl. */
 PL, 
 /** The nl. */
 NL, 
 /** The sv. */
 SV, 
 /** The sv fi. */
 SV_FI, 
 /** The sv se. */
 SV_SE,
    
    /** The lt. */
    LT, 
 /** The lt lt. */
 LT_LT, 
 /** The zh. */
 ZH, 
 /** The zh cn. */
 ZH_CN, 
 /** The zh hk. */
 ZH_HK, 
 /** The zh chs. */
 ZH_CHS, 
 /** The zh cht. */
 ZH_CHT, 
 /** The zh mo. */
 ZH_MO, 
 /** The zh sg. */
 ZH_SG, 
 /** The zh tw. */
 ZH_TW, 
 /** The en zz. */
 EN_ZZ;

    /**
     * Gets the formated language code.
     *
     * @return the string representing the language code and dialect code
     */
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
    
    /**
     * Gets the formated language without a dialect code.
     *
     * @return the string representing the language code with no dialect code
     */
    public String getFormatedLanguageNoDialectCode() {
        String name = this.name();
        String[] nameArray = name.split("_");
        return nameArray[0].toLowerCase();
    }

    /**
     * Gets the lang code.
     *
     * @param name the string representing the language code
     * @return the lang code
     */
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

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.name();
    }
    
}
