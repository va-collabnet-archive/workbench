// Project: maven-dita-plugin
// Source: src/main/java/info/healthbase/plugins/DitaGenMojo.java
// perhaps package dwfa.org.ace.maven-mojos/maven-dita.plugin ??
package info.ihtsdo.mojo.mpjp.dita;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.regex.*;
import java.io.*; 

import org.jdom.*;
import org.jdom.output.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import com.thoughtworks.qdox.*;
import com.thoughtworks.qdox.model.*;



/**
 * Maven Mojo for generating DITA files from javabean source files.
 * 
 * @author Eric Browne
 * 
 * @goal gen-tasks
 * @phase process-resources
 */
public class GenerateDitaTaskTopicsMojo extends AbstractMojo {

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
	 * 
	 * @parameter default-value=
	 *            "${project.build.directory}/generated-resources/info/healthbase/dita/task-lib"
	 * @required  
	 * was @ readonly
	 */
	private File outputDirectory;

	/**
	 * DITA .ditamap filename
	 * 
	 * @parameter default-value="TaskLibrary.ditamap"
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

	// --------------------  END OF POM PARAMETERS  --------------------------

	private class PropertyInfo {
		public String displayName = "";
		public String description = "";
	}

	/*
	 * a TaskDoc object is used to hold all the required information garnered about a task.
	 */
	private class TaskDoc {
		public String name ="";
		public String description = "";
		public String author = "unknown";
		public String detail = "no detail provided.";
		public String location = "";
		public String implPackage = "";
		public String implClass = "";
		public HashMap<String, PropertyInfo> properties = new HashMap<String, PropertyInfo>();
		
		public TaskDoc(String name) {
			this.name = name;
		}
		public TaskDoc() {
			this.name = "";
		}
	}

	
	/*
	 * taskDocs is used to contain the task Details for each task.
	 * Once generated, it is iterated over to build each topic file, 
	 * and the overall .ditamap file.
	 */
	private HashMap<String, TaskDoc> taskDocs;
	private TaskDoc taskDoc;
	
	public class CaseInsensitiveComparator 
	 implements java.util.Comparator {
	  public int compare(Object o1, Object o2) {
	    String s1 = o1.toString().toUpperCase();
	    String s2 = o2.toString().toUpperCase();
	    return s1.compareTo(s2);
	  }
	}

	
	private class TopicrefLine implements Comparable{
		public String href = "";
		public String navtitle = "";
		public String toString() {
			return "[href=" + href + ", navtitle=\"" + navtitle + "\"]";
		}

	    
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

	
	/*
	 * =================================================================
	 * 
	 *                     E X E C U T E
	 * (non-Javadoc)
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
 
	public void execute() throws MojoExecutionException {
		getLog().info("Weaving DITA topic files from directories:- ");
		if (inputDirectories != null) {
			for (int i=0; i<inputDirectories.length; i++) {
				getLog().info("   " + inputDirectories[i] );
			}
		}
		DITABuilder db = new DITABuilder();
		// convert these from file name to package name notation
		List<String> classnames = new ArrayList<String>();
		getLog().info(
			"Creating following topic files in \"" + outputDirectory.getAbsolutePath() + "\":\n"
				+ 	StringUtils.join(classnames.iterator(), " ") );
		try {
			taskDocs = new HashMap<String, TaskDoc>();
			db.beanPropertyFinder();
			db.checkMisssingInfo();
			db.generateDITA(outputDirectory);
			db.sortTopicrefData();	
			db.writeDitamapXML(outputDirectory.getAbsolutePath());
		} catch (IOException e) {
			throw new MojoExecutionException("Java execution failed", e);

		}
	}

	
	/*
	 * 
	 */
	private class DITABuilder {

