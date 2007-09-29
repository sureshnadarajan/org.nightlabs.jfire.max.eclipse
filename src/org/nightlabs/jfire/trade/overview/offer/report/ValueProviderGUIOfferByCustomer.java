/**
 * 
 */
package org.nightlabs.jfire.trade.overview.offer.report;

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
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.reporting.parameter.AbstractValueProviderGUI;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.TradePlugin;
import org.nightlabs.jfire.trade.articlecontainer.OfferDAO;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.overview.offer.OfferListComposite;
import org.nightlabs.jfire.trade.query.OfferQuery;
import org.nightlabs.jfire.trade.resource.Messages;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class ValueProviderGUIOfferByCustomer 
extends AbstractValueProviderGUI 
{
	public static final String[] FETCH_GROUPS_OFFERS = new String[] {
		FetchPlan.DEFAULT,
		Offer.FETCH_GROUP_THIS_OFFER,
		State.FETCH_GROUP_STATE_DEFINITION,
		StateDefinition.FETCH_GROUP_NAME,
		LegalEntity.FETCH_GROUP_PERSON, 
		OfferLocal.FETCH_GROUP_THIS_OFFER_LOCAL
	};
	
	private OfferListComposite offerListComposite = null;
	
	public ValueProviderGUIOfferByCustomer(ValueProviderConfig valueProviderConfig) {
		super(valueProviderConfig);
	}
	
	public Control createGUI(Composite wrapper) {
		offerListComposite = new OfferListComposite(wrapper, SWT.NONE);
		offerListComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				notifyOutputChanged();
			}
		});
		return offerListComposite;
	}

	public Object getOutputValue() {
		if (offerListComposite.getSelectedElements().size() >= 1)
			return JDOHelper.getObjectId(offerListComposite.getSelectedElements().iterator().next());
		return null;
	}

	public boolean isAcquisitionComplete() {
		return getOutputValue() != null;
	}

	public void setInputParameterValue(String parameterID, final Object value) {
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.trade.overview.offer.report.ValueProviderGUIOfferByCustomer.loadOffersJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				OfferQuery query = new OfferQuery();
				query.setCustomerID((AnchorID) value);
				Collection<OfferQuery> qs = new HashSet<OfferQuery>();
				qs.add(query);
				Set<OfferID> offerIDs = null;
				try {
					offerIDs = TradePlugin.getDefault().getTradeManager().getArticleContainerIDs(qs);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				final Collection<Offer> offers = (Collection<Offer>) OfferDAO.
					sharedInstance().getOffers(offerIDs, FETCH_GROUPS_OFFERS, 
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						offerListComposite.setInput(offers);
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}

}
