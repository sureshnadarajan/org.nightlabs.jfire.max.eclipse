package org.nightlabs.jfire.voucher.admin.ui.editor;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.trade.admin.ui.editor.AbstractProductTypeDetailPage;
import org.nightlabs.jfire.trade.admin.ui.editor.IProductTypeSectionPart;
import org.nightlabs.jfire.trade.admin.ui.editor.ownervendor.OwnerConfigSection;
import org.nightlabs.jfire.trade.admin.ui.editor.ownervendor.VendorConfigSection;
import org.nightlabs.jfire.voucher.admin.ui.resource.Messages;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherTypeDetailPage
extends AbstractProductTypeDetailPage
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link EventDetailPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new VoucherTypeDetailPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new VoucherTypeDetailPageController(editor);
		}
	}

	@Override
	protected IProductTypeSectionPart createNameSection(Composite parent) {
		return new VoucherTypeNameSection(this, parent, ExpandableComposite.TITLE_BAR);
	}

	@Override
	protected IProductTypeSectionPart createNestedProductTypesSection(Composite parent) {
		return null;
	}

	@Override
	protected IProductTypeSectionPart createOwnerSection(Composite parent) {
		return new OwnerConfigSection(this, parent, ExpandableComposite.TITLE_BAR);
	}

	@Override
	protected IProductTypeSectionPart createVendorSection(Composite parent) {
		return new VendorConfigSection(this, parent, ExpandableComposite.TITLE_BAR);
	}

	@Override
	protected IProductTypeSectionPart createSaleAccessControlSection(Composite parent) {
		return new VoucherTypeSaleAccessControlSection(this, parent);
	}

	public VoucherTypeDetailPage(FormEditor editor) {
		super(editor, VoucherTypeDetailPage.class.getName(),
				Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.VoucherTypeDetailPage.title")); //$NON-NLS-1$
	}

//	private VoucherLayoutSection voucherLayoutSection = null;
//	public VoucherLayoutSection getVoucherLayoutSection() {
//		return voucherLayoutSection;
//	}

//	@Override
//	protected void addSections(Composite parent)
//	{
//		super.addSections(parent);
//
//		voucherLayoutSection = new VoucherLayoutSection(this, parent);
//		voucherLayoutSection.getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		getManagedForm().addPart(voucherLayoutSection);
//	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		VoucherTypeDetailPageController controller = (VoucherTypeDetailPageController) getPageController();
		final VoucherType voucherType = controller.getProductType();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setProductTypePageController(getPageController());
//				if (voucherLayoutSection != null
//						&& !voucherLayoutSection.getSection().isDisposed()) {
//
//					voucherLayoutSection.getVoucherLayoutComposite().setVoucherType(voucherType);
//				}
				getNameSection().setProductTypePageController(getPageController());
				getSaleAccessControlSection().setProductTypePageController(getPageController());

				if (voucherType.isClosed()) {
					getManagedForm().getForm().getForm().setMessage(
							Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.VoucherTypeDetailPage.productTypeClosedMessage"), //$NON-NLS-1$
							IMessageProvider.INFORMATION);
					RCPUtil.setControlEnabledRecursive(getManagedForm().getForm(), false);
				}
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.VoucherTypeDetailPage.pageFormTitle"); //$NON-NLS-1$
	}

}