		/**
		 * For a given sourceFolder, beanPropertyFinder traverses this and sub
		 * directories looking for BeanFiles that contain @ BeanList annotations
		 * and BeanInfoFiles that contain property descriptors. A map containing
		 * relevant information per Bean is generated for later processing by a
		 * separate method - generateDITA.
		 * 
		 * @ param sourceFolder
		 * @throws IOException
		 */
		private void beanPropertyFinder() throws IOException {

			JavaDocBuilder parser = new JavaDocBuilder();
			if (inputDirectories != null) {
				for (int i=0; i<inputDirectories.length; i++) {
					File f = new File(inputDirectories[i]);
					if (!f.exists()) {
						error("Source Folder \"" + f.getAbsolutePath() + "\" does not exist!");
					} else {
						parser.addSourceTree(f);
						info("  Processing Task Beans in " + f.getAbsolutePath());
					}
				}
			}
			
			com.thoughtworks.qdox.model.JavaClass[] javaClasses = parser.getClasses();
			for (com.thoughtworks.qdox.model.JavaClass javaClass : javaClasses) {
				String className    = javaClass.getFullyQualifiedName();
				String rawBeanName  = className.replaceAll("BeanInfo$","");
				String shortName    = javaClass.getName();
				String classComment = javaClass.getComment();
				DocletTag authorTag = javaClass.getTagByName("author");
				
				//String packageName = javaClass.getPackage().toString();  // broken??
				String packageName = className.replaceAll("."+shortName, "");
				debug("PROCESSING file " + shortName);
				com.thoughtworks.qdox.model.Annotation[] annotations = javaClass.getAnnotations();
				for (com.thoughtworks.qdox.model.Annotation a : annotations) {
					debug("     processing annotation \"" + a.toString() + "\".");
					// need to extract the directory from annotation of this
					// form @ BeanList(specs=[@Spec(directory="tasks/ncch/classifier",type=BeanType.TASK_BEAN)])
					//System.out.println("    Annotation " + a.toString() + " in task " + shortName);
					// Pattern beanListExpression = Pattern.compile(
					// "^@BeanList.*@Spec(directory=\"(.*)\".*).*$",Pattern.COMMENTS+Pattern.DOTALL );
					Pattern beanListExpression = Pattern.compile("^[a-zA-Z0-9_.\\-@]*BeanList.*directory=\"(.*)\".*",Pattern.COMMENTS+Pattern.DOTALL);
					Matcher matcher = beanListExpression.matcher(a.toString());
					if (matcher.find()) {
						boolean newFile = false;
						if (taskDocs.containsKey(rawBeanName)) {
							taskDoc = taskDocs.get(rawBeanName);
						} else {
							taskDoc = new TaskDoc();
							newFile = true;
						}
						taskDoc.location    = matcher.group(1);
						debug("      setting location for \"" + rawBeanName + "\" to \"" + taskDoc.location + "\"");
						taskDoc.name        = shortName;
						if (authorTag   != null ) {
							taskDoc.author  = authorTag.getValue() ;
						}
						taskDoc.implClass   = shortName + ".java";
						taskDoc.implPackage = packageName ;
						if (classComment   != null ) {
							taskDoc.detail  = classComment ;
						}
						if (newFile) {
							taskDoc.properties = new HashMap<String, PropertyInfo>();
						}
						taskDocs.put(rawBeanName, taskDoc);
					}
				}
				// now we need to extract any property information from the
				// corresponding BeanInfo file
				JavaMethod[] methods = javaClass.getMethods();
				for (JavaMethod m : methods) {

					HashMap<String,PropertyInfo> properties = new HashMap<String,PropertyInfo>();
					PropertyInfo pInfo;

					if (m.getName().equals("getBeanDescriptor")) {
						if (taskDocs.containsKey(rawBeanName)) {
							taskDoc = taskDocs.get(rawBeanName);
							debug("   found existing taskDoc for \"" + taskDoc.name + "\"");
						} else {
							debug("   creating new TaskDoc for \"" + rawBeanName + "\"");
							taskDoc = new TaskDoc(rawBeanName);
							taskDoc.properties = new HashMap<String, PropertyInfo>();
						}

						String source = m.getSourceCode();
						/*
						 *  TODO - need to support multiline, concatenated strings as argument to setDisplayName
						 *  Sometimes the argument may even include variables. - tricky!
						 */
						Pattern descriptorNameExpression = Pattern.compile("(\\w*).setDisplayName\\(\"(.+?)\".*\\);",Pattern.COMMENTS+Pattern.DOTALL);
						Matcher matcher = descriptorNameExpression.matcher(source);	
						if (matcher.find()) {
							String clean = StringEscapeUtils.unescapeHtml(matcher.group(2));
							clean = clean.replaceAll("<br>", " ");
							taskDoc.description = clean.replaceAll(htmlRegex,"");
							taskDocs.put(rawBeanName, taskDoc);
						} else {
							warn("no DisplayName for task bean \"" + className + "\"");
						}
						debug( "   "  +  taskDoc.name + ": " + taskDoc.location + " ; " + taskDoc.description + " ; " + taskDoc.properties.size() + " properties.");

					}

					if (m.getName().equals("getPropertyDescriptors")) {

						Integer pCount = 0;
						String source = m.getSourceCode();
						Pattern descriptorNameExpression = Pattern.compile("(\\w*).setDisplayName\\(\"(.+?)\"\\);");
						Matcher matcher = descriptorNameExpression.matcher(source);
						while (matcher.find()) {
							String pName = matcher.group(1);
							if (properties.containsKey(pName) ) {
								pInfo = properties.get(pName);
							} else {
								pInfo = new PropertyInfo();		
								pCount++ ;
							}
							pInfo.displayName = matcher.group(2);
							properties.put(pName, pInfo);
						}
						Pattern descriptorDescriptionExpression = Pattern.compile("(\\w*).setShortDescription\\(\"(.+?)\"\\);");
						matcher = descriptorDescriptionExpression.matcher(source);
						while (matcher.find()) {
							String pName = matcher.group(1);
							if ( properties.containsKey(pName) ) {
								pInfo = properties.get(pName);
							} else {
								pInfo = new PropertyInfo();	
								pCount++ ;
							}
							pInfo.description = matcher.group(2).replaceAll(htmlRegex,"");
							properties.put(pName, pInfo);
						}

						if (taskDocs.containsKey(rawBeanName)) {
							taskDoc = taskDocs.get(rawBeanName);
						} else {
							debug("   creating new TaskDoc for \"" + shortName + "\"");
							taskDoc = new TaskDoc(rawBeanName);
						}

						taskDoc.properties = properties;
						taskDocs.put(rawBeanName, taskDoc);
					}
				}
			}
		}

