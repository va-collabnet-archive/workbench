package org.ihtsdo.mojo.pbl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal generates plugin metadata.xml, must be run prior to deploy
 * 
 * @goal pbl-pre-deploy
 */

public class PblPreDeploy extends AbstractMojo {
   
    /**
     * Plugins to include in group metadata.
     * 
     * @parameter
     * @required
     */
    private String[] pluginsInMetadata;


    /**
     * Group id
     * 
     * @parameter expression="${project.groupId}"
     * @required
     */
    String groupId;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("groupId: " + groupId);
        StringBuffer buf = new StringBuffer();
        
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buf.append("   <metadata>\n");
        buf.append("      <plugins>\n");
        
        for (String plugin: pluginsInMetadata) {
            buf.append("        <plugin>\n");
            buf.append("           <name>" + plugin + "</name>\n");
            buf.append("           <prefix>" + plugin + "</prefix>\n");
            buf.append("           <artifactId>" + plugin + "</artifactId>\n");
            buf.append("        </plugin>\n");
        }
        
        buf.append("      </plugins>\n");
        buf.append("   </metadata>\n");
        
        String metaData = buf.toString();
        
        File outFile = new File("maven2");
        for (String part: groupId.split("\\.")) {
            outFile = new File(outFile, part);
        }
        outFile.mkdirs();
        outFile = new File(outFile, "maven-metadata.xml");

        try {
            FileWriter outFileWriter = new FileWriter(outFile);
            outFileWriter.append(metaData);
            outFileWriter.close();
            
            MessageDigest sha1 = MessageDigest.getInstance( "SHA" );
            sha1.update(metaData.getBytes());
            byte[] sha1Digest = sha1.digest();
            
            File sha1File = new File(outFile.getParent(), "maven-metadata.xml.sha1");
            FileWriter sha1FileWriter = new FileWriter(sha1File);
            // dump out the hash
            for (byte b: sha1Digest) {
                sha1FileWriter.append(Integer.toHexString( b & 0xff ));
            }
            sha1FileWriter.close();
            
            MessageDigest md5 = MessageDigest.getInstance( "MD5" );
            md5.update(metaData.getBytes());
            byte[] md5Digest = md5.digest();
            
            File md5File = new File(outFile.getParent(), "maven-metadata.xml.md5");
            FileWriter md5FileWriter = new FileWriter(md5File);
            // dump out the hash
            for (byte b: md5Digest) {
                md5FileWriter.append(Integer.toHexString( b & 0xff ));
            }
            md5FileWriter.close();
            
            
            
            
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
    }

}
