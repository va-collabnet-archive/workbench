/*
 * Created on Mar 30, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.entry.AbstractEntry;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.process.I_QueueProcesses;

/**
 * @author kec
 * 
 */
public class LookupJiniAndLocal implements I_LookupServices {

	private static Logger logger = Logger.getLogger(LookupJiniAndLocal.class
			.getName());

	private static List<ServiceItem> localServices = Collections
			.synchronizedList(new ArrayList<ServiceItem>());

	ServiceDiscoveryManager sdm;

   private LookupCache queueCache;

	/**
	 * @throws RemoteException 
	 * 
	 */
	public LookupJiniAndLocal(ServiceDiscoveryManager sdm) throws RemoteException {
		super();
		this.sdm = sdm;
      ServiceTemplate tmpl = new ServiceTemplate(null,
            new Class[] { I_QueueProcesses.class }, null);
      ServiceItemFilter filter = null;
      ServiceListModel serviceListener = new ServiceListModel();
      this.queueCache = sdm.createLookupCache(tmpl, filter,
            serviceListener);

	}
   
   public ServiceItem lookupQueue(ServiceItemFilter filter) {
      return this.queueCache.lookup(filter);
   }
   public ServiceItem[] lookupQueue(ServiceItemFilter filter, int max) {
      return this.queueCache.lookup(filter, max);
   }
   
   private static class TemplateFilter implements ServiceItemFilter {
      ServiceTemplate tmpl;
      ServiceItemFilter filter;
      
      public TemplateFilter(ServiceTemplate tmpl, ServiceItemFilter filter) {
         super();
         this.tmpl = tmpl;
         this.filter = filter;
      }

      @SuppressWarnings("unchecked")
      public boolean check(ServiceItem item) {
         if (tmpl.serviceID != null) {
            if (tmpl.serviceID.equals(item.serviceID)) {
               return true;
            }
         }
         if (tmpl.serviceTypes != null) {
            for (Class serviceClass: tmpl.serviceTypes) {
               if (serviceClass.isAssignableFrom(item.service.getClass()) == false) {
                  return false;
               }
            }
         }
         if (tmpl.attributeSetTemplates != null) {
            Set<Entry> entrySet = new HashSet<Entry>(Arrays.asList(tmpl.attributeSetTemplates));
            if (item.attributeSets == null) {
               return false;
            } else {
               for (Entry e: item.attributeSets) {
                  if (entrySet.contains(e) == false) {
                     return false;
                  }
               }
            }
         }
         if (filter != null) {
            return filter.check(item);
         }
         return true;
      }
      
   }

