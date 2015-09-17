package net.sllmdilab.debugtool.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public class LogEntriesDto {

	private String deviceId;
	private String sessionId;

	private LogType type;
	private String messageLog;

	public String getMessageLog() {
		return messageLog;
	}

	public void setMessageLog(String messageLog) {
		this.messageLog = messageLog;
	}

	public LogType getType() {
		return type;
	}

	public void setType(LogType type) {
		this.type = type;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public static enum LogType {
		SUCCESSFUL("successful"), ERRORS("errors"), ALL("all");

		private String label;

		private LogType(String label) {
			this.label = label;
		}

		@JsonValue
		@Override
		public String toString() {
			return label;
		}
	}
}
