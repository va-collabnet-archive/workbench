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
package org.ihtsdo.helper.owl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.ihtsdo.helper.msfile.FileToListReader;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;

/**
 *
 * Converts an OWL file into OwlConcepts.
 */
public class OwlConceptConverter {

    private File owlFile;
    private HashMap<String, OwlConcept> owlConcepts = new HashMap<>();
            
    public OwlConceptConverter(File owlFile) {
        this.owlFile = owlFile;
    }

    public void convert() throws IOException {
        ArrayList<String> fileList = FileToListReader.getFileList(owlFile);
        Iterator<String> iterator = fileList.iterator();
        String line = iterator.next();
        while (iterator.hasNext()) {
            if (line.contains("AnnotationAssertion")) {
                break; //this is the start of the content
            }
            line = iterator.next();
        }

        OwlConcept currentConcept = null;
        while (iterator.hasNext()) {
            String id = getId(line);
            boolean makeNewConcept = false;
            if(currentConcept == null){
                makeNewConcept = true;
            }else if (line.contains("SubClassOf")) {
                String[] parts = line.split(" ");
                if(!parts[0].contains(currentConcept.getId())){
                    makeNewConcept = true;
                }
            }else if(!line.contains(currentConcept.getId())){
                makeNewConcept = true;
            }
            if (currentConcept == null || makeNewConcept) {
                if (currentConcept != null) {
                    //add completed concept to map
                    owlConcepts.put(currentConcept.getId(), currentConcept); 
                }
                //start new concept
                currentConcept = new OwlConcept(id);
            }
            if (line.contains("rdfs")) {
                currentConcept.setLabel(getThingInQuotes(line));
            }
            if (line.contains("SCTID")) {
                currentConcept.setSctId(Integer.parseInt(getThingInQuotes(line)));
            }
            if (line.contains("FSN")) {
                currentConcept.setFsn(getThingInQuotes(line));
            }
            if (line.contains("FMAID")) {
                currentConcept.setFmaid(Integer.parseInt(getThingInQuotes(line)));
            }
            if (line.contains("SubClassOf")) {
                currentConcept.addParent(getParent(line));
            }
            line = iterator.next();
        }
        //add last completed concept to map
        owlConcepts.put(currentConcept.getId(), currentConcept); 
    }

    private String getThingInQuotes(String line) {
        int start = line.indexOf("\"");
        int end = line.lastIndexOf("\"");
        String thing = line.substring(start + 1, end);
        return thing;

    }

    private String getId(String line) {
        int end = line.indexOf(">");
        String part = line.substring(0, end);
        String id = null;
        if (part.contains("SubClassOf") || part.contains("rdfs")) {
            int start = part.lastIndexOf("/");
            id = part.substring(start + 1);
        } else {
            int start = line.lastIndexOf("<");
            end = line.lastIndexOf(">");
            part = line.substring(start + 1, end);
            start = part.lastIndexOf("/");
            id = part.substring(start + 1);
        }
        return id;
    }

    private String getParent(String line) {
        if(line.contains("owl:Thing")){
            //top-most parent
            return "thing"; 
        }
        
        int start = line.lastIndexOf("/");
        int end = line.lastIndexOf(">");
        String parent = line.substring(start + 1, end);
        return parent;
    }

    public HashMap<String, OwlConcept> getOwlConcepts() {
        return owlConcepts;
    }
}