		public void printWarn (String message) {
			System.out.println("\n[WARN] " + message);
		}

		public void debug (String message) {
			//System.out.println("\n[DEBUG] " + message);
			getLog().debug(message);
		}
		
		public void info (String message) {
			getLog().info(message);
		}
		
		public void warn (String message) {
			getLog().warn(message);
		}
		
		public void error (String message) {
			getLog().error(message);
		}
		
		private void generateDITA(File baseDirectory) throws IOException {

			info("  Processed " + taskDocs.size() + " Task Beans.");
			System.out.println("\n---------------------------------------------------------------------------");
			info(" Now we'll iterate through the " + taskDocs.size() + " task info tables and build DITA topic files..");
			


			for (String s : taskDocs.keySet()) {
				try {  
					TaskDoc taskDoc = taskDocs.get(s);
					if (taskDoc.name.equals("")) {
						throw new Exception("missing bean name!");
					}
					
					DITATopic dt = new DITATopic();
					String ditaBaseDirectory = outputDirectory.getAbsolutePath();
					dt.writeTopicXML(taskDoc,ditaBaseDirectory);
					
				} catch (Exception e) {
					printWarn("problem [" + e + "] whilst processing info for task \"" + s + "\".  skipping task...");
				}
			}
		}

			/*
			 * method writeDitamapXML(String)
			 * 
			 * Writes DITA ditamap file, to reference the set of topic files for tasks in this folder.
			 * 
			 * Format of XML file is:
			 * 
			 * <?xml version="1.0" encoding="UTF-8" ?>
			 * <!DOCTYPE map PUBLIC "-//OASIS//DTD DITA 1.1 Map//EN"
			 *   "http://docs.oasis-open.org/dita/v1.1/OS/dtd/map.dtd">
			 * <map id="TaskLibrary" xml:lang="en" title="Task Library">
			 *    <title>Standard Task Library</title>
			 *    <topicref href="../tlib_intro.dita" type="topic" navtitle="Standard Task Library Catalogue">
			 *       <topicmeta><keywords><indexterm>Task</indexterm></keywords></topicmeta>
			 *       <topicref href="tlib_process_flow.dita" type="topic" navtitle="Process control: flow tasks"> </topicref>
			 *       <topicref href="tlib_process_start.dita" type="topic" navtitle="Process control: Start tasks"> </topicref>
			 *       ...
			 *       
			 *   TODO - rewrite using an XML  template file rather than hard-coded XML structure.
			 *       
			 * @param baseDir
			 */
		private void writeDitamapXML(String baseDir) throws MojoExecutionException {

			DocType docType = new DocType("map", "-//OASIS//DTD DITA 1.1 Map//EN","http://docs.oasis-open.org/dita/v1.1/OS/dtd/map.dtd");
			Element root = new Element("map");
			root.setAttribute("id", "TaskLibrary");
			root.setAttribute("title", "Task Library");

			//doc.setDocType(docType);
			Element title = new Element("title");
			title.setText("Standard Task Library");
			Element topicref = new Element("topicref");
			topicref.setAttribute("href", "../tlib_intro.dita");
			topicref.setAttribute("type", "topic");
			topicref.setAttribute("navtitle", "Standard Task Library Catalogue");
			
			Element topicmeta = new Element("topicmeta");
			Element keywords = new Element("keywords");
			Element indexterm = new Element("indexterm");
			indexterm.setText("Task");
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

			//String ditamapFile = "_TaskLibrary.ditamap";  // already defined as a Maven configuration parameter
			getLog().info("Writing DITA ditamap file \"" + outputDirectory.getAbsolutePath() + ditamapFileName + "\".");
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
			
			List<String> badTaskList = new ArrayList<String>();
			info("checking the " + taskDocs.size() + " tasks' data..");
			
			for (String task : taskDocs.keySet()) {
				if (task.equals("")) {
					error("Blank Bean name for task \"" + taskDoc.name + "\" : SUPRESSING topic file");
					badTaskList.add(task);

				}
				TaskDoc taskDoc = taskDocs.get(task);
				if (taskDoc.location.equals("")) {
					error("Missing location for task \"" + task + "\" : SUPRESSING topic file");
					badTaskList.add(task);
				}
				if (taskDoc.name.equals("")) {
					error("Missing name for task \"" + task + "\" : SUPRESSING topic file");
					badTaskList.add(task);
				}
				if (taskDoc.description.equals("")) {
					warn("Missing description for task \"" + task + "\"");
					taskDoc.description = taskDoc.name;
					debug("  setting description to \"" + taskDoc.name + "\"");
					//badTaskList.add(task);
				}
				for (String taskProp : taskDoc.properties.keySet()) {
					if (taskProp.equals("")) {				
						error("Blank property name for task \"" + task + "\" : SUPRESSING topic file");
						badTaskList.add(task);
					}
					PropertyInfo property = taskDoc.properties.get(taskProp);
					if (property.displayName.equals("")) {
						error("Missing displayName for property \"" + taskProp + "\" of task \"" + task + "\" : SUPRESSING topic file");
						taskDoc.properties.remove(property);
						taskDocs.put(task, taskDoc);
					}
					if (property.description.equals("")) {
						warn("Missing description for property \"" + taskProp + "\" of task \"" + task + "\"");
					}
				}		
			}
			for (String t : badTaskList) {
				taskDocs.remove(t);
			}
			info("removed data for " + badTaskList.size() + " tasks.");
		}
		
