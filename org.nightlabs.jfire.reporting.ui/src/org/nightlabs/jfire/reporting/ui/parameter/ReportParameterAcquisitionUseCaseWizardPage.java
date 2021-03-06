/**
 * 
 */
package org.nightlabs.jfire.reporting.ui.parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.resource.SharedImages.ImageDimension;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.dao.ReportParameterAcquisitionSetupDAO;
import org.nightlabs.jfire.reporting.ui.ReportingPlugin;
import org.nightlabs.jfire.reporting.ui.resource.Messages;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportParameterAcquisitionUseCaseWizardPage extends WizardHopPage {

	private ReportRegistryItemID reportLayoutID;
	private ReportParameterAcquisitionSetup setup;
	
	private ReportParameterAcquisitionUseCaseTable useCaseTable;
	private boolean isScheduledReportConfiguration;
	
	/**
	 * @param pageName
	 */
	public ReportParameterAcquisitionUseCaseWizardPage(String pageName, ReportRegistryItemID reportLayoutID, boolean isScheduledReport) {
		super(pageName);
		setTitle(Messages.getString("org.nightlabs.jfire.reporting.ui.parameter.eportParameterAcquisitionUseCaseWizardPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.reporting.ui.parameter.eportParameterAcquisitionUseCaseWizardPage.description")); //$NON-NLS-1$
		init(reportLayoutID, isScheduledReport);
	}

	/**
	 * @param pageName
	 * @param title
	 */
	public ReportParameterAcquisitionUseCaseWizardPage(String pageName, String title, ReportRegistryItemID reportLayoutID, boolean isScheduledReport) {
		super(pageName, title);
		init(reportLayoutID, isScheduledReport);
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public ReportParameterAcquisitionUseCaseWizardPage(String pageName, String title,
			ImageDescriptor titleImage, ReportRegistryItemID reportLayoutID, boolean isScheduledReport) {
		super(pageName, title, titleImage);
		init(reportLayoutID, isScheduledReport);
	}
	
	protected void init(ReportRegistryItemID reportLayoutID, boolean isScheduledReport) {
		setImageDescriptor(SharedImages.getSharedImageDescriptor(ReportingPlugin.getDefault(), ReportParameterAcquisitionUseCaseWizardPage.class, null, ImageDimension._75x70));
		this.reportLayoutID = reportLayoutID;
		this.isScheduledReportConfiguration = isScheduledReport;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		try {
			setup = ReportParameterAcquisitionSetupDAO.sharedInstance().getSetupForReportLayout(
					reportLayoutID, ReportParameterAcquisitionSetupDAO.DEFAULT_FETCH_GROUPS,
					new NullProgressMonitor()
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		useCaseTable = new ReportParameterAcquisitionUseCaseTable(parent, SWT.NONE);
		useCaseTable.setReportParameterAcquisitionSetup(setup);
		if (setup.getDefaultSetup() != null) {
			List<ReportParameterAcquisitionUseCase> sel = new ArrayList<ReportParameterAcquisitionUseCase>(1);
			for (Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry: setup.getValueAcquisitionSetups().entrySet()) {
				if (entry.getKey().equals(setup.getDefaultSetup())) {
					sel.add(entry.getKey());
					break;
				}
			}
			useCaseTable.setSelection(sel);
		}
		useCaseTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (useCaseTable.getFirstSelectedElement() == null)
					return;
				ReportParameterWizardHop.populateValueProviderSetupPages(
						setup.getValueAcquisitionSetups().get(useCaseTable.getFirstSelectedElement()),
						getReportParameterWizardHop(),
						isScheduledReportConfiguration,
						false
					);
				getContainer().updateButtons();
			}
		});
		
		return useCaseTable;
	}
	
	protected ReportParameterWizardHop getReportParameterWizardHop() {
		return (ReportParameterWizardHop) getWizardHop();
	}
}
