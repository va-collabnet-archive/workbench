/**
 * 
 */
package org.dwfa.vodb.bind;

public class MemberAndSecondaryId {
    private int memberId;

    private int secondaryId;

    public MemberAndSecondaryId() {
        super();
    }

    public MemberAndSecondaryId(int secondaryId, int memberId) {
        super();
        this.memberId = memberId;
        this.secondaryId = secondaryId;
    }

    public int getSecondaryId() {
        return secondaryId;
    }

    public int getMemberId() {
        return memberId;
    }
}