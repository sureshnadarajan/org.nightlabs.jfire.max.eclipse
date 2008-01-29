/**
 * 
 */
package org.nightlabs.jfire.issuetracking.trade.ui.issuelink;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issuetracking.ui.issuelink.AbstractIssueLinkAdder;
import org.nightlabs.jfire.trade.ui.overview.offer.OfferEntryFactory;
import org.nightlabs.jfire.trade.ui.overview.offer.OfferEntryViewer;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 */
public class IssueOfferLinkAdder extends AbstractIssueLinkAdder {

	private OfferEntryViewer oViewer;
	@Override
	protected Composite doCreateComposite(Composite parent) {
		oViewer = new OfferEntryViewer(new OfferEntryFactory().createEntry()) {
			@Override
			protected void addResultTableListeners(AbstractTableComposite tableComposite) {
				tableComposite.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent evt) {
						notifyIssueLinkDoubleClickListeners();
					}
				});
			}
		};
		oViewer.createComposite(parent);
		return oViewer.getComposite();
	}

	public Set<ObjectID> getIssueLinkObjectIds() {
		Set<ObjectID> result = new HashSet<ObjectID>();
		Collection<Object> elements = oViewer.getListComposite().getSelectedElements();
		for(Object o : elements) {
			result.add((ObjectID)JDOHelper.getObjectId(o));
		}
		return result;
	}

	public boolean isComplete() {
		if(getIssueLinkObjectIds() == null || getIssueLinkObjectIds().size() <= 0) {
			return false;
		}
		return true; 
	}
}
