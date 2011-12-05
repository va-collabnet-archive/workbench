package org.ihtsdo.project.workflow.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

public class TestInterpreter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//saveKB();

	}

	public static void saveKB() {
		DecisionTableConfiguration dtableconfiguration =
			KnowledgeBuilderFactory.newDecisionTableConfiguration();
		dtableconfiguration.setInputType( DecisionTableInputType.XLS );

		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

		Resource xlsRes = ResourceFactory.newFileResource("/Users/alo/Desktop/test-dtable.xls");
		kbuilder.add( xlsRes,
				ResourceType.DTABLE,
				dtableconfiguration );

		if ( kbuilder.hasErrors() ) {
			System.err.print( kbuilder.getErrors() );
		}

		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

		try {
			ObjectOutput out = new ObjectOutputStream(new FileOutputStream("rules/workFlowCanada1.wfkb"));
			out.writeObject(kbase);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("KBase saved!");
	}

}
