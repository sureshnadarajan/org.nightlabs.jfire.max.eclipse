package org.nightlabs.jfire.issuetracking.ui.issue;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.dao.IssueDAO;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issuetracking.ui.IssueTrackingPlugin;
import org.nightlabs.jfire.security.User;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueCommentView
extends LSDViewPart
{
	public static final String VIEW_ID = IssueCommentView.class.getName();

	private FormToolkit toolkit;
	private ScrolledForm scrolledForm;
	private Composite body;
	private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	private IMemento initMemento = null;
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.initMemento = memento;
	}

	private Composite parent;
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.part.ControllablePart#createPartContents(org.eclipse.swt.widgets.Composite)
	 */

	private User user;
	@Override
	public void createPartContents(Composite parent)
	{
		this.parent = parent;

		Job job = new Job("Loading login user...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				user = Login.sharedInstance().getUser(null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
		toolkit = new FormToolkit(parent.getDisplay());
		scrolledForm = toolkit.createScrolledForm(parent);
		scrolledForm.setText("No issue selected");
		scrolledForm.setBackground(null);
		
		body = scrolledForm.getBody();
		body.setBackground(null);
		
		GridLayout layout = new GridLayout(4, true);
		body.setLayout(layout);

		SelectionManager.sharedInstance().addNotificationListener(IssueTrackingPlugin.ZONE_PROPERTY, Issue.class, issueSelectionListener);

		scrolledForm.addDisposeListener(new DisposeListener() {
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

	private static String[] FETCH_GROUP = new String[] {
		FetchPlan.DEFAULT,
		Issue.FETCH_GROUP_ISSUE_COMMENTS,
		Issue.FETCH_GROUP_SUBJECT,
		IssueComment.FETCH_GROUP_USER,
		IssueComment.FETCH_GROUP_ISSUE};

	private Issue issue;
	private NotificationListener issueSelectionListener = new NotificationAdapterCallerThread(){
		public void notify(NotificationEvent notificationEvent) {
			Object firstSelection = notificationEvent.getFirstSubject();
			if (firstSelection instanceof IssueID) {
				IssueID issueID = (IssueID) firstSelection;
				issue = IssueDAO.sharedInstance().getIssue(issueID, FETCH_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
				scrolledForm.setText("Issue " + issue.getIssueID() + " - " + issue.getSubject().getText());

				createCommentsByToolkit(issue);
			}
		}
	};

	private void createCommentsByToolkit(Issue issue) {
		//Dispose old controls
		for (Control control : body.getChildren()) {
			control.dispose();
		}
		
		for (final IssueComment comment : issue.getComments()) {
			createCommentEntry(body, comment);
		}

		scrolledForm.reflow(true);
	}

	private static Color reportBackgroundColor = new Color(Display.getCurrent(), 200, 200, 232);
	private static Color commentBackgroundColor = new Color(Display.getCurrent(), 232, 232, 232);
	private Map<Long, Text> commentMap = new HashMap<Long, Text>();
	private void createCommentEntry(final Composite body, final IssueComment comment) {
		/**Reporter**/
		Composite reporterComposite = toolkit.createComposite(body);
		GridLayout gridLayout = new GridLayout(1, false);
		reporterComposite.setLayout(gridLayout);
		reporterComposite.setBackground(reportBackgroundColor);
		//Label
		Hyperlink idLink = toolkit.createHyperlink(reporterComposite, "(" + Long.toString(comment.getCommentID()) + ")", SWT.NONE);
		idLink.setBackground(reportBackgroundColor);
		
		Hyperlink userNameLink = toolkit.createHyperlink(reporterComposite, comment.getUser().getName(), SWT.NONE);
		userNameLink.setBackground(reportBackgroundColor);
		
		Label timeLabel = toolkit.createLabel(reporterComposite, dateTimeFormat.format(comment.getCreateTimestamp()));
		timeLabel.setBackground(reportBackgroundColor);

		//Actions
		if (user.getUserID().equals(comment.getUser().getUserID())) {
			Composite actionComposite = toolkit.createComposite(reporterComposite);
			actionComposite.setLayout(new GridLayout(3, false));
			actionComposite.setBackground(reportBackgroundColor);
			//Images
			ImageHyperlink editLink = toolkit.createImageHyperlink(actionComposite, SWT.NONE);
			editLink.setImage(SharedImages.EDIT_16x16.createImage(scrolledForm.getShell().getDisplay()));
			editLink.setHoverImage(SharedImages.EDIT_24x24.createImage(scrolledForm.getShell().getDisplay()));
			editLink.setBackground(reportBackgroundColor);
			editLink.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					IssueCommentEditDialog dialog = new IssueCommentEditDialog(RCPUtil.getActiveShell(), comment);
					int result = dialog.open();
					if (result == Dialog.OK) {
						Text t = commentMap.get(comment.getCommentID());
						t.setText(comment.getText());
						t.getParent().layout();
						body.layout();
					}
				}
			});
			
			ImageHyperlink deleteLink = toolkit.createImageHyperlink(actionComposite, SWT.NONE);
			deleteLink.setImage(SharedImages.DELETE_16x16.createImage());
			deleteLink.setHoverImage(SharedImages.DELETE_24x24.createImage());
			deleteLink.setBackground(reportBackgroundColor);

			ImageHyperlink printLink = toolkit.createImageHyperlink(actionComposite, SWT.NONE);
			printLink.setImage(SharedImages.PRINT_16x16.createImage());
			printLink.setHoverImage(SharedImages.PRINT_24x24.createImage());
			printLink.setBackground(reportBackgroundColor);
		}
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		reporterComposite.setLayoutData(gridData);

		/**Detail**/
		Composite textComposite = toolkit.createComposite(body);
		textComposite.setLayout(new GridLayout(3, false));
		textComposite.setBackground(commentBackgroundColor);

		//Label
		Text text = toolkit.createText(textComposite, comment.getText(), SWT.WRAP);
		text.setBackground(commentBackgroundColor);
		commentMap.put(comment.getCommentID(), text);

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		textComposite.setLayoutData(gridData);
	}
	
	/*private URL url = FileLocator.find(IssueTrackingPlugin.getDefault().getBundle(), new Path("default.css"), null);
	private void createCommentsByBrowser(Issue issue) {
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		if (browser == null) {
			browser = new Browser(body, SWT.NONE);
			browser.setLayoutData(new GridData(GridData.FILL_BOTH));
			browser.addLocationListener(new LocationAdapter() {
			    @Override
				public void changing(LocationEvent event) {
			      int i = event.location.indexOf('#');
			      String value = event.location.substring(i + 1);
			    }
			  });
		}

		StringBuffer buf = new StringBuffer();
		buf.append("<html>");
		buf.append("<head><link rel=\"stylesheet\" type=\"text/css\" href=\""+ url.toString() + "\" />");
		buf.append("</head><body><table class=\"width100\" cellspacing=\"1\">");

		for (IssueComment comment : issue.getComments()) {
			buf.append("<tr class=\"bugnote\">");
			buf.append("<td class=\"bugnote-public\"");
			buf.append("<span class=\"small\">" + comment.getCommentID() + "</span><br/>");
			buf.append(comment.getUser().getName() + "<br />");
			buf.append("<span class=\"small\">" + dateTimeFormat.format(comment.getCreateTimestamp()) + "</span><br />");
			buf.append("<br /><span class=\"small\">");
			buf.append("<form method=\"POST\" action=\"bugnote_edit_page.php?bugnote_id=1706\"><input type=\"submit\" class=\"button-small\" value=\"Edit\" /></form><form method=\"POST\" action=\"bugnote_delete.php?bugnote_id=1706\"><input type=\"submit\" class=\"button-small\" value=\"Delete\" /></form><form method=\"POST\" action=\"bugnote_set_view_state.php?private=1&amp;bugnote_id=1706\"><input type=\"submit\" class=\"button-small\" value=\"Make Private\" /></form></span>");
			buf.append("</td>");

			buf.append("<td class=\"bugnote-note-public\">" + comment.getText() + "<br /></td>");
			buf.append("</tr>");

			buf.append("<tr><td class=\"spacer\" colspan=\"2\">&nbsp;</td></tr>");
		}

		buf.append("</table></body></html>");
		browser.setText(buf.toString());
		scrolledForm.reflow(true);
	}*/

	@Override
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	};
}