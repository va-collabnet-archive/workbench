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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.db.bdb.Bdb;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 *
 * @author kec
 */
public class BdbTestRunner extends BlockJUnit4ClassRunner {

    private static boolean addHook = true;
    private static String bdbLocation = null;

    public BdbTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        BdbTestRunnerConfig annotation = klass.getAnnotation(BdbTestRunnerConfig.class);
        if (annotation == null) {
            throw new InitializationError(
                    "You must specify a BdbTestRunnerConfig annotation for the test");
        }
        if (bdbLocation != null && !bdbLocation.equals(annotation.bdbLocation())) {
            try {
                Bdb.close();
                bdbLocation = null;
            } catch (InterruptedException ex) {
                throw new InitializationError(ex);
            } catch (ExecutionException ex) {
                throw new InitializationError(ex);
            }
        }
        if (bdbLocation == null) {
            File dbDir = new File(annotation.bdbLocation());
            try {
                Bdb.selectJeProperties(dbDir, dbDir);
            } catch (IOException ex) {
                throw new InitializationError(ex);
            }
            Bdb.setup(annotation.bdbLocation());
            bdbLocation = annotation.bdbLocation();
        }
        System.out.println("Created BdbTestRunner for: " + klass);
        if (addHook) {
            addHook = false;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bdb.close();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BdbTestRunner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(BdbTestRunner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }));
        }
    }
}
