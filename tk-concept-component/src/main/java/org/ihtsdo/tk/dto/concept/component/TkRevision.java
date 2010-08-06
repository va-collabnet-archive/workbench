package org.ihtsdo.tk.dto.concept.component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.ihtsdo.tk.api.ext.I_VersionExternally;

public class TkRevision implements I_VersionExternally {

    public static final long serialVersionUID = 1;
    public static UUID unspecifiedUserUuid = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
    
    public UUID statusUuid;
    public UUID authorUuid;
    public UUID pathUuid;
    public long time = Long.MIN_VALUE;

    public TkRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRevision() {
        super();
    }

    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        pathUuid = new UUID(in.readLong(), in.readLong());
        statusUuid = new UUID(in.readLong(), in.readLong());
        if (dataVersion >= 3) {
        	authorUuid = new UUID(in.readLong(), in.readLong());
        } else {
         	authorUuid = unspecifiedUserUuid;
        }
        time = in.readLong();
        assert time != Long.MIN_VALUE : "Time is Long.MIN_VALUE. Was it initialized?";
    }

    public void writeExternal(DataOutput out) throws IOException {
        assert time != Long.MIN_VALUE : "Time is Long.MIN_VALUE. Was it initialized?";
        out.writeLong(pathUuid.getMostSignificantBits());
        out.writeLong(pathUuid.getLeastSignificantBits());
        
        out.writeLong(statusUuid.getMostSignificantBits());
        out.writeLong(statusUuid.getLeastSignificantBits());
        
        if (authorUuid == null) {
        	authorUuid = unspecifiedUserUuid;
        }
        out.writeLong(authorUuid.getMostSignificantBits());
        out.writeLong(authorUuid.getLeastSignificantBits());
        
        out.writeLong(time);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_VersionExternal#getPathUuid()
     */
    public UUID getPathUuid() {
        return pathUuid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_VersionExternal#getStatusUuid()
     */
    public UUID getStatusUuid() {
        return statusUuid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_VersionExternal#getTime()
     */
    public long getTime() {
        return time;
    }

    public void setPathUuid(UUID pathUuid) {
        this.pathUuid = pathUuid;
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" statusUuid:");
        buff.append(this.statusUuid);
        buff.append(" authorUuid:");
        buff.append(this.authorUuid);
        buff.append(" pathUuid:");
        buff.append(this.pathUuid);
        buff.append(" Time:");
        buff.append("(" + new Date(this.time) + " " + this.time + ")");
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EVersion</code>.
     * 
     * @return a hash code value for this <tt>EVersion</tt>.
     */
    public int hashCode() {
        return Arrays.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EVersion</tt> object, and contains the same values, 
     * field by field, as this <tt>EVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRevision.class.isAssignableFrom(obj.getClass())) {
            TkRevision another = (TkRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            if (!this.statusUuid.equals(another.statusUuid)) {
                return false;
            }
            if (this.authorUuid != null && another.authorUuid != null) {
                if (!this.authorUuid.equals(another.authorUuid)) {
                    return false;
                }
            } else if (!(this.authorUuid == null && another.authorUuid == null)) {
            	return false;
            }
            if (!this.pathUuid.equals(another.pathUuid)) {
                return false;
            }
            if (this.time != another.time) {
                return false;
            }
            // Objects are equal! (Don't climb any higher in the hierarchy)
            return true;
        }
        return false;
    }

    public UUID getAuthorUuid() {
		return authorUuid;
	}

	public void setAuthorUuid(UUID authorUuid) {
		this.authorUuid = authorUuid;
	}

}
