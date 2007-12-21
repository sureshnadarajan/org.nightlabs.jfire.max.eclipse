/**
 * 
 */
package org.nightlabs.jfire.issuetracking.ui.issue.editor;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.editor.FormPage;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.jbpm.ui.state.CurrentStateComposite;
import org.nightlabs.jfire.jbpm.ui.transition.next.NextTransitionComposite;
import org.nightlabs.jfire.jbpm.ui.transition.next.SignalEvent;
import org.nightlabs.jfire.jbpm.ui.transition.next.SignalListener;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class IssueTypeAndStateSection extends AbstractIssueEditorGeneralSection {

	private Label issueTypeLabel;
	private Label statusLabel;
	
	private CurrentStateComposite currentStateComposite;
	private NextTransitionComposite nextTransitionComposite;
	
	/**
	 * @param section
	 * @param managedForm
	 */
	public IssueTypeAndStateSection(FormPage page, Composite parent, IssueEditorPageController controller) {
		super(page, parent, controller);
		getClient().getGridLayout().numColumns = 3;
		getClient().getGridLayout().makeColumnsEqualWidth = false;
		getSection().setText("Type and Status");
		
		issueTypeLabel = new Label(getClient(), SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		issueTypeLabel.setLayoutData(gd);
		
		statusLabel = new Label(getClient(), SWT.WRAP);
		statusLabel.setText("Status: ");
		statusLabel.setLayoutData(new GridData());
		
		currentStateComposite = new CurrentStateComposite(getClient(), SWT.NONE);
		nextTransitionComposite = new NextTransitionComposite(getClient(), SWT.NONE);
		nextTransitionComposite.addSignalListener(new SignalListener() {
			public void signal(SignalEvent event) {
				signalIssue(event);
			}
		});
	}
	
	protected void signalIssue(final SignalEvent event) {
		Job job = new Job("Performing transition") {
			@Override
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				try {
					IssueManager im = IssueManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					im.signalIssue((IssueID)JDOHelper.getObjectId(getIssue()), event.getTransition().getJbpmTransitionName());
				} catch (Exception x) {
					throw new RuntimeException(x);
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.setUser(true);
		job.schedule();		
	}
	
	protected void doSetIssue(Issue issue) {
		issueTypeLabel.setText(
			String.format(
				"Issue type: %s", 
				issue.getIssueType().getName().getText())
		);
		currentStateComposite.setStatable(issue);
		nextTransitionComposite.setStatable(issue);		
	}

}
