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
package org.dwfa.ace.task.refset.spec.compute;

public class ProgressReport {

    private int nonMembersCleanedCount = 0;
    private int membersCount = 0;
    private int newMembersCount = 0;
    private int toBeRetiredMembersCount = 0;
    private int databaseCount = 0;
    long startTime = 0;
    long endTime = 0;
    boolean complete = false;
    boolean step1Complete = false;
    boolean step2Complete = false;
    boolean step3Complete = false;
    boolean step4Complete = false;
    boolean step5Complete = false;

    public String toString() {

        // step 1
        String result = "<html>" + "1) Creating / validating refset spec query :";
        if (!step1Complete) {
            result = result + "<font color='green'> Executing.";
        } else {
            result = result + "<font color='red'> COMPLETE.";
        }
        result = result + "<br><font color='black'>";

        // step 2
        result = result + "2) Calculating required updates :";

        if (step1Complete) {
            if (!step2Complete) {
                result = result + "<font color='green'> Executing.";
            } else {
                result = result + "<font color='red'> COMPLETE.";
            }
        }
        result = result + "<br><font color='black'>";

        result = result + "Number of new members : " + newMembersCount + " / " + databaseCount + "<br>"
            + "Number of to-be-retired members : " + toBeRetiredMembersCount + " / " + databaseCount + "<br>";

        // step 3
        result = result + "3) Creating new member refsets :";
        // + getMembersCount() + " / " + newMembersCount;
        if (step2Complete) {
            if (!step3Complete) {
                result = result + "<font color='green'> Executing.";
            } else {
                result = result + "<font color='red'> COMPLETE.";
            }
        }
        result = result + "<br><font color='black'>";

        // step 4
        result = result + "4) Retiring old member refsets :";
        // + getNonMembersCleanedCount() + " / " + toBeRetiredMembersCount;
        if (step3Complete) {
            if (!step4Complete) {
                result = result + "<font color='green'> Executing.";
            } else {
                result = result + "<font color='red'> COMPLETE.";
            }
        }
        result = result + "<br><font color='black'>";

        // step 5
        result = result + "5) Adding / removing marked parent refsets :";
        if (step4Complete) {
            if (!step5Complete) {
                result = result + "<font color='green'> Executing.";
            } else {
                result = result + "<font color='red'> COMPLETE.";
            }
        }
        result = result + "<br><font color='black'>";

        if (complete) {
            long minutes = (endTime - startTime) / 60000;
            long seconds = ((endTime - startTime) % 60000) / 1000;
            result = result + "Total execution time: " + minutes + " minutes, " + seconds + " seconds.";
        } /*
           * else { long minutes = (new Date().getTime() - startTime) / 60000;
           * long seconds = ((new Date().getTime() - startTime) % 60000) / 1000;
           * result = result + "Current execution time: " + minutes +
           * " minutes, " + seconds + " seconds."; }
           */
        return result;
    }

    public int getNonMembersCleanedCount() {
        return nonMembersCleanedCount;
    }

    public void setNonMembersCleanedCount(int nonMembersCleanedCount) {
        this.nonMembersCleanedCount = nonMembersCleanedCount;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isStep1Complete() {
        return step1Complete;
    }

    public void setStep1Complete(boolean step1Complete) {
        this.step1Complete = step1Complete;
    }

    public boolean isStep2Complete() {
        return step2Complete;
    }

    public void setStep2Complete(boolean step2Complete) {
        this.step2Complete = step2Complete;
    }

    public boolean isStep3Complete() {
        return step3Complete;
    }

    public void setStep3Complete(boolean step3Complete) {
        this.step3Complete = step3Complete;
    }

    public boolean isStep4Complete() {
        return step4Complete;
    }

    public void setStep4Complete(boolean step4Complete) {
        this.step4Complete = step4Complete;
    }

    public boolean isStep5Complete() {
        return step5Complete;
    }

    public void setStep5Complete(boolean step5Complete) {
        this.step5Complete = step5Complete;
    }

    public int getNewMembersCount() {
        return newMembersCount;
    }

    public void setNewMembersCount(int newMembersCount) {
        this.newMembersCount = newMembersCount;
    }

    public int getToBeRetiredMembersCount() {
        return toBeRetiredMembersCount;
    }

    public void setToBeRetiredMembersCount(int toBeRetiredMembersCount) {
        this.toBeRetiredMembersCount = toBeRetiredMembersCount;
    }

    public int getDatabaseCount() {
        return databaseCount;
    }

    public void setDatabaseCount(int databaseCount) {
        this.databaseCount = databaseCount;
    }
}
