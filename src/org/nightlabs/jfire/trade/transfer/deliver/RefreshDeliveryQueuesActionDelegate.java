package org.nightlabs.jfire.trade.transfer.deliver;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class RefreshDeliveryQueuesActionDelegate implements IViewActionDelegate {
	
	private DeliveryQueueBrowsingView deliveryQueueBrowsingView;

	public void init(IViewPart view) {
		if (view instanceof DeliveryQueueBrowsingView) {
			deliveryQueueBrowsingView = (DeliveryQueueBrowsingView) view;			
		} else
			throw new IllegalArgumentException("Given IViewPart is no instance of DeliveryQueueBrowsingView."); //$NON-NLS-1$
	}

	public void run(IAction action) {
		deliveryQueueBrowsingView.refreshContent();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
