package org.nightlabs.jfire.issuetracking.ui.issue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.dao.IssueSeverityTypeDAO;
import org.nightlabs.progress.ProgressMonitor;

public class CreateIssueComposite extends XComposite{
	
	private List<IssueSeverityType> issueSeverityTypes = new ArrayList<IssueSeverityType>();
	
	public CreateIssueComposite(Composite parent, int style) {
		super(parent, style);
		createComposite(this);
	}
	
	/**
	 * Create the content for this composite.
	 * @param parent The parent composite
	 */
	protected void createComposite(Composite parent) 
	{
		setLayout(new GridLayout(2, false));
		
		int textStyle = SWT.READ_ONLY | SWT.BORDER;
		
		Label severityLbl = new Label(this, SWT.NONE);
		severityLbl.setText("Severity");
		
		ILabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) 
			{
				if (element instanceof IssueSeverityType) {
					IssueSeverityType issueSeverityType = (IssueSeverityType) element;
					return issueSeverityType.getIssueSeverityTypeText().getText();
				}
				return super.getText(element);
			}		
		};
		
		final XComboComposite<IssueSeverityType> severityCombo = new XComboComposite<IssueSeverityType>(this, SWT.NONE, labelProvider);
		
		Job loadJob = new Job("Loading Issue Severity Types....") {
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				try {
					List<IssueSeverityType> issueSeverityTypes = IssueSeverityTypeDAO.sharedInstance().getIssueSeverityTypes(monitor);
				} catch (Exception e) {
					ExceptionHandlerRegistry.asyncHandleException(e);
					throw new RuntimeException(e);
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						severityCombo.removeAll();
						for (Iterator it = issueSeverityTypes.iterator(); it.hasNext(); ) {
							IssueSeverityType issueSeverityType = (IssueSeverityType) it.next();
							severityCombo.addElement(issueSeverityType);
						}
					}
				});

				return Status.OK_STATUS;
			} 
			
		};
		loadJob.schedule();
		
//		 Category  	
//		 Reproducibility 	
//		 Severity 	
//		 Priority 	
	}
}
