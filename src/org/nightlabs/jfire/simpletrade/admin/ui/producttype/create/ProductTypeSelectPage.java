package org.nightlabs.jfire.simpletrade.admin.ui.producttype.create;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.FadeableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.simpletrade.admin.ui.producttype.ProductTypeTree;
import org.nightlabs.jfire.store.ProductType;

public class ProductTypeSelectPage
extends DynamicPathWizardPage
{
	private ProductTypeTree productTypeTree;
	private ProductType selectedProductType;
	
	/**
	 * @param pageName
	 */
	public ProductTypeSelectPage()
	{
		super(ProductTypeSelectPage.class.getName(), "Title");
		this.setDescription("Description");
	}
 
	/**
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent)
	{
		final FadeableComposite page = new FadeableComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		XComposite comp = new XComposite(page, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.getGridLayout().numColumns = 2;
		
		productTypeTree = new ProductTypeTree(comp);
		productTypeTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				getContainer().updateButtons();
			}
		});

		return page;
	}

	private boolean isPageAdded = false;
	@Override
	public boolean canFlipToNextPage() {
		selectedProductType = productTypeTree.getFirstSelectedElement();
		
		if (selectedProductType != null && selectedProductType.getInheritanceNature() == ProductType.INHERITANCE_NATURE_BRANCH) {
			CreateProductTypeNewWizard newWizard = (CreateProductTypeNewWizard)getWizard();
			newWizard.setParentProductTypeID(selectedProductType.getObjectId());
			
			if (!isPageAdded) {
				newWizard.addRemainingPages();
				isPageAdded = true;
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isPageComplete() {
		return selectedProductType != null;
	}
	
	@Override
	public boolean canBeLastPage() {
		return false;
	}
}