package org.nightlabs.jfire.issuetracking.ui.issue.editor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.editor.FormPage;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueLabelProvider;
import org.nightlabs.jfire.issuetracking.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssuePropertySection extends AbstractIssueEditorGeneralSection {

	private Label priorityLabel;
	private Label severityLabel;
	private Label resolutionLabel;

	private XComboComposite<IssuePriority> issuePriorityCombo;
	private XComboComposite<IssueSeverityType> issueSeverityTypeCombo;
	private XComboComposite<IssueResolution> issueResolutionCombo;

	/**
	 * @param section
	 * @param managedForm
	 */
	public IssuePropertySection(FormPage page, Composite parent, final IssueEditorPageController controller) {
		super(page, parent, controller);
		
		getClient().getGridLayout().numColumns = 6;
		getClient().getGridLayout().makeColumnsEqualWidth = false;
		
		getClient().getGridData().grabExcessHorizontalSpace = false;
		getClient().getGridData().grabExcessVerticalSpace = false;
		getClient().getGridData().horizontalAlignment = SWT.BEGINNING;
		getClient().getGridData().verticalAlignment = SWT.BEGINNING;
		
		getSection().setText(Messages.getString("org.nightlabs.jfire.issuetracking.ui.issue.editor.IssuePropertySection.section.text")); //$NON-NLS-1$

		priorityLabel = new Label(getClient(), SWT.WRAP);
		priorityLabel.setText(Messages.getString("org.nightlabs.jfire.issuetracking.ui.issue.editor.IssuePropertySection.label.priority.text")); //$NON-NLS-1$

		issuePriorityCombo = new XComboComposite<IssuePriority>(getClient(), SWT.NONE | SWT.READ_ONLY);
		issuePriorityCombo.setLabelProvider(new IssueLabelProvider());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		issuePriorityCombo.setLayoutData(gd);
		issuePriorityCombo.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent e) {
				controller.getIssue().setIssuePriority(issuePriorityCombo.getSelectedElement());
				markDirty();
			}
		});
		
		severityLabel = new Label(getClient(), SWT.WRAP);
		severityLabel.setText(Messages.getString("org.nightlabs.jfire.issuetracking.ui.issue.editor.IssuePropertySection.label.severity.text")); //$NON-NLS-1$

		issueSeverityTypeCombo = new XComboComposite<IssueSeverityType>(getClient(), SWT.NONE | SWT.READ_ONLY);
		issueSeverityTypeCombo.setLabelProvider(new IssueLabelProvider());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		issueSeverityTypeCombo.setLayoutData(gd);
		issueSeverityTypeCombo.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent e) {
				controller.getIssue().setIssueSeverityType(issueSeverityTypeCombo.getSelectedElement());
				markDirty();
			}
		});

		resolutionLabel = new Label(getClient(), SWT.WRAP);
		resolutionLabel.setText(Messages.getString("org.nightlabs.jfire.issuetracking.ui.issue.editor.IssuePropertySection.label.resolution.text")); //$NON-NLS-1$

		issueResolutionCombo = new XComboComposite<IssueResolution>(getClient(), SWT.NONE | SWT.READ_ONLY);
		issueResolutionCombo.setLabelProvider(new IssueLabelProvider());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		issueResolutionCombo.setLayoutData(gd);
		issueResolutionCombo.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent e) {
				controller.getIssue().setIssueResolution(issueResolutionCombo.getSelectedElement());
				markDirty();
			}
		});
	}

	@Override
	protected void doSetIssue(final Issue issue) {
		loadProperties(issue);

//		Display.getDefault().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				issuePriorityCombo.selectElement(issue.getIssuePriority());
//				issueSeverityTypeCombo.selectElement(issue.getIssueSeverityType());
//				issueResolutionCombo.selectElement(issue.getIssueResolution());
//			}
//		});
		
	}

	private void loadProperties(final Issue issue){
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.issuetracking.ui.issue.editor.IssuePropertySection.job.loadingIssueProp.text")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final ProgressMonitor monitor) {
				try {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							issueResolutionCombo.removeAll();
							for (IssueResolution ir : issue.getIssueType().getIssueResolutions()) {
								issueResolutionCombo.addElement(ir);
							}

							issuePriorityCombo.removeAll();
							for (IssuePriority ip : issue.getIssueType().getIssuePriorities()) {
								issuePriorityCombo.addElement(ip);
							}

							issueSeverityTypeCombo.removeAll();
							for (IssueSeverityType it : issue.getIssueType().getIssueSeverityTypes()) {
								issueSeverityTypeCombo.addElement(it);
							}
							
							issuePriorityCombo.selectElement(issue.getIssuePriority());
							issueSeverityTypeCombo.selectElement(issue.getIssueSeverityType());
							issueResolutionCombo.selectElement(issue.getIssueResolution());
						}
					});
				}catch (Exception e1) {
					ExceptionHandlerRegistry.asyncHandleException(e1);
					throw new RuntimeException(e1);
				}

				return Status.OK_STATUS;
			} 
		};
		loadJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		loadJob.schedule();
	}

	public IssuePriority getSelectedPriority(){
		return issuePriorityCombo.getSelectedElement();
	}
	
	public IssueSeverityType getSelectedSeverityType(){
		return issueSeverityTypeCombo.getSelectedElement();
	}
	
	public IssueResolution getSelectedResolution(){
		return issueResolutionCombo.getSelectedElement();
	}
}
