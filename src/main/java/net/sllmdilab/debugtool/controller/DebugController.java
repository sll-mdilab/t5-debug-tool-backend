package net.sllmdilab.debugtool.controller;

import java.util.Date;

import net.sllmdilab.debugtool.dto.LogEntriesDto;
import net.sllmdilab.debugtool.dto.StatusSummaryDto;
import net.sllmdilab.debugtool.service.DebugService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
	@Autowired
	private DebugService debugService;

	@RequestMapping(value = "/patient/{patientId}/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public StatusSummaryDto getStatusSummary(@PathVariable String patientId,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
		return debugService.getStatusSummary(patientId, start, end);
	}
	
	@RequestMapping(value = "/device/{deviceId}/log", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public LogEntriesDto getSuccessfulMessageLog(@PathVariable String deviceId,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
		return debugService.getAllMessages(deviceId, start, end);
	}

	@RequestMapping(value = "/device/{deviceId}/error_log", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public LogEntriesDto getErrorMessageLog(@PathVariable String deviceId,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
		return debugService.getErrorMessageLog(deviceId, start, end);
	}
	
	@RequestMapping(value = "/session/{sessionId}/log", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public LogEntriesDto getRequestLog(@PathVariable String sessionId,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
		return debugService.getRequestLog(sessionId, start, end);
	}
	
	@RequestMapping(value = "/session/{sessionId}/error_log", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public LogEntriesDto getRequestErrorLog(@PathVariable String sessionId,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
			@RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
		return debugService.getRequestErrorLog(sessionId, start, end);
	}
}
