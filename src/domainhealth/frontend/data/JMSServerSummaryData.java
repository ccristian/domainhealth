package domainhealth.frontend.data;

import java.util.Comparator;

public class JMSServerSummaryData implements Comparator<JMSServerSummaryData>
{
	private String destinationName;
	
	private String messagesCurrentCount;
	private String messagesPendingCount;
	private String messagesReceivedCount;
	private String messagesHighCount;
	
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
		super();
		this.destinationName = destinationName;
		this.messagesCurrentCount = messagesCurrentCount;
		this.messagesPendingCount = messagesPendingCount;
		this.messagesReceivedCount = messagesReceivedCount;
		this.messagesHighCount = messagesHighCount;
		this.consumersCurrentCount = consumersCurrentCount;
		this.consumersHighCount = consumersHighCount;
		this.consumersTotalCount = consumersTotalCount;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getMessagesCurrentCount() {
		return messagesCurrentCount;
	}

	public void setMessagesCurrentCount(String messagesCurrentCount) {
		this.messagesCurrentCount = messagesCurrentCount;
	}

	public String getMessagesPendingCount() {
		return messagesPendingCount;
	}

	public void setMessagesPendingCount(String messagesPendingCount) {
		this.messagesPendingCount = messagesPendingCount;
	}

	public String getMessagesReceivedCount() {
		return messagesReceivedCount;
	}

	public void setMessagesReceivedCount(String messagesReceivedCount) {
		this.messagesReceivedCount = messagesReceivedCount;
	}

	public String getMessagesHighCount() {
		return messagesHighCount;
	}

	public void setMessagesHighCount(String messagesHighCount) {
		this.messagesHighCount = messagesHighCount;
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
	
	public int compare(JMSServerSummaryData arg0, JMSServerSummaryData arg1)
	{
		String nameData0 = arg0.getDestinationName();
		String nameData1 = arg1.getDestinationName();
		
		return nameData0.compareTo(nameData1);
	}
}
