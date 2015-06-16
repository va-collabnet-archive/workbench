/*
 * Copyright 2015 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.rf1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Takes RF2 files and adds the appropriate RF1 relationships.
 * 
 * @author aimeefurber
 *
 * @goal make-historical-rels
 * @phase process-sources
 */
public class MakeHistoricalRels extends AbstractMojo {

    /**
     * Directory containing the Stated Relationships file.
     *
     * @parameter
     * expression="${project.build.directory}/input-files/Terminology"
     * @required
     */
    private File statedRelationshipsDir;
    /**
     * Directory containing the Inferred relationships file.
     *
     * @parameter
     * expression="${project.build.directory}/input-files/Terminology"
     * @required
     */
    private File inferredRelationshipsDir;
    /**
     * Directory containing the Historical association file.
     *
     * @parameter
     * expression="${project.build.directory}/input-files/Refsets/Content"
     * @required
     */
    private File historicalAssociationDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        BufferedReader reader = null;
        BufferedWriter statedWriter = null;
        BufferedWriter inferredWriter = null;
        try {
            File[] historicalFiles = historicalAssociationDir.listFiles(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    if(name.startsWith("der2_cRefset_AssociationReference")){
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            File[] statedFiles = statedRelationshipsDir.listFiles(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    if(name.startsWith("sct2_StatedRelationship")){
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            File[] inferredFiles = inferredRelationshipsDir.listFiles(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    if(name.startsWith("sct2_Relationship")){
                        return true;
                    }else{
                        return false;
                    }
                }
            }); 
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(historicalFiles[0]), "UTF-8"));
            statedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(statedFiles[0], true), "UTF-8"));
            inferredWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(inferredFiles[0], true), "UTF-8"));
            
            String line = reader.readLine(); //read header
            line = reader.readLine();
            while(line != null){
                String[] parts = line.split("\t");
                //id	effectiveTime	active	moduleId	refsetId	referencedComponentId	targetComponent
                String effectiveTime = parts[1];
                String moduleSct = parts[3];
                String refsetSct = parts[4];
                String rcSct = parts[5];
                String targetSct = parts[6];
                
                String relType = getHistoricalRelType(refsetSct);
                String relTarget = getIsARelTarget(refsetSct);
                
                if(relType != null && relTarget != null){
                    //id	effectiveTime	active	moduleId	sourceId	destinationId
                    //relationshipGroup	typeId	characteristicTypeId	modifierId
                    String tab = "\t";
                    String end = "\r\n";
                    // RF1 historical rels are not released, they get a default ID of "FFFFFFFFF" 
                    // which converts to 0 for the sake of compatibility with existing method signatures
                    // a 0 will cause a random uuid to be generated
                    String id= "FFFFFFFFF";
                    String active = "1";
                    String isaType = "116680003";
                    String relGroup = "0";
                    String statedSct = "900000000000010007";
                    String inferredSct = "900000000000011006";
                    String modifierSct = "900000000000451002"; //exsitential
                    
                    //Stated Isa
                    statedWriter.append(id);
                    statedWriter.append(tab);
                    statedWriter.append(effectiveTime);
                    statedWriter.append(tab);
                    statedWriter.append(active);
                    statedWriter.append(tab);
                    statedWriter.append(moduleSct);
                    statedWriter.append(tab);
                    statedWriter.append(rcSct);
                    statedWriter.append(tab);
                    statedWriter.append(relTarget);
                    statedWriter.append(tab);
                    statedWriter.append(relGroup);
                    statedWriter.append(tab);
                    statedWriter.append(isaType);
                    statedWriter.append(tab);
                    statedWriter.append(statedSct);
                    statedWriter.append(tab);
                    statedWriter.append(modifierSct);
                    statedWriter.append(end);
                    
                    //Historical
                    statedWriter.append(id);
                    statedWriter.append(tab);
                    statedWriter.append(effectiveTime);
                    statedWriter.append(tab);
                    statedWriter.append(active);
                    statedWriter.append(tab);
                    statedWriter.append(moduleSct);
                    statedWriter.append(tab);
                    statedWriter.append(rcSct);
                    statedWriter.append(tab);
                    statedWriter.append(targetSct);
                    statedWriter.append(tab);
                    statedWriter.append(relGroup);
                    statedWriter.append(tab);
                    statedWriter.append(relType);
                    statedWriter.append(tab);
                    statedWriter.append("-1"); //from arf-econcept for historical characteristic type (see Sct2_RelRecord.java)
                    statedWriter.append(tab);
                    statedWriter.append(modifierSct);
                    
                    //Inferred Isa
                    inferredWriter.append(id);
                    inferredWriter.append(tab);
                    inferredWriter.append(effectiveTime);
                    inferredWriter.append(tab);
                    inferredWriter.append(active);
                    inferredWriter.append(tab);
                    inferredWriter.append(moduleSct);
                    inferredWriter.append(tab);
                    inferredWriter.append(rcSct);
                    inferredWriter.append(tab);
                    inferredWriter.append(relTarget);
                    inferredWriter.append(tab);
                    inferredWriter.append(relGroup);
                    inferredWriter.append(tab);
                    inferredWriter.append(isaType);
                    inferredWriter.append(tab);
                    inferredWriter.append(inferredSct);
                    inferredWriter.append(tab);
                    inferredWriter.append(modifierSct);
                    statedWriter.append(end);
                }
                line = reader.readLine();
            }
            
            reader.close();
            statedWriter.flush();
            statedWriter.close();
            inferredWriter.flush();
            inferredWriter.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MakeHistoricalRels.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MakeHistoricalRels.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MakeHistoricalRels.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            try {
//                reader.close();
//                statedWriter.close();
//                inferredWriter.close();
//            } catch (IOException ex) {
//                Logger.getLogger(MakeHistoricalRels.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }

    }
    
    //See TIG 7.4.2.3 
    private String getHistoricalRelType(String refsetSct){
        switch(refsetSct){
            case "900000000000523009": 
                return "149016008";    //POSSIBLY EQUIVALENT TO refset -- MAY BE A type
            case "900000000000526001": 
                return "370124000";    //REPLACED BY refset -- REPLACED BY type
            case "900000000000527005": 
                return "168666000";    //SAME AS refset -- SAME AS type
            case "900000000000528000": 
                return "159083000";    //WAS A refset -- WAS A type
            default: 
                break;
        }
        return null;
    }
    
    //In some cases there is more than one historical concept that could be used
    //we're just picking one since this will never make it into release files
    private String getIsARelTarget(String refsetSct){
        switch(refsetSct){
            case "900000000000523009":
                return"363660007";    //POSSIBLY EQUIVALENT TO refset -- Ambiguous concept
            case "900000000000526001":
                return"363664003";    //REPLACED BY refset -- Erroneous concept
            case "900000000000527005":
                return"363662004";    //SAME AS refset -- Duplicate concept
            case "900000000000528000":
                return"363664003";    //WAS A refset -- Erroneous concept
            default: 
                break;
        }
        return null;
    }
}
