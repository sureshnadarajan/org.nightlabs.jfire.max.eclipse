package org.nightlabs.jfire.issuetracking.ui.issue;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.login.ui.part.LSDViewPart;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItemDAO;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issuetracking.ui.IssueTrackingPlugin;
import org.nightlabs.jfire.issuetracking.ui.issuehistory.IssueHistoryTable;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueHistoryView
extends LSDViewPart
{
	public static final String VIEW_ID = IssueHistoryView.class.getName();

	private IMemento initMemento = null;
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.initMemento = memento;
	}

	private IssueHistoryTable issueHistoryTable;
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.part.ControllablePart#createPartContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartContents(Composite parent)
	{
		issueHistoryTable = new IssueHistoryTable(parent, SWT.NONE);
		issueHistoryTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		SelectionManager.sharedInstance().addNotificationListener(IssueTrackingPlugin.ZONE_PROPERTY, Issue.class, issueSelectionListener);

//		if (initMemento != null)
//			descriptionDetailComposite.init(initMemento);

		issueHistoryTable.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				SelectionManager.sharedInstance().removeNotificationListener(IssueTrackingPlugin.ZONE_PROPERTY, Issue.class, issueSelectionListener);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.login.part.LSDViewPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
	}

	public static String[] FETCH_GROUP = new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST};

	private NotificationListener issueSelectionListener = new NotificationAdapterCallerThread(){
		public void notify(NotificationEvent notificationEvent) {
			Object firstSelection = notificationEvent.getFirstSubject();
			if (firstSelection instanceof IssueID) {
				IssueID issueID = (IssueID) firstSelection;
				if (issueHistoryTable != null && !issueHistoryTable.isDisposed()) {
					Collection<IssueHistoryItem> issueHistoryItems = IssueHistoryItemDAO.sharedInstance().getIssueHistoryItems(
							issueID,
							new String[]{FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
							new NullProgressMonitor());

					issueHistoryTable.setInput(issueHistoryItems);
				}
			}
		}
	};
}