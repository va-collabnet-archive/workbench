package org.dwfa.ace.api;

import java.util.List;

import org.tigris.subversion.javahl.PromptUserPassword3;

public interface I_HandleSubversion {

    public void svnCheckout(SubversionData svd);
    public void svnCommit(SubversionData svd);
	public void svnCompleteRepoInfo(SubversionData svd);
    public void svnCleanup(SubversionData svd);
    public void svnStatus(SubversionData svd);
    public void svnPurge(SubversionData svd);
    public void svnUpdate(SubversionData svd);
    public List<String> svnList(SubversionData svd);

	public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive);
	public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive);
	public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive);
	public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive);
	public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive);
	public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive);
	
}
