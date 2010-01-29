/**
 *
 */
package org.nightlabs.jfire.reporting.admin.ui.layout.action.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jdo.FetchPlan;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.FileSelectionComposite;
import org.nightlabs.base.ui.composite.LabeledText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.reporting.ReportingConstants;
import org.nightlabs.jfire.reporting.ReportingInitialiser;
import org.nightlabs.jfire.reporting.admin.ui.layout.editor.l10n.ReportLayoutL10nUtil;
import org.nightlabs.jfire.reporting.admin.ui.layout.editor.l10n.ReportLayoutL10nUtil.PreparedLayoutL10nData;
import org.nightlabs.jfire.reporting.admin.ui.resource.Messages;
import org.nightlabs.jfire.reporting.dao.ReportRegistryItemDAO;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.config.AcquisitionParameterConfig;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.dao.ReportParameterAcquisitionSetupDAO;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Dialog to export a layout as needed for the initialisation in the server.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ExportReportLayoutDialog extends ResizableTrayDialog {

	private XComposite wrapper;
	private FileSelectionComposite folderComposite;
	private LabeledText layoutFileName;
	private ReportRegistryItemID layoutID;

	private Button needZipButton;

	private static String ZIP_SUFFIX = ".zip";
	private static String REPORT_LAYOUT_SUFFIX = ".rptdesign";

	private ReportParameterAcquisitionSetup parameterSetup;
	/**
	 * @param parentShell
	 */
	public ExportReportLayoutDialog(Shell parentShell, ReportRegistryItemID layoutID ) {
		super(parentShell, null);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.layoutID = layoutID;

		Job job = new Job("Loading Data...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				ReportRegistryItemDAO reportRegistryItemDAO = ReportRegistryItemDAO.sharedInstance();
				reportRegistryItem = reportRegistryItemDAO.getReportRegistryItem(
						ExportReportLayoutDialog.this.layoutID, 
						new String[] {FetchPlan.DEFAULT, 
								ReportRegistryItem.FETCH_GROUP_NAME, 
								ReportRegistryItem.FETCH_GROUP_DESCRIPTION, 
								ReportRegistryItem.FETCH_GROUP_PARENT_CATEGORY}, 
								monitor);
				
				ReportParameterAcquisitionSetupDAO parameterSetupDAO = ReportParameterAcquisitionSetupDAO.sharedInstance();
				parameterSetup = parameterSetupDAO.getSetupForReportLayout(
						ExportReportLayoutDialog.this.layoutID, 
						new String[] {FetchPlan.DEFAULT, 
								ReportParameterAcquisitionSetup.FETCH_GROUP_VALUE_ACQUISITION_SETUPS,
								ReportParameterAcquisitionSetup.FETCH_GROUP_DEFAULT_USE_CASE,
								ReportParameterAcquisitionUseCase.FETCH_GROUP_NAME,
								ReportParameterAcquisitionUseCase.FETCH_GROUP_DESCRIPTION,
								ValueAcquisitionSetup.FETCH_GROUP_VALUE_CONSUMER_BINDINGS,
								ValueAcquisitionSetup.FETCH_GROUP_VALUE_PROVIDER_CONFIGS,
								ValueAcquisitionSetup.FETCH_GROUP_PARAMETER_CONFIGS,
								ValueProviderConfig.FETCH_GROUP_MESSAGE,
								ValueConsumerBinding.FETCH_GROUP_CONSUMER,
								ValueConsumerBinding.FETCH_GROUP_PROVIDER}, 
								monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.reporting.admin.ui.layout.action.export.ExportReportLayoutDialog.window.title")); //$NON-NLS-1$
		newShell.setSize(400, 400);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE);
		layoutFileName = new LabeledText(wrapper, Messages.getString("org.nightlabs.jfire.reporting.admin.ui.layout.action.export.ExportReportLayoutDialog.label.selectExportFileName")); //$NON-NLS-1$
		layoutFileName.setText(layoutID.reportRegistryItemID /*+ ZIP_SUFFIX*/);
		folderComposite = new FileSelectionComposite(
				wrapper,
				SWT.NONE, FileSelectionComposite.OPEN_DIR,
				Messages.getString("org.nightlabs.jfire.reporting.admin.ui.layout.action.export.ExportReportLayoutDialog.label.selectExportFolder"), //$NON-NLS-1$
				Messages.getString("org.nightlabs.jfire.reporting.admin.ui.layout.action.export.ExportReportLayoutDialog.label.selectFolder")); //$NON-NLS-1$

		needZipButton = new Button(wrapper, SWT.CHECK);
		needZipButton.setText("Export in Zip file");
		needZipButton.setSelection(true);

		return wrapper;
	}

	private ReportRegistryItem reportRegistryItem;
	@Override
	protected void okPressed() {
		ReportLayoutExportInput editorInput = new ReportLayoutExportInput(layoutID);

		String parentName = folderComposite.getFileText();
		if (needZipButton.getSelection() == true) {
			try {
				parentName = IOUtil.createUserTempDir("jfire_report.exported.", "." + layoutFileName.getText()).getPath();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		String reportID = null;
		if (parentName != null) {
			//Report File (.rptdesign)
			File exportFile = new File(parentName, layoutFileName.getText() + REPORT_LAYOUT_SUFFIX);
			reportID = IOUtil.getFileNameWithoutExtension(exportFile.getName());
			try {
				ReportingInitialiser.exportLayoutToTemplateFile(editorInput.getFile(), exportFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			//Resource Files(.properties)
			PreparedLayoutL10nData l10nData = ReportLayoutL10nUtil.prepareReportLayoutL10nData(editorInput);
			File resourceFolder = new File(parentName, "resource"); //$NON-NLS-1$
			resourceFolder.mkdirs();
			for (ReportLayoutLocalisationData data : l10nData.getLocalisationBundle().values()) {
				String l10nFileName = reportID;
				if ("".equals(data.getLocale())) //$NON-NLS-1$
					l10nFileName = l10nFileName + ".properties"; //$NON-NLS-1$
				else
					l10nFileName = l10nFileName + "_" + data.getLocale() + ".properties";  //$NON-NLS-1$ //$NON-NLS-2$

				try {
					File dataFile = new File(resourceFolder, l10nFileName);
					dataFile.createNewFile();
					InputStream in = data.createLocalisationDataInputStream();
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dataFile));
					try {
						IOUtil.transferStreamData(in, out);
					} finally {
						in.close();
						out.close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			//Generates descriptor file(content.xml)
			File descriptorFile = new File(parentName, ReportingConstants.DESCRIPTOR_FILE);
			try {
				descriptorFile.createNewFile();

				//Create document
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();

				/*****ReportCategory-node*****/
				Element reportCategory = doc.createElement(ReportingConstants.REPORT_CATEGORY_ELEMENT);
				reportCategory.setAttribute(ReportingConstants.REPORT_CATEGORY_ELEMENT_ATTRIBUTE_ID, reportRegistryItem.getParentCategoryID().reportRegistryItemID);
				reportCategory.setAttribute(ReportingConstants.REPORT_CATEGORY_ELEMENT_ATTRIBUTE_TYPE, layoutID.reportRegistryItemType);
				doc.appendChild(reportCategory);

				//Category-names
				generateI18nElements(doc, reportCategory, ReportingConstants.REPORT_CATEGORY_ELEMENT_NAME, reportRegistryItem.getParentCategory().getName());

				//Report
				Element report = doc.createElement(ReportingConstants.REPORT_ELEMENT);
				report.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_FILE, exportFile.getName());
				report.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_ID, layoutID.reportRegistryItemID);
				report.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_ENGINE_TYPE, "BIRT");
				report.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_OVERWRITE_ON_INIT, "true"); //Has to overwrite the old file
				reportCategory.appendChild(report);

				//Report-names
				generateI18nElements(doc, report, ReportingConstants.REPORT_ELEMENT_NAME, reportRegistryItem.getName());

				//Report-descriptions
				generateI18nElements(doc, report, ReportingConstants.REPORT_ELEMENT_DESCRIPTION, reportRegistryItem.getDescription());

				//Parameter-acquisition
				Element parameterAcquisition = doc.createElement(ReportingConstants.PARAMETER_ACQUISITION_ELEMENT);
				report.appendChild(parameterAcquisition);

				int idNo = 0;
				Map<Object, Integer> object2IdNo = new HashMap<Object, Integer>();
				
				//Use-case
				Map<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> valueAcquisitionSetups = 
					parameterSetup.getValueAcquisitionSetups();
				for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> setup : valueAcquisitionSetups.entrySet()) {
					Element useCase = doc.createElement(ReportingConstants.USE_CASE_ELEMENT);
					useCase.setAttribute(ReportingConstants.USE_CASE_ATTRIBUTE_ID, setup.getKey().getReportParameterAcquisitionUseCaseID());
					useCase.setAttribute(ReportingConstants.USE_CASE_ATTRIBUTE_DEFAULT, "true");

					generateI18nElements(doc, useCase, ReportingConstants.USE_CASE_ELEMENT_NAME, setup.getKey().getName());
					generateI18nElements(doc, useCase, ReportingConstants.USE_CASE_ELEMENT_DESCRIPTION, setup.getKey().getDescription());

					//Parameters
					Element parameters = doc.createElement("parameters");
					useCase.appendChild(parameters);

					int idx = 0;
					for (AcquisitionParameterConfig parameterConfig : setup.getValue().getParameterConfigs()) {
						Element parameter = doc.createElement(ReportingConstants.PARAMETER_ELEMENT);
						parameter.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_ID, Integer.toString(idx++));
						parameter.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_NAME, parameterConfig.getParameterID()); //TODO!!! WATCHME!!! Name == PARAMETER_ID ;-)
						parameter.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_TYPE, parameterConfig.getParameterType());
						parameter.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_X, Integer.toString(parameterConfig.getX()));
						parameter.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_Y, Integer.toString(parameterConfig.getY()));
						
						object2IdNo.put(parameterConfig, idNo);
						idNo++;
						
						parameters.appendChild(parameter);
					}

					//Value-provider-configs
					Element valueProviderConfigs = doc.createElement(ReportingConstants.VALUE_PROVIDER_CONFIGS_ELEMENT);
					useCase.appendChild(valueProviderConfigs);

					for (ValueProviderConfig valueProviderConfig : setup.getValue().getValueProviderConfigs()) {
						Element providerConfig = doc.createElement(ReportingConstants.PROVIDER_CONFIG_ELEMENT);
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ID, Integer.toString(idx++));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ORGANISATION_ID, "dev.jfire.org");
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_CATEGORY_ID, valueProviderConfig.getValueProviderCategoryID());
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_VALUE_PROVIDER_ID, valueProviderConfig.getValueProviderID());
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_INDEX, Integer.toString(valueProviderConfig.getPageIndex()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_ROW, Integer.toString(valueProviderConfig.getPageRow()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_COLUMN, Integer.toString(valueProviderConfig.getPageColumn()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ALLOW_NULL_OUTPUT_VALUE, Boolean.toString(valueProviderConfig.isAllowNullOutputValue()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_SHOW_MESSAGE_IN_HEADER, Boolean.toString(valueProviderConfig.isShowMessageInHeader()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_GROW_VERTICALLY, Boolean.toString(valueProviderConfig.isGrowVertically()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_X, Integer.toString(valueProviderConfig.getX()));
						providerConfig.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_Y, Integer.toString(valueProviderConfig.getY()));
						
						object2IdNo.put(valueProviderConfig, idNo);
						idNo++;
						
						generateI18nElements(doc, providerConfig, ReportingConstants.PROVIDER_CONFIG_ELEMENT_MESSAGE, valueProviderConfig.getMessage());
						
						valueProviderConfigs.appendChild(providerConfig);
					}
					
					//Value-consumer-bindings
					Element valueConsumerBindings = doc.createElement(ReportingConstants.VALUE_CONSUMER_BINDINGS_ELEMENT);
					useCase.appendChild(valueConsumerBindings);

					for (ValueConsumerBinding valueConsumerBinding : setup.getValue().getValueConsumerBindings()) {
						Element consumerBinding = doc.createElement(ReportingConstants.VALUE_CONSUMER_BINDING_ELEMENT);
						
						Element bindingProvider = doc.createElement(ReportingConstants.BINDING_PROVIDER_ELEMENT);
						bindingProvider.setAttribute(ReportingConstants.BINDING_PROVIDER_ELEMENT_ATTRIBUTE_ID, String.valueOf(object2IdNo.get(valueConsumerBinding.getProvider())));
						
						Element bindingParameter = doc.createElement(ReportingConstants.BINDING_PARAMETER_ELEMENT);
						bindingParameter.setAttribute(ReportingConstants.BINDING_PARAMETER_ELEMENT_ATTRIBUTE_NAME, valueConsumerBinding.getParameterID());
						
						Element bindingConsumer = doc.createElement(ReportingConstants.BINDING_CONSUMER_ELEMENT); 
						bindingConsumer.setAttribute(ReportingConstants.BINDING_CONSUMER_ELEMENT_ATTRIBUTE_ID, String.valueOf(object2IdNo.get(valueConsumerBinding.getConsumer())));
						
						consumerBinding.appendChild(bindingProvider);
						consumerBinding.appendChild(bindingParameter);
						consumerBinding.appendChild(bindingConsumer);
						
						object2IdNo.put(valueConsumerBinding, idNo);
						idNo++;
						
						valueConsumerBindings.appendChild(consumerBinding);
					}

					parameterAcquisition.appendChild(useCase);	
				}

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, ReportingConstants.DESCRIPTOR_FILE_ENCODING);
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, ReportingConstants.DESCRIPTOR_FILE_DOCTYPE_SYSTEM);
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ReportingConstants.DESCRIPTOR_FILE_DOCTYPE_PUBLIC); 

				//Write the XML document to a file
				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, result);

				String xmlString = result.getWriter().toString();

				IOUtil.writeTextFile(descriptorFile, xmlString);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (needZipButton.getSelection() == true) {
				File outputFilePath = new File(folderComposite.getFile(), layoutFileName.getText() + ZIP_SUFFIX);
				try {
					IOUtil.zipFolder(outputFilePath, IOUtil.getUserTempDir("jfire_report.exported.", "." + layoutFileName.getText()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		super.okPressed();
	}

	private void generateI18nElements(Document document, Element parentElement, String elementName, I18nText i18nText) {
		for (Entry<String, String> entry : i18nText.getTexts()) {
			Element element = document.createElement(elementName);
			element.setAttribute("language", entry.getKey());
			element.setTextContent(entry.getValue());

			parentElement.appendChild(element);
		}
	}
}
