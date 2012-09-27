/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.task.owl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 *
 * @author marc
 */
@BeanList(specs = {
    @Spec(directory = "tasks/owl", type = BeanType.TASK_BEAN)})
public class CondorRun extends AbstractTask implements ActionListener {

    private boolean continueThisAction;

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            // show in Activity Viewer window
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
            I_ShowActivity gui1 = tf.newActivityPanel(true, config, "Run ConDOR", false);
            gui1.addRefreshActionListener(this);
            gui1.setProgressInfoUpper("Run ConDOR");
            gui1.setIndeterminate(true);
            gui1.setProgressInfoLower("... in process ...");
            long startTime1 = System.currentTimeMillis();
            Runtime cliRuntime = Runtime.getRuntime();

            // run the Unix "ps -ef" command
            // using the Runtime exec method:
            // Process p = Runtime.getRuntime().exec("ps -ef");
            // Process cli = Runtime.getRuntime().exec("pwd");

            Process cli = null;
            if (isWindows()) {
                // cli = Runtime.getRuntime().exec("DIR");
                // printProcess(cli);
                //String cmd[] = {"cmd.exe", "/c", "start dir"};  //This test works but for DIR you need the cmd.exe and the /c
                cli = cliRuntime.exec("./bin2/condor32.exe -i condor_in.owl -o condor_out.owl");
            } else if (isMac()) {
                // Process cli = Runtime.getRuntime().exec("./bin/condor -i condor_in.owl -o condor_out.owl");
                cli = cliRuntime.exec("./bin2/condor -i condor_in.owl -o condor_out.owl");
            } else if (isUnix()) {
                System.out.println("Unix or Linux is not supported");
            } else if (isSolaris()) {
                System.out.println("Solaris is not supported");
            } else {
                System.out.println("This OS is not support!!");
            }

            printProcess(cli);

            gui1.setProgressInfoLower("complete, time = " + toStringLapseSec(startTime1));
            gui1.complete();

        } catch (Exception e) {
            throw new TaskFailedException("exception happened - here's what I know: ", e);
        }
        return Condition.CONTINUE;
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0); // windows
    }

    private static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0); // Mac
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0); // linux or unix
    }

    private static boolean isSolaris() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("sunos") >= 0); // Solaris
    }

    private void printProcess(Process cli) throws IOException {
        String s;

        if (cli == null) {
            System.out.println("no ConDOR CLI process executed.\n");
        }

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(cli.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(cli.getErrorStream()));

        // read the output from the command
        System.out.println("ConDOR CLI standard output:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println("ConDOR CLI standard error (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    /**
     * actionPerformed sets an internal flag to stop from processing
     *
     * @param arg0
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        continueThisAction = false;
    }

    private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((float) lapseTime / 1000).append(" (seconds)");
        return s.toString();
    }
}
