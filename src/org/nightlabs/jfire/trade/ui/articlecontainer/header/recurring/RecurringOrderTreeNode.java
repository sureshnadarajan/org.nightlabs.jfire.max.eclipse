package org.nightlabs.jfire.trade.ui.articlecontainer.header.recurring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.trade.ArticleContainerUtil;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.dao.OfferDAO;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOrder;
import org.nightlabs.jfire.trade.recurring.dao.RecurringOrderDAO;
import org.nightlabs.jfire.trade.ui.articlecontainer.header.HeaderTreeNode;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Fitas Amine - fitas at nightlabs dot de
 */
public class RecurringOrderTreeNode extends HeaderTreeNode.ArticleContainerNode {

	private RecurringOrder recurringOrder;	
	
	public static final String[] FETCH_GROUPS_ORDER = new String[] {
		FetchPlan.DEFAULT,
		Order.FETCH_GROUP_OFFERS
	};
	public static final String[] FETCH_GROUPS_OFFER = new String[] {
		FetchPlan.DEFAULT,
		Offer.FETCH_GROUP_ORDER
	};
	private Set<OfferID> offerIDsLoaded = new HashSet<OfferID>();
	
	
	public RecurringOrderTreeNode(HeaderTreeNode parent, byte position ,RecurringOrder recurringOrder) {
		super(parent, position);
		this.recurringOrder = recurringOrder;
		init();

	}
	

	@Override
	protected List<HeaderTreeNode> createChildNodes(List<Object> childData) {
		// TODO Auto-generated method stub
		ArrayList<HeaderTreeNode> res = new ArrayList<HeaderTreeNode>();
		for (Iterator<Object> it = childData.iterator(); it.hasNext(); ) {
			Offer offer = (Offer) it.next();
			OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
			synchronized (offerIDsLoaded) {
				if (!offerIDsLoaded.contains(offerID)) {
					offerIDsLoaded.add(offerID);
					res.add(new RecurringOfferTreeNode(this, POSITION_LAST_CHILD, (RecurringOffer) offer));
				}
			}
		}
		return res;
	}

	
	@Override
	public Image getColumnImage(int columnIndex)
	{
		switch (columnIndex) {
			case 0:
				return getHeaderTreeComposite().getImageOrderTreeNode();
			default:
				return null;
		}
	}

	@Override
	public String getColumnText(int columnIndex)
	{
		switch (columnIndex) {
			case 0: return ArticleContainerUtil.getArticleContainerID(recurringOrder);
			default:
				return null;
		}
	}
	
	@Override
	protected List<Object> loadChildData(ProgressMonitor monitor) {
		try {
			OrderID orderID = (OrderID) JDOHelper.getObjectId(recurringOrder);
			RecurringOrder o = RecurringOrderDAO.sharedInstance().getRecurringOrder(orderID, FETCH_GROUPS_ORDER,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);

			ArrayList<Offer> res = new ArrayList<Offer>(o.getOffers());

			Collections.sort(res, new Comparator<Offer>() {
				public int compare(Offer o0, Offer o1)
				{
					long id0 = o0.getOfferID();
					long id1 = o1.getOfferID();

					if (id0 == id1)
						return 0;

					if (id0 > id1)
						return 1;
					else
						return -1;
				}
			});

			return CollectionUtil.castList(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	@Override
	public Collection<DirtyObjectID> onNewElementsCreated(Collection<DirtyObjectID> dirtyObjectIDs, ProgressMonitor monitor)
	{
		if (children != null) {
			Map<Object, DirtyObjectID> objectID2DirtyObjectIDMap = new HashMap<Object, DirtyObjectID>(dirtyObjectIDs.size());
	
			Set<OfferID> offerIDsToLoad = new HashSet<OfferID>();
			for (Iterator<DirtyObjectID> itD = dirtyObjectIDs.iterator(); itD.hasNext(); ) {
				DirtyObjectID dirtyObjectID = itD.next();
				objectID2DirtyObjectIDMap.put(dirtyObjectID.getObjectID(), dirtyObjectID);
				
				if (dirtyObjectID.getObjectID() instanceof OfferID) {
					OfferID offerID = (OfferID) dirtyObjectID.getObjectID();
					itD.remove();
					synchronized (offerIDsLoaded) {
						if (!offerIDsLoaded.contains(offerID))
							offerIDsToLoad.add(offerID);
					}
				
				
				}
			}
	
			if (!offerIDsToLoad.isEmpty()) {
				final List<Offer> offers = new OfferDAO().getOffers(offerIDsToLoad, FETCH_GROUPS_OFFER,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	
				OrderID orderID = (OrderID) JDOHelper.getObjectId(getArticleContainer());
	
				for (Iterator<Offer> it = offers.iterator(); it.hasNext(); ) {
					Offer offer = it.next();
					if (!orderID.equals(JDOHelper.getObjectId(offer.getOrder()))) {
						it.remove();
						dirtyObjectIDs.add(objectID2DirtyObjectIDMap.get(JDOHelper.getObjectId(offer)));
					}
				}
	
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						if (children == null)
							return;
	
						for (Offer offer : offers) {
							OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
	
							synchronized (offerIDsLoaded) {
								if (!offerIDsLoaded.contains(offerID)) {
									offerIDsLoaded.add(offerID);
									new RecurringOfferTreeNode(RecurringOrderTreeNode.this, POSITION_FIRST_CHILD, (RecurringOffer) offer);
								}
							}
						}
					}
				});
			}
		} // if (children != null) {
		return super.onNewElementsCreated(dirtyObjectIDs, monitor);
	}

	@Override
	public RecurringOrder getArticleContainer() {
		return recurringOrder;
	}
}
