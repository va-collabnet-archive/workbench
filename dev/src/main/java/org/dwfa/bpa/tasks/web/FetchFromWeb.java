package org.dwfa.bpa.tasks.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

@BeanList(specs = 
{ @Spec(directory = "tasks/web", type = BeanType.TASK_BEAN)})
public class FetchFromWeb extends AbstractTask {

    private URL webURL = new URL("http://sparrow.vacd.med.umich.edu/vistawebsvcs/siteservice.asmx/getSite?siteID=688");
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(webURL);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        	webURL = (URL) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public FetchFromWeb() throws MalformedURLException {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
             InputStream inStream = (InputStream) webURL.getContent();
             BufferedReader in   = 
                 new BufferedReader (new InputStreamReader (inStream));
               String line;
               StringBuffer msgBuff = new StringBuffer();
               while ((line = in.readLine()) != null) {
            	   msgBuff.append(line);
            	   msgBuff.append("\n");
               }
        	   JOptionPane.showMessageDialog(new JFrame(), msgBuff.toString());
               return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {  };
    }

    /**
     * @return Returns the webURL.
     */
    public String getWebURLString() {
        return webURL.toString();
    }

    /**
     * @param webURL The webURL to set.
     * @throws MalformedURLException 
     */
    public void setWebURLString(String webURLString) throws MalformedURLException {
        this.webURL = new URL(webURLString);
    }

}