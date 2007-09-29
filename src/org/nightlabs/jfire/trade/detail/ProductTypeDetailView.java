package org.nightlabs.jfire.trade.detail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.TradePlugin;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

/**
 * This View shows detailed informations of selected {@link ProductType}s.
 * The shown composites come from the {@link ProductTypeDetailViewRegistry} where 
 * {@link IProductTypeDetailView} can be registered for ProductType Classes. 
 * This is done via the Extension-Point org.nightlabs.jfire.trade.productTypeDetailView.
 * The View listenes of selections coming from the {@link SelectionManager} with the 
 * zone {@link TradePlugin#ZONE_SALE} and the class {@link ProductType}
 * 
 * 						
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ProductTypeDetailView 
//extends ViewPart 
extends LSDViewPart
{
	public static final String ID_VIEW = ProductTypeDetailView.class.getName();

	public ProductTypeDetailView() {
		super();
	}

	@Implement
	public void createPartContents(Composite parent) 
	{
		SelectionManager.sharedInstance().addNotificationListener(
				TradePlugin.ZONE_SALE, ProductType.class, productTypeSelectionListener);
		productTypeDetailComposite = new ProductTypeDetailViewComposite(parent, SWT.NONE);
		productTypeDetailComposite.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				SelectionManager.sharedInstance().removeNotificationListener(TradePlugin.ZONE_SALE, 
						ProductType.class, productTypeSelectionListener);
			}
			
		});
	}

	private ProductTypeDetailViewComposite productTypeDetailComposite;

	@Override
	public void setFocus() {

	}

	private NotificationListener productTypeSelectionListener = new NotificationAdapterCallerThread(){	
		public void notify(NotificationEvent notificationEvent) {
			Object firstSelection = notificationEvent.getFirstSubject();
			if (firstSelection instanceof ProductTypeID) {
				ProductTypeID productTypeID = (ProductTypeID) firstSelection;
				if (productTypeDetailComposite != null)
					productTypeDetailComposite.showProductTypeDetail(productTypeID);					
			}
		}	
	};

}
