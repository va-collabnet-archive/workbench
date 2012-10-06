/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.lang;

// TODO: Auto-generated Javadoc
/**
 * The Enum LANG_CODE.
 * TODO-javadoc: Alo says that the dialect is no longer part of the language, are we using these?
 *
 * @see http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
 */
public enum LANG_CODE {

    /**
     * English.
     */
    EN,
    /**
     * English (Australia).
     */
    EN_AU,
    /**
     * English (Belize).
     * 
     */
    EN_BZ,
    /**
     * English (Canadian).
     */
    EN_CA,
    /**
     * English (United Kingdom).
     */
    EN_GB,
    /**
     * English (Ireland).
     */
    EN_IE,
    /**
     * English (Jamaica).
     */
    EN_JM,
    /**
     * English (New Zealand).
     */
    EN_NZ,
    /**
     * English (Trinidad).
     */
    EN_TT,
    /**
     * English (United States).
     * 
     */
    EN_US,
    /**
     * English (South African).
     */
    EN_ZA,
    /**
     * Lithuanian.
     * TODO-javadoc: should be lt for 639-1 standard
     */
    LIT,
    /**
     * Spanish.
     */
    ES,
    /**
     * Spanish (Argentina).
     */
    ES_AR,
    /**
     * Spanish (Bolivia).
     */
    ES_BO,
    /**
     * Spanish (Chile).
     */
    ES_CL,
    /**
     * Spanish (Colombia).
     */
    ES_CO,
    /**
     * Spanish (Costa Rica).
     */
    ES_CR,
    /**
     * Spanish (Dominican Republic).
     */
    ES_DO,
    /**
     * Spanish (Ecuador).
     */
    ES_EC,
    /**
     * Spanish (Spain).
     */
    ES_ES,
    /**
     * Spanish (Guatemala).
     */
    ES_GT,
    /**
     * Spanish (Honduras).
     */
    ES_HN,
    /**
     * Spanish (Mexico).
     */
    EX_MX,
    /**
     * Spanish (Nicaragua).
     */
    ES_NI,
    /**
     * Spanish (Panama).
     */
    ES_PA,
    /**
     * Spanish (Peru).
     */
    ES_PE,
    /**
     * TODO-javadoc: can't find this
     */
    ES_ER,
    /**
     * Spanish (Paraguay).
     */
    ES_PY,
    /**
     * Spanish (El Salvador).
     */
    ES_SV,
    /**
     * Spanish (Uruguay).
     */
    ES_UY,
    /**
     * Spanish (Venezuela).
     */
    ES_VE,
    /**
     * French.
     */
    FR,
    /**
     * French (Belgium).
     */
    FR_BE,
    /**
     * French (Canada).
     */
    FR_CA,
    /**
     * French (France).
     */
    FR_FR,
    /**
     * French (Switzerland).
     */
    FR_CH,
    /**
     * French (Luxembourg).
     */
    FR_LU,
    /**
     * French (Monaco).
     */
    FR_MC,
    /**
     * Danish.
     */
    DA,
    /**
     * Danish (Denmark).
     */
    DA_DK,
    /**
     * Polish.
     */
    PL,
    /**
     * Dutch.
     */
    NL,
    /**
     * Swedish.
     */
    SV,
    /**
     * Swedish (Finland).
     */
    SV_FI,
    /**
     * Swedish (Sweden).
     */
    SV_SE,
    /**
     * Lithuanian.
     */
    LT,
    /**
     * Lithuanian (Lithuania).
     */
    LT_LT,
    /**
     * Chinese.
     */
    ZH,
    /**
     * Chinese (S).
     */
    ZH_CN,
    /**
     * Chinese (Hong Kong).
     */
    ZH_HK,
    /**
     * The zh chs.
     */
    ZH_CHS,
    /**
     * The zh cht.
     */
    ZH_CHT,
    /**
     * Chinese (Macau).
     */
    ZH_MO,
    /**
     * The zh sg.
     */
    ZH_SG,
    /**
     * The zh tw.
     */
    ZH_TW,
    /**
     * The en zz.
     */
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
