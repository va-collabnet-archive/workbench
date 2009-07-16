package org.dwfa.ace.task.refset.spec.compute;

public class ProgressReport {

    private int nonMembersCleanedCount = 0;
    private int membersCount = 0;
    long startTime = 0;
    long endTime = 0;
    boolean complete = false;
    boolean step1Complete = false;
    boolean step2Complete = false;

    public String toString() {
        String result = "<html>" + "1) Creating refset spec query.";
        if (!step1Complete) {
            result = result
                    + "<font color='green'> Executing. <br><font color='black'>";
        } else {
            result = result
                    + "<font color='red'> COMPLETE. <br><font color='black'>";
        }

        result = result + "2) Executing refset spec query over database.";

        if (!step2Complete) {
            result = result
                    + "<font color='green'> Executing. <br><font color='black'>";
        } else {
            result = result
                    + "<font color='red'> COMPLETE. <br><font color='black'>";
        }

        result = result + "Members found : " + membersCount + "<br>"
                + "Non-members cleaned : " + nonMembersCleanedCount + "<br>";

        if (!complete && step2Complete) {
            result = result + "<br>" + "Finalising refset, please wait...";
        }

        if (complete) {
            long minutes = (endTime - startTime) / 60000;
            long seconds = ((endTime - startTime) % 60000) / 1000;
            result = result + "Total execution time: " + minutes + " minutes, "
                    + seconds + " seconds.";
        }
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
}
