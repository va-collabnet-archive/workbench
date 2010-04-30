package org.ihtsdo.mojo.mojo.exporter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.mojo.mojo.exporter.publishers.I_PublishConcepts;
import org.ihtsdo.mojo.mojo.exporter.publishers.PublisherDTO;

/**
 * The <code>ExportMojo</code> class generates Export files files
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Adam Flinton
 * @goal aceExport
 * @phase process-resources
 */

public class Exporter extends AbstractMojo {

	private static final Logger log = Logger
			.getLogger(Exporter.class.getName());
	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;
	/**
	 * Positions to export data.
	 * 
	 * @parameter
	 * @required
	 */
	// private PositionDescriptor[] positionsForExport;

	/**
	 * Status values to include in export
	 * 
	 * @parameter
	 * @required
	 */
	// private ConceptDescriptor[] statusValuesForExport;

	/**
	 * The path to a properties file for use by the publisher
	 * 
	 * @parameter
	 * @required
	 */
	//private String propsFN;
	/**
	 * The (java) classname of the publisher you will be using
	 * 
	 * @parameter
	 * @required
	 */
	//private String publisherClassName;
	/**
	 * The path to the berkeley DB
	 * 
	 * @parameter
	 * @required
	 */
	private String dbPath;
	/**
	 * 1 or more Publisher DTO's
	 * 
	 * @parameter
	 * @required
	 */
	public PublisherDTO[] publishers;
	
	publisherController pubCon = new publisherController();
	

	private I_TermFactory tf = null;
	//private I_PublishConcepts publisher = null;

	private boolean pubOK = false;
	private boolean tfOK = false;
	
	Hashtable<String,Integer>uidI = new Hashtable();

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if(uidI ==null){
			uidI = new Hashtable();
		}
		if (isTfOK()) {
		processPublisherDTOs();
		}

		if (isTfOK()) {
		if(!pubOK){
			log.severe("Publisher not OK error = ");
			Iterator<I_PublishConcepts> pcIt = pubCon.getClassCache().values().iterator();
			while (pcIt.hasNext()) {
				I_PublishConcepts pubC = pcIt.next();
				if(!pubC.isPdtoOK()){
				log.severe("Error in "+pubC.getPubDTO().getName() +" Err message  = "+pubC.getErrorMsg());
				}
			}
			
		}	
		if (pubOK) {
			long start = System.currentTimeMillis();
			try {	
				Terms.get().iterateConcepts(pubCon);
			} catch (Exception e) {
				log.log(Level.SEVERE,"Exporter execute() Error Iterating the concepts error = ",e);
			}
			long elapsedTimeMillis = System.currentTimeMillis()-start;
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			log.severe("execute found total concepts "+pubCon.getConCount()+" . It took "+elapsedTimeSec +" seconds" );
			Iterator<I_PublishConcepts> pcIt = pubCon.getClassCache().values().iterator();
			while (pcIt.hasNext()) {
				I_PublishConcepts pubC = pcIt.next();
				log.severe("Number of "+pubC.getPubDTO().getName() +" concepts found = "+pubC.getfoundConceptCount());
			}
			
		}
		}
		
	}
	
	private void processPublisherDTOs(){
		if(publishers != null && publishers.length > 0){
			for(PublisherDTO pdto : publishers){
				logPubDTO(pdto);
				String cn = pdto.getPublisherClassName();
				if(cn != null && cn.length() > 0){
					try {
						I_PublishConcepts publisher = getPublisher(cn);
						pubOK = publisher.init(pdto, tf,uidI);
						pubCon.getClassCache().put(publisher.getIntKey(), publisher);
					} catch (Exception e) {
						log.log(Level.SEVERE,"Exporter processPublisherDTOs() Error creating the publisher error = ",e);
						logPubDTO(pdto);
						pubOK = false;
					} 
				}
				else{
					pubOK = false;
					log.severe("No Publisher class name set ");
					logPubDTO(pdto);
				}	
			}	
		}
		else{
			log.severe("processPublisherDTOs no PublisherDTOs found");
		}
		log.severe("pubCon classcache size = "+pubCon.getClassCache().size());
		
	}
	
	private void logPubDTO(PublisherDTO pdto){
		log.severe("PublisherDTO Name = " + pdto.getName());
		log.severe("PublisherDTO id uuid = " + pdto.getId_uuidS());
		log.severe("PublisherDTO className = " + pdto.getPublisherClassName());
		log.severe("PublisherDTO OutputDirectory = " + pdto.getOutputDirectory());
		log.severe("PublisherDTO props = " + pdto.getConfigProps());
	}
	

	private I_PublishConcepts getPublisher(String classname) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException {
		Object Ob = instantiateClass(classname);
		checkState(classname, Ob);
		I_PublishConcepts publisher = (I_PublishConcepts) Ob;
		return publisher;
	}

	/** Instantiates a class from it's (String) Classname */
	private Object instantiateClass(String className)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		log.severe("instantiateClass called className = " + className);
		if (className == null)
			return null;
		return Class.forName(className).newInstance();
	}

	/** Checks to see that a class has been loaded correctly */
	private void checkState(String interfaceName, Object interfaceObject) {
		if (interfaceObject == null) {
			throw new IllegalStateException("Name of class that implements "
					+ interfaceName + " not set.");
		}
	}

	public I_TermFactory getTf() {
		// TODO: Add CacheSize and readonly
		if (tf == null) {
			I_TermFactoryCreator itc = new I_TermFactoryCreator(dbPath);
			try {
				tf = itc.getTf();
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Error Getting the I_TermFactory error = ", e);
			}
		}
		return tf;
	}

	public void setTf(I_TermFactory tf) {
		this.tf = tf;
	}

	public boolean isTfOK() {

		if (!tfOK) {
			getTf();
			if (tf != null) {
				tfOK = true;
			}
		}
		return tfOK;
	}

	public void setTfOK(boolean tfOK) {
		this.tfOK = tfOK;
	}

}
