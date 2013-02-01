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

//~--- non-JDK imports --------------------------------------------------------


//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 *
 * @author kec
 */
public class EnumBasedPreferences {
    private Preferences preferences;
    private String      appPrefix;

    enum FIELDS { CLASS_NAME }

    /**
     *
     * @param appPrefix Of the form "/" + &lt;app node name&gt;
     */
    public EnumBasedPreferences(String appPrefix) {
        this.appPrefix   = appPrefix;
        this.preferences = Preferences.userRoot().node(appPrefix);
    }

    private EnumBasedPreferences(EnumBasedPreferences enumPref, Preferences preferences) {
        this.appPrefix   = enumPref.appPrefix;
        this.preferences = preferences;
    }

    public void flush() throws BackingStoreException {
        preferences.flush();
    }

    public void sync() throws BackingStoreException {
        preferences.sync();
    }

    public Enum getEnum(PreferenceWithDefaultEnumBI key) {
        return valueOf(key.getDefaultValue().getClass(),
                       preferences.get(EnumPropertyKeyHelper.getKeyString(key), 
                ((Enum) key.getDefaultValue()).name()));
    }

    public void putEnum(PreferenceWithDefaultEnumBI key, Enum value) {
        put(key, value.name());
    }

    public void put(PreferenceWithDefaultEnumBI key, String value) {
        preferences.put(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public String get(PreferenceWithDefaultEnumBI key) {
        return preferences.get(EnumPropertyKeyHelper.getKeyString(key), (String) key.getDefaultValue());
    }

    public <T extends Enum<T>> void put(Class<T> key, String value) {
        preferences.put(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> String get(Class<T> key, String value) {
        return preferences.get(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public void remove(PreferenceWithDefaultEnumBI key) {
        preferences.remove(EnumPropertyKeyHelper.getKeyString(key));
    }

    public <T extends Enum<T>> void remove(Class<T> key) {
        preferences.remove(EnumPropertyKeyHelper.getKeyString(key));
    }

    public void clear() throws BackingStoreException {
        preferences.clear();
    }

    public void putInt(PreferenceWithDefaultEnumBI key, int value) {
        preferences.putInt(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putInt(Class<T> key, int value) {
        preferences.putInt(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public int getInt(PreferenceWithDefaultEnumBI key) {
        return preferences.getInt(EnumPropertyKeyHelper.getKeyString(key), (Integer) key.getDefaultValue());
    }

    public <T extends Enum<T>> int getInt(Class<T> key, int value) {
        return preferences.getInt(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public void putLong(PreferenceWithDefaultEnumBI key, long value) {
        preferences.putLong(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putLong(Class<T> key, long value) {
        preferences.putLong(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public long getLong(PreferenceWithDefaultEnumBI key) {
        return preferences.getLong(EnumPropertyKeyHelper.getKeyString(key), (Long) key.getDefaultValue());
    }

    public <T extends Enum<T>> long getLong(Class<T> key, long value) {
        return preferences.getLong(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public void putBoolean(PreferenceWithDefaultEnumBI key, boolean value) {
        preferences.putBoolean(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putBoolean(Class<T> key, boolean value) {
        preferences.putBoolean(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public boolean getBoolean(PreferenceWithDefaultEnumBI key) {
        return preferences.getBoolean(EnumPropertyKeyHelper.getKeyString(key), (Boolean) key.getDefaultValue());
    }

    public <T extends Enum<T>> boolean getBoolean(Class<T> key, boolean value) {
        return preferences.getBoolean(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putList(PreferenceWithDefaultEnumBI key, List<? extends PreferenceObject> value) {
        int count = value.size();

        this.putInt(key, count);

        for (int i = 0; i < count; i++) {
            String               itemNodeKey = EnumPropertyKeyHelper.getKeyString(key) + "." + i;
            PreferenceObject     item        = value.get(i);
            EnumBasedPreferences itemNode    = childNode(itemNodeKey);

            itemNode.preferences.put(EnumPropertyKeyHelper.getKeyString(FIELDS.CLASS_NAME), item.getClass().getName());
            item.exportFields(itemNode);
        }
    }

    public List<? extends PreferenceObject> getList(PreferenceWithDefaultEnumBI key) {
        int                    count = getInt(key);
        List<PreferenceObject> list  = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            try {
                String               itemNodeKey = EnumPropertyKeyHelper.getKeyString(key) + "." + i;
                EnumBasedPreferences itemNode    = childNode(itemNodeKey);
                String               className   =
                    itemNode.preferences.get(EnumPropertyKeyHelper.getKeyString(FIELDS.CLASS_NAME), "");
                Class                itemClass   = Class.forName(className);
                Constructor          constructor = itemClass.getConstructor(EnumBasedPreferences.class);
                PreferenceObject     item        = (PreferenceObject) constructor.newInstance(itemNode);

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
        preferences.putFloat(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putFloat(Class<T> key, float value) {
        preferences.putFloat(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public float getFloat(PreferenceWithDefaultEnumBI key) {
        return preferences.getFloat(EnumPropertyKeyHelper.getKeyString(key), (Float) key.getDefaultValue());
    }

    public <T extends Enum<T>> float getFloat(Class<T> key, float value) {
        return preferences.getFloat(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public void putDouble(PreferenceWithDefaultEnumBI key, double value) {
        preferences.putDouble(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putDouble(Class<T> key, double value) {
        preferences.putDouble(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public double getDouble(PreferenceWithDefaultEnumBI key) {
        return preferences.getDouble(EnumPropertyKeyHelper.getKeyString(key), (Double) key.getDefaultValue());
    }

    public <T extends Enum<T>> double getDouble(Class<T> key, double value) {
        return preferences.getDouble(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public void putByteArray(PreferenceWithDefaultEnumBI key, byte[] value) {
        preferences.putByteArray(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public <T extends Enum<T>> void putByteArray(Class<T> key, byte[] value) {
        preferences.putByteArray(EnumPropertyKeyHelper.getKeyString(key), value);
    }

    public byte[] getByteArray(PreferenceWithDefaultEnumBI key) {
        return preferences.getByteArray(EnumPropertyKeyHelper.getKeyString(key), (byte[]) key.getDefaultValue());
    }

    public <T extends Enum<T>> byte[] getByteArray(Class<T> key, byte[] value) {
        return preferences.getByteArray(EnumPropertyKeyHelper.getKeyString(key), value);
    }

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
        if (pathName.startsWith("/") &&!pathName.startsWith(appPrefix)) {
            pathName = appPrefix + pathName;
        }

        return new EnumBasedPreferences(this, preferences.node(pathName));
    }

    public boolean nodeExists(String pathName) throws BackingStoreException {
        if (pathName.startsWith("/") &&!pathName.startsWith(appPrefix)) {
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

    public EnumBasedPreferences appNodeForPackage(Class<?> c) {
        return new EnumBasedPreferences(this, preferences.node(nodeName(c, true)));
    }

    public EnumBasedPreferences appNodeForClass(Class<?> c) {
        return new EnumBasedPreferences(this, preferences.node(nodeName(c, false)));
    }

    public EnumBasedPreferences childNode(String name) {
        return new EnumBasedPreferences(this, preferences.node(preferences.absolutePath() + "/" + name));
    }

    private String nodeName(Class c, boolean packageOnly) {
        String nodeName = c.getName();

        if (packageOnly) {
            int lastDot = nodeName.lastIndexOf('.');

            nodeName = nodeName.substring(0, lastDot);
        }

        return appPrefix + "/" + nodeName.replace('.', '/');
    }

    private Enum valueOf(Class enumTypeClass, String defaultName) {
        return Enum.valueOf(enumTypeClass, get(enumTypeClass, defaultName));
    }
}
