package net.sllmdilab.debugtool.dto;

public class ClientStatusSummaryDto {

	private String clientVersion;
	private String sessionId;
	private int messageCount;
	private int errorMessageCount;

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public int getErrorMessageCount() {
		return errorMessageCount;
	}

	public void setErrorMessageCount(int errorMessageCount) {
		this.errorMessageCount = errorMessageCount;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
