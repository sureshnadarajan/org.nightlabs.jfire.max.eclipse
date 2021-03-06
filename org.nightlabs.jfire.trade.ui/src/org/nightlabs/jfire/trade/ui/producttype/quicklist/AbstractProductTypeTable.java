package org.nightlabs.jfire.trade.ui.producttype.quicklist;

import java.text.Collator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.dao.ProductTypeDAO;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.NLLocale;

/**
 * Abstract implementation of an {@link AbstractTableComposite} which display {@link ProductType}s
 * and can handle {@link ISelection}s which contain {@link ProductTypeID}s.
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public abstract class AbstractProductTypeTable<P extends ProductType>
extends AbstractTableComposite<P>
implements ISelectionHandler
{
	public class ContentProvider
	implements IStructuredContentProvider
	{
		private Map<ProductTypeID, ProductType> productTypeID2ProductType =
			new HashMap<ProductTypeID, ProductType>();

		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof Collection)
			{
				Collection<P> collection = (Collection<P>) inputElement;
				for (Iterator<P> it = collection.iterator(); it.hasNext(); ) {
					ProductType productType = it.next();
					productTypeID2ProductType.put((ProductTypeID)JDOHelper.getObjectId(productType), productType);
				}
				return collection.toArray();
			}
			else
				throw new IllegalArgumentException("AbstractProductTypeTable.ContentProvider expects a Collection as inputElement. Recieved "+inputElement.getClass().getName()); //$NON-NLS-1$
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			productTypeID2ProductType.clear();
		}

		public Map<ProductTypeID, ProductType> getProductTypeID2ProductType() {
			return productTypeID2ProductType;
		}
	}

	public class LabelProvider
	extends TableLabelProvider
	{
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return ((ProductType)element).getName().getText(NLLocale.getDefault().getLanguage());
		}
	}

	private ContentProvider contentProvider;

	/**
	 * @param parent
	 */
	public AbstractProductTypeTable(Composite parent) {
		super(parent, SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public AbstractProductTypeTable(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * By default creates only one {@link TableColumn} which displays the productType name
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.producttype.quicklist.AbstractProductTypeTable.column.name"));  //$NON-NLS-1$
		// makes problems under windows, led to the fact that the column was only 20 px wide
//		TableLayout l = new TableLayout();
//		l.addColumnData(new ColumnWeightData(1, true));
//		table.setLayout(l);
		table.setLayout(new WeightedTableLayout(new int[] {1}));
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		contentProvider = new ContentProvider();
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(new LabelProvider());

		tableViewer.setComparator(new ViewerSorter(Collator.getInstance(NLLocale.getDefault())));
	}

	@Override
	public void setSelection(ISelection selection) {
		Set<ProductTypeID> typeIDs = SelectionUtil.getProductTypesIDs(selection);
		if (typeIDs.isEmpty()) {
			super.setSelection(selection);
		}
		else {
			final Set<ProductTypeID> productTypeIDs = typeIDs;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					List<P> productTypes = (List<P>) ProductTypeDAO.sharedInstance().
					getProductTypes(productTypeIDs, new String[] {FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
					superSetSelection(productTypes);
				}
			});
		}
	}

	protected void superSetSelection(List<P> elements) {
		super.setSelection(elements, true);
	}

	public boolean canHandleSelection(ISelection selection)
	{
		Set<ProductTypeID> typeIDs = SelectionUtil.getProductTypesIDs(selection);
		if (!typeIDs.isEmpty()) {
			for (ProductTypeID productTypeID : typeIDs) {
				if (contentProvider != null) {
					ProductType productType = contentProvider.getProductTypeID2ProductType().get(productTypeID);
					if (productType != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
