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
package org.ihtsdo.ttk.lookup;

import java.util.Collection;
import java.util.Collections;
import org.openide.util.lookup.AbstractLookup;

/**
 *
 * @author kec
 */
public class InstanceWrapper<T> extends AbstractLookup.Pair<T> {
    
    private T instance;
    private String id;
    private String displayName;
    private Collection<Object> instanceProperties;

    public InstanceWrapper(T instance, String id, String displayName, 
            Collection<Object> instanceProperties) {
        this.instance = instance;
        this.id = id;
        this.displayName = displayName;
        this.instanceProperties = instanceProperties;
    }

    public Collection<Object> getInstanceProperties() {
        if (instanceProperties == null) {
            return Collections.EMPTY_LIST;
        }
        return instanceProperties;
    }

    @Override
    protected boolean instanceOf(Class<?> c) {
        return c.isInstance(instance);
    }

    @Override
    protected boolean creatorOf(Object obj) {
        return instance == obj;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public Class<? extends T> getType() {
        return (Class<? extends T>) instance.getClass();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return "InstanceWrapper{" + "id=" + id + ", displayName=" + displayName + ", instanceProperties=" + instanceProperties + '}';
    }
    
}
