package org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.reverse;

import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.TimerText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ReverseProductComposite
extends XComposite
{
	private Button reverseAllButton;
	private Button reverseArticleButton;
//	private Button reversePaymentAndDeliveryButton;
//	private Button releaseArticlesButton;
	private boolean reverseAll;
	private boolean reverseArticle;
//	private boolean reversePaymentAndDelivery;
//	private boolean releaseArticles;
	private TimerText productIDText;
	private String text;
	private ListenerList listeners;

	/**
	 * @param parent
	 * @param style
	 */
	public ReverseProductComposite(Composite parent, int style)
	{
		super(parent, style);
		listeners = new ListenerList();

		XComposite wrapper = new XComposite(parent, SWT.NONE);

		Composite searchWrapper = new XComposite(wrapper, SWT.NONE);
		searchWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(searchWrapper, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.reverse.ReverseProductComposite.label")); //$NON-NLS-1$
		productIDText = new TimerText(searchWrapper, wrapper.getBorderStyle());
		productIDText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		productIDText.setFocus();
		productIDText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				text = productIDText.getText();
			}
		});

		Composite chooseComposite = new XComposite(wrapper, SWT.NONE);
		reverseAllButton = new Button(chooseComposite, SWT.RADIO);
		reverseAllButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.reverse.ReverseProductComposite.button.reverseAll.text")); //$NON-NLS-1$
		reverseAllButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				reverseAll = true;
				reverseArticle = false;
				fireReverseProductEvent();
			}
		});

		reverseArticleButton = new Button(chooseComposite, SWT.RADIO);
		reverseArticleButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.reverse.ReverseProductComposite.button.reverseOnlyArticle.text"));		 //$NON-NLS-1$
		reverseArticleButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				reverseAll = false;
				reverseArticle = true;
				fireReverseProductEvent();
			}
		});

//		reversePaymentAndDeliveryButton = new Button(chooseComposite, SWT.CHECK);
//		reversePaymentAndDeliveryButton.setText("Reverse Payment And Delivery");
//		reversePaymentAndDeliveryButton.addSelectionListener(new SelectionAdapter(){
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				reversePaymentAndDelivery = reversePaymentAndDeliveryButton.getSelection();
//				releaseArticlesButton.setEnabled(reversePaymentAndDelivery);
//				if (!reversePaymentAndDelivery) {
//					releaseArticlesButton.setSelection(false);
//				}
//				fireReverseProductEvent();
//			}
//		});
//
//		releaseArticlesButton = new Button(chooseComposite, SWT.CHECK);
//		releaseArticlesButton.setText("Release Articles");
//		releaseArticlesButton.addSelectionListener(new SelectionAdapter(){
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				releaseArticles = releaseArticlesButton.getSelection();
//				fireReverseProductEvent();
//			}
//		});

		reverseAllButton.setSelection(true);
		reverseArticleButton.setSelection(false);
//		reversePaymentAndDeliveryButton.setSelection(true);
		reverseAll = reverseAllButton.getSelection();
		reverseArticle = reverseArticleButton.getSelection();
//		releaseArticles = releaseArticlesButton.getSelection();
//		reversePaymentAndDelivery = reversePaymentAndDeliveryButton.getSelection();
	}

	public boolean isReverseAll() {
		return reverseAll;
	}

	public boolean isReverseArticle() {
		return reverseArticle;
	}

//	public boolean isReleaseArticles() {
//		return releaseArticles;
//	}
//
//	public boolean isReversePaymentAndDelivery() {
//		return reversePaymentAndDelivery;
//	}

	public ProductID getProductID(ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.reverse.ReverseProductComposite.job.name"), 100); //$NON-NLS-1$
		try {
			Collection<IProductIDParser> parsers = ProductIDParserRegistry.sharedInstance().getProductIDParser();
			for (IProductIDParser parser : parsers) {
				ProductID productID = parser.getProductID(text, new SubProgressMonitor(monitor, 100 / parsers.size()));
				if (productID != null) {
					return productID;
				}
			}
			return null;
		} finally {
			monitor.done();
		}
	}

	public TimerText getProductIDText() {
		return productIDText;
	}

	public void addReverseProductListener(IReverseProductListener listener) {
		listeners.add(listener);
	}

	public void removeReverseProductListener(IReverseProductListener listener) {
		listeners.remove(listener);
	}

	private void fireReverseProductEvent()
	{
		ReverseProductEvent event = new ReverseProductEvent(reverseAll, reverseArticle,
//				reversePaymentAndDelivery, releaseArticles);
				false, false);
		for (Object o : listeners.getListeners()) {
			((IReverseProductListener) o).reverseProductChanged(event);
		}
	}
}
