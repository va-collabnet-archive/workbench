/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven;

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
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Goal which writes tasks as java beans for the builder application.
 * 
 * PLEASE USE THE export-beans goal instead...
 * 
 * @deprecated
 * 
 * @goal write-annotated-beans
 * @requiresDependencyResolution compile
 */
public class WriteAnnotatedBeans extends AbstractMojo implements ExceptionListener {

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
     */
    private boolean throwWriteBeansExceptions = false;

    /**
     * @parameter
     */
    private String[] allowedRoots = { "org.dwfa", "org.jehri", "au.gov.nehta" };
    private String[] forbiddenRoots = { "org.dwfa.cement" };

    /**
     * @parameter
     * @required
     */
    private String targetSubDir;

    private Exception e = null;

    private File rootDir;

    /**
     * The maven session
     * 
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    private String[] allowedGoals = new String[] { "install", "write-annotated-beans" };

    public WriteAnnotatedBeans() {
        super();
    }

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        String classNameNoDotClass = "";
        /*
         * getLog().info("java.class.version: " +
         * System.getProperty("java.class.version"));
         * getLog().info("java.vendor: " + System.getProperty("java.vendor"));
         * getLog().info("java.version: " + System.getProperty("java.version"));
         * getLog().info("os.arch: " + System.getProperty("os.arch"));
         * getLog().info("os.name: " + System.getProperty("os.name"));
         * getLog().info("java.home: " + System.getProperty("java.home"));
         */
        if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
            List<Dependency> dependencyWithoutProvided = new ArrayList<Dependency>();
            for (Dependency d : dependencies) {
                if (d.getScope().equals("provided")) {
                    // don't add
                } else if (d.getGroupId().endsWith("jini")) {
                    // don't add
                    // getLog().info("Skipping: " + d);
                } else {
                    if (d.getScope().equals("system")) {
                        getLog().info("System dependency: " + d);
                    }
                    dependencyWithoutProvided.add(d);
                }
            }

