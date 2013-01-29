package org.intsdo.junit.bdb;

/*
* Copyright 2011 International Health Terminology Standards Development Organisation.
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.Bdb;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * A test runner which initialises the connection to a terminology database.
 * Also provides support for dependency ordering of test methods within a test clases.<p>
 * Use the @{@link BdbTestRunnerConfig} annotation to specify the configuration for the test class. 
 */
public class BdbTestRunner extends BlockJUnit4ClassRunner implements OrderedDependencyRunner {
    private static boolean addHook = false;
    private static String bdbLocation = null;

    // ~--- fields --------------------------------------------------------------

    private File buildDirFile = new File("target");

    // ~--- constructors --------------------------------------------------------

    public BdbTestRunner(Class<?> klass) throws InitializationError {
        super(klass);

        String surefireClassPath = System.getProperty("surefire.test.class.path");

        if (surefireClassPath != null) {
            String[] surefireClassPathParts = surefireClassPath.split(System.getProperty("path.separator"));

            buildDirFile = new File(surefireClassPathParts[0].replaceAll("test-classes$", ""));
            System.out.println(buildDirFile.getAbsolutePath());
        }

        BdbTestRunnerConfig annotation = klass.getAnnotation(BdbTestRunnerConfig.class);

        if (annotation == null) {
            throw new InitializationError("You must specify a BdbTestRunnerConfig annotation for the test");
        }

        String configDbPath = annotation.bdbLocation();
        
        String sysDbPath = System.getProperty("test.bdb.dir");
        if (sysDbPath != null) {
            configDbPath = sysDbPath;
            AceLog.getAppLog().info("Using system property defined db location (test.bdb.dir)");
        }
        
        boolean absolutePath = (configDbPath.startsWith(System.getProperty("file.separator")));
        File dbDir = (absolutePath) ? new File (configDbPath) : new File(buildDirFile, configDbPath);
        
        if (!dbDir.exists()) {
            throw new InitializationError("Database directory not found! (" + dbDir.getAbsolutePath() + ")");
        }
        
        if ((bdbLocation != null) && !bdbLocation.equals(dbDir.getAbsolutePath())) {
            try {
                AceLog.getAppLog().info("Switching to new bdb location, was " + bdbLocation);
                Bdb.close();
                bdbLocation = null;
            } catch (InterruptedException ex) {
                throw new InitializationError(ex);
            } catch (ExecutionException ex) {
                throw new InitializationError(ex);
            }
        }

        if (bdbLocation == null) {
            try {
                Bdb.selectJeProperties(dbDir, dbDir);
            } catch (IOException ex) {
                throw new InitializationError(ex);
            }

            AceLog.getAppLog().info("Using DB location : " + dbDir.getAbsolutePath());
            Bdb.setup(dbDir.getAbsolutePath());
            bdbLocation = dbDir.getAbsolutePath();
        }

        System.out.println("Created BdbTestRunner for: " + klass);

        if (addHook) {
            addHook = false;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {                        
                        Bdb.close();
                        AceLog.getAppLog().info("Database closed on shutdown");
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(BdbTestRunner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, "BdbTestRunner Shutdown hook"));
        }
        
        for (Class<? extends BdbTestInitialiser> initialiserClass : annotation.init()) {
            try {
                initialiserClass.newInstance().init();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InitializationError(e);
            }
        }
        
    }

    /**
     * Order the tests to fulfil the wishes of the @{@link DependsOn} annotation on test methods.
     */
    @Override
    public List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methodList = new ArrayList<>(super.computeTestMethods());
        final int methodCount = methodList.size();
        // First sort by dependency depth, so that methods with no dependencies are first. 
        // This ensures the success of the next sorting phase since it
        // does not re-sweep the collection.
        
        Collections.sort(methodList, new Comparator<FrameworkMethod>() {
            @Override
            public int compare(FrameworkMethod o1, FrameworkMethod o2) {
                return (getDependencyDepth(o1) - getDependencyDepth(o2));
            }
            
            private int getDependencyDepth(FrameworkMethod method) {
                int depth=0;
                Method m = method.getMethod();
                DependsOn dependency = m.getAnnotation(DependsOn.class);
                try {
                    while (dependency != null) {
                        ++depth;
                        
                        if (depth > methodCount) {
                            // InitializationError
                            throw new RuntimeException("Dependency loop detected");
                        }
                        
                        String dependentMethodName = dependency.value();
                        m = getTestClass().getJavaClass().getMethod(dependentMethodName);
                        dependency = m.getAnnotation(DependsOn.class);
                    }
                } catch (NoSuchMethodException | SecurityException e) {}
                return depth;
            }
        });

        // Now sort the collection again by just looking for a direct dependency between the  
        // two comparison subjects.
        
        Collections.sort(methodList, new Comparator<FrameworkMethod>() {
            @Override
            public int compare(FrameworkMethod o1, FrameworkMethod o2) {
                DependsOn dependency = o2.getAnnotation(DependsOn.class);
                if ((dependency != null) && (dependency.value().equals(o1.getName()))) return -1;

                dependency = o1.getAnnotation(DependsOn.class);
                if ((dependency != null) && (dependency.value().equals(o2.getName()))) return 1;
                
                return 0;
            }
        });
        
        return methodList;
    }
}
