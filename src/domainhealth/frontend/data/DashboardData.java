package domainhealth.frontend.data;

import java.util.Comparator;

public abstract class DashboardData implements Comparator<DashboardData>
{
	private String destinationName;
	
	private String messagesCurrentCount;
	private String messagesPendingCount;
	private String messagesReceivedCount;
	private String messagesHighCount;
	
	/**
	 * 
	 */
	public DashboardData()
	{
		this(null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param destinationName
	 * @param messagesCurrentCount
	 * @param messagesPendingCount
	 * @param messagesReceivedCount
	 * @param messagesHighCount
	 */
	public DashboardData(String destinationName,
			String messagesCurrentCount, String messagesPendingCount,
			String messagesReceivedCount, String messagesHighCount) {
		super();
		this.destinationName = destinationName;
		this.messagesCurrentCount = messagesCurrentCount;
		this.messagesPendingCount = messagesPendingCount;
		this.messagesReceivedCount = messagesReceivedCount;
		this.messagesHighCount = messagesHighCount;
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
	
	public int compare(DashboardData arg0, DashboardData arg1)
	{
		String nameData0 = arg0.getDestinationName();
		String nameData1 = arg1.getDestinationName();
		
		return nameData0.compareTo(nameData1);
	}
}
