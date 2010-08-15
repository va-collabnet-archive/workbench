// Project: maven-dita-plugin
// Source: src/main/java/info/healthbase/plugins/DitaGenMojo.java
// perhaps package dwfa.org.ace.maven-mojos/maven-dita.plugin ??
package org.ihtsdo.mojo.dita;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.PropertySpec;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * Maven Mojo for generating DITA files from Business Process .bp files.
 * 
 * @author Eric Browne
 * 
 * @goal gen-bp
 * @phase generate-sources
 */
public class GenerateDitaBusinessProcessTopicsMojo extends AbstractMojo {

	/**
	 * Input directory
	 * 
	 * @ parameter default-value="${project.build.directory}"
	 * @parameter alias="inputDirectories"
	 * @required was @ readonly
	 */
	private String[] inputDirectories;

	/**
	 * Output directory
	 *  *** CHANGE-ME ***
	 * @parameter default-value=
	 *            "${project.build.directory}/generated-resources/info/healthbase/dita/proc-lib"
	 * @required  
	 */
	private File outputDirectory;

	/**
	 * DITA .ditamap filename
	 * 
	 * @parameter default-value="BusinessProcess.ditamap"
	 * @required  
	 */
	private String ditamapFileName;

	

	/**
	 * Specifies patterns to exclude. Multiple patterns can be specified and are
	 * treated as exclude ${exclude[0]} OR ${exclude[1]} OR ...
	 * 
	 * @parameter alias="excludes"
	 */
	private String[] excludes;

	/**
	 * Specifies patterns to include. Multiple patterns can be specified and are
	 * treated as include ${include[0]} OR ${include[1]} OR ...
	 * 
	 * @parameter alias="includes"
	 */
	private String[] includes;

	// --------------------  END OF POM PARAMETERS  -----------------------------

	private class TaskInfo {
		public String name = "";
		public String description = "";
	}

	private class InternalAttachmentInfo {
		public String key;
		public String description;
		public String externalName;
		public String externalDescription;
		public InternalAttachmentInfo(PropertySpec spec) throws IntrospectionException  {
			this.key = spec.getKey();
			this.description = spec.getShortDescription();
			this.externalName = spec.getExternalName();			
			this.externalDescription = spec.getExternalToolTip();
			getLog().debug("internal attachment key=" + this.key);

		}
		public Element toDita() {

			Element dlentry   = new Element("dlentry");
			Element dt        = new Element("dt");
			Element ph	      = new Element("ph");
			Element codeph    = new Element("codeph");
			Element dd        = new Element("dd");
			Element pb        = new Element("b");

			codeph.setText(key);
			pb.addContent(codeph);
			ph.addContent(pb);
			dt.addContent(ph);
			dlentry.addContent(dt);
			dd.setText(description.replaceAll(htmlRegex,""));
			getLog().debug("    attachment=\"" + key + "\", description=\"" + description + "\"");
			dlentry.addContent(dd);
			return dlentry;
		}
	}

	
	private class ExportedAttachmentInfo {
		public String key;
		public String description;
		public String externalName;
		public String externalDescription;
		public ExportedAttachmentInfo(PropertySpec spec) throws IntrospectionException  {
			this.key = spec.getKey();
			this.description = spec.getShortDescription();
			this.externalName = spec.getExternalName();			
			this.externalDescription = spec.getExternalToolTip();
			getLog().debug("exported attachment key=" + this.key);

		}
		public Element toDita() {

			Element dlentry   = new Element("dlentry");
			Element dt        = new Element("dt");
			Element ph	      = new Element("ph");
			Element codeph    = new Element("codeph");
			Element dd        = new Element("dd");
			Element pb        = new Element("b");

			codeph.setText(key);
			pb.addContent(codeph);
			ph.addContent(pb);
			dt.addContent(ph);
			dlentry.addContent(dt);
			dd.setText(description.replaceAll(htmlRegex,""));
			getLog().debug("    attachment=\"" + key + "\", description=\"" + description + "\"");
			dlentry.addContent(dd);
			return dlentry;
		}
	}
	
	private class ExportedTaskPropertyInfo {
		public String key;
		public String description;
		public String externalName;
		public String externalDescription;
		public ExportedTaskPropertyInfo(PropertySpec spec) throws IntrospectionException  {
			this.key = spec.getKey();
			this.description = spec.getShortDescription();
			this.externalName = spec.getExternalName();			
			this.externalDescription = spec.getExternalToolTip();
			getLog().debug("Exported task property=" + this.key);

		}
		public Element toDita() {

			Element dlentry   = new Element("dlentry");
			Element dt        = new Element("dt");
			Element ph	      = new Element("ph");
			Element codeph    = new Element("codeph");
			Element dd        = new Element("dd");
			Element pb        = new Element("b");

			codeph.setText(key);
			pb.addContent(codeph);
			ph.addContent(pb);
			dt.addContent(ph);
			dlentry.addContent(dt);
			dd.setText(description.replaceAll(htmlRegex,""));
			getLog().debug("    exported property=\"" + key + "\", description=\"" + description + "\"");
			dlentry.addContent(dd);
			return dlentry;
		}	}
	
