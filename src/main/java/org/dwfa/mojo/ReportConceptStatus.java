package org.dwfa.mojo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.graph.MojoGraph;
import org.dwfa.vodb.bind.ThinVersionHelper;


/**
*
* <h1>ReportConceptStatus</h1>
* <br/>
* <p>The <code>ReportConceptStatus</code> class queries the database to get details of concept status changes over time.</p>
* <p>This data is then used by an instance of the <code>MojoGraph</code> class to create a graphical representation</p>
* <p>of the details for use in a maven generated site.</p>
* <br/>
* <br/>
* @see <code>org.apache.maven.plugin.AbstractMojo</code>
* @see <code>org.dwfa.maven.graph.mojoGraph</code>
* @author PeterVawser 
* @goal reportstatus
*/
public class ReportConceptStatus extends AbstractMojo{
	
    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would 
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin. 
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;

    private class CheckConceptStatus implements I_ProcessConcepts{
		I_TermFactory termFactory;
		private int currentCount =0;
		private int retiredCount =0;
		private int totalConcepts = 0;
		
		private HashMap<String, double[][]> statusMap = new HashMap<String, double[][]>();
		private Vector currentStatusCounts ;
		private Vector retiredStatusCounts ;
		
		private double [][] pointData = new double[60][2];
		private double [][] curStatusData = new double[60][2];
		private double [][] retStatusData = new double[60][2];
		
		public CheckConceptStatus() throws Exception {
            termFactory = LocalVersionedTerminology.get();
            currentCount = 0;
            retiredCount = 0;
            for (int i=0; i < 60; i++){
            	pointData[i][0] = 0;
            	pointData[i][1] = 0;
            }
           
        }
		
		public HashMap<String, double[][]> getStatusCounts(){
			return statusMap;
		}
			
		public void processConcept(I_GetConceptData concept) throws Exception{
	
			I_Path architectonicPath = termFactory.getPath(
                    ArchitectonicAuxiliary.
                    Concept.ARCHITECTONIC_BRANCH.
                    getUids());

			I_Position latestOnArchitectonicPath = termFactory.newPosition(
                                architectonicPath,
                                Integer.MAX_VALUE);
			Set<I_Position> positionSet = new HashSet<I_Position>();
			positionSet.add(latestOnArchitectonicPath);
			
			//Get status 'CURRENT' concept
			I_IntSet statusTypeSet = termFactory.newIntSet();
			statusTypeSet.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
			statusTypeSet.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
			
			
			List<I_ConceptAttributeTuple> statusTuples = concept.getConceptAttributeTuples(statusTypeSet, positionSet);
			
			Iterator it = statusTuples.iterator();
			
			int conceptStatus = 0;
			int versionId = 0;
			while (it.hasNext()){
				I_ConceptAttributeTuple tuple = (I_ConceptAttributeTuple)it.next();
				conceptStatus = tuple.getConceptStatus();
				versionId = tuple.getVersion();
			}
			
			GregorianCalendar checkDate;
			GregorianCalendar prevCheckDate = new GregorianCalendar();
					
			for(int i = 59; i > -1; i--){
				checkDate = (GregorianCalendar)prevCheckDate.clone();
				prevCheckDate.add(Calendar.DAY_OF_MONTH, -1);
			
				curStatusData[i][1] = checkDate.getTime().getTime();
				retStatusData[i][1] = checkDate.getTime().getTime();
				
				int curentConceptCount = 0;	
				int retiredConceptCount = 0;
				
				long time = ThinVersionHelper.convert(versionId);
				Date partDate = new Date(time);	
				I_TermFactory tf = LocalVersionedTerminology.get();
				
				if (conceptStatus == tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids())){
												
					if( partDate.before(checkDate.getTime()) ){
						curentConceptCount++;
					}									
				}
				if (conceptStatus == tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids())){
					if( partDate.before(checkDate.getTime()) ){
						retiredConceptCount++;
					}
				}
				
				curStatusData[i][0] += curentConceptCount;
				retStatusData[i][0] += retiredConceptCount;
			}//End For Loop
			
			statusMap.put("CURRENT", curStatusData);
			statusMap.put("RETIRED", retStatusData);
									
		 }//End method processConcept
		 public String toString() {
		       return "conceptsProcessed: " + totalConcepts ;
		    }

		
	}//End class CheckConceptStatus
	
	 public void execute() throws MojoExecutionException, MojoFailureException {
		 try{
			 I_TermFactory termFactory = LocalVersionedTerminology.get();
			 CheckConceptStatus ccs = new CheckConceptStatus();
			 termFactory.iterateConcepts(ccs);
			 		 
			 MojoGraph mg = new MojoGraph(ccs.getStatusCounts(),MojoGraph.DataType.VALUEOVERTIME);
			 mg.setTitle("Concept status' for the past 60 days");
			 mg.setAxisLabels("Days","Number of Concepts");
			 mg.setSiteTitle("Concept Status' Progression");
			 mg.setSiteDesc("The progression of concept status' of a 60 day period are shown below in table and graph format.");
			 mg.createGraph();
			 
		 }catch(Exception e){
			 throw new MojoExecutionException(e.getLocalizedMessage(), e);
		 }
		 
	 }//End method execute
	 
}//End class ConceptStatusReportMojo