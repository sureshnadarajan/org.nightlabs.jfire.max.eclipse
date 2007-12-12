/**
 * 
 */
package org.nightlabs.jfire.issuetracking.admin.ui.overview.issueproperty;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issuetracking.admin.ui.IssueTrackingAdminPlugin;

/**
 * @author Chairat Kongarayawetchakun 
 *
 */
public class IssueTypePriorityCreateWizardPage extends DynamicPathWizardPage {
	
	private IssuePriority issuePriority;
	
	public IssueTypePriorityCreateWizardPage(IssuePriority issuePriority) {
		super(	IssueTypePriorityCreateWizardPage.class.getName(),
	    		"Title",
	    		SharedImages.getWizardPageImageDescriptor(IssueTrackingAdminPlugin.getDefault(), IssueTypePriorityCreateWizardPage.class)
	    	);
		this.issuePriority = issuePriority;
	    setDescription("Description");
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		return new IssueTypePriorityComposite(issuePriority, parent, SWT.NONE);
	}

}