	/*
	 * a ProcessDoc object is used to hold all the required information garnered about a process.
	 */
	private class ProcessDoc {
		public String name =" ";
		public String description = " ";
		public String author = "Informatics Inc.";
		public String originator = "";
		public String subject = "unknown";
		public String detail = "no detail provided.";
		public String location = " ";
		public Integer taskCount = 0;
		public Integer internalAttachmentCount = 0;
		public Integer exportedAttachmentCount = 0;
		public Integer exportedTaskPropertyCount = 0;
		public String implPackage = " ";
		public String implClass = " ";
		public HashMap<String, TaskInfo> tasks = new HashMap<String, TaskInfo>();
		public HashMap<String, InternalAttachmentInfo> internalAttachments = new HashMap<String,InternalAttachmentInfo>();
		public HashMap<String, ExportedAttachmentInfo> exportedAttachments = new HashMap<String,ExportedAttachmentInfo>();
		public HashMap<String, ExportedTaskPropertyInfo> exportedTaskProperties = new HashMap<String,ExportedTaskPropertyInfo>();
	}

	/*
	 * processDocs is used to contain the details for each process.
	 * When fully populated, it is iterated over to build each topic file, 
	 * and the overall .ditamap file.
	 */
	private HashMap<String, ProcessDoc> processDocs;
	private ProcessDoc processDoc;
	private List<File> ditaTopicDirs = new ArrayList<File>();
	
	
	private class TopicrefLine implements Comparable{
		public String href = "";
		public String navtitle = "";
		public String toString() {
			return "[href=" + href + ", navtitle=\"" + navtitle + "\"]";
		}

/*	    public int compareTo(TopicrefLine t1, TopicrefLine t2) {
	        return t1.href.compareTo(t2.href);
	    }*/
	    
		public int compareTo(Object obj) {
			TopicrefLine a = (TopicrefLine) obj;
			int tComp = href.compareTo(a.href);

			return ((tComp == 0) ? navtitle.compareTo(a.navtitle)
					: tComp);
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof TopicrefLine)) {
				return false;
			}
			TopicrefLine a = (TopicrefLine) obj;
			return href.equals(a.href)
			&& navtitle.equals(a.navtitle);
		}
	}
	
	private List<TopicrefLine> topicrefLines = new ArrayList<TopicrefLine>();
	
	private static final String htmlRegex = "<[^>]+>";
	//private String htmlRegex = "/(&([^&;]+);)/ig";
	//private String htmlRegex = "&[A-Za-z09]*;";
	
	private static final String fileSep = System.getProperty("file.separator","/");

	public class CaseInsensitiveComparator 
	 implements java.util.Comparator {
	  public int compare(Object o1, Object o2) {
	    String s1 = o1.toString().toUpperCase();
	    String s2 = o2.toString().toUpperCase();
	    return s1.compareTo(s2);
	  }
	}

	
	/*
	 * =================================================================
	 * 
	 *                     E X E C U T E
	 * (non-Javadoc)
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
 
	public void execute() throws MojoExecutionException {
		getLog().info("Weaving DITA topic files from process bean (.bp files) in directories:- ");
	 	List<File> inputFiles = new ArrayList<File>();	
	 	if (inputDirectories != null) {

	 		ProcessDirectoryWalker walker = new ProcessDirectoryWalker();
	 		for (int i=0; i<inputDirectories.length; i++) {
	 			getLog().info("   " + inputDirectories[i] );
	 			File inputDirectory = new File(inputDirectories[i]);
	 			try {
	 				walker.walk(inputDirectory, inputFiles);
	 			} catch (IOException e) {
	 				throw new MojoExecutionException("Problem walking directory", e);
	 			}

	 		}
	 	}
		DITABuilder db = new DITABuilder();


		// convert these from file name to package name notation
		List<String> classnames = new ArrayList<String>();

		
		getLog().info(
			"Creating following topic files in \"" + outputDirectory.getAbsolutePath() + "\":\n"
				+ 	StringUtils.join(classnames.iterator(), " ") );
		try {
			processDocs = new HashMap<String, ProcessDoc>();
			for (File inputFile : inputFiles) {
				getLog().info("   parsing business process " + inputFile + "\".");
				db.parseBpFile(inputFile);
				}
			db.generateDITA(outputDirectory);

			/*db.checkMisssingInfo(); */
			db.sortTopicrefData();	
			db.writeDitamapXML(outputDirectory.getAbsolutePath());/**/
		} catch (IOException e) {
			throw new MojoExecutionException("Java execution failed", e);

		}
	}

	/*
	 * 
	 */
	private class ProcessDirectoryWalker extends DirectoryWalker {

		/**
		 * Walks through tree of directories searching for files that match a
		 * pattern. The pattern to match is specified here with additional
		 * exclude/include constraints supplied through the Maven build process
		 * For a given sourceFolder, method walk traverses this and sub
		 * directories looking for .bp files.
		 */
		public ProcessDirectoryWalker() {
			super(new FileFilter() {
				public boolean accept(File f) {
					if (f.isDirectory()) {
						// we don't want directories in our list
						//getLog().info("found topic directory \"" + f.getAbsolutePath() + "\"");
						//ditaTopicDirs.add(f);
						return true;
					}
					if (!f.getName().endsWith(".bp")) {
						// we only want .bp files in our list
						return false;
					}
					boolean included = false;
					boolean excluded = false;
					String filename = f.getAbsolutePath();
					if (includes != null) {
						for (int i = 0; i < includes.length; i++) {
							//System.out.println("       >>> matching against /" + includes[i] + "/");
							if (filename.matches(includes[i])) {
								getLog().debug(filename + " matches requested " + includes[i]);
								included = true;
								break;
							}
						}
					}
					if (excludes != null) {
						for (int i = 0; i < excludes.length; i++) {
							if (filename.matches(excludes[i])) {
								getLog().debug("   excluding file \"" + f.getName() + "\"");
								excluded = true;
								break;
							}
						}
					}
					return included && (!excluded);
				}
			}, -1);
		}

		public void walk(File directory, List<File> filenames) throws IOException {
			super.walk(directory, filenames);
			getLog().info("Found " + filenames.size() + " matching Business Process files during walk");
		}

		@Override
		protected void handleFile(File file, int depth, Collection results)
		throws IOException {
			results.add(file);
		}
	}
	
	/*
	 * 
	 */
	private class DITABuilder {

		/**
		 * parseBpFile generates a map containing
		 * relevant information per process Bean  for later processing by a
		 * separate method - generateDITA.
		 * 
		 * @ param sourceFolder
		 * @throws IOException
		 */
		private void parseBpFile(File processFile) throws IOException {

			try {
				

				if (processFile != null) {
					if (processFile.getName().endsWith(".xml")) {
						XMLDecoder d = new XMLDecoder(new BufferedInputStream(
								new FileInputStream(processFile)));
						I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) d.readObject();
						d.close();
						// may be implemented in the future, if required.
						//parseProcess(process);
					} else {
						processDoc = new ProcessDoc();
						FileInputStream fis = new FileInputStream(processFile);
						BufferedInputStream bis = new BufferedInputStream(fis);
						ObjectInputStream ois = new ObjectInputStream(bis);
						I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
						debug("     process     Subject: " + process.getSubject());
						debug("     process        Name: " + process.getName());
						debug("     process Description: " + process.getProcessDocumentationSource());
						processDoc.originator = process.getOriginator();
						processDoc.implClass  = processFile.getName();
						processDoc.subject    = process.getSubject();
						processDoc.name       = process.getName();
				 		for (int i=0; i<inputDirectories.length; i++) {
				 			String prefix = inputDirectories[i] + fileSep;
						    if (processFile.getPath().startsWith(prefix)) {
						    	processDoc.location = processFile.getParent().replaceFirst(prefix, "");
						    }
				 		}
						processDoc.description = process.getProcessDocumentationSource();
						if (processDoc.description == null) {
							processDoc.description = ""; 
						}
						if (processDoc.subject == null) {
							processDoc.subject = ""; 
						}
						if (processDoc.location == null) {
							processDoc.location = ""; 
						}
						//Collection<I_DefineTask> taskCollection = process.getTasks();
						Iterator<I_DefineTask> taskItr = process.getTasks().iterator();
						processDoc.taskCount = 0;
				        while (taskItr.hasNext()) {
				            I_DefineTask task = taskItr.next();
				            if (task != null) {
				            	processDoc.taskCount++;
				            	TaskInfo taskInfo = new TaskInfo();
				                BeanInfo beanInfo = task.getBeanInfo();
						        debug("               task Name: " + task.getName());
						        taskInfo.name = task.getName();
						        
								//String clean = StringEscapeUtils.unescapeHtml(beanInfo.getBeanDescriptor().getDisplayName());
								String clean = beanInfo.getBeanDescriptor().getDisplayName();
								clean = clean.replaceAll("<br>", " ");
								taskInfo.description = clean.replaceAll(htmlRegex,"");

						        processDoc.tasks.put(task.getName(), taskInfo);
				                PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
				                for (int i = 0; i < properties.length; i++) {
				                    debug("                          task property: " + properties[i].getName());
				                }           		
				            }
				        }
				        processDoc.internalAttachments = getInternalAttachmentInfo(process);
				        processDoc.exportedAttachments = getExportedAttachmentInfo(process);
				        processDoc.exportedTaskProperties = getExportedTaskPropertyInfo(process);
				        processDoc.internalAttachmentCount = processDoc.internalAttachments.size();
				        processDoc.exportedAttachmentCount = processDoc.exportedAttachments.size();
				        processDoc.exportedTaskPropertyCount = processDoc.exportedTaskProperties.size();
						ois.close();
						processDocs.put(processFile.getName(), processDoc);
						//parseProcess(process);
					}
				}
			} catch (Exception ex) {
				warn( "Exception reading process file: "
						+ processFile.getName() + "   - " + ex);
				ex.printStackTrace();
			}
		}


		public void printWarn (String message) {
			System.out.println("\n[WARN] " + message);
		}

		public void debug (String message) {
			getLog().debug(message);
		}
		
		public void info (String message) {
			getLog().info(message);
		}
		
		public void warn (String message) {
			getLog().warn(message);
		}
		
		private void generateDITA(File baseDirectory) throws IOException {

			info("  Processed " + processDocs.size() + " Business Process Beans.");
			info("---------------------------------------------------------------------------");
			info(" Now we'll iterate through the " + processDocs.size() + " process info tables and build DITA topic files..\n");
			for (String s : processDocs.keySet()) {
				try {  
					ProcessDoc processDoc = processDocs.get(s);
					if (processDoc.name.equals("")) {
						throw new Exception("missing bean name!");
					}
					
					DITATopic dt = new DITATopic();
					String ditaBaseDirectory = outputDirectory.getAbsolutePath();
					//dt.printTopic(processDoc);  //uncomment for debugging
					dt.writeTopicXML(processDoc,ditaBaseDirectory);
				} catch (Exception e) {
					printWarn("problem [" + e + "] whilst processing info for process \"" + s + "\".  skipping process...");
				}
			}
		}

			/*
			 * method writeDitamapXML(String)
			 * 
			 * Writes DITA ditamap file, to reference the set of topic files for processs in this folder.
			 * 
			 * Format of XML file is:
			 * 
			 * <?xml version="1.0" encoding="UTF-8" ?>
			 * <!DOCTYPE map PUBLIC "-//OASIS//DTD DITA 1.1 Map//EN"
			 *   "http://docs.oasis-open.org/dita/v1.1/OS/dtd/map.dtd">
			 * <map id="ProcessLibrary" xml:lang="en" title="Process Library">
			 *    <title>Standard Process Library</title>
			 *    <topicref href="../tlib_intro.dita" type="topic" navtitle="Standard Process Library Catalogue">
			 *       <topicmeta><keywords><indexterm>Process</indexterm></keywords></topicmeta>
			 *       <topicref href="tlib_process_flow.dita" type="topic" navtitle="Process control: flow processs"> </topicref>
			 *       <topicref href="tlib_process_start.dita" type="topic" navtitle="Process control: Start processs"> </topicref>
			 *       ...
			 *       
			 *   TODO - rewrite using an XML  template file rather than hard-coded XML structure.
			 *       
			 * @param baseDir
			 */
		private void writeDitamapXML(String baseDir) throws MojoExecutionException {

			
			//ProcessDirectoryWalker walker = new ProcessDirectoryWalker( HiddenFileFilter.VISIBLE,null);
/*			ProcessDirectoryWalker walker = new ProcessDirectoryWalker();
			List<File> topicDirs = new ArrayList<File>();
			try {
				walker.walk(topicDirs);
			} catch (IOException e) {
				throw new MojoExecutionException("Problem walking DITA topic directories", e);
			}*/

			//String docType = "<!DOCTYPE topic PUBLIC \"-//OASIS//DTD DITA 1.1 Composite//EN\"  \"http://docs.oasis-open.org/dita/v1.1/OS/dtd/topic.dtd\">";
			DocType docType = new DocType("map", "-//OASIS//DTD DITA 1.1 Map//EN","http://docs.oasis-open.org/dita/v1.1/OS/dtd/map.dtd");
			Element root = new Element("map");
			root.setAttribute("id", "ProcessLibrary");
			root.setAttribute("title", "Process Library");

			//doc.sepDocType(docType);
			Element title = new Element("title");
			title.setText("Standard Process Library");
			Element topicref = new Element("topicref");
			topicref.setAttribute("href", "../bp_library_intro.dita");
			topicref.setAttribute("type", "topic");
			topicref.setAttribute("navtitle", "Standard Process Library Catalogue");
			String topicFileName = "Find Me";
			String navTitle = "Set Me";
			
			Element topicmeta = new Element("topicmeta");
			Element keywords = new Element("keywords");
			Element indexterm = new Element("indexterm");
			indexterm.setText("Process");
			keywords.addContent(indexterm);
			topicmeta.addContent(keywords);
			topicref.addContent(topicmeta);

			for ( TopicrefLine topicrefLine : topicrefLines) {
				Element innerTopicref = new Element("topicref");
				innerTopicref.setAttribute("href", topicrefLine.href);
				innerTopicref.setAttribute("type", "topic");
				innerTopicref.setAttribute("navtitle", topicrefLine.navtitle);
				topicref.addContent(innerTopicref);
			}
			root.addContent(title);
			Comment ditaWarning = new Comment("WARNING - do not edit! This file is autogenerated from java source files.");
			root.addContent(ditaWarning);
			root.addContent(topicref);

			Document doc = new Document(root,docType);

			debug("Writing DITA ditamap file \"" + outputDirectory.getAbsolutePath() + fileSep + ditamapFileName + "\".");
			try {
				XMLOutputter serializer = new XMLOutputter();
				Format format = serializer.getFormat();
				format.setIndent("   ");
				format.setLineSeparator("\n");
				//format.setNewlines(true); 
				serializer.setFormat(format);

				File file = new File(outputDirectory.getAbsolutePath() + fileSep + ditamapFileName);
				FileOutputStream fos = new FileOutputStream(file);
				serializer.output(doc, fos);
				fos.flush();
				fos.close();

				//serializer.output(doc, System.out);
			}
			catch (Exception e) {
				System.err.println(e);
			}

		}

		private void sortTopicrefData() {
			Collections.sort(topicrefLines, new CaseInsensitiveComparator());
		}

		private void checkMisssingInfo() {
			
			List<String> badProcessList = new ArrayList<String>();
			info("checking the " + processDocs.size() + " processs' data..");
			
			for (String process : processDocs.keySet()) {
				if (process.equals(" ")) {
					warn("Blank Bean name for process \"" + processDoc.name + "\" : SUPRESSING topic file");
					badProcessList.add(process);

				}
				ProcessDoc processDoc = processDocs.get(process);
				if (processDoc.location.equals("")) {
					warn("Missing location for process \"" + process + "\" : SUPRESSING topic file");
					badProcessList.add(process);
				}
				if (processDoc.name.equals("") || processDoc.name.equals(" ")) {
					warn("Missing name for process \"" + process + "\" : SUPRESSING topic file");
					badProcessList.add(process);
				}
				if (processDoc.description.equals("") || processDoc.description.equals(" ")) {
					warn("Missing description for process \"" + process + "\"");
					//badProcessList.add(process);
				}
				for (String processProp : processDoc.tasks.keySet()) {
					if (processProp.equals(" ")) {				
						warn("Blank tasks name for process \"" + process + "\" : SUPRESSING topic file");
						badProcessList.add(process);
					}
					TaskInfo task = processDoc.tasks.get(processProp);
					if (task.name.equals("")) {
						warn("Missing displayName for task \"" + processProp + "\" of process \"" + process + "\" : SUPRESSING topic file");
						processDoc.tasks.remove(task);
						processDocs.put(process, processDoc);
					}
					if (task.description.equals("")) {
						warn("Missing description for task \"" + processProp + "\" of process \"" + process + "\"");
					}
				}		
			}
			for (String t : badProcessList) {
				processDocs.remove(t);
			}
			info("removed data for " + badProcessList.size() + " processs.");
		}
		
		
		private HashMap<String, InternalAttachmentInfo> getInternalAttachmentInfo(I_EncodeBusinessProcess process) throws IntrospectionException {

			HashMap<String, InternalAttachmentInfo> attachments = new HashMap<String,InternalAttachmentInfo>();
			for (PropertyDescriptor pdwt: process.getAllPropertiesBeanInfo().getPropertyDescriptors()) {
				PropertySpec spec = (PropertySpec) pdwt.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
				if (spec.getType().equals(PropertySpec.SourceType.ATTACHMENT) && 
						(process.isPropertyExternal(spec) == false))  {
					InternalAttachmentInfo info = new InternalAttachmentInfo(spec);
					attachments.put(info.key, info);
					debug("internal attachment key=" + info.key);
				}
			}
			return attachments;
		}
		
		private HashMap<String, ExportedAttachmentInfo> getExportedAttachmentInfo(I_EncodeBusinessProcess process) throws IntrospectionException {

			HashMap<String, ExportedAttachmentInfo> attachments = new HashMap<String,ExportedAttachmentInfo>();
			for (PropertyDescriptor pdwt: process.getBeanInfo().getPropertyDescriptors()) {
				PropertySpec spec = (PropertySpec) pdwt.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
				if (spec.getType().equals(PropertySpec.SourceType.ATTACHMENT)) {
				ExportedAttachmentInfo info = new ExportedAttachmentInfo(spec);
					attachments.put(info.key, info);
					debug("exported attachment key=" + info.key);
				}
			}
			return attachments;
		}

		private HashMap<String, ExportedTaskPropertyInfo> getExportedTaskPropertyInfo(I_EncodeBusinessProcess process) throws IntrospectionException {

			HashMap<String, ExportedTaskPropertyInfo> properties = new HashMap<String,ExportedTaskPropertyInfo>();
			for (PropertyDescriptor pdwt: process.getBeanInfo().getPropertyDescriptors()) {
				PropertySpec spec = (PropertySpec) pdwt.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
				if (spec.getType().equals(PropertySpec.SourceType.TASK)) {
				ExportedTaskPropertyInfo info = new ExportedTaskPropertyInfo(spec);
					properties.put(info.key, info);
					debug("  exported task property key=" + info.key);
				}
			}
			return properties;
		}
		
		private class DITATopic {

			private void printTopic(ProcessDoc pDoc) {
				System.out.println("\n" + pDoc.implClass + ":");
				
				System.out.println("    name:        " + pDoc.name );
				System.out.println("    location:    " + pDoc.location );
				System.out.println("    description: " + pDoc.description);
				System.out.println("    author:      " + pDoc.author);
				System.out.println("    originator:  " + pDoc.originator);
				System.out.println("    subject:     " + pDoc.subject);
				System.out.println("    detail:      " + pDoc.description);
				System.out.println("    bp filename: " + pDoc.implClass);
				//System.out.println("    bp package:  " + pDoc.implPackage);
				System.out.println("    task count:  " + pDoc.taskCount);
				//System.out.println("    tasks:");
				Integer tCount = 0;
				for (String taskProp : pDoc.tasks.keySet()) {
					tCount++;
					TaskInfo tInfo = pDoc.tasks.get(taskProp);
					System.out.println("         " + tCount + ")" + taskProp + ": \"" + tInfo.name + "\", \"" + tInfo.description + "\"");
				}
				System.out.println("    internal attachment count:  " + pDoc.internalAttachmentCount);
				Integer iaCount = 0;
				for (String attachDetail : pDoc.internalAttachments.keySet()) {
					iaCount++;
					InternalAttachmentInfo aInfo = pDoc.internalAttachments.get(attachDetail);
					System.out.println("         " + iaCount + ")" + attachDetail + ": \"" + aInfo.externalName + "\", \"" + aInfo.description + "\"");
				}
				System.out.println("    exported attachment count:  " + pDoc.exportedAttachmentCount);
				Integer eaCount = 0;
				for (String attachDetail : pDoc.exportedAttachments.keySet()) {
					eaCount++;
					ExportedAttachmentInfo aInfo = pDoc.exportedAttachments.get(attachDetail);
					System.out.println("         " + eaCount + ")" + attachDetail + ": \"" + aInfo.externalName + "\", \"" + aInfo.description + "\"");
				}
				System.out.println("    exported task property count:  " + pDoc.exportedTaskPropertyCount);
				Integer etpCount = 0;
				for (String attachDetail : pDoc.exportedTaskProperties.keySet()) {
					etpCount++;
					ExportedTaskPropertyInfo aInfo = pDoc.exportedTaskProperties.get(attachDetail);
					System.out.println("         " + etpCount + ")" + attachDetail + ": \"" + aInfo.externalName + "\", \"" + aInfo.description + "\"");
				}
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++\n");
			}

				/*
				 * ===============================================================================
				 *  METHOD:   writeTopicXML
				 * ===============================================================================
				 * 
				 * Writes DITA topic file, populated with a table of standard metadata about a WorkBench process
				 * Format of XML file is:
				 * 
				 * <?xml version="1.0" encoding="UTF-8"?>
				 * <!DOCTYPE topic PUBLIC "-//OASIS//DTD DITA 1.1 Composite//EN" "http://docs.oasis-open.org/dita/v1.1/OS/dtd/topic.dtd">
				 * <topic id="topic3" xml:lang="en">
				 *  <title>Start process</title>
				 *  <shortdesc>This topic ....</shortdesc>
				 *  <prolog>
				 *    <author type="creator">Eric Browne</author>
				 *    <copyright>
				 *       <copyryear year="2010"/>
				 *       <copyrholder>IHTSDO</copyrholder>
				 *    </copyright>
				 *    <critdates>
				 *       <created date="2009-June-01"/>
				 *       <revised modified="2009-July-30"/>
				 *    </critdates>
				 *    <metadata><keywords><indexterm>Process <indexterm>Start Process</indexterm></indexterm></keywords></metadata>
				 *  </prolog>
				 *  <body>
				 *    <table id="ap1007082">
				 *      <tgroup cols="2" >
				 *        <colspec colname="key" colwidth="23*"></colspec>
				 *        <colspec colname="value" colwidth="77*"></colspec>
				 *
				 *        <thead>
				 *          <row>
				 *            <entry>
				 *              <b>Name of Process</b>
				 *              <ph>
				 *                 <indexterm>Process <indexterm>Launch Process from Internal Process</indexterm>
				 *                 </indexterm>
				 *              </ph>
				 *            </entry>
				 *            <entry> leave blank</entry>
				 *         </row>
				 *       </thead>
				 *       <tbody>
				 *         <row>
				 *           <entry>
				 *              <p>Description:</p>
				 *           </entry>
				 *           <entry>
				 *              <p>Launches a process that is represented as a process within the enclosing process.</p>
				 *           </entry>
				 *         </row>
				 *       ...
				 *       TODO - rewrite using an XML  template file rather than hard-coded XML structure.
				 *       
				 * @param ProcessDoc
				 * @param String
				 */
			private void writeTopicXML(ProcessDoc pDoc,String baseDir) {

				DocType docType = new DocType("topic", "-//OASIS//DTD DITA 1.1 Composite//EN","http://docs.oasis-open.org/dita/v1.1/OS/dtd/topic.dtd");
				Element root = new Element("topic");
				root.setAttribute("id", pDoc.name.replaceAll("\\W","_"));
				info("Processing topic file for \"" + pDoc.name + "\"");
				
				Element title              = new Element("title");
				Element shordesc           = new Element("shortdesc");
				Element prolog             = new Element("prolog");
				Element author             = new Element("author");
				Element metadata           = new Element("metadata");
				Element keywords           = new Element("keywords");
				Element kwIndextermOuter   = new Element("indexterm");
				Element kwIndextermInner   = new Element("indexterm");
				Element kwIndextermProcess = new Element("indexterm");
				Element indextermAuthor    = new Element("indexterm");
				
				title.setText(pDoc.name );
				author.setText(pDoc.author);
				prolog.addContent(author);
				kwIndextermInner.setText(pDoc.name);
				kwIndextermOuter.setText("Process");
				kwIndextermOuter.addContent(kwIndextermInner);
				keywords.addContent(kwIndextermOuter);
				indextermAuthor.setText(pDoc.author);
				kwIndextermProcess.setText("process author:");
				kwIndextermProcess.addContent(indextermAuthor);
				keywords.addContent(kwIndextermProcess);
				metadata.addContent(keywords);
				prolog.addContent(metadata);
				Element body          = new Element("body");
				Element table         = new Element("table");
				Element tgroup        = new Element("tgroup");
				tgroup.setAttribute("cols", "2");
				Element colspec1      = new Element("colspec");
				colspec1.setAttribute("colname", "key");
				colspec1.setAttribute("colwidth", "23*");
				tgroup.addContent(colspec1);
				Element colspec2      = new Element("colspec");
				colspec2.setAttribute("colname", "value");
				colspec2.setAttribute("colwidth", "77*");
				tgroup.addContent(colspec2);
				Element thead         = new Element("thead");
				Element row           = new Element("row");
				Element entry1        = new Element("entry");
				entry1.setAttribute("namest", "key");
				entry1.setAttribute("nameend","value");
				Element b             = new Element("b");
				b.setText("Process:  " + pDoc.name.replaceAll(htmlRegex,"") );
				entry1.addContent(b);
				
				Element ph = new Element("ph");
				Element indextermOuter = new Element("indexterm");
				Element indextermInner = new Element("indexterm");
				indextermOuter.setText("Process");
				indextermInner.setText(pDoc.name);
				//indextermOuter.addContent(indextermInner);

				//ph.addContent(indextermOuter);
				ph.addContent(indextermInner);
				entry1.addContent(ph);
				row.addContent(entry1);

				thead.addContent(row);
				tgroup.addContent(thead);

				// now build the body rows


				String [] keys = { "IDE plugin subfolder", "Description", "Author", "Originator", "Subject", "Implementation File", "Number of tasks" };
				String [] values = {pDoc.location, pDoc.description.replaceAll(htmlRegex,""), pDoc.author, pDoc.originator, pDoc.subject, pDoc.implClass, pDoc.taskCount.toString() };

				Element tbody = new Element("tbody");

				for (int i=0; i<keys.length; i++) {
					Element bodyRow    = new Element("row");
					Element bodyEntry1 = new Element("entry");
					Element bodyEntry2 = new Element("entry");
					Element p1      = new Element("p");
					Element p2      = new Element("p");
					Element b2      = new Element("b");
					Element codeph  = new Element("codeph");
					p1.setText(keys[i]);
					if (i==0) {
						codeph.setText(values[i]);
						b2.addContent(codeph);
						p2.addContent(b2);
					} else if (i==3 || i==5) {
						codeph.setText(values[i]);
						p2.addContent(codeph);
					} else {
						p2.setText(values[i]);
					}
					bodyEntry1.addContent(p1);
					bodyEntry2.addContent(p2);
					bodyRow.addContent(bodyEntry1);
					bodyRow.addContent(bodyEntry2);
					tbody.addContent(bodyRow);
				}
				
				/* 
				 * now add the process tasks according to the following sample snippet:
				 *  <row>
				 *     <entry>
				 *         <p>Process Tasks:</p>
				 *     </entry>
				 *     <entry>
				 *         <dl>
				 *            <dlentry><dt><ph><codeph>TasksDataId</codeph>:</ph></dt> <dd>A data id for the process container to load. </dd></dlentry>
				 *            <dlentry><dt><ph><codeph>taskURLString</codeph></ph>:</dt> <dd>A URL from which a process is loaded.</dd></dlentry>
				 *         </dl>
				 *     </entry>
				 *  </row>
				 */

				Element propRow        = new Element("row");
				Element propEntryKey   = new Element("entry");
				Element propEntryValue = new Element("entry");
				Element dl             = new Element("dl");
				propEntryKey.setText("Tasks");
								
				if (pDoc.taskCount >0) {
					for (String task : pDoc.tasks.keySet()) {
						Element dlentry   = new Element("dlentry");
						Element dt        = new Element("dt");
						Element pph	      = new Element("ph");
						Element codeph    = new Element("codeph");
						Element dd        = new Element("dd");
						Element pb        = new Element("b");

						codeph.setText(task);
						pb.addContent(codeph);
						pph.addContent(pb);
						dt.addContent(pph);
						dlentry.addContent(dt);
						dd.setText(pDoc.tasks.get(task).description.replaceAll(htmlRegex,""));
						debug("Name=\"" + pDoc.name + "\", task=\"" + task + "\", name=\"" + pDoc.tasks.get(task).name + "\"");
						dlentry.addContent(dd);
						dl.addContent(dlentry);
					}
					propEntryValue.addContent(dl);
				} else {
					propEntryValue.setText("none");
				}

				Element iAttachmentRow        = new Element("row");
				Element iAttachmentEntryKey   = new Element("entry");
				Element iAttachmentEntryValue = new Element("entry");
				Element iAttachmentDl         = new Element("dl");
				iAttachmentEntryKey.setText("Internal Attachments");				
				if (pDoc.internalAttachmentCount >0) {
					for (String attachment : pDoc.internalAttachments.keySet()) {
						iAttachmentDl.addContent(pDoc.internalAttachments.get(attachment).toDita());
					}
					iAttachmentEntryValue.addContent(iAttachmentDl);
				} else {
					iAttachmentEntryValue.setText("none");
				}

				Element eAttachmentRow        = new Element("row");
				Element eAttachmentEntryKey   = new Element("entry");
				Element eAttachmentEntryValue = new Element("entry");
				Element eAttachmentDl         = new Element("dl");
				eAttachmentEntryKey.setText("Exported Attachments");				
				if (pDoc.exportedAttachmentCount >0) {
					for (String attachment : pDoc.exportedAttachments.keySet()) {

					    eAttachmentDl.addContent(pDoc.exportedAttachments.get(attachment).toDita());
					}
					eAttachmentEntryValue.addContent(eAttachmentDl);
				} else {
					eAttachmentEntryValue.setText("none");
				}

				Element exTaskPropRow        = new Element("row");
				Element exTaskPropEntryKey   = new Element("entry");
				Element exTaskPropEntryValue = new Element("entry");
				Element exTaskPropDl             = new Element("dl");
				exTaskPropEntryKey.setText("Exported Task Properties");				
				if (pDoc.exportedTaskPropertyCount >0) {
					for (String property : pDoc.exportedTaskProperties.keySet()) {
					    eAttachmentDl.addContent(pDoc.exportedTaskProperties.get(property).toDita());
					}
					exTaskPropEntryValue.addContent(exTaskPropDl);
				} else {
					exTaskPropEntryValue.setText("none");
				}
			
				propRow.addContent(propEntryKey);
				propRow.addContent(propEntryValue);
				tbody.addContent(propRow);
				
				iAttachmentRow.addContent(iAttachmentEntryKey);
				iAttachmentRow.addContent(iAttachmentEntryValue);
				tbody.addContent(iAttachmentRow);
				
				eAttachmentRow.addContent(eAttachmentEntryKey);
				eAttachmentRow.addContent(eAttachmentEntryValue);
				tbody.addContent(eAttachmentRow);
				
				exTaskPropRow.addContent(exTaskPropEntryKey);
				exTaskPropRow.addContent(exTaskPropEntryValue);
				tbody.addContent(exTaskPropRow);

				tgroup.addContent(tbody);
				table.addContent(tgroup);
				body.addContent(table);
				root.addContent(title);
				Comment ditaWarning = new Comment("WARNING - do not edit! This file is autogenerated from java source files.");
				root.addContent(ditaWarning);
				root.addContent(prolog);
				root.addContent(body);

				Document doc = new Document(root,docType);
				
				String topicFileName = pDoc.implClass + ".dita";
				String fullyQualifiedTopicFileName = baseDir + fileSep + pDoc.location + fileSep + topicFileName;				
				// the TopicrefLines are used later for building the ditamap
				TopicrefLine topicrefLine = new TopicrefLine();
				topicrefLine.href = pDoc.location + fileSep + topicFileName;
				topicrefLine.navtitle = pDoc.name.replaceAll(htmlRegex, "");
				topicrefLines.add(topicrefLine);

				debug("Writing DITA topic file \"" + topicFileName + "\".");
				try {
					Format compactFormat = Format.getCompactFormat();
					XMLOutputter serializer = new XMLOutputter(compactFormat);
					// JDOM with indenting produces  <indexterm> entries that the DITA-OT mishandles.
					//compactFormat.setIndent("   ");
					//compactFormat.setLineSeparator("\n");
					serializer.setFormat(compactFormat);
					//compactFormat.setTextMode(Format.TextMode.TRIM);
					File dir = new File(baseDir  + fileSep + pDoc.location );
					dir.mkdirs();
					File file = new File(fullyQualifiedTopicFileName);
					FileOutputStream fos = new FileOutputStream(file);
					serializer.output(doc, fos);
					fos.flush();
					fos.close();
				}
				catch (Exception e) {
					System.err.println(e);
				}

			}

		}
	}

}
