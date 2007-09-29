/**
 * 
 */
package org.nightlabs.jfire.trade.overview.invoice.report;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceLocal;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.query.InvoiceQuery;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.reporting.parameter.AbstractValueProviderGUI;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.TradePlugin;
import org.nightlabs.jfire.trade.articlecontainer.InvoiceDAO;
import org.nightlabs.jfire.trade.overview.invoice.InvoiceListComposite;
import org.nightlabs.jfire.trade.resource.Messages;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderGUIInvoiceByCustomer 
extends AbstractValueProviderGUI 
{
	public static final String[] FETCH_GROUPS_INVOICES = new String[] {
		FetchPlan.DEFAULT,
		Invoice.FETCH_GROUP_THIS_INVOICE,
		State.FETCH_GROUP_STATE_DEFINITION,
		StateDefinition.FETCH_GROUP_NAME,
		LegalEntity.FETCH_GROUP_PERSON,
		InvoiceLocal.FETCH_GROUP_THIS_INVOICE_LOCAL
	};
	
	private InvoiceListComposite invoiceListComposite;
	
	public ValueProviderGUIInvoiceByCustomer(ValueProviderConfig valueProviderConfig) {
		super(valueProviderConfig);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.parameter.IValueProviderGUI#createGUI(org.eclipse.swt.widgets.Composite)
	 */
	public Control createGUI(Composite wrapper) {
		invoiceListComposite = new InvoiceListComposite(wrapper, SWT.NONE);
		invoiceListComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				notifyOutputChanged();
			}
		});
		return invoiceListComposite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.parameter.IValueProviderGUI#getOutputValue()
	 */
	public Object getOutputValue() {
		if (invoiceListComposite.getSelectedElements().size() >= 1)
			return JDOHelper.getObjectId(invoiceListComposite.getSelectedElements().iterator().next());
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.parameter.IValueProviderGUI#isAcquisitionComplete()
	 */
	public boolean isAcquisitionComplete() {
		return getOutputValue() != null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.parameter.IValueProviderGUI#setInputParameterValue(java.lang.String, java.lang.Object)
	 */
	public void setInputParameterValue(String parameterID, final Object value) {
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.trade.overview.invoice.report.ValueProviderGUIInvoiceByCustomer.loadInvoicesJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				InvoiceQuery query = new InvoiceQuery();
				query.setCustomerID((AnchorID) value);
				Collection<InvoiceQuery> qs = new HashSet<InvoiceQuery>();
				qs.add(query);
				Set<InvoiceID> invoiceIDs = null;
				try {
//					invoiceIDs = TradePlugin.getDefault().getTradeManager().getInvoiceIDs(qs);
					invoiceIDs = TradePlugin.getDefault().getAccountingManager().getInvoiceIDs(qs);					
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				final Collection<Invoice> invoices = (Collection<Invoice>) InvoiceDAO.sharedInstance().getInvoices(invoiceIDs, FETCH_GROUPS_INVOICES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						invoiceListComposite.setInput(invoices);
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}

}
