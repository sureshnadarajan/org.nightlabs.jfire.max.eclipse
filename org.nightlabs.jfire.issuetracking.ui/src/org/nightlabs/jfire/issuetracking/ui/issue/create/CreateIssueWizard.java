package org.nightlabs.jfire.issuetracking.ui.issue.create;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.base.ui.wizard.WizardDelegateRegistry;

/**
 * The content and logic for this class comes from the {@link CreateIssueWizardDelegate} which is registered via
 * the extension-point with the id {@link WizardDelegateRegistry#EXTENSION_POINT_ID}.
 *
 * @author Chairat Kongarayawetchakun - chairat[at]nightlabs[dot]de
 */
public class CreateIssueWizard
extends DynamicPathWizard
implements INewWizard
{
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// do nothing
	}

//	private Issue newIssue;
//	private CreateIssueDetailWizardPage issueCreateGeneralWizardPage;
//
//	public CreateIssueWizard()
//	{
//		setWindowTitle(Messages.getString("org.nightlabs.jfire.issuetracking.ui.issue.create.CreateIssueWizard.title"));	 //$NON-NLS-1$
//		newIssue = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class));
//		newIssue.setReporter(Login.sharedInstance().getUser(new String[]{User.FETCH_GROUP_NAME}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()));
//	}
//
//	/**
//	 * Adding the page to the wizard.
//	 */
//	@Override
//	public void addPages() {
//		issueCreateGeneralWizardPage = new CreateIssueDetailWizardPage(newIssue);
//		addPage(issueCreateGeneralWizardPage);
//	}
//
//	private static String[] FETCH_GROUP = new String[]{
//		FetchPlan.DEFAULT,
//		Issue.FETCH_GROUP_STATE,
//		Issue.FETCH_GROUP_STATES,
//		Issue.FETCH_GROUP_ISSUE_LOCAL,
//		Issue.FETCH_GROUP_ISSUE_TYPE,
//		IssueLocal.FETCH_GROUP_STATE,
//		IssueLocal.FETCH_GROUP_STATES,
//		Statable.FETCH_GROUP_STATE,
//		Issue.FETCH_GROUP_ISSUE_LINKS,
//		StatableLocal.FETCH_GROUP_STATE,
//		State.FETCH_GROUP_STATE_DEFINITION,
//	};
//
//	@Override
//	public boolean performFinish() {
//		try {
//			getContainer().run(false, false, new IRunnableWithProgress() {
//				public void run(IProgressMonitor _monitor) throws InvocationTargetException, InterruptedException {
//
//					Issue issue = IssueDAO.sharedInstance().storeIssue(newIssue, true, FETCH_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
//					IssueEditorInput editorInput = new IssueEditorInput((IssueID)JDOHelper.getObjectId(issue));
//					try {
//						Editor2PerspectiveRegistry.sharedInstance().openEditor(editorInput, IssueEditor.EDITOR_ID);
//					} catch (Exception e) {
//						throw new RuntimeException(e);
//					}
//				}
//			});
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return true;
//	}
//

}