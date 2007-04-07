package org.dwfa.svn;

import org.dwfa.ace.AceLog;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.NotifyStatus;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.SVNClient;

public class Svn {
	
	private static SVNClient client;
	
	public static SVNClient getSvnClient() {
		if (client == null)  {
			client = new SVNClient();
			// The SVNClient needs an implementation of Notify before
			// successfully executing any other methods.
			client.notification2(new Notify2() {
				public void onNotify(NotifyInformation info) {
					SvnLog.info("svn onNotify: " + 
							" path: " + info.getPath() +  "\n" + 
							" kind: " + NodeKind.getNodeKindName(info.getKind()) +  " " + 
							" content state: " + NotifyStatus.statusNames[info.getContentState()] +  " " + 
							" prop state: " + NotifyStatus.statusNames[info.getPropState()] +  " \n" + 
							" err msg: " + info.getErrMsg() +  " " + 
							" mime : " + info.getMimeType() + " " + 
							" revision: " + info.getRevision() + " \n" + 
							" lock: " + info.getLock() + " " + 
							" lock state: " + info.getLockState() + " " + 
							" action: " + NotifyAction.actionNames[info.getAction()]);
				}
			});		}
		return client;
	}

	/**
	 * @param args
	 * @throws ClientException
	 */
	public static void main(String[] args) throws ClientException {

		String localRepo = "target/maven-javadoc-plugin";
		AceLog.getLog().info("checkout");
		getSvnClient().checkout("http://svn.apache.org/repos/asf/maven/plugins/trunk/maven-javadoc-plugin",
				localRepo, Revision.HEAD, true);
		AceLog.getLog().info("cleanup");
		getSvnClient().cleanup(localRepo);
		AceLog.getLog().info("client version: " + getSvnClient().info(localRepo));
	}
}