		private class DITATopic {

			private void printTopic(TaskDoc tDoc) {
				System.out.println("\n" + tDoc.name + ":");
				System.out.println("    " + tDoc.location );
				System.out.println("    " + tDoc.description);
				System.out.println("    " + tDoc.detail);
				System.out.println("    " + tDoc.implClass);
				System.out.println("    " + tDoc.implPackage);
				System.out.println("    properties:");
				Integer pCount = 0;
				for (String taskProp : tDoc.properties.keySet()) {
					pCount++;
					PropertyInfo pInfo = tDoc.properties.get(taskProp);
					System.out.println("         " + pCount + ")" + taskProp + ": \"" + pInfo.displayName + "\", \"" + pInfo.description + "\"");
				}
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++\n");
			}

				/*
				 * Writes DITA topic file, populated with a table of standard metadata about a WorkBench task
				 * Format of XML file is:
				 * 
				 * <?xml version="1.0" encoding="UTF-8"?>
				 * <!DOCTYPE topic PUBLIC "-//OASIS//DTD DITA 1.1 Composite//EN" "http://docs.oasis-open.org/dita/v1.1/OS/dtd/topic.dtd">
				 * <topic id="topic3" xml:lang="en">
				 *  <title>Start task</title>
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
				 *    <metadata><keywords><indexterm>Task <indexterm>Start Task</indexterm></indexterm></keywords></metadata>
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
				 *              <b>Name of Task</b>
				 *              <ph>
				 *                 <indexterm>Task <indexterm>Launch Process from Internal Task</indexterm>
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
				 *              <p>Launches a process that is represented as a task within the enclosing process.</p>
				 *           </entry>
				 *         </row>
				 *       ...
				 *       TODO - rewrite using an XML  template file rather than hard-coded XML structure.
				 *       
				 * @param TaskDoc
				 * @param String
				 */
			private void writeTopicXML(TaskDoc tDoc,String baseDir) {

				DocType docType = new DocType("topic", "-//OASIS//DTD DITA 1.1 Composite//EN","http://docs.oasis-open.org/dita/v1.1/OS/dtd/topic.dtd");
				Element root = new Element("topic");
				root.setAttribute("id", tDoc.name);

				Element title            = new Element("title");
				Element prolog           = new Element("prolog");
				Element author           = new Element("author");
				Element metadata         = new Element("metadata");
				Element keywords         = new Element("keywords");
				Element kwIndextermOuter = new Element("indexterm");
				Element kwIndextermInner = new Element("indexterm");
				Element kwIndextermTask  = new Element("indexterm");
				Element indextermAuthor  = new Element("indexterm");
				
				title.setText(tDoc.description);
			    author.setText(tDoc.author);
				prolog.addContent(author);
				kwIndextermInner.setText(tDoc.description);
				kwIndextermOuter.setText("Task");
				kwIndextermOuter.addContent(kwIndextermInner);
				keywords.addContent(kwIndextermOuter);
				indextermAuthor.setText(tDoc.author);
				kwIndextermTask.setText("task author:");
				kwIndextermTask.addContent(indextermAuthor);
				keywords.addContent(kwIndextermTask);
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
				b.setText("Task:  " + tDoc.name.replaceAll(htmlRegex,"") );
				entry1.addContent(b);
				
				Element ph = new Element("ph");
				Element indextermOuter = new Element("indexterm");
				Element indextermInner = new Element("indexterm");
				indextermOuter.setText("Task");
				indextermInner.setText(tDoc.name);
				//indextermOuter.addContent(indextermInner);

				//ph.addContent(indextermOuter);
				ph.addContent(indextermInner);
				Element indextermLocation = new Element("indexterm");
				indextermLocation.setText(tDoc.location);
				ph.addContent(indextermLocation);
				entry1.addContent(ph);
				row.addContent(entry1);

				thead.addContent(row);
				tgroup.addContent(thead);

				// now build the body rows


				String [] keys = { "Description", "Availability in IDE", "Detail", "Author", "Implementation Class", "Implementation Package" };
				String [] values = { tDoc.description.replaceAll(htmlRegex,""), tDoc.location, tDoc.detail.replaceAll(htmlRegex,""), tDoc.author, tDoc.implClass, tDoc.implPackage };

				Element tbody = new Element("tbody");

				for (int i=0; i<keys.length; i++) {
					Element bodyRow    = new Element("row");
					Element bodyEntry1 = new Element("entry");
					Element bodyEntry2 = new Element("entry");
					Element p1      = new Element("p");
					Element p2      = new Element("p");
					Element codeph  = new Element("codeph");
					p1.setText(keys[i]);
					if (i==4 || i==5) {
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
				 * now add the task properties according to the following sample snippet:
				 *  <row>
				 *     <entry>
				 *         <p>Task Properties:</p>
				 *     </entry>
				 *     <entry>
				 *         <dl>
				 *            <dlentry><dt><ph><codeph>ProcessDataId</codeph>:</ph></dt> <dd>A data id for the process container to load. </dd></dlentry>
				 *            <dlentry><dt><ph><codeph>processURLString</codeph></ph>:</dt> <dd>A URL from which a process is loaded.</dd></dlentry>
				 *         </dl>
				 *     </entry>
				 *  </row>
				 */

				Element propRow        = new Element("row");
				Element propEntryKey   = new Element("entry");
				Element propEntryValue = new Element("entry");
				Element dl             = new Element("dl");
				propEntryKey.setText("Properties");
				
				//debug("TOPIC: \"" + tDoc.name + "\" processing " + tDoc.properties.size() + " properties.");
				
				if (tDoc.properties.size() >0) {
					for (String property : tDoc.properties.keySet()) {
						Element dlentry   = new Element("dlentry");
						Element dt        = new Element("dt");
						Element pph	      = new Element("ph");
						Element codeph    = new Element("codeph");
						Element dd        = new Element("dd");
						Element pb        = new Element("b");

						codeph.setText(property);
						pb.addContent(codeph);
						pph.addContent(pb);
						dt.addContent(pph);
						dlentry.addContent(dt);
						dd.setText(tDoc.properties.get(property).description.replaceAll(htmlRegex,""));
						//debug("Name=\"" + tDoc.name + "\", property=\"" + property + "\", description=\"" + tDoc.properties.get(property).description + "\"");
						dlentry.addContent(dd);
						dl.addContent(dlentry);
					}
					propEntryValue.addContent(dl);
				} else {
					propEntryValue.setText("none");
				}

				
				propRow.addContent(propEntryKey);
				propRow.addContent(propEntryValue);
				tbody.addContent(propRow);
				tgroup.addContent(tbody);
				table.addContent(tgroup);
				body.addContent(table);
				root.addContent(title);
				Comment ditaWarning = new Comment("WARNING - do not edit! This file is autogenerated from java source files.");
				root.addContent(ditaWarning);
				root.addContent(prolog);
				root.addContent(body);

				Document doc = new Document(root,docType);
				
				String topicFileName = tDoc.name + ".dita";
				String fullyQualifiedTopicFileName = baseDir + fileSep + tDoc.location + fileSep + topicFileName;				
				// the TopicrefLines are used later for building the ditamap
				TopicrefLine topicrefLine = new TopicrefLine();
				topicrefLine.href = tDoc.location + fileSep + topicFileName;
				topicrefLine.navtitle = tDoc.description.replaceAll(htmlRegex, "");
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
					File dir = new File(baseDir  + fileSep + tDoc.location );
					dir.mkdirs();
					File file = new File(fullyQualifiedTopicFileName);
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

		}
	}

}
