package domainhealth.frontend.data;

public class SAFAgentSummaryData extends DashboardData
{	
	private String failedMessagesTotal;

	private String downtimeHigh;
	private String downtimeTotal;
	private String uptimeHigh;
	private String uptimeTotal;
	
	/**
	 * 
	 */
	public SAFAgentSummaryData() {
		this(null, null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param destinationName
	 * @param messagesCurrentCount
	 * @param messagesPendingCount
	 * @param messagesReceivedCount
	 * @param messagesHighCount
	 * @param failedMessagesTotal
	 * @param downtimeHigh
	 * @param downtimeTotal
	 * @param uptimeHigh
	 * @param uptimeTotal
	 */
	public SAFAgentSummaryData(String destinationName,
			String messagesCurrentCount, String messagesPendingCount,
			String messagesReceivedCount, String messagesHighCount,
			String failedMessagesTotal, String downtimeHigh,
			String downtimeTotal, String uptimeHigh, String uptimeTotal) {
		
		super(destinationName, messagesCurrentCount, messagesPendingCount, messagesReceivedCount, messagesHighCount);
		
		this.failedMessagesTotal = failedMessagesTotal;
		this.downtimeHigh = downtimeHigh;
		this.downtimeTotal = downtimeTotal;
		this.uptimeHigh = uptimeHigh;
		this.uptimeTotal = uptimeTotal;
	}
	
	public String getFailedMessagesTotal() {
		return failedMessagesTotal;
	}

	public void setFailedMessagesTotal(String failedMessagesTotal) {
		this.failedMessagesTotal = failedMessagesTotal;
	}

	public String getDowntimeHigh() {
		return downtimeHigh;
	}

	public void setDowntimeHigh(String downtimeHigh) {
		this.downtimeHigh = downtimeHigh;
	}

	public String getDowntimeTotal() {
		return downtimeTotal;
	}

	public void setDowntimeTotal(String downtimeTotal) {
		this.downtimeTotal = downtimeTotal;
	}

	public String getUptimeHigh() {
		return uptimeHigh;
	}

	public void setUptimeHigh(String uptimeHigh) {
		this.uptimeHigh = uptimeHigh;
	}

	public String getUptimeTotal() {
		return uptimeTotal;
	}

	public void setUptimeTotal(String uptimeTotal) {
		this.uptimeTotal = uptimeTotal;
	}
}
