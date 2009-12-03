package org.dwfa.ace.task.classify;

public class SnoCon implements Comparable<Object> {
    public int id;
    public boolean isDefined;

    public SnoCon() {
        id = Integer.MIN_VALUE;
        isDefined = false;
    }

    public SnoCon(int id, boolean isDefined) {
        this.id = id;
        this.isDefined = isDefined;
    }

    public void SetId(int i) {
        this.id = i;
    }

    public void SetIsDefined(boolean b) {
        this.isDefined = b;
    }

    public int compareTo(Object o) {
        SnoCon other = (SnoCon) o;
        if (this.id > other.id) {
            return 1; // this is greater than received
        } else if (this.id < other.id) {
            return -1; // this is less than received
        } else {
            return 0; // this == received
        }
    }

}
