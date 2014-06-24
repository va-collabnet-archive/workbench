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

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.prefs.Preferences;

/**
 *
 * @author kec
 */
public class EnumPropertyKeyHelper {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Calculate a full-length key to be used when storing a preference with
     * the Java {@link Preferences} API.
     * <p>
     * Specifically, this method will take the package name and replace dots with
     * slashes, and if the class is an inner class, will replace the dollar sign
     * with a slash.  For example:
     * <ul>
     * <li><code>a.b.c.Class</code> will generate a keyString of <code>a/b/c/Class</code>.</li>
     * <li><code>a.b.c.Class.Inner</code> will generate a keyString of <code>a/b/c/Class/Inner</code>.</li>
     * </ul>
     * Thus a preference added with this key will be inserted hierarchically
     * with a unique path, and properties for inner classes will be visible at
     * the same level.
     *
     * @param keyClass the {@link Class} to use to generate the key.
     */
    public static String getFullKeyString(Class<?> keyClass) {
        return keyClass.getName().replace('.', '/').replace('$', '/');
    }

   /**
     * Helper method to calculate a key to be used by the {@link EnumBasedPreferences}
     * class.  Contains logic to shorten class name portion of key to a {@link UUID}
     * if it would otherwise exceed {@link Preferences#MAX_KEY_LENGTH}.
     *
     * @param keyClass
     */
     public static String getKeyString(Class<?> keyClass) {
        String keyString = getFullKeyString(keyClass);

        if (keyString.length() > Preferences.MAX_KEY_LENGTH) {
            keyString = shortenClassNameToUUID(keyClass);
        }

        return keyString;
    }

    /**
     * Helper method to get the class of {@code preferenceKey} and
     * call {@link #getKeyString(java.lang.Class)}.
     *
     * @param preferenceKey
     */
    public static String getKeyString(PreferenceWithDefaultEnumBI preferenceKey) {
        return getKeyString(preferenceKey.getClass());
    }


    /**
     * Helper method to get the class of {@code preferenceKey} and
     * call {@link #getKeyString(java.lang.Class)}.
     *
     * @param enumKey
     */
    public static String getKeyString(Enum enumKey) {
        return getKeyString(enumKey.getClass());
    }

    private static String shortenClassNameToUUID(Class<?> keyClass) {
        String prefix = keyClass.getPackage().getName().replace('.', '/');
        UUID uuid = UUID.nameUUIDFromBytes(keyClass.getSimpleName().getBytes(CHARSET));
        return prefix + "/" + uuid;
    }
}
