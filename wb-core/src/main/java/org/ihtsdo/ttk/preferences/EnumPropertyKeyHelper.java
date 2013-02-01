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



package org.ihtsdo.ttk.preferences;

//~--- JDK imports ------------------------------------------------------------

import java.nio.charset.Charset;

import java.util.UUID;
import java.util.prefs.Preferences;

/**
 *
 * @author kec
 */
public class EnumPropertyKeyHelper {
    static final Charset charset = Charset.forName("UTF-8");
    
    public static String getKeyString(Class<?> key) {
        String keyString = key.getDeclaringClass().getCanonicalName();
        if (keyString.length() > Preferences.MAX_KEY_LENGTH) {
            String prefix = keyString.substring(0, Preferences.MAX_KEY_LENGTH -  40 - key.getDeclaringClass().getSimpleName().length());
            UUID   uuid   = UUID.nameUUIDFromBytes(key.getDeclaringClass().getCanonicalName().getBytes(charset));

            keyString = prefix + "." + uuid + "." + key.getDeclaringClass().getSimpleName();
        }

        return keyString;
        
    }

    public static String getKeyString(PreferenceWithDefaultEnumBI preferenceKey) {
        Enum key = (Enum) preferenceKey;
        String keyString = key.getDeclaringClass().getCanonicalName() + "." + key.name();

        if (keyString.length() > Preferences.MAX_KEY_LENGTH) {
            String prefix = keyString.substring(0, Preferences.MAX_KEY_LENGTH - key.name().length() - 40 - key.getDeclaringClass().getSimpleName().length());
            UUID   uuid   = UUID.nameUUIDFromBytes(key.getDeclaringClass().getCanonicalName().getBytes(charset));

            keyString = prefix + "." + uuid + "." + key.getDeclaringClass().getSimpleName() + "." + key.name();
        }

        return keyString;
    }
    
    public static String getKeyString(Enum key) {
        String keyString = key.getDeclaringClass().getCanonicalName() + "." + key.name();

        if (keyString.length() > Preferences.MAX_KEY_LENGTH) {
            String prefix = keyString.substring(0, Preferences.MAX_KEY_LENGTH - key.name().length() - 40 - key.getDeclaringClass().getSimpleName().length());
            UUID   uuid   = UUID.nameUUIDFromBytes(key.getDeclaringClass().getCanonicalName().getBytes(charset));

            keyString = prefix + "." + uuid + "." + key.getDeclaringClass().getSimpleName() + "." + key.name();
        }

        return keyString;
    }
}
