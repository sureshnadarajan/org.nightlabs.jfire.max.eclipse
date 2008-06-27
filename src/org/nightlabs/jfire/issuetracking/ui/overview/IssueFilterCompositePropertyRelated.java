package org.nightlabs.jfire.issuetracking.ui.overview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryEvent;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jdo.query.AbstractSearchQuery.FieldChangeCarrier;
import org.nightlabs.jfire.base.ui.search.AbstractQueryFilterComposite;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.dao.IssuePriorityDAO;
import org.nightlabs.jfire.issue.dao.IssueResolutionDAO;
import org.nightlabs.jfire.issue.dao.IssueSeverityTypeDAO;
import org.nightlabs.jfire.issue.dao.IssueTypeDAO;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.query.IssueQuery;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueLabelProvider;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueFilterCompositePropertyRelated 
	extends AbstractQueryFilterComposite<IssueQuery> 
{	
	private static final Logger logger = Logger.getLogger(IssueFilterCompositePropertyRelated.class);
	private Object mutex = new Object();

	private XComboComposite<IssueType> issueTypeCombo;
	private XComboComposite<IssueSeverityType> issueSeverityCombo;
	private XComboComposite<IssuePriority> issuePriorityCombo;
	private XComboComposite<IssueResolution> issueResolutionCombo;

	private IssueType selectedIssueType;
	private IssueSeverityType selectedIssueSeverityType;
	private IssuePriority selectedIssuePriority;
	private IssueResolution selectedIssueResolution;

	/**
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param layoutMode
	 *          The layout mode to use. See {@link XComposite.LayoutMode}.
	 * @param layoutDataMode
	 *          The layout data mode to use. See {@link XComposite.LayoutDataMode}.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public IssueFilterCompositePropertyRelated(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode,
			QueryProvider<? super IssueQuery> queryProvider)
	{
		super(parent, style, layoutMode, layoutDataMode, queryProvider);
		createComposite(this);
		prepareIssueProperties();
	}

	/**
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public IssueFilterCompositePropertyRelated(Composite parent, int style,
			QueryProvider<? super IssueQuery> queryProvider)
	{
		super(parent, style, queryProvider);
		createComposite(this);
		prepareIssueProperties();
	}

	@Override
	public Class<IssueQuery> getQueryClass() {
		return IssueQuery.class;
	}

	@Override
	protected void createComposite(Composite parent)
	{
		parent.setLayout(new GridLayout(3, false));

		XComposite issueTypeComposite = new XComposite(parent, SWT.NONE,
			LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		issueTypeComposite.getGridLayout().numColumns = 2;

		new Label(issueTypeComposite, SWT.NONE).setText("Issue Type: ");
		issueTypeCombo = new XComboComposite<IssueType>(issueTypeComposite, getBorderStyle());
		issueTypeCombo.setLabelProvider(labelProvider);
		issueTypeCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent e)
			{
				selectedIssueType = issueTypeCombo.getSelectedElement();

				issueSeverityCombo.removeAll();
				issueSeverityCombo.addElement(ISSUE_SEVERITY_TYPE_ALL);
				for (IssueSeverityType is : selectedIssueType.getIssueSeverityTypes()) {
					issueSeverityCombo.addElement(is);
				}
				selectedIssueSeverityType = ISSUE_SEVERITY_TYPE_ALL;
				getQuery().setIssueSeverityTypeID(null); // null <=> ISSUE_SEVERITY_TYPE_ALL

				issuePriorityCombo.removeAll();
				issuePriorityCombo.addElement(ISSUE_PRIORITY_ALL);
				for (IssuePriority ip : selectedIssueType.getIssuePriorities()) {
					issuePriorityCombo.addElement(ip);
				}
				selectedIssuePriority = ISSUE_PRIORITY_ALL;
				getQuery().setIssuePriorityID(null); // null <=> ISSUE_PRIORITY_ALL
			}
		});

		new Label(issueTypeComposite, SWT.NONE).setText("Severity: ");
		issueSeverityCombo = new XComboComposite<IssueSeverityType>(issueTypeComposite, getBorderStyle());
		issueSeverityCombo.setLabelProvider(labelProvider);
		issueSeverityCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent e)
			{
				selectedIssueSeverityType = issueSeverityCombo.getSelectedElement();
				getQuery().setIssueSeverityTypeID((IssueSeverityTypeID) JDOHelper.getObjectId(selectedIssueSeverityType));
			}
		});

		new Label(issueTypeComposite, SWT.NONE).setText("Priority: ");
		issuePriorityCombo = new XComboComposite<IssuePriority>(issueTypeComposite, getBorderStyle());
		issuePriorityCombo.setLabelProvider(labelProvider);
		issuePriorityCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent e)
			{
				selectedIssuePriority = issuePriorityCombo.getSelectedElement();
				getQuery().setIssuePriorityID((IssuePriorityID) JDOHelper.getObjectId(selectedIssuePriority));
			}
		});

		new Label(issueTypeComposite, SWT.NONE).setText("Resolution: ");
		issueResolutionCombo = new XComboComposite<IssueResolution>(issueTypeComposite, getBorderStyle());
		issueResolutionCombo.setLabelProvider(labelProvider);
		issueResolutionCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent e)
			{
				selectedIssueResolution = issueResolutionCombo.getSelectedElement();
				getQuery().setIssueResolutionID((IssueResolutionID) JDOHelper.getObjectId(selectedIssueResolution));
			}
		});

		loadProperties();
	}
	
	@Override
	protected void resetSearchQueryValues(IssueQuery query)
	{
		if (ISSUE_TYPE_ALL.equals(selectedIssueType))
		{
			query.setIssueTypeID(null);
		}
		else
		{
			query.setIssueTypeID((IssueTypeID) JDOHelper.getObjectId(selectedIssueType));
		}
		
		if (ISSUE_SEVERITY_TYPE_ALL.equals(selectedIssueSeverityType))
		{
			query.setIssueSeverityTypeID(null);
		}
		else
		{
			query.setIssueSeverityTypeID((IssueSeverityTypeID) JDOHelper.getObjectId(selectedIssueSeverityType));
		}

		if (ISSUE_PRIORITY_ALL.equals(selectedIssuePriority))
		{
			query.setIssuePriorityID(null);
		}
		else
		{
			query.setIssuePriorityID((IssuePriorityID)JDOHelper.getObjectId(selectedIssuePriority));
		}

		if (ISSUE_RESOLUTION_ALL.equals(selectedIssueResolution))
		{
			query.setIssueResolutionID(null);
		}
		else
		{
			query.setIssueResolutionID((IssueResolutionID)JDOHelper.getObjectId(selectedIssueResolution));
		}
	}

	@Override
	protected void unsetSearchQueryValues(IssueQuery query)
	{
		query.setIssueTypeID(null);
		query.setIssueSeverityTypeID(null);
		query.setIssuePriorityID(null);
		query.setIssueResolutionID(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void updateUI(QueryEvent event)
	{
		if (event.getChangedQuery() == null)
		{
			issuePriorityCombo.setSelection(ISSUE_PRIORITY_ALL);
			issueResolutionCombo.setSelection(ISSUE_RESOLUTION_ALL);
			issueSeverityCombo.setSelection(ISSUE_SEVERITY_TYPE_ALL);
			issueTypeCombo.setSelection(ISSUE_TYPE_ALL);
		}
		else
		{ // there is a new Query -> the changedFieldList is not null!
			for (FieldChangeCarrier changedField : event.getChangedFields())
			{
				if (IssueQuery.PROPERTY_ISSUE_PRIORITY_ID.equals(changedField.getPropertyName()))
				{
					IssuePriorityID tmpPriorityID = (IssuePriorityID) changedField.getNewValue();
					if (! Util.equals(JDOHelper.getObjectId(selectedIssuePriority), tmpPriorityID) )
					{
						if (tmpPriorityID == null)
						{
							issuePriorityCombo.setSelection(ISSUE_PRIORITY_ALL);
						}
						else
						{
							selectedIssuePriority = IssuePriorityDAO.sharedInstance().getIssuePriority(
								tmpPriorityID, new String[] { IssuePriority.FETCH_GROUP_NAME }, 
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
							);
							issuePriorityCombo.setSelection(selectedIssuePriority);
						}
					}
				}
				
				if (IssueQuery.PROPERTY_ISSUE_RESOLUTION_ID.equals(changedField.getPropertyName()))
				{
					IssueResolutionID tmpResolutionID = (IssueResolutionID) changedField.getNewValue();
					if (! Util.equals(JDOHelper.getObjectId(selectedIssueResolution), tmpResolutionID) )
					{
						if (tmpResolutionID == null)
						{
							issueResolutionCombo.setSelection(ISSUE_RESOLUTION_ALL);
						}
						else
						{
							selectedIssueResolution = IssueResolutionDAO.sharedInstance().getIssueResolution(
									tmpResolutionID, 
									new String[] { IssueResolution.FETCH_GROUP_NAME}, 
									NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
								);
							issueResolutionCombo.setSelection(selectedIssueResolution);
						}
					}
				}
				
				if (IssueQuery.PROPERTY_ISSUE_SEVERITY_TYPE_ID.equals(changedField.getPropertyName()))
				{
					IssueSeverityTypeID tmpSeverityID = (IssueSeverityTypeID) changedField.getNewValue();
					if (! Util.equals(JDOHelper.getObjectId(selectedIssueSeverityType), tmpSeverityID) )
					{
						if (tmpSeverityID == null)
						{
							issueSeverityCombo.setSelection(ISSUE_SEVERITY_TYPE_ALL);
						}
						else
						{
							selectedIssueSeverityType = IssueSeverityTypeDAO.sharedInstance().getIssueSeverityType(
									tmpSeverityID, 
									new String[] { IssueSeverityType.FETCH_GROUP_NAME }, 
									NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
								);
							issueSeverityCombo.setSelection(selectedIssueSeverityType);
						}
					}
				}
				
				if (IssueQuery.PROPERTY_ISSUE_TYPE_ID.equals(changedField.getPropertyName()))
				{
					IssueTypeID tmpTypeID = (IssueTypeID) changedField.getNewValue();
					if (! Util.equals(JDOHelper.getObjectId(selectedIssueType), tmpTypeID) )
					{
						if (tmpTypeID == null)
						{
							issueTypeCombo.setSelection(ISSUE_TYPE_ALL);
						}
						else
						{
							selectedIssueType = IssueTypeDAO.sharedInstance().getIssueType(
									tmpTypeID, 
									new String[] { IssueType.FETCH_GROUP_NAME }, 
									NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
								);
							issueTypeCombo.setSelection(selectedIssueType);
						}
					}
				}				
			} // for (FieldChangeCarrier changedField : event.getChangedFields())
		} // changedQuery != null		
	}
	
	private static IssueType ISSUE_TYPE_ALL = new IssueType(Organisation.DEV_ORGANISATION_ID, "Issue_Type_All");
	private static IssueSeverityType ISSUE_SEVERITY_TYPE_ALL = new IssueSeverityType(Organisation.DEV_ORGANISATION_ID, "Issue_Severity_Type_All");
	private static IssuePriority ISSUE_PRIORITY_ALL = new IssuePriority(Organisation.DEV_ORGANISATION_ID, "Issue_Priority_All");
	private static IssueResolution ISSUE_RESOLUTION_ALL = new IssueResolution(Organisation.DEV_ORGANISATION_ID, "Issue_Resolution_All");

	private void prepareIssueProperties(){
		ISSUE_TYPE_ALL.getName().setText(Locale.ENGLISH.getLanguage(), "All");
		ISSUE_SEVERITY_TYPE_ALL.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "All");
		ISSUE_PRIORITY_ALL.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "All");
		ISSUE_RESOLUTION_ALL.getName().setText(Locale.ENGLISH.getLanguage(), "All");
	}


	private static final String[] FETCH_GROUPS_ISSUE_TYPE = { IssueType.FETCH_GROUP_NAME,
		IssueType.FETCH_GROUP_ISSUE_PRIORITIES,
		IssueType.FETCH_GROUP_ISSUE_RESOLUTIONS,
		IssueType.FETCH_GROUP_ISSUE_SEVERITY_TYPES,
		IssueSeverityType.FETCH_GROUP_NAME, 
		IssuePriority.FETCH_GROUP_NAME, 
		IssueResolution.FETCH_GROUP_NAME, FetchPlan.DEFAULT };
	private IssueLabelProvider labelProvider = new IssueLabelProvider();
	// TODO: Why does this flag exist? It is never read, but only set. If it isn't used anymore then remove this please.
//	private boolean loadJobRunning = false;

	private void loadProperties(){
		Job loadJob = new Job("Loading Issue Properties....") {
			@Override
			protected IStatus run(final ProgressMonitor monitor) {				
				synchronized (mutex) {
//					loadJobRunning = true;
					logger.debug("Load Job running....");
				}
				try {
					try {
						final List<IssueType> issueTypeList = new ArrayList<IssueType>(IssueTypeDAO.sharedInstance().getAllIssueTypes(FETCH_GROUPS_ISSUE_TYPE, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
						final List<IssuePriority> issuePriorityList = new ArrayList<IssuePriority>();
						final List<IssueSeverityType> issueSeverityTypeList = new ArrayList<IssueSeverityType>();

						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								issueTypeCombo.removeAll();
								issueTypeCombo.addElement(ISSUE_TYPE_ALL);
								for (Iterator<IssueType> it = issueTypeList.iterator(); it.hasNext(); ) {
									IssueType issueType = it.next();
									issueTypeCombo.addElement(issueType);
									for (IssuePriority p : issueType.getIssuePriorities())
										issuePriorityList.add(p);
									for (IssueSeverityType s : issueType.getIssueSeverityTypes())
										issueSeverityTypeList.add(s);
								}
								selectedIssueType = ISSUE_TYPE_ALL;
//								getQuery().setIssueTypeID(null); // null <=> ISSUE_TYPE_ALL

								/**************************************************/
								ISSUE_TYPE_ALL.getIssuePriorities().addAll(issuePriorityList);
								ISSUE_TYPE_ALL.getIssueSeverityTypes().addAll(issueSeverityTypeList);
								/**************************************************/

								issueSeverityCombo.removeAll();
								issueSeverityCombo.addElement(ISSUE_SEVERITY_TYPE_ALL);
								for (IssueSeverityType is : selectedIssueType.getIssueSeverityTypes()) {
									if (!issueSeverityCombo.contains(is))
										issueSeverityCombo.addElement(is);
								}
								selectedIssueSeverityType = ISSUE_SEVERITY_TYPE_ALL;
//								getQuery().setIssueSeverityTypeID(null); // null <=> ISSUE_SEVERITY_TYPE_ALL

								issuePriorityCombo.removeAll();
								issuePriorityCombo.addElement(ISSUE_PRIORITY_ALL);
								for (IssuePriority ip : selectedIssueType.getIssuePriorities()) {
									if (!issuePriorityCombo.contains(ip))
										issuePriorityCombo.addElement(ip);
								}
								selectedIssuePriority = issuePriorityCombo.getSelectedElement();

								issueResolutionCombo.removeAll();
								issueResolutionCombo.addElement(ISSUE_RESOLUTION_ALL);
								for (IssueResolution ir : selectedIssueType.getIssueResolutions()) {
									if (!issueResolutionCombo.contains(ir))
										issueResolutionCombo.addElement(ir);
								}
								selectedIssueResolution = ISSUE_RESOLUTION_ALL;
//								getQuery().setIssueResolutionID(null); // null <=> ISSUE_RESOLUTION_ALL
							}
						});
					}catch (Exception e1) {
						ExceptionHandlerRegistry.asyncHandleException(e1);
						throw new RuntimeException(e1);
					}

					return Status.OK_STATUS;
				} finally {
					synchronized (mutex) {
//						if (storedIssueQueryRunnable != null) {
//							logger.debug("Running storedIssueQueryRunnable from load Job.");
//							storedIssueQueryRunnable.run(monitor);
//							storedIssueQueryRunnable = null;
//						}
//						loadJobRunning = false;
						logger.debug("Load Job finished.");
					}
				}
			} 
		};
		
		loadJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		loadJob.schedule();
	} 

}
