package org.ihtsdo.rf2.file.delta.snapshot.tasks;

import java.util.HashSet;

public class Converter extends AbstractTask {

	HashSet<AbstractTask> tasks;
	
	
	public Converter(){
//		tasks=new HashSet<AbstractTask>();
//		GenerateRF1ComponentHistory compoHistory=new GenerateRF1ComponentHistory();
//		GenerateRF1CptDescLangSubs langSubsets=new GenerateRF1CptDescLangSubs();
//		GenerateRF1Relationships relationships=new GenerateRF1Relationships();
//		GenerateRF1Descriptions descriptions=new GenerateRF1Descriptions();
//		GenerateRF1Concepts concepts=new GenerateRF1Concepts();
//		
//		tasks.add(compoHistory);
//		
//		tasks.add(langSubsets);
//		
//		tasks.add(relationships);
//
//		tasks.add(descriptions);
//
//		tasks.add(concepts);
		
	}
	
	public void execute(){
		for (AbstractTask task:tasks){
			
			task.execute();
			
		}
	}

}