            try {
                if (targetSubDir != null) {
                    rootDir = new File(this.outputDirectory, targetSubDir);
                } else {
                    rootDir = this.outputDirectory;

                }
                URLClassLoader libLoader = MojoUtil.getProjectClassLoaderWithoutProvided(dependencies, localRepository,
                    outputDirectory + "/classes/");
                Class beanListClass = libLoader.loadClass(BeanList.class.getName());
                for (Dependency d : dependencyWithoutProvided) {
                    if (d.getScope().equals("provided")) {
                        continue;
                    }

                    if (d.getScope().equals("runtime-directory")) {
                        continue;
                    }

                    if (d.getScope().equals("system")) {
                        continue;
                    }

                    String dependencyPath = MojoUtil.dependencyToPath(localRepository, d);
                    File dependencyFile = new File(dependencyPath);
                    if (dependencyFile.exists()) {
                        getLog().info("writing annotated beans for: " + dependencyPath);
                        JarFile jf = new JarFile(dependencyPath);
                        Enumeration<JarEntry> jarEnum = jf.entries();
                        while (jarEnum.hasMoreElements()) {
                            JarEntry je = jarEnum.nextElement();
                            if (je.getName().endsWith(".class")) {
                                String className = je.getName().replace('/', '.');
                                boolean allowed = false;
                                for (String allowedRoot : allowedRoots) {
                                    if (className.startsWith(allowedRoot)) {
                                        allowed = true;
                                        for (String forbidden : forbiddenRoots) {
                                            if (className.startsWith(forbidden)) {
                                                getLog().info("forbidden: " + je.getName());
                                                allowed = false;
                                                break;
                                            }
                                        }
                                        // getLog().info("allowed: " +
                                        // je.getName());
                                        break;
                                    }
                                }
                                if (allowed) {
                                    classNameNoDotClass = className.substring(0, className.length() - 6);
                                    Class<?> c = libLoader.loadClass(classNameNoDotClass);
                                    Annotation a = c.getAnnotation(beanListClass);
                                    if (c.getAnnotation(beanListClass) != null) {
                                        // getLog().info("Writing annotation for: "
                                        // + c.getCanonicalName());
                                        BeanList bl = (BeanList) Proxy.newProxyInstance(getClass().getClassLoader(),
                                            new Class[] { BeanList.class }, new GenericInvocationHandler(a));
                                        for (Spec s : bl.specs()) {
                                            if (s.type().equals(BeanType.GENERIC_BEAN)) {
                                                writeGenericBean(c, s);
                                            } else if (s.type().equals(BeanType.TASK_BEAN)) {
                                                writeTaskBean(c, s);
                                            } else {
                                                throw new UnsupportedOperationException("Data beans are deprecated. "
                                                    + s);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        getLog().info("Warning. Dependency file does not exist: " + dependencyPath);
                    }

                }
            } catch (Throwable e) {
                getLog().error(e.getMessage() + " while loading " + classNameNoDotClass);
                throw new MojoExecutionException(e.getMessage() + " while loading " + classNameNoDotClass, e);
            }
        } else {
            getLog().info("Not writing. Not an allowed goal.");
        }
    }

    @SuppressWarnings("unchecked")
    private void writeTaskBean(Class c, Spec spec) throws MojoExecutionException {
        try {
            String suffix = ".task";
            writeBean(c, spec, rootDir, suffix);
        } catch (Throwable e) {
            if (throwWriteBeansExceptions) {
                throw new MojoExecutionException("Problem writing bean: " + spec, e);
            } else {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeGenericBean(Class c, Spec spec) throws MojoExecutionException {
        try {
            String suffix = ".bean";
            writeBean(c, spec, rootDir, suffix);

        } catch (Exception e) {
            throw new MojoExecutionException("Problem writing bean: " + spec, e);
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
    @SuppressWarnings("unchecked")
    private void writeBean(Class beanClass, Spec s, File rootDir, String suffix) throws IOException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException, FileNotFoundException {
        rootDir.mkdirs();
        Object obj;
        if (beanClass.isEnum()) {
            Method m = beanClass.getMethod("valueOf", new Class[] { String.class });
            obj = m.invoke(null, (Object[]) s.constructArgs());
        } else {
            if (s.constructArgs().length == 0) {
                Constructor beanConstructor = beanClass.getConstructor(new Class[] {});
                obj = beanConstructor.newInstance(new Object[] {});
            } else {
                Constructor beanConstructor = beanClass.getConstructor(new Class[] { String.class });
                obj = beanConstructor.newInstance((Object[]) s.constructArgs());
            }
        }

        File beanDir = rootDir;
        if (s.directory().length() > 1) {
            beanDir = new File(rootDir, s.directory());
        }
        beanDir.mkdirs();
        File beanFile;
        if (s.beanName().length() == 0) {
            beanFile = new File(beanDir, beanClass.getName() + suffix);
        } else {
            beanFile = new File(beanDir, s.beanName() + suffix);
        }
        // getLog().info("  Writing: " + beanFile.getName());
        FileOutputStream fos = new FileOutputStream(beanFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    @SuppressWarnings( { "unused", "unchecked" })
    private void writeProcessBean(BeanSpec spec) throws MojoExecutionException {
        try {
            File rootDir = new File(this.outputDirectory, "processes");
            rootDir.mkdirs();

            final URLClassLoader libLoader = MojoUtil.getProjectClassLoader(dependencies, localRepository,
                this.outputDirectory + "/classes/");
            ClassLoader origCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(libLoader);
            Class decoderClass = libLoader.loadClass("java.beans.XMLDecoder");
            Constructor decoderConstructor = decoderClass.getConstructor(new Class[] { InputStream.class });
            File xmlSourceFile = new File(sourceDirectory.getAbsolutePath() + "/../process/" + spec.getSourceName()
                + ".xml");
            final XMLDecoder d = (XMLDecoder) decoderConstructor.newInstance(new Object[] { new BufferedInputStream(
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
                File processDir = new File(this.outputDirectory, spec.getDirName());
                processDir.mkdirs();
                /*
                 * Creating its own entry id causes duplicate entries if mvn is
                 * run twice Not creating its own entry id reauires each process
                 * to have a unique id, or else it will be overwritten. Uuid
                 * entryId = UuidFactory.generate(); processFile = new
                 * File(processDir, procIdStr + "." + entryId + ".bp");
                 */
                processFile = new File(processDir, procIdStr + "." + procIdStr + ".bp");
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
            throw new MojoExecutionException("Problem making process: " + spec.getSourceName(), e);
        }
        if (e != null) {
            throw new MojoExecutionException("Problem making process: " + spec.getSourceName(), e);
        }
    }

    public void exceptionThrown(Exception e) {
        this.getLog().error(e);
        this.e = e;

    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public MavenSession getSession() {
        return session;
    }

    public void setSession(MavenSession session) {
        this.session = session;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
    }

}
