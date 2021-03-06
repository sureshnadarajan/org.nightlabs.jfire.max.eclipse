package org.nightlabs.jfire.reporting.trade.ui.articlecontainer.deliverynote;

import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
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
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.reporting.trade.ReportingTradeConstants;
import org.nightlabs.jfire.reporting.ui.parameter.AbstractValueProviderGUI;
import org.nightlabs.jfire.reporting.ui.parameter.IValueProviderGUI;
import org.nightlabs.jfire.reporting.ui.parameter.IValueProviderGUIFactory;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.DeliveryNoteLocal;
import org.nightlabs.jfire.store.dao.DeliveryNoteDAO;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.query.DeliveryNoteQuery;
import org.nightlabs.jfire.trade.ui.overview.deliverynote.DeliveryNoteListComposite;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class ValueProviderGUIDeliveryNoteByCustomer
extends AbstractValueProviderGUI<DeliveryNoteID>
{
	public static final String[] FETCH_GROUPS_DELIVERY_NOTES = new String[] {
		FetchPlan.DEFAULT,
		DeliveryNote.FETCH_GROUP_THIS_DELIVERY_NOTE,
		State.FETCH_GROUP_STATE_DEFINITION,
		StateDefinition.FETCH_GROUP_NAME,
		LegalEntity.FETCH_GROUP_PERSON,
		DeliveryNoteLocal.FETCH_GROUP_THIS_DELIVERY_NOTE_LOCAL
	};

	public static class Factory implements IValueProviderGUIFactory
	{
		public IValueProviderGUI<DeliveryNoteID> createValueProviderGUI(
				ValueProviderConfig valueProviderConfig, boolean isScheduledReportParameterConfig)
		{
			return new ValueProviderGUIDeliveryNoteByCustomer(valueProviderConfig);
		}

		public ValueProviderID getValueProviderID() {
			return ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_DELIVERY_NOTE_BY_CUSTOMER;
		}

		public void setInitializationData(IConfigurationElement config,
				String propertyName, Object data) throws CoreException {

		}

	}

	private DeliveryNoteListComposite deliveryNoteListComposite = null;

	public ValueProviderGUIDeliveryNoteByCustomer(ValueProviderConfig valueProviderConfig) {
		super(valueProviderConfig);
	}

	public Control createGUI(Composite wrapper) {
		deliveryNoteListComposite = new DeliveryNoteListComposite(wrapper, SWT.NONE);
		deliveryNoteListComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				notifyOutputChanged();
			}
		});
		return deliveryNoteListComposite;
	}

	public DeliveryNoteID getOutputValue() {
		if (deliveryNoteListComposite.getSelectedElements().size() >= 1)
			return (DeliveryNoteID) JDOHelper.getObjectId(deliveryNoteListComposite.getSelectedElements().iterator().next());
		return null;
	}

	public boolean isAcquisitionComplete() {
		return getOutputValue() != null || getValueProviderConfig().isAllowNullOutputValue();
	}

	public void setInputParameterValue(String parameterID, final Object value) {
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.trade.ui.overview.deliverynote.report.ValueProviderGUIDeliveryNoteByCustomer.loadDeliveryNotesJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				DeliveryNoteQuery query = new DeliveryNoteQuery();
				query.setCustomerID((AnchorID) value);
				QueryCollection<DeliveryNoteQuery> qs =
					new QueryCollection<DeliveryNoteQuery>(DeliveryNote.class);

				qs.add(query);
				final Collection<DeliveryNote> deliveryNotes = DeliveryNoteDAO.sharedInstance()
					.getDeliveryNotesByQueries(
						qs, FETCH_GROUPS_DELIVERY_NOTES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						deliveryNoteListComposite.setInput(deliveryNotes);
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}

}