	/**
	 * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
	 *      int, int, net.jini.lookup.ServiceItemFilter, long)
	 */
	public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches,
			int maxMatches, ServiceItemFilter filter, long waitDur)
			throws InterruptedException, RemoteException {
		Collection<ServiceItem> matches = this.lookupLocal(tmpl, filter);
		if (this.sdm != null) {
         if (I_QueueProcesses.class.isAssignableFrom(tmpl.serviceTypes[0])) {
            matches.addAll(Arrays.asList(this.queueCache.lookup(new TemplateFilter(tmpl, filter), maxMatches - matches.size())));
         } else {
            matches.addAll(Arrays.asList(this.sdm.lookup(tmpl, minMatches,
                  maxMatches - matches.size(), filter, waitDur)));
         }
		}
		return (ServiceItem[]) matches.toArray(new ServiceItem[matches.size()]);
	}

	/**
	 * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
	 *      int, net.jini.lookup.ServiceItemFilter)
	 */
	public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches,
			ServiceItemFilter filter) {
		Collection<ServiceItem> matches = this.lookupLocal(tmpl, filter);
		if (this.sdm != null) {
         if (I_QueueProcesses.class.isAssignableFrom(tmpl.serviceTypes[0])) {
            matches.addAll(Arrays.asList(this.queueCache.lookup(new TemplateFilter(tmpl, filter), maxMatches - matches.size())));
         } else {
            ServiceItem[] jiniMatches = this.sdm.lookup(tmpl, maxMatches
                  - matches.size(), filter);
            matches.addAll(Arrays.asList(jiniMatches));
         }
		}
		return (ServiceItem[]) matches.toArray(new ServiceItem[matches.size()]);
	}

	/**
	 * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
	 *      net.jini.lookup.ServiceItemFilter)
	 */
	public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter) {
		List<ServiceItem> matches = this.lookupLocal(tmpl, filter);
		if (matches.size() > 0) {
			return matches.get(0);
		}
		if (this.sdm != null) {
         if (I_QueueProcesses.class.isAssignableFrom(tmpl.serviceTypes[0])) {
            matches.addAll(Arrays.asList(this.queueCache.lookup(new TemplateFilter(tmpl, filter))));
            if (matches.size() > 0) {
               return matches.get(0);
            }
         } else {
            return this.sdm.lookup(tmpl, filter);
         }
		}
		return null;
	}

	/**
	 * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
	 *      net.jini.lookup.ServiceItemFilter, long)
	 */
	public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter,
			long waitDur) throws InterruptedException, RemoteException {
		List<ServiceItem> matches = this.lookupLocal(tmpl, filter);
		if (matches.size() > 0) {
			return matches.get(0);
		}
		if (this.sdm != null) {
         if (I_QueueProcesses.class.isAssignableFrom(tmpl.serviceTypes[0])) {
            matches.addAll(Arrays.asList(this.queueCache.lookup(new TemplateFilter(tmpl, filter))));
            if (matches.size() > 0) {
               return matches.get(0);
            }
         }
			return this.sdm.lookup(tmpl, filter, waitDur);
		}
		return null;
	}

	private List<ServiceItem> lookupLocal(ServiceTemplate tmpl,
			ServiceItemFilter filter) {
		List<ServiceItem> matches = new ArrayList<ServiceItem>();
		synchronized (LookupJiniAndLocal.localServices) {
			for (ServiceItem serviceItem : LookupJiniAndLocal.localServices) {
				if (checkServiceId(tmpl, serviceItem)) {
					if (checkServiceTypes(tmpl, serviceItem)) {
						if (checkEntries(tmpl, serviceItem)) {
							if ((filter == null) || (filter.check(serviceItem))) {
								matches.add(serviceItem);
							}
						}
					}
				}
			}
		}
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "Local services found: " + matches);
		}
		return matches;
	}

	/**
	 * @param tmpl
	 * @param serviceItem
	 */
	private boolean checkServiceId(ServiceTemplate tmpl, ServiceItem serviceItem) {
		if (tmpl.serviceID == null) {
			return true;
		}
		if (tmpl.serviceID.equals(serviceItem.serviceID)) {
			return true;
		}
		return false;
	}

	/**
	 * @param tmpl
	 * @param serviceItem
	 */
	@SuppressWarnings("unchecked")
	private boolean checkServiceTypes(ServiceTemplate tmpl,
			ServiceItem serviceItem) {
		if (tmpl.serviceTypes == null) {
			return true;
		}
		for (int i = 0; i < tmpl.serviceTypes.length; i++) {
			if (tmpl.serviceTypes[i].isAssignableFrom(serviceItem.service
					.getClass()) == false) {
				return false;
			}
		}
		return true;
	}

	private boolean checkEntries(ServiceTemplate tmpl, ServiceItem serviceItem) {
		if (tmpl.attributeSetTemplates == null) {
			return true;
		}
		if (tmpl.attributeSetTemplates.length == 0) {
			return true;
		}
		for (Entry tmplEntry: tmpl.attributeSetTemplates) {
			boolean found = false;
			for (Entry serviceEntry: serviceItem.attributeSets) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("comparing: " + tmplEntry + " and " + serviceEntry);
				}
				if (matches(tmplEntry, serviceEntry)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public void addLocalService(ServiceItem serviceItem) {
		LookupJiniAndLocal.addToLocalServices(serviceItem);
	}

	public static void addToLocalServices(ServiceItem serviceItem) {
		LookupJiniAndLocal.localServices.add(serviceItem);
	}

    /**
     * Returns <code>true</code> if the two arguments are of the same
     * class and for each entry field <em>F</em>, the arguments'
     * values for <em>F</em> on e1 is null (a wildcard), or if 
     * either both <code>null</code> or the
     * invocation of <code>equals</code> on one argument's value for
     * <em>F</em> with the other argument's value for <em>F</em> as
     * its parameter returns <code>true</code>. Will also return
     * <code>true</code> if both arguments are <code>null</code>. In
     * all other cases an invocation of this method will return
     * <code>false</code>.<p>
     *
     * @param e1  an entry object to match to e2
     * @param e2  an entry object to match to e1
     * @return <code>true</code> if the two arguments are equivalent 
     */
    public static boolean matches(Entry e1, Entry e2) {
	if (e1 == e2)
	    return true;

	// Note, if both e1 and e2 are null the previous test would
	// have returned true.
	if (e1 == null)
	    return true; // wildcard...
	
	if (e1.equals(e2)) {
		return true;
	}

	if (e1.getClass() != e2.getClass())
	    return false;

	Field[] fields = fieldInfo(e1);
	try {
	    // compare each field
	    for (int i = 0; i < fields.length; i++) {

		// f works for other since other is the same type as this
		Field f = fields[i];
		Object ov = f.get(e1);
		Object tv = f.get(e2);

		if (tv == ov)			// same obj or both null is OK
		    continue;
		if (ov == null)	// wild card value
			continue;
		if (!tv.equals(ov))		// not equals is not OK
		    return false;
	    }
	    return true;
	} catch (IllegalAccessException e) {
	    // should never happen, all entry fields are public
	    throw new AssertionError(e);
	}
    }
    private static WeakHashMap<Class, Field[]> fieldArrays;
    /**
     * Calculate the list of usable fields for this type
     */
    private static Field[] fieldInfo(Entry entry) {
	Field[] fields = null;

	synchronized (LookupJiniAndLocal.class) {
	    if (fieldArrays == null)
		fieldArrays = new WeakHashMap<Class, Field[]>();
	    else {
		fields = (Field[]) fieldArrays.get(entry.getClass());
		if (fields != null)
		    return fields;
	    }
	}
	/*
	 * Scan the array to see if we can use it or if we must build up
	 * a smaller array because we must skip some fields.  If so, we
	 * create an ArrayList and add the unskippable fields to it, and
	 * then fetch the array back out of it.
	 */
	final int SKIP_MODIFIERS =
	    (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL);
	fields = entry.getClass().getFields();
	ArrayList<Field> usable = null;
	for (int i = 0; i < fields.length; i++) {
	    // exclude this one?
	    if ((fields[i].getModifiers() & SKIP_MODIFIERS) != 0 ||
		(fields[i].getType().isPrimitive())) 
	    {
		if (usable == null) {		// first excluded: set up for it
		    usable = new ArrayList<Field>();	// allocate the list of usable
		    for (int j = 0; j < i; j++)	// earlier fields are usable
			usable.add(fields[j]);
		}
	    } else {				// not excluded
		if (usable != null)		// tracking usable fields?
		    usable.add(fields[i]);
	    }
	}
	if (usable != null)
	    fields = (Field[]) usable.toArray(new Field[usable.size()]);

	synchronized (AbstractEntry.class) {
	    // We could check to make sure someone else 
	    // has not already stuck a value for entry.getClass() in,
	    // fieldArrays but there should be no harm in an overwrite
	    // and if anything likely to be less efficient
	    fieldArrays.put(entry.getClass(), fields);
	}

	return fields;
    }
}