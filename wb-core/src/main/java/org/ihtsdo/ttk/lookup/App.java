package org.ihtsdo.ttk.lookup;

//~--- non-JDK imports --------------------------------------------------------

import java.util.Collection;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        InstanceContent         content     = new InstanceContent();
        AbstractLookup          lookup      = new AbstractLookup(content);
        String                  testObject  = "Test Object";
        String                  testObject2 = "Test Object 2";
        InstanceWrapper<String> instance    = new InstanceWrapper(testObject, "An ID", "A name",
                                                  Collections.singletonList("a property"));

        content.addPair(instance);
        String result = lookup.lookup(String.class);
        System.out.println("Found: " + result);

        instance = new InstanceWrapper(testObject2, "An ID 2", "A name 2", 
                Collections.singletonList("a property"));
        content.addPair(instance);

        result = lookup.lookup(String.class);
        
        System.out.println("Found: " + result);
        
        Collection<String> results = (Collection<String>) lookup.lookupAll(String.class);
        
        System.out.println("Found: " + results);
        
        Result<String> resultResult = lookup.lookupResult(String.class);
        
        System.out.println("Found result: " + resultResult);
        System.out.println("Found result: " + resultResult.allItems());
        
        Lookup.Template lt = new Lookup.Template<>(String.class, "An ID 2", null);
        
        Lookup.Item resultItem = lookup.lookupItem(lt);

        System.out.println("Found resultItem: " + resultItem);
        
        
       lt = new Lookup.Template<>(String.class, null, null);
       Lookup.Result lookupResult = lookup.lookup(lt);
       
       System.out.println("Found lookupResult: " + lookupResult.allItems());

        
     }
}
