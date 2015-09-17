package net.sllmdilab.debugtool.dto;

public class DeviceStatusSummary {

	private String deviceId;
	private int messageCount;
	private int errorMessageCount;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

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

}
