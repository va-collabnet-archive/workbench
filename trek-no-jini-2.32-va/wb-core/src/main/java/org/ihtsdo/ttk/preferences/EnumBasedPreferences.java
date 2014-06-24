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

import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import org.dwfa.bpa.util.AppInfoProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * A wrapper around the Java {@link Preferences} which allows a "prefixing"
 * of user preferences to allow different hierarchical trees of preferences.
 * This allows different WB versions and/or different WB users to have
 * their own preferences without interfering with each other.
 *
 * @author kec
 * @author ocarlsen
 */
public class EnumBasedPreferences {

    private Preferences preferences;
    private String appPrefix;

    enum Fields {
        CLASS_NAME
    }

    /**
     * Create an EnumBasedPreferences with the specified appPrefix.
     * Otherwise, different WB versions and/or different WB users will
     * overwrite the preferences of the previous version/user.
     * @param appPrefix A unique string to disambiguate subtrees of preferences.
     */
    public EnumBasedPreferences(String appPrefix) {
        this.appPrefix = appPrefix;
        this.preferences = Preferences.userRoot().node(appPrefix);
    }

    private EnumBasedPreferences(EnumBasedPreferences enumPref, Preferences preferences) {
        this.appPrefix = enumPref.appPrefix;
        this.preferences = preferences;
    }

    public void flush() throws BackingStoreException {
        preferences.flush();
    }

    public void sync() throws BackingStoreException {
        preferences.sync();
    }

    public void putEnum(PreferenceWithDefaultEnumBI key, Enum value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.put(key.name(), value.name());
    }

    public Enum getEnum(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        Enum defaultValue = (Enum) key.getDefaultValue();
        String name = enumNode.preferences.get(key.name(), defaultValue.name());
        return Enum.valueOf(defaultValue.getClass(), name);
    }

    public void write(PreferenceObject preference) {
        preference.exportFields(this);
    }
    public void put(PreferenceWithDefaultEnumBI key, String value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.put(key.name(), value);
    }

//  public <T extends Enum<T>> void put(Class<T> key, String value) {
//      preferences.put(EnumPropertyKeyHelper.getKeyString(key), value);
//  }

    public String get(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.get(key.name(), (String) key.getDefaultValue());
    }

//    public <T extends Enum<T>> String get(Class<T> key, String value) {
//        return preferences.get(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public void remove(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.remove(key.name());
    }

//    public <T extends Enum<T>> void remove(Class<T> key) {
//        preferences.remove(EnumPropertyKeyHelper.getKeyString(key));
//    }

    public void clear() throws BackingStoreException {
        preferences.clear();
    }

    public void putInt(PreferenceWithDefaultEnumBI key, int value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.putInt(key.name(), value);
    }

//    public <T extends Enum<T>> void putInt(Class<T> key, int value) {
//        preferences.putInt(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public int getInt(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.getInt(key.name(), (Integer) key.getDefaultValue());
    }

//    public <T extends Enum<T>> int getInt(Class<T> key, int value) {
//        return preferences.getInt(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public void putLong(PreferenceWithDefaultEnumBI key, long value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.putLong(key.name(), value);
    }

//    public <T extends Enum<T>> void putLong(Class<T> key, long value) {
//        preferences.putLong(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public long getLong(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.getLong(key.name(), (Long) key.getDefaultValue());
    }

//    public <T extends Enum<T>> long getLong(Class<T> key, long value) {
//        return preferences.getLong(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public void putBoolean(PreferenceWithDefaultEnumBI key, boolean value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.putBoolean(key.name(), value);
    }

//    public <T extends Enum<T>> void putBoolean(Class<T> key, boolean value) {
//        preferences.putBoolean(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public boolean getBoolean(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.getBoolean(key.name(), (Boolean) key.getDefaultValue());
    }

//    public <T extends Enum<T>> boolean getBoolean(Class<T> key, boolean value) {
//        return preferences.getBoolean(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public void putList(PreferenceWithDefaultEnumBI key, List<? extends PreferenceObject> value) {
        // Need to erase existing list if it exists...
        EnumBasedPreferences enumNode = getNode(key);
        int oldCount = enumNode.preferences.getInt(key.name(), 0);
        for (int i = 0; i < oldCount; i++) {
            String indexNodeKey = Integer.toString(i);
            EnumBasedPreferences indexNode = enumNode.childNode(indexNodeKey);
            try {
                indexNode.removeNode();
            } catch (BackingStoreException ex) {
                Logger.getLogger(EnumBasedPreferences.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        
        int count = value.size();

        enumNode.preferences.putInt(key.name(), count);

        for (int i = 0; i < count; i++) {
            String indexNodeKey = Integer.toString(i);
            PreferenceObject item = value.get(i);
            EnumBasedPreferences indexNode = enumNode.childNode(indexNodeKey);
            String fieldsNodeKey = EnumPropertyKeyHelper.getKeyString(Fields.CLASS_NAME);
            EnumBasedPreferences fieldsNode = indexNode.childNode(fieldsNodeKey);
            fieldsNode.preferences.put(Fields.CLASS_NAME.name(), item.getClass().getName());
            item.exportFields(indexNode);
        }
    }

    public List<? extends PreferenceObject> getList(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        String keyString = key.name();
        int count = enumNode.preferences.getInt(keyString,
                (Integer) key.getDefaultValue());

        List<PreferenceObject> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            try {
                String indexNodeKey = Integer.toString(i);
                EnumBasedPreferences indexNode = enumNode.childNode(indexNodeKey);
                String fieldsNodeKey = EnumPropertyKeyHelper.getKeyString(Fields.CLASS_NAME);
                EnumBasedPreferences fieldsNode = indexNode.childNode(fieldsNodeKey);
                String className = fieldsNode.preferences.get(Fields.CLASS_NAME.name(), "");

                // Instantiate class.
                Class itemClass = Class.forName(className);
                Constructor constructor = itemClass.getConstructor(EnumBasedPreferences.class);
                PreferenceObject item = (PreferenceObject) constructor.newInstance(indexNode);

                list.add(item);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                     | InvocationTargetException | NoSuchMethodException | SecurityException
                     | ClassNotFoundException ex) {
                Logger.getLogger(EnumBasedPreferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return list;
    }

    public void putFloat(PreferenceWithDefaultEnumBI key, float value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.putFloat(key.name(), value);
    }

//    public <T extends Enum<T>> void putFloat(Class<T> key, float value) {
//        preferences.putFloat(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public float getFloat(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.getFloat(key.name(), (Float) key.getDefaultValue());
    }

//    public <T extends Enum<T>> float getFloat(Class<T> key, float value) {
//        return preferences.getFloat(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public void putDouble(PreferenceWithDefaultEnumBI key, double value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.putDouble(key.name(), value);
    }

//    public <T extends Enum<T>> void putDouble(Class<T> key, double value) {
//        preferences.putDouble(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public double getDouble(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.getDouble(key.name(), (Double) key.getDefaultValue());
    }

//    public <T extends Enum<T>> double getDouble(Class<T> key, double value) {
//        return preferences.getDouble(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public void putByteArray(PreferenceWithDefaultEnumBI key, byte[] value) {
        EnumBasedPreferences enumNode = getNode(key);
        enumNode.preferences.putByteArray(key.name(), value);
    }

//    public <T extends Enum<T>> void putByteArray(Class<T> key, byte[] value) {
//        preferences.putByteArray(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public byte[] getByteArray(PreferenceWithDefaultEnumBI key) {
        EnumBasedPreferences enumNode = getNode(key);
        return enumNode.preferences.getByteArray(key.name(), (byte[]) key.getDefaultValue());
    }

//    public <T extends Enum<T>> byte[] getByteArray(Class<T> key, byte[] value) {
//        return preferences.getByteArray(EnumPropertyKeyHelper.getKeyString(key), value);
//    }

    public String[] keys() throws BackingStoreException {
        return preferences.keys();
    }

    public String[] childrenNames() throws BackingStoreException {
        return preferences.childrenNames();
    }

    public EnumBasedPreferences parent() {
        return new EnumBasedPreferences(this, preferences.parent());
    }

    public EnumBasedPreferences node(String pathName) {
        if (pathName.startsWith("/") && !pathName.startsWith(appPrefix)) {
            pathName = appPrefix + pathName;
        }

        return new EnumBasedPreferences(this, preferences.node(pathName));
    }

    public boolean nodeExists(String pathName) throws BackingStoreException {
        if (pathName.startsWith("/") && !pathName.startsWith(appPrefix)) {
            pathName = appPrefix + pathName;
        }

        return preferences.nodeExists(pathName);
    }

    public void removeNode() throws BackingStoreException {
        preferences.removeNode();
    }

    public String name() {
        return preferences.name();
    }

    public String absolutePath() {
        return preferences.absolutePath();
    }

    public boolean isUserNode() {
        return preferences.isUserNode();
    }

    @Override
    public String toString() {
        return preferences.toString();
    }

    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.addPreferenceChangeListener(pcl);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.removePreferenceChangeListener(pcl);
    }

    public void addNodeChangeListener(NodeChangeListener ncl) {
        preferences.addNodeChangeListener(ncl);
    }

    public void removeNodeChangeListener(NodeChangeListener ncl) {
        preferences.removeNodeChangeListener(ncl);
    }

    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        preferences.exportNode(os);
    }

    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        preferences.exportSubtree(os);
    }

    public EnumBasedPreferences childNode(String name) {
        return new EnumBasedPreferences(this, preferences.node(preferences.absolutePath() + "/" + name));
    }

    private EnumBasedPreferences getNode(PreferenceWithDefaultEnumBI key) {
        String enumNodeKey = EnumPropertyKeyHelper.getKeyString(key);
        EnumBasedPreferences enumNode = childNode(enumNodeKey);
        return enumNode;
    }

    /**
     * Compute an 'app prefix' to be used as the root of the user preference hierarchy.
     * @param groupId The Maven ${groupId} property.
     * @param artifactId The Maven ${artifactId} property.
     * @param version The Maven ${version} property.
     * @param userName The name of the workbench user whose preferences will be accessed.
     * @return A String to pass into the {@link EnumBasedPreferences} constructor.
     */
    public static String getDefaultAppPrefix(String groupId, String artifactId, String version, String userName) {
        return groupId + ";" + artifactId + ";" + version + ";" + userName;
    }
    
    
    /**
     * Gets the default app prefix from the specified {@link Properties} object,
     * using the {@code GROUP_ID}, {@code ARTIFACT_ID}, and {@code VERSION} constants
     * as keys.  
     *
     * @param appInfoProperties the app info properties
     * @param userName the user name
     * @return the default app prefix
     */
    public static String getDefaultAppPrefix(Properties appInfoProperties, String userName) {
        String groupId = appInfoProperties.getProperty(GROUP_ID);
        String artifactId = appInfoProperties.getProperty(ARTIFACT_ID);
        String version = appInfoProperties.getProperty(VERSION);
        return getDefaultAppPrefix(groupId, artifactId, version, userName);
    }
}
