package org.nightlabs.jfire.voucher.print.ui.transfer.deliver;

import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.print.ui.transfer.delivery.AbstractClientDeliveryProcessorOSPrint;
import org.nightlabs.jfire.scripting.print.ui.transfer.delivery.AbstractClientDeliveryProcessorPrint;
import org.nightlabs.jfire.scripting.print.ui.transfer.delivery.AbstractScriptDataProviderThread;
import org.nightlabs.jfire.scripting.print.ui.transfer.delivery.DeliveryProcessorPrintDebugInfo;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.transfer.RequirementCheckResult;
import org.nightlabs.jfire.voucher.editor2d.iofilter.VoucherXStreamFilter;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */
public class ClientDeliveryProcessorOSPrint
extends AbstractClientDeliveryProcessorOSPrint
{
	private static final Logger logger = Logger.getLogger(ClientDeliveryProcessorOSPrint.class);

	@Override
	protected AbstractScriptDataProviderThread createScriptDataProviderThread(
			AbstractClientDeliveryProcessorPrint clientDeliveryProcessor)
	{
		return new VoucherDataProviderThread(clientDeliveryProcessor);
	}

	private RequirementCheckResult checkRequirementsResult = null;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.ui.transfer.deliver.AbstractClientDeliveryProcessor#getRequirementCheckResult()
	 */
	@Override
	public RequirementCheckResult getRequirementCheckResult()
	{
		return checkRequirementsResult;
	};

	@Override
	public void init()
	{
		//		super.init();
		//
		//		requirementCheckKey = null;
		//
		//		// check, whether all Article's VoucherType s have a layout assigned
		//		try {
		//			VoucherManager vm = JFireEjbFactory.getBean(VoucherManager.class, Login.getLogin().getInitialContextProperties());
		//			Map<ArticleID, PrintabilityStatus> m = vm.getArticleID2PrintabilityStatusMap(new HashSet<ArticleID>(getDelivery().getArticleIDs()));
		//			for (PrintabilityStatus s : m.values()) {
		//				if (PrintabilityStatus.MISSING_VOUCHER_LAYOUT == s) {
		//					requirementCheckKey = PrintabilityStatus.class.getName() + '.' + PrintabilityStatus.MISSING_VOUCHER_LAYOUT.toString();
		//					MessageDialog.openError(getDeliveryEntryPage().getShell(), "Missing Voucher Layout", "Cannot print, because at least one VoucherType has no VoucherLayout assigned!");
		//				}
		//				else if (PrintabilityStatus.OK != s)
		//					throw new IllegalStateException("Unexpected PrintabilityStatus: " + s);
		//
		//				if (PrintabilityStatus.OK != s) {
		//					// TODO deactivate the wizard's next button somehow!
		//					break;
		//				}
		//			}
		//		} catch (Exception e) {
		//			throw new RuntimeException(e);
		//		}
	}

	@Override
	protected void printDocuments(List<ScriptRootDrawComponent> ticketDrawComponents, boolean lastEntry)
	throws DeliveryException
	{
		long start = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("print "+ticketDrawComponents.size()+" in printJob");
		}
		PrinterJob printJob = createConfiguredPrinterJob();
//		printJob.setJobName(
//				"CrossTicket_Ticket_"
//				+ getDelivery().getOrganisationID()
//				+ "_" + ObjectIDUtil.longObjectIDFieldToString(getDelivery().getDeliveryID())
//				+ "_" + ObjectIDUtil.longObjectIDFieldToString(System.currentTimeMillis()));
		printJob.setJobName(
				"JFireVoucher_Voucher_" +
				Long.toString(System.currentTimeMillis(), 36)
				+ "_" + getDelivery().getOrganisationID()
				+ "_" + ObjectIDUtil.longObjectIDFieldToString(getDelivery().getDeliveryID())
		);
		DeliveryProcessorPrintDebugInfo.addTime(
				DeliveryProcessorPrintDebugInfo.CAT_PROCESS_DATA_PREPARE_PRINT_ENGINE, System.currentTimeMillis() - start);
		start = System.currentTimeMillis();

		printJob.setPageable(getPageable(ticketDrawComponents, printJob.defaultPage()));
		try {
			printJob.print();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		DeliveryProcessorPrintDebugInfo.addTime(
				DeliveryProcessorPrintDebugInfo.CAT_PROCESS_DATA_PRINT, System.currentTimeMillis() - start);

		if (logger.isDebugEnabled()) {
			logger.debug("printJob.print() took "+(System.currentTimeMillis()-start)+" ms!");
		}
	}

	public static final String PRINTER_USE_CASE_VOUCHER_PRINT = "PrinterUseCase-OSVoucherPrint";
	@Override
	protected String getPrinterUseCaseID() {
		return PRINTER_USE_CASE_VOUCHER_PRINT;
	}

	@Override
	protected ScriptRootDrawComponent getScriptRootDrawComponent(File file)
	{
		long start = 0;
		if (logger.isDebugEnabled())
			start = System.currentTimeMillis();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			ScriptRootDrawComponent scriptRootDrawComponent = null;
			try {
				VoucherXStreamFilter xStreamFilter = new VoucherXStreamFilter();
				scriptRootDrawComponent = (ScriptRootDrawComponent) xStreamFilter.read(in);
				if (logger.isDebugEnabled())
					logger.debug("Parsing voucher file '" + file + "' took " + (System.currentTimeMillis() - start) + " ms.");
				return scriptRootDrawComponent;
			} finally {
				in.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
