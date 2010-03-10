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
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

public class SnoDL implements Serializable {
    private static final long serialVersionUID = 1L;
    List<SnoConSer> lhs;
    SnoConSer rhs;

    // STRINGS
    // black bullet, &bull; U+2022 (8226)
    public final static String BLACK_BULLET_STR = new String(String.valueOf('\u2022'));
    // white bullet
    public final static String WHITE_BULLET_STR = new String(String.valueOf('\u25E6'));
    // equivalence aka limit
    public final static String EQUIVALENCE_STR = new String(String.valueOf("\u2250"));
    // alternately " [ "
    public final static String LEFT_INCLUSION_STR = new String(String.valueOf("\u2291"));
    // alternately " ] "
    public final static String RIGHT_INCLUSION_STR = new String(String.valueOf("\u2292"));
    // conjunction
    public final static String CONJUNCTION_STR = new String(String.valueOf("\u2293"));
    // epsilon
    public final static String EPSILON_STR = new String(String.valueOf("\u03F5"));

    public SnoDL(SnoConSer r) {
        this.rhs = r;
        lhs = new ArrayList<SnoConSer>();
    }

    public SnoDL(SnoDL dl) {
        this.rhs = new SnoConSer(dl.rhs.id, dl.rhs.isDefined);
        lhs = new ArrayList<SnoConSer>();
        for (SnoConSer sc : dl.lhs)
            lhs.add(new SnoConSer(sc.id, sc.isDefined));
    }

    public List<SnoConSer> getLhs() {
        return this.lhs;
    }

    public void addLhs(SnoConSer l) {
        this.lhs.add(l);
    }

    public void deleteLhs(int index) {
        this.lhs.remove(index);
    }

    public void duplicateLhs(int nid) {
        this.lhs.add(new SnoConSer(nid, false));
    }

    public void moveLhs(int indexA, int direction) {
        int max = lhs.size();
        int indexB = indexA + direction;

        if (indexB >= 0 && indexB < max) {
            SnoConSer tmp = lhs.get(indexA);
            lhs.set(indexA, lhs.get(indexB)); // move B to A
            lhs.set(indexB, tmp); // move A to B
        }
    }

    public void setRhs(SnoConSer r) {
        this.rhs = r;
    }

    public SnoConSer getRhs() {
        return this.rhs;
    }

    public int getRhsNid() {
        return this.rhs.id;
    }

    public int[] getLhsNids() {
        int lhsNids[] = new int[lhs.size()];
        int i = 0;
        for (SnoConSer scs : lhs) {
            lhsNids[i] = scs.id;
            i += 1;
        }
        return lhsNids;
    }

    public String toString() {
        return toStringDl() + "; " + toStringKrss();
    }

    public String toStringHtml() {
        return "<html><font face='Dialog' size='3' color='blue'>" + toStringDl()
            + "; <font face='Dialog' size='3' color='green'>" + toStringKrss();
    }

    public String toStringDl() {
        I_TermFactory tf = Terms.get();
        StringBuilder s = null;
        try {
            if (lhs.size() == 0) {
                // reflexive
                s = new StringBuilder(EPSILON_STR + " " + LEFT_INCLUSION_STR + " \""
                    + tf.getConcept(rhs.id).getInitialText() + "\"");
            } else if (lhs.size() == 1) {
                s = new StringBuilder("\"" + tf.getConcept(lhs.get(0).id).getInitialText() + "\" "
                        + LEFT_INCLUSION_STR + " \"" + tf.getConcept(rhs.id).getInitialText()
                        + "\"");
            } else if (lhs.size() == 2) {
                int lhs0Nid = lhs.get(0).id;
                int lhs1Nid = lhs.get(1).id;
                // int rhsNid = rhs.id;
                s = new StringBuilder("\"" + tf.getConcept(lhs0Nid).getInitialText() + "\" "
                        + WHITE_BULLET_STR + " \"" + tf.getConcept(lhs1Nid).getInitialText()
                        + "\" " + LEFT_INCLUSION_STR + " \""
                    + tf.getConcept(rhs.id).getInitialText() + "\"");
            } else if (lhs.size() >= 3) {
                s = new StringBuilder();
                int i;
                for (i = 0; i < lhs.size() - 1; i++) {
                    s.append("\"" + tf.getConcept(lhs.get(i).id).getInitialText() + "\" "
                            + WHITE_BULLET_STR + " ");
                }
                s.append("\"" + tf.getConcept(lhs.get(i).id).getInitialText() + "\" "
                        + LEFT_INCLUSION_STR + " \"" + tf.getConcept(rhs.id).getInitialText()
                        + "\"");
            }
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s.toString();
    }

    public String toStringKrss() {
        I_TermFactory tf = Terms.get();
        StringBuilder s = null;
        try {
            if (lhs.size() == 0) {
                // (reflexive RN)
                s = new StringBuilder("(reflexive \"" + tf.getConcept(rhs.id).getInitialText()
                        + "\")");
            } else if (lhs.size() == 1) {
                // (define-primitive-role RN1 :parent RN2)
                s = new StringBuilder("(define-primitive-role "
                        + tf.getConcept(lhs.get(0).id).getInitialText() + "\" :parent \""
                        + tf.getConcept(rhs.id).getInitialText() + "\")");
            } else if (lhs.size() == 2) {
                int lhs0Nid = lhs.get(0).id;
                int lhs1Nid = lhs.get(1).id;
                int rhsNid = rhs.id;
                if (lhs0Nid == lhs1Nid && lhs1Nid == rhsNid) {
                    // (transitive RN)
                    s = new StringBuilder("(transitive \""
                            + tf.getConcept(lhs0Nid).getInitialText() + "\")");
                } else if (lhs0Nid == rhsNid) {
                    // (define-primitive-role RHS :right-identity LHS1)
                    s = new StringBuilder("(define-primitive-role \""
                            + tf.getConcept(rhsNid).getInitialText() + "\" :right-identity \""
                            + tf.getConcept(lhs1Nid).getInitialText() + "\")");

                } else if (lhs1Nid == rhsNid) {
                    // (define-primitive-role RHS :left-identity LHS0)
                    s = new StringBuilder("(define-primitive-role \""
                            + tf.getConcept(rhsNid).getInitialText() + "\" :left-identity \""
                            + tf.getConcept(lhs0Nid).getInitialText() + "\")");
                } else {
                    // (role-inclusion (compose LHS0 LHS1) RHS)
                    s = new StringBuilder("(role-inclusion (compose \""
                            + tf.getConcept(lhs0Nid).getInitialText() + "\" \""
                            + tf.getConcept(lhs1Nid).getInitialText() + "\") \""
                            + tf.getConcept(rhsNid).getInitialText() + "\")");
                }

            } else if (lhs.size() >= 3) {
                s = new StringBuilder(" .. ");
            }
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s.toString();
    }

    public void del(int index) {
        lhs.remove(index);
    }

    public void dupl(int index) {
        SnoConSer sc = lhs.get(index);
        lhs.add(new SnoConSer(sc.id, sc.isDefined));
    }

    public void move(int indexA, int direction) {
        int max = lhs.size();
        int indexB = indexA + direction;

        if (indexB >= 0 && indexB < max) {
            SnoConSer tmp = lhs.get(indexA);
            lhs.set(indexA, lhs.get(indexB)); // move B to A
            lhs.set(indexB, tmp); // move A to B
        }
    }

}
