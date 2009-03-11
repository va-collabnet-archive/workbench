/**
 * 
 */
package org.dwfa.bpa.worker.task;

import java.util.Map;
import java.util.TreeMap;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

class JiniConfigWrapper implements Configuration {
  
  private Configuration jiniConfig;
  private Map<String, Object> entryMap = new TreeMap<String, Object>();

  JiniConfigWrapper(Configuration jiniConfig) {
    super();
    this.jiniConfig = jiniConfig;
  }

  @SuppressWarnings("unchecked")
  public Object getEntry(String component, String name, Class type)
      throws ConfigurationException {
    if (entryMap.containsKey(makeKey(component, name))) {
      return entryMap.get(makeKey(component, name));
    }
    return jiniConfig.getEntry(component, name, type);
  }

  private String makeKey(String component, String name) {
    return component + ": " + name;
  }

  @SuppressWarnings("unchecked")
  public Object getEntry(String component,
      String name,
      Class type,
      Object defaultValue)
      throws ConfigurationException {
    if (entryMap.containsKey(makeKey(component, name))) {
      return entryMap.get(makeKey(component, name));
    }
    return jiniConfig.getEntry(component, name, type, defaultValue);
  }

  @SuppressWarnings("unchecked")
  public Object getEntry(String component,
      String name,
      Class type,
      Object defaultValue,
      Object data) throws ConfigurationException {
    if (entryMap.containsKey(makeKey(component, name))) {
      return entryMap.get(makeKey(component, name));
    }
    return jiniConfig.getEntry(component, name, type, defaultValue, data);
  }
  
  public void addObject(String component,
      String name,
      Object value) {
    entryMap.put(makeKey(component, name), value);
  }
}