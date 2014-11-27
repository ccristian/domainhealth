package domainhealth.frontend.data;

public class JMSServerSummaryData extends DashboardData
{
	private String consumersCurrentCount;
	private String consumersHighCount;
	private String consumersTotalCount;
	
	/**
	 * 
	 */
	public JMSServerSummaryData()
	{
		this(null, null, null, null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param destinationName
	 * @param messagesCurrentCount
	 * @param messagesPendingCount
	 * @param messagesReceivedCount
	 * @param messagesHighCount
	 * @param consumersCurrentCount
	 * @param consumersHighCount
	 * @param consumersTotalCount
	 */
	public JMSServerSummaryData(String destinationName,
			String messagesCurrentCount, String messagesPendingCount,
			String messagesReceivedCount, String messagesHighCount,
			String consumersCurrentCount, String consumersHighCount,
			String consumersTotalCount) {
		
		super(destinationName, messagesCurrentCount, messagesPendingCount, messagesReceivedCount, messagesHighCount);
		
		this.consumersCurrentCount = consumersCurrentCount;
		this.consumersHighCount = consumersHighCount;
		this.consumersTotalCount = consumersTotalCount;
	}

	public String getConsumersCurrentCount() {
		return consumersCurrentCount;
	}

	public void setConsumersCurrentCount(String consumersCurrentCount) {
		this.consumersCurrentCount = consumersCurrentCount;
	}

	public String getConsumersHighCount() {
		return consumersHighCount;
	}

	public void setConsumersHighCount(String consumersHighCount) {
		this.consumersHighCount = consumersHighCount;
	}

	public String getConsumersTotalCount() {
		return consumersTotalCount;
	}

	public void setConsumersTotalCount(String consumersTotalCount) {
		this.consumersTotalCount = consumersTotalCount;
	}
}
