/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.dao.CurrencyDAO;
import org.nightlabs.jfire.trade.admin.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CurrencySelectionPage extends DynamicPathWizardPage
{
	private final List<Currency> currencies = new ArrayList<Currency>();
	private org.eclipse.swt.widgets.List currencyList;
	private Currency selectedCurrency = null;
	private Button createNewCurrencyRadio;
	private Button chooseExistingCurrencyRadio;

	public CurrencySelectionPage()
	{
		super(CurrencySelectionPage.class.getName(), Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency.CurrencySelectionPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency.CurrencySelectionPage.description")); //$NON-NLS-1$
	}

	/**
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(final Composite parent)
	{
		XComposite page = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		createNewCurrencyRadio = new Button(page, SWT.RADIO);
		createNewCurrencyRadio.setText(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency.CurrencySelectionPage.createNewCurrencyRadio.text"));
		createNewCurrencyRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				((AddCurrencyWizard) getWizard()).setCreateNewCurrencyEnabled(createNewCurrencyRadio.getSelection());
			}
		});

		chooseExistingCurrencyRadio = new Button(page,SWT.RADIO);
		chooseExistingCurrencyRadio.setText(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency.CurrencySelectionPage.chooseExistingCurrencyRadio.text"));
		chooseExistingCurrencyRadio.setSelection(true);

		currencyList = new org.eclipse.swt.widgets.List(page, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		currencyList.setLayoutData(new GridData(GridData.FILL_BOTH));
		currencyList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				int selIdx = currencyList.getSelectionIndex();
				if (selIdx < 0) {
					selectedCurrency = null;
				} else if (selIdx < currencies.size()) {
					selectedCurrency = currencies.get(selIdx);
					createNewCurrencyRadio.setSelection(false);
					chooseExistingCurrencyRadio.setSelection(true);
				}
				((DynamicPathWizard)getWizard()).removeAllDynamicWizardPages();
				((DynamicPathWizard)getWizard()).updateDialog();
			}
		});

		currencyList.add(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency.CurrencySelectionPage.pseudoEntry_loading")); //$NON-NLS-1$
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.addcurrency.CurrencySelectionPage.loadCurrenciesJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final ProgressMonitor monitor) {
				try {
					currencies.clear();
					currencies.addAll(CurrencyDAO.sharedInstance().getCurrencies(monitor));
				} catch (Exception e) {
					ExceptionHandlerRegistry.asyncHandleException(e);
					throw new RuntimeException(e);
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						currencyList.removeAll();
						for (Iterator<Currency> it = currencies.iterator(); it.hasNext(); ) {
							Currency currency = it.next();
							currencyList.add(currency.getCurrencySymbol());
						}
					}
				});

				return Status.OK_STATUS;
			}

		};
		loadJob.schedule();
		return page;
	}

	@Override
	public boolean canFlipToNextPage() {
		if (createNewCurrencyRadio.getSelection()) {
			return true;
		}
		return false;
	}

	/**
	 * User must not finish the wizard here when he/she wants to create a new currency as this would lead to exceptions.
	 * I'm using a flag here, perhaps there is a "more sophisticated" solution.
	 * TODO When entering valid (!) data in the next page (currency creation page), moving back here and performing finish,
	 * the data entered in the creation page is taken into consideration even when the (other) checkbox to choose an existing
	 * currency is selected before. This leads to some kind of user confusion. My suggestion: disable the finish button when
	 * moving back to this page in the case currency creation checkbox is selected (this is the case when opening the wizard
	 * the first time before moving to the next page, but not in this special situation).
	 */
	boolean canPerformFinish;

	@Override
	public void onNext() {
		canPerformFinish = true;
	}

	@Override
	public void onShow() {
		canPerformFinish = false;
		isPageComplete();
	}

	/**
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	@Override
	public boolean isPageComplete()
	{
		if (canPerformFinish) {
			return true;
		}
		return (chooseExistingCurrencyRadio != null && chooseExistingCurrencyRadio.getSelection() && selectedCurrency != null);
	}

	/**
	 * @return Returns the selectedCurrency.
	 */
	public Currency getSelectedCurrency()
	{
		return selectedCurrency;
	}
}
