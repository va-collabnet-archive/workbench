/*
 * Created on Dec 11, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.maven;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which writes tasks as java beans for the builder application.
 * 
 * @goal write-beans
  * @requiresDependencyResolution compile
 * 
 */
public class WriteBeans extends AbstractMojo implements ExceptionListener {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * @parameter
	 * @required
	 */
	BeanSpec[] specs;

	/**
	 * @parameter expression="${project.dependencies}"
	 * @required
	 */
	private List<Dependency> dependencies;

	/**
	 * @parameter expression="${settings.localRepository}"
	 * @required
	 */
	private String localRepository;
	
	/**
	 * @parameter
	 * 
	 */
	private String targetSubDir;

	private Exception e = null;

	private File rootDir;

	public WriteBeans() {
		super();
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		rootDir = new File(outputDirectory, targetSubDir);
		for (int i = 0; i < specs.length; i++) {
			//System.out.println(specs[i]);
			if (specs[i].getType().equalsIgnoreCase("task")) {
				writeTaskBean(specs[i]);
			} else if (specs[i].getType().equalsIgnoreCase("bean")) {
                writeGenericBean(specs[i]);
            } else if (specs[i].getType().equalsIgnoreCase("data")) {
                writeDataBean(specs[i]);
            } else {
                writeProcessBean(specs[i]);
            }
		}

	}

    private void writeTaskBean(BeanSpec spec) throws MojoExecutionException {
        try {
            String suffix = ".task";
            writeBean(spec, rootDir, suffix);
        } catch (Exception e) {
            throw new MojoExecutionException("Problem loading class: "
                    + spec.getSourceName(), e);
        }
    }
    private void writeDataBean(BeanSpec spec) throws MojoExecutionException {
        try {
            String suffix = ".data";
            writeBean(spec, rootDir, suffix);

        } catch (Exception e) {
            throw new MojoExecutionException("Problem loading class: "
                    + spec.getSourceName(), e);
        }
    }
    private void writeGenericBean(BeanSpec spec) throws MojoExecutionException {
        try {
            String suffix = ".bean";
            writeBean(spec, rootDir, suffix);

        } catch (Exception e) {
            throw new MojoExecutionException("Problem loading class: "
                    + spec.getSourceName(), e);
        }
    }
    /**
     * @param spec
     * @param rootDir
     * @param suffix
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws FileNotFoundException
     */
    private void writeBean(BeanSpec spec, File rootDir, String suffix) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, FileNotFoundException {
        rootDir.mkdirs();
        URLClassLoader libLoader = MojoUtil.getProjectClassLoader(
                dependencies, localRepository, this.outputDirectory
                        + "/classes/");
        Class beanClass = libLoader.loadClass(spec.getSourceName());
        Object obj;
        if (beanClass.isEnum()) {
            Method m = beanClass.getMethod("valueOf", new Class[] { String.class});
            obj = m.invoke(null, new Object[] { spec.getConstructArg() });
        } else {
            if (spec.getConstructArg() == null) {
                Constructor beanConstructor = beanClass.getConstructor(new Class[] {});
                obj = beanConstructor.newInstance(new Object[] {});
            } else {
                Constructor beanConstructor = beanClass.getConstructor(new Class[] { String.class });
                obj = beanConstructor.newInstance(new Object[] { spec.getConstructArg() });
            }
        }
    
        File taskDir = rootDir;
        if (spec.getDirName() != null) {
           taskDir = new File(rootDir, spec.getDirName());
            
        }
        taskDir.mkdirs();
        File taskFile;
        if (spec.getBeanName() == null) {
            taskFile = new File(taskDir, spec.getSourceName() + suffix);
        } else {
            taskFile = new File(taskDir, spec.getBeanName() + suffix);
        }
        FileOutputStream fos = new FileOutputStream(taskFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }
	

	private void writeProcessBean(BeanSpec spec) throws MojoExecutionException {
		try {
			rootDir.mkdirs();

			final URLClassLoader libLoader = MojoUtil.getProjectClassLoader(
					dependencies, localRepository, this.outputDirectory
							+ "/classes/");
			ClassLoader origCl = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(libLoader);
			Class decoderClass = libLoader.loadClass("java.beans.XMLDecoder");
			Constructor decoderConstructor = decoderClass
					.getConstructor(new Class[] { InputStream.class });
            File xmlSourceFile = new File(sourceDirectory
                    .getAbsolutePath()
                    + "/../process/"
                    + spec.getSourceName()
                    + ".xml");
			final XMLDecoder d = (XMLDecoder) decoderConstructor
					.newInstance(new Object[] { new BufferedInputStream(
							new FileInputStream(xmlSourceFile)) });

			d.setExceptionListener(this);
			Object process = d.readObject();
			d.close();
			Thread.currentThread().setContextClassLoader(origCl);

            File processFile;
            if (spec.getFormat().equalsIgnoreCase("queue")) {
                FileInputStream fis = new FileInputStream(xmlSourceFile);
                int length = fis.available();
                byte[] data = new byte[length];
                fis.read(data);
                fis.close();
                String xmlData = new String(data);
                int processIdStrLoc = xmlData.indexOf("processIdStr");
                int stringElemLoc = xmlData.indexOf("<string>", processIdStrLoc);
                String procIdStr = xmlData.substring(stringElemLoc + 8, stringElemLoc + 8 + 36);
                File processDir = new File(rootDir, spec.getDirName());
                processDir.mkdirs();
                /* 
                 * Creating its own entry id causes duplicate entries if mvn is run twice
                 * Not creating its own entry id reauires each process to have a unique id, or else
                 * it will be overwritten. 
                Uuid entryId = UuidFactory.generate();
                processFile = new File(processDir, procIdStr + "." + 
                        entryId + ".bp");
                        */
                processFile = new File(processDir, procIdStr + "." + 
                        procIdStr + ".bp");
            } else {
                File processDir = new File(rootDir, spec.getDirName());
                processDir.mkdirs();
                processFile = new File(processDir, spec.getSourceName() + ".bp");
            }
			FileOutputStream fos = new FileOutputStream(processFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(process);
			oos.close();

		} catch (Exception e) {
			throw new MojoExecutionException("Problem making process: "
					+ spec.getSourceName(), e);
		}
		if (e != null) {
			throw new MojoExecutionException("Problem making process: "
					+ spec.getSourceName(), e);
		}
	}

	public void exceptionThrown(Exception e) {
		this.getLog().error(e);
		this.e = e;
		
	}

	
}
