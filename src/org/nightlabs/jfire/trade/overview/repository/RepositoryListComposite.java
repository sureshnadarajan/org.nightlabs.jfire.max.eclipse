package org.nightlabs.jfire.trade.overview.repository;

import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.trade.repository.editor.RepositoryEditor;
import org.nightlabs.jfire.trade.repository.editor.RepositoryEditorInput;
import org.nightlabs.jfire.trade.resource.Messages;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class RepositoryListComposite 
extends AbstractTableComposite 
{

	public RepositoryListComposite(Composite parent, int style) {
		super(parent, style);
		
		getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				StructuredSelection s = (StructuredSelection)e.getSelection();
				if (s.isEmpty())
					return;

				Repository repository = (Repository)s.getFirstElement();
				try {
					RCPUtil.openEditor(
							new RepositoryEditorInput((AnchorID) JDOHelper.getObjectId(repository)),
							RepositoryEditor.EDITOR_ID);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) 
	{
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.trade.overview.repository.RepositoryListComposite.idColumn.text")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.trade.overview.repository.RepositoryListComposite.repositoryNameTableColumn.text")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.trade.overview.repository.RepositoryListComposite.ownerTableColumn.text")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.trade.overview.repository.RepositoryListComposite.anchorTypeIdTableColumn.text")); //$NON-NLS-1$
		table.setLayout(new WeightedTableLayout(new int[] {
				20, 20, 20, 20
				}));		
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new RepositoryListLabelProvider());
	}

	class RepositoryListLabelProvider
	extends TableLabelProvider
	{
		public String getColumnText(Object element, int columnIndex) 
		{
			if (element instanceof Repository) {
				Repository repository = (Repository) element;
				switch (columnIndex) 
				{
					case(0):
						return repository.getAnchorID();
					case(1):
						if (repository.getName() != null)
							return repository.getName().getText();
					case(2):
						if (repository.getOwner() != null && repository.getOwner().getPerson() != null)
							return repository.getOwner().getPerson().getDisplayName(); 
						break;
					case(3):
						return getAnchorTypeIDName(repository.getAnchorTypeID());
					default:
						return ""; //$NON-NLS-1$
				}
			}
			return null;
		}		
	}
	
	public static String getAnchorTypeIDName(String anchorTypeID) 
	{
		if (anchorTypeID.equals(Repository.ANCHOR_TYPE_ID_HOME)) {
			return Messages.getString("org.nightlabs.jfire.trade.overview.repository.RepositoryListComposite.homeRepositoryAnchorTypeIdName"); //$NON-NLS-1$
		}
		else if (anchorTypeID.equals(Repository.ANCHOR_TYPE_ID_OUTSIDE)) {
			return Messages.getString("org.nightlabs.jfire.trade.overview.repository.RepositoryListComposite.outsideRepositoryAnchorTypeIdName"); //$NON-NLS-1$
		}
		else
			return anchorTypeID;
	}
}
