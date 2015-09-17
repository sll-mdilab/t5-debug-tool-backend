package net.sllmdilab.debugtool.dto;

import java.util.List;

public class StatusSummaryDto {
	
	private List<DeviceStatusSummary> deviceStatusSummaries;
	private List<ClientStatusSummaryDto> clientStatusSummaries;
	
	public StatusSummaryDto() {
	}
	
	public StatusSummaryDto(List<DeviceStatusSummary> deviceStatusSummaries, List<ClientStatusSummaryDto> clientStatusSummaryDtos) {
		this.deviceStatusSummaries = deviceStatusSummaries;
		this.clientStatusSummaries = clientStatusSummaryDtos;
	}

	public List<DeviceStatusSummary> getDeviceStatusSummaries() {
		return deviceStatusSummaries;
	}

	public void setDeviceStatusSummaries(List<DeviceStatusSummary> deviceStatusSummaries) {
		this.deviceStatusSummaries = deviceStatusSummaries;
	}

	public List<ClientStatusSummaryDto> getClientStatusSummaries() {
		return clientStatusSummaries;
	}

	public void setClientStatusSummaries(List<ClientStatusSummaryDto> clientStatusSummaries) {
		this.clientStatusSummaries = clientStatusSummaries;
	}
}
