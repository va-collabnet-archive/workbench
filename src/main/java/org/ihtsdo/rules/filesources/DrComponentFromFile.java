package org.ihtsdo.rules.filesources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.testmodel.DrDescription;
import org.ihtsdo.testmodel.DrLanguageDesignationSet;
import org.ihtsdo.testmodel.DrRelationship;

public class DrComponentFromFile{

	private File conceptFile;
	private File descriptionFile;
	private File relationshipFile;
	private File associationsFile;
	private File languageFileEN;
	private File languageFileGB;

	private BufferedReader conceptReader;
	private BufferedReader descriptionReader;
	private BufferedReader relationshipReader;
	private BufferedReader associationsReader;
	
	private String previousDescriptionLine;
	private String previousRelationshipLine;
	private String previousAssociationsLine;
	private File relSortedFile;
	private File asoSortedFile;
	private HashMap<String, DrDescription> desigSetEN;
	private HashMap<String, DrDescription> desigSetGB;
	
	
	public static void main(String[] argsv){
		
		DrComponentFromFile cff = new DrComponentFromFile(new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedConcept.txt"),
				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedDescription.txt"), 
				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedRelationship.txt"),
				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedAssociations.txt"),
				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedENLanguage.txt"),
				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedGBLanguage.txt"));
		boolean found= true;
		while (found) {
			try {
				DrConcept concept= cff.getNextDrConcept();
				if(concept==null)
					found = false; 
				System.out.println("");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public DrComponentFromFile(File conceptFile, File descriptionFile, File relationshipFile, File associationsFile, File languageFileEN, File languageFileGB) {
		super();
		this.conceptFile = conceptFile;
		this.descriptionFile = descriptionFile;
		this.relationshipFile = relationshipFile;
		this.associationsFile= associationsFile;
		this.languageFileEN= languageFileEN;
		this.languageFileGB= languageFileGB;
		
		try {
			conceptReader= new BufferedReader(new FileReader(conceptFile));
			conceptReader.readLine();

			descriptionReader= new BufferedReader(new FileReader(descriptionFile));
			descriptionReader.readLine();
			
			File temp= new File("temp");
			temp.mkdir();
			
			relSortedFile= new File("relSorted.txt");
			FileSorter fs= new FileSorter(relationshipFile, relSortedFile, temp, new int[]{4,5});
			System.out.println("Sorting Relationships");
			fs.execute();
			System.out.println("Done");
			relationshipReader= new BufferedReader(new FileReader(relSortedFile));
			relationshipReader.readLine();
			
			asoSortedFile= new File("asoSorted.txt");
			fs= new FileSorter(associationsFile, asoSortedFile, temp, new int[]{5,6});
			System.out.println("Sorting Associations");
			fs.execute();
			System.out.println("Done");
			associationsReader= new BufferedReader(new FileReader(asoSortedFile));
			associationsReader.readLine();
			
			readDesignationSetsFromFile();
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public DrConcept getNextDrConcept() throws IOException {
		
		String conceptLine= conceptReader.readLine();
		if(conceptLine==null) 
			return null;
		DrConcept concept = new DrConcept();
		String[] values= conceptLine.split("\t");
		concept.setPrimordialUuid(values[0]);
		concept.setTime(Long.parseLong(values[1]));
		concept.setActive((values[2].trim().equals("1"))?true:false);
		//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
		concept.setDefined((values[4].trim().equals("900000000000128007"))?true:false);
		concept.setDescriptions(getDrDescriptions(concept));
		List<DrRelationship> rel=getDrRelationships(concept);
		List<DrRelationship> asoc = getDrAssociations(concept);
		if(asoc!=null && !asoc.isEmpty())rel.addAll(asoc);
		concept.setOutgoingRelationships(rel);
		
		DrLanguageDesignationSet deg = getDrLanguageDesignationSetEN(concept);
		DrLanguageDesignationSet deg2 = getDrLanguageDesignationSetGB(concept);
		if(deg!=null || deg2!=null){
			List<DrLanguageDesignationSet> degSet = new ArrayList<DrLanguageDesignationSet>();
			if(deg!=null)
				degSet.add(deg);
			if(deg2!=null)
				degSet.add(deg2);
			concept.setLanguageDesignationSets(degSet);
		}
		
		return concept;
	}
	
	
	private List<DrDescription> getDrDescriptions(DrConcept concept) throws IOException {
		
		String tempDescriptionLine= descriptionReader.readLine();
		if(tempDescriptionLine==null && previousDescriptionLine==null) 
			return null;
		
		List<DrDescription> descriptions = new ArrayList<DrDescription>();
		DrDescription desc;
		
		if(previousDescriptionLine!=null){
			String[] values= previousDescriptionLine.split("\t");

			if(values[4].equals(concept.getPrimordialUuid())){
				desc= new DrDescription();
				desc.setPrimordialUuid(values[0]);
				desc.setTime(Long.parseLong(values[1]));
				desc.setActive((values[2].trim().equals("1"))?true:false);
				//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
				desc.setConceptUuid(values[4]);
				desc.setLang(values[5]);
				desc.setTypeUuid(values[6]);
				desc.setText(values[7]);
				desc.setCaseSignificantCategory(values[8]);
				descriptions.add(desc);
			}
			previousDescriptionLine=null;
		}
		
		
		boolean found= true;
		if(tempDescriptionLine==null)      //in case of EOF
			found= false;
		
		while(found){
			
			String[] values= tempDescriptionLine.split("\t");
			// si next no es del concept la pasa q previous y corto el while
			if(values[4].equals(concept.getPrimordialUuid())){
				desc= new DrDescription();
				desc.setPrimordialUuid(values[0]);
				desc.setTime(Long.parseLong(values[1]));
				desc.setActive((values[2].trim().equals("1"))?true:false);
				//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
				desc.setConceptUuid(values[4]);
				desc.setLang(values[5]);
				desc.setTypeUuid(values[6]);
				desc.setText(values[7]);
				desc.setCaseSignificantCategory(values[8]);
				descriptions.add(desc);
				tempDescriptionLine= descriptionReader.readLine();
				if(tempDescriptionLine==null)      //in case of EOF
					found= false;
			}
			else{
				previousDescriptionLine=tempDescriptionLine;
				found=false;
			}
		}
		
		return descriptions;
	}
	
	
	private List<DrRelationship> getDrRelationships(DrConcept concept) throws IOException {
		
		String tempRelationshipLine= relationshipReader.readLine();
		if(tempRelationshipLine==null && previousRelationshipLine==null) 
			return null;
		
		List<DrRelationship> relationships = new ArrayList<DrRelationship>();
		DrRelationship rel;
		
		if(previousRelationshipLine!=null){
			String[] values= previousRelationshipLine.split("\t");

			if(values[4].equals(concept.getPrimordialUuid())){
				rel= new DrRelationship();
				rel.setPrimordialUuid(values[0]);
				rel.setTime(Long.parseLong(values[1]));
				rel.setActive((values[2].trim().equals("1"))?true:false);
				//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
				rel.setSourceUuid(values[4]);
				rel.setTargetUuid(values[5]);
				rel.setRelGroup(Integer.parseInt(values[6]));
				rel.setTypeUuid(values[7]);
				rel.setCharacteristicUuid(values[8]);
				rel.setModifierUuid(values[9]);
				relationships.add(rel);
			}
			previousRelationshipLine=null;
		}
		
		boolean found= true;
		if(tempRelationshipLine==null)      //in case of EOF
			found= false;
		
		while(found){
			
			String[] values= tempRelationshipLine.split("\t");
			// si next no es del concept la pasa q previous y corto el while
			if(values[4].equals(concept.getPrimordialUuid())){
				rel= new DrRelationship();
				rel.setPrimordialUuid(values[0]);
				rel.setTime(Long.parseLong(values[1]));
				rel.setActive((values[2].trim().equals("1"))?true:false);
				//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
				rel.setSourceUuid(values[4]);
				rel.setTargetUuid(values[5]);
				rel.setRelGroup(Integer.parseInt(values[6]));
				rel.setTypeUuid(values[7]);
				rel.setCharacteristicUuid(values[8]);
				rel.setModifierUuid(values[9]);
				relationships.add(rel);
				tempRelationshipLine= relationshipReader.readLine();
				if(tempRelationshipLine==null)      //in case of EOF
					found= false;
			}
			else{
				previousRelationshipLine=tempRelationshipLine;
				found=false;
			}
		}
		return relationships;
	}
	
	
	
	private List<DrRelationship> getDrAssociations(DrConcept concept) throws IOException {
		
			String tempAssociationsLine= associationsReader.readLine();
			if(tempAssociationsLine==null && previousAssociationsLine==null) 
				return null;
			
			List<DrRelationship> relationships = new ArrayList<DrRelationship>();
			DrRelationship rel;
			
			if(previousAssociationsLine!=null){
				String[] values= previousAssociationsLine.split("\t");
				
				if(values[4].equals(concept.getPrimordialUuid())){
					rel= new DrRelationship();
					rel.setPrimordialUuid(values[0]);
					rel.setTime(Long.parseLong(values[1]));
					rel.setActive((values[2].trim().equals("1"))?true:false);
					//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
					rel.setTypeUuid(values[4]);
					rel.setSourceUuid(values[5]);
					rel.setTargetUuid(values[6]);
					relationships.add(rel);
				}
				previousRelationshipLine=null;
			}
			
			boolean found= true;
			if(tempAssociationsLine==null)      //in case of EOF
				found= false;
			
			while(found){
				
				String[] values= tempAssociationsLine.split("\t");
				// si next no es del concept la pasa q previous y corto el while
				if(values[5].equals(concept.getPrimordialUuid())){
					rel= new DrRelationship();
					rel.setPrimordialUuid(values[0]);
					rel.setTime(Long.parseLong(values[1]));
					rel.setActive((values[2].trim().equals("1"))?true:false);
					//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
					rel.setTypeUuid(values[4]);
					rel.setSourceUuid(values[5]);
					rel.setTargetUuid(values[6]);
					relationships.add(rel);
					tempAssociationsLine= relationshipReader.readLine();
					if(tempAssociationsLine==null)      //in case of EOF
						found= false;
				}
				else{
					previousRelationshipLine=tempAssociationsLine;
					found=false;
				}
			}
			
			return relationships;
		}
		
	private void readDesignationSetsFromFile() throws NumberFormatException, IOException{
		
		BufferedReader languageENReader= new BufferedReader(new FileReader(languageFileEN));
		languageENReader.readLine();
		
		desigSetEN = new HashMap<String,DrDescription>();
		DrDescription desc;
		String[] values = null;
		String line;
		
		while((line= languageENReader.readLine()) != null){
				values= line.split("\t");

				desc= new DrDescription();
				//in this case the sctid field, stored in values[0] is useless, so i don't save it in the DrDescription object
				desc.setTime(Long.parseLong(values[1]));
				desc.setActive((values[2].trim().equals("1"))?true:false);
				//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
				desc.setLanguageRefsetUuid(values[4]);
				desc.setPrimordialUuid(values[5]);
				desc.setAcceptabilityUuid(values[6]);
				desigSetEN.put(values[5],desc);
		}
		languageENReader.close();
		
		BufferedReader languageGBReader= new BufferedReader(new FileReader(languageFileGB));
		languageGBReader.readLine();
		
		desigSetGB = new HashMap<String,DrDescription>();
		
		while((line= languageGBReader.readLine()) != null){
				values= line.split("\t");

				desc= new DrDescription();
				//in this case the sctid field, stored in values[0] is useless, so i don't save it in the DrDescription object
				desc.setTime(Long.parseLong(values[1]));
				desc.setActive((values[2].trim().equals("1"))?true:false);
				//module id (values[3]) is not been saved because there isn't similar attribute in DrConcept
				desc.setLanguageRefsetUuid(values[4]);
				desc.setPrimordialUuid(values[5]);
				desc.setAcceptabilityUuid(values[6]);
				desigSetGB.put(values[5],desc);
		}
		languageGBReader.close();
	}
	
	

	private DrLanguageDesignationSet getDrLanguageDesignationSetEN(DrConcept concept) throws IOException {
		
		List<DrDescription> descriptions = concept.getDescriptions();
		HashSet<DrDescription> descriptionsReturned= new HashSet<DrDescription>();
		DrDescription drD;
		
		for (DrDescription desc : descriptions) {
			if((drD= desigSetEN.get(desc.getPrimordialUuid()))!=null)
				descriptionsReturned.add(drD);
			
		}
		if(descriptionsReturned.isEmpty()) return null;
		
		DrLanguageDesignationSet ds = new DrLanguageDesignationSet();
		ds.setDescriptions(descriptionsReturned);
		ds.setLanguageRefsetUuid(descriptionsReturned.iterator().next().getLanguageRefsetUuid());
		
		return ds;
	}
	
	private DrLanguageDesignationSet getDrLanguageDesignationSetGB(DrConcept concept) throws IOException {
		
		List<DrDescription> descriptions = concept.getDescriptions();
		HashSet<DrDescription> descriptionsReturned= new HashSet<DrDescription>();
		DrDescription drD;
		
		for (DrDescription desc : descriptions) {
			if((drD= desigSetGB.get(desc.getPrimordialUuid()))!=null)
				descriptionsReturned.add(drD);
			
		}
		if(descriptionsReturned.isEmpty()) return null;
		
		DrLanguageDesignationSet ds = new DrLanguageDesignationSet();
		ds.setDescriptions(descriptionsReturned);
		ds.setLanguageRefsetUuid(descriptionsReturned.iterator().next().getLanguageRefsetUuid());
		
		return ds;
	}
	
		
}



