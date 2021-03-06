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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.admin.ui.layout.action.importlayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.FileSelectionComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditorMultiLine;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.reporting.ReportManagerRemote;
import org.nightlabs.jfire.reporting.ReportingConstants;
import org.nightlabs.jfire.reporting.ReportingInitialiser;
import org.nightlabs.jfire.reporting.dao.ReportRegistryItemDAO;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author  Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 */
public class ImportReportLayoutDialog
extends ResizableTrayDialog
{
	private ReportCategory reportCategory;

	/**
	 * @param parentShell
	 */
	public ImportReportLayoutDialog(Shell parentShell, ReportCategory reportCategory)
	{
		super(parentShell, null);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.reportCategory = reportCategory;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Import Report Layout");
		newShell.setSize(400, 500);
	}

	private Text reportCategoryNameText;

	private XComposite wrapper;
	private FileSelectionComposite reportLayoutFileSelectionComposite;
	private I18nTextEditor reportRegistryItemNameEditor;

	private Group reportItemGroup;
	private Button autogenerateIDCheckbox;
	private Text reportRegistryItemIDText;

	private I18nTextEditor reportRegistryItemDescriptionEditor;

	private boolean needRenamingPropertyFiles = false;

	@Override
	protected Control createDialogArea(Composite parent)
	{
		wrapper = new XComposite(parent, SWT.NONE);

		//Report Category
		new Label(wrapper, SWT.NONE).setText("Report category: ");
		reportCategoryNameText = new Text(wrapper, SWT.BORDER);
		reportCategoryNameText.setEditable(false);
		reportCategoryNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportCategoryNameText.setText(reportCategory.getName().getText());

		//File
		reportLayoutFileSelectionComposite = new FileSelectionComposite(
				wrapper,
				SWT.NONE, FileSelectionComposite.OPEN_FILE,
				"File: ",	"Caption"){
			@Override
			protected String doSetUpFileDialog(
					List<String> filterNames,
					List<String> filterExtensions) {
				filterNames.add("Zip files");
				filterExtensions.add("*.zip");
				return super.doSetUpFileDialog(filterNames, filterExtensions);
			};
		};
		reportLayoutFileSelectionComposite.getFileTextControl().setEditable(false);
		reportLayoutFileSelectionComposite.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent m) {
				loadDescriptorFile(reportLayoutFileSelectionComposite.getFile());
				validateValues();
			}
		});

		//Imported Report
		reportItemGroup = new Group(wrapper, SWT.NONE);
		reportItemGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		reportItemGroup.setLayout(new GridLayout(1, false));
		reportItemGroup.setText("Report Registry Item: ");

		autogenerateIDCheckbox = new Button(reportItemGroup, SWT.CHECK);
		autogenerateIDCheckbox.setText("Auto generate ReportRegistryItemID: ");
		autogenerateIDCheckbox.setSelection(true);
		autogenerateIDCheckbox.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				needRenamingPropertyFiles = autogenerateIDCheckbox.getSelection();
				reportRegistryItemIDText.setEnabled(!autogenerateIDCheckbox.getSelection());
			}
		});

		//Report ID
		new Label(reportItemGroup, SWT.NONE).setText("Report Registry Item ID: ");
		reportRegistryItemIDText = new Text(reportItemGroup, SWT.BORDER);
		reportRegistryItemIDText.setEnabled(false);
		reportRegistryItemIDText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportRegistryItemIDText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				needRenamingPropertyFiles = true;
			}
		});

		//Report Name
		new Label(reportItemGroup, SWT.NONE).setText("Report layout name: ");
		reportRegistryItemNameEditor = new I18nTextEditor(reportItemGroup);
		reportRegistryItemNameEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportRegistryItemNameEditor.setEnabled(false);
		reportRegistryItemNameEditor.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) {
				validateValues();
			}
		});

		//Report Description
		new Label(reportItemGroup, SWT.NONE).setText("Description: ");
		reportRegistryItemDescriptionEditor = new I18nTextEditorMultiLine(reportItemGroup, reportRegistryItemNameEditor.getLanguageChooser());
		reportRegistryItemDescriptionEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
		reportRegistryItemDescriptionEditor.setEnabled(false);
		reportRegistryItemDescriptionEditor.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) {
				validateValues();
			}
		});

		Job job = new Job("Loading Report Category...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				ReportRegistryItemID id = (ReportRegistryItemID)JDOHelper.getObjectId(reportCategory);
				reportCategory =
					(ReportCategory)ReportRegistryItemDAO.sharedInstance().getReportRegistryItem(id,
							new String[] {FetchPlan.DEFAULT, ReportRegistryItem.FETCH_GROUP_NAME, ReportCategory.FETCH_GROUP_CHILD_ITEMS},
							monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

		return wrapper;
	}

	@Override
	protected void okPressed()
	{
		//Modify the Report Descriptor File based on the changes
		//Report Category Node
		Node reportCategoryNode = NLDOMUtil.findElementNode(ReportingConstants.REPORT_CATEGORY_ELEMENT, reportDescriptorDocument.getDocumentElement());

		NamedNodeMap nodeMap = reportCategoryNode.getAttributes();
		nodeMap.getNamedItem(ReportingConstants.REPORT_CATEGORY_ELEMENT_ATTRIBUTE_ID).setNodeValue(reportCategory.getReportRegistryItemID());
		nodeMap.getNamedItem(ReportingConstants.REPORT_CATEGORY_ELEMENT_ATTRIBUTE_TYPE).setNodeValue(reportCategory.getReportRegistryItemType());

		//Report Cateogory Names
		Collection<Node> reportCategoryChildNameNodes = NLDOMUtil.findNodeList(reportCategoryNode, ReportingConstants.REPORT_CATEGORY_ELEMENT_NAME);
		for (Node nameNode : reportCategoryChildNameNodes) {
			String language = nameNode.getAttributes().getNamedItem("language").getNodeValue();
			String newText = reportCategory.getName().getText(language);
			nameNode.setTextContent(newText);
		}

		//Report Node
		Node reportNode =
			NLDOMUtil.findElementNode(ReportingConstants.REPORT_ELEMENT, reportDescriptorDocument.getDocumentElement());

		//ID
		Node reportIDNode = reportNode.getAttributes().getNamedItem(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_ID);
		if (autogenerateIDCheckbox.getSelection() != true) {
			reportIDNode.setNodeValue(reportRegistryItemIDText.getText());
		}
		else {
			reportIDNode.setNodeValue(IDGenerator.nextIDString(ReportRegistryItem.class));
		}

		//Report Names
		Collection<Node> reportChildNameNodes = NLDOMUtil.findNodeList(reportNode, ReportingConstants.REPORT_ELEMENT_NAME);
		for (Node nameNode : reportChildNameNodes) {
			String language = nameNode.getAttributes().getNamedItem("language").getNodeValue();
			String newText = reportRegistryItemNameEditor.getI18nText().getText(language);
			nameNode.setTextContent(newText);
		}

		//Report Description
		Collection<Node> reportChildDescriptionNodes = NLDOMUtil.findNodeList(reportNode, ReportingConstants.REPORT_ELEMENT_DESCRIPTION);
		for (Node descriptionNode : reportChildDescriptionNodes) {
			String language = descriptionNode.getAttributes().getNamedItem("language").getNodeValue();
			String newText = reportRegistryItemDescriptionEditor.getI18nText().getText(language);
			descriptionNode.setTextContent(newText);
		}

		//Overwrite the old descriptor file
		try {
			NLDOMUtil.writeDocument(reportDescriptorDocument, new FileOutputStream(contentFile), ReportingConstants.DESCRIPTOR_FILE_ENCODING);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

//		File resourceFolder = new File(tmpFolder, "resource");
//
//		File[] resourceFiles = resourceFolder.listFiles();
//		for (File resourceFile : resourceFiles) {
//			if (!resourceFile.getName().startsWith(reportRegistryItemID)) {
//				resourceFile.renameTo(new File(resourceFolder, reportRegistryItemID + resourceFile.getName().split("_")[1]));
//			}
//		}

		//Zip
		File outputZipFile = new File(IOUtil.getUserTempDir(
				TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX), reportLayoutFileSelectionComposite.getFile().getName());

		if (needRenamingPropertyFiles) {
			File resourceFolder = new File(IOUtil.getUserTempDir(TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX),
					RESOURCE_FOLDER_NAME);
			for (File resourceFile : resourceFolder.listFiles()) {
				String newResourceFileName = resourceFile.getName().replaceAll(IOUtil.getFileNameWithoutExtension(reportLayoutZipFile.getName()), reportIDNode.getNodeValue()); //TODO Check me!!!
				resourceFile.renameTo(new File(resourceFolder, newResourceFileName));
			}

			File textPartConfigurationFile =
				new File(
						IOUtil.getUserTempDir(TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX),
						IOUtil.getFileNameWithoutExtension(reportLayoutZipFile.getName()) + ReportingConstants.TEXT_PART_CONFIGURATION_FILE_SUFFIX
				);
			String newReportTextPartConfurationName =
				textPartConfigurationFile.getName().replaceAll(IOUtil.getFileNameWithoutExtension(reportLayoutZipFile.getName()), reportRegistryItemIDText.getText()); //TODO Check me!!!
			textPartConfigurationFile.renameTo(new File(IOUtil.getUserTempDir(TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX), newReportTextPartConfurationName));
		}


		try {
			IOUtil.zipFolder(outputZipFile, IOUtil.getUserTempDir(TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ReportManagerRemote rm;
		try {
			rm = JFireEjb3Factory.getRemoteBean(ReportManagerRemote.class, Login.getLogin().getInitialContextProperties());
			rm.importReportLayoutZipFile(IOUtil.getBytesFromFile(outputZipFile), (ReportRegistryItemID)JDOHelper.getObjectId(reportCategory));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		IOUtil.deleteDirectoryRecursively(tmpFolder);

		super.okPressed();
	}

	private static final String TMP_FOLDER_PREFIX = "jfire_report.client.imported.";
	private static final String TMP_FOLDER_SUFFIX = ".report";

	private static final String RESOURCE_FOLDER_NAME = "resource";

	private Document reportDescriptorDocument;
	private File contentFile;

	private String reportRegistryItemID;
	private File reportLayoutZipFile;

	private File tmpFolder;
	private void loadDescriptorFile(final File reportLayoutZipFile) {
		this.reportLayoutZipFile = reportLayoutZipFile;
		try {
			//Delete old stuff
			File existingTmpFolder = IOUtil.getUserTempDir(TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX);
			if (existingTmpFolder.exists()) {
				IOUtil.deleteDirectoryRecursively(existingTmpFolder);
			}

			//Unzip to the temp folder
			tmpFolder = IOUtil.createUserTempDir(TMP_FOLDER_PREFIX, TMP_FOLDER_SUFFIX);
			tmpFolder.mkdir();
			IOUtil.unzipArchive(reportLayoutZipFile, tmpFolder);
			tmpFolder.deleteOnExit();

			contentFile = new File(tmpFolder, ReportingConstants.DESCRIPTOR_FILE);
			if (contentFile.exists()) {
				reportDescriptorDocument  = ReportingInitialiser.parseFile(contentFile);

				//Report Element
				Node reportNode = NLDOMUtil.findElementNode(ReportingConstants.REPORT_ELEMENT, reportDescriptorDocument.getDocumentElement());
				reportRegistryItemID = NLDOMUtil.getAttributeValue(reportNode, "id");
				reportRegistryItemIDText.setText(reportRegistryItemID);
				for (ReportRegistryItem childItem : reportCategory.getChildItems()) {
					if (childItem.getReportRegistryItemID().equals(reportRegistryItemID)) {
						MessageDialog.openWarning(
								getShell(),
								"Warning",
						"The report id is deplicated. So, the report will be replaced by the imported one.");
					}
				}


				//Report Names
				Collection<Node> reportChildNameNodes =
					NLDOMUtil.findNodeList(reportNode, ReportingConstants.REPORT_ELEMENT_NAME);

				for (Node nameNode : reportChildNameNodes) {
					reportRegistryItemNameEditor.getI18nText().setText(
							NLDOMUtil.getAttributeValue(nameNode, "language"),
							nameNode.getTextContent());
				}
				reportRegistryItemNameEditor.refresh();

				//Report Descriptions
				Collection<Node> reportChildDescriptionNodes =
					NLDOMUtil.findNodeList(reportNode, ReportingConstants.REPORT_ELEMENT_DESCRIPTION);

				for (Node nameNode : reportChildDescriptionNodes) {
					reportRegistryItemDescriptionEditor.getI18nText().setText(
							NLDOMUtil.getAttributeValue(nameNode, "language"),
							nameNode.getTextContent());
				}
				reportRegistryItemDescriptionEditor.refresh();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean validateValues() {
		boolean result = true;
		if (reportRegistryItemNameEditor.getI18nText().isEmpty()) {
			result = false;
		}
		if (reportLayoutFileSelectionComposite.getFile().exists()) {
			reportRegistryItemNameEditor.setEnabled(true);
			reportRegistryItemDescriptionEditor.setEnabled(true);
		}

		setOKButtonEnabled(result);
		return result;
	}

	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == OK)
			button.setEnabled(false);
		return button;
	}

	public void setOKButtonEnabled(boolean enabled) {
		getButton(OK).setEnabled(enabled);
	}

	public boolean isNeedRenamingPropertyFiles() {
		return needRenamingPropertyFiles;
	}
}