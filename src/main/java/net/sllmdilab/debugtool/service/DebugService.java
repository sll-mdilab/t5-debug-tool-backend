package net.sllmdilab.debugtool.service;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sllmdilab.commons.exceptions.XmlParsingException;
import net.sllmdilab.commons.util.Constants;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.debugtool.dao.DeviceUseStatementDao;
import net.sllmdilab.debugtool.dao.FhirLogDao;
import net.sllmdilab.debugtool.dao.T5MessagesDao;
import net.sllmdilab.debugtool.dto.ClientStatusSummaryDto;
import net.sllmdilab.debugtool.dto.DeviceStatusSummary;
import net.sllmdilab.debugtool.dto.LogEntriesDto;
import net.sllmdilab.debugtool.dto.StatusSummaryDto;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Service
public class DebugService {
	private Logger logger = LoggerFactory.getLogger(DebugService.class);

	@Autowired
	private T5MessagesDao messagesDao;

	@Autowired
	private FhirLogDao fhirLogDao;

	@Autowired
	private DeviceUseStatementDao deviceUseStatementDao;

	public StatusSummaryDto getStatusSummary(String patientId, Date start, Date end) {
		List<String> deviceIds = deviceUseStatementDao.findActiveDeviceIdsDuringPeriodForPatient(patientId, start, end);
 
		List<DeviceStatusSummary> deviceSummaries = deviceIds.stream()
				.map(deviceId -> createDeviceStatusSummary(start, end, deviceId)).collect(Collectors.toList());

		List<String> sessionIds = fhirLogDao.findAllSessionsForPatient(patientId, start, end);

		List<ClientStatusSummaryDto> clientSummaries = sessionIds
				.stream()
				.map(sessionId -> createClientStatusSummary(start, end, sessionId,
						fhirLogDao.findClientVersionForSession(sessionId))).collect(Collectors.toList());

		return new StatusSummaryDto(deviceSummaries, clientSummaries);
	}

	private DeviceStatusSummary createDeviceStatusSummary(Date start, Date end, String deviceId) {
		logger.debug("Device: \"" + deviceId + "\"");

		DeviceStatusSummary deviceSummary = new DeviceStatusSummary();
		deviceSummary.setDeviceId(deviceId);
		deviceSummary.setMessageCount(messagesDao.countSuccessfulMessages(deviceId, start, end));
		deviceSummary.setErrorMessageCount(messagesDao.countErrorMessages(deviceId, start, end));
		return deviceSummary;
	}

	private ClientStatusSummaryDto createClientStatusSummary(Date start, Date end, String sessionId,
			String clientVersionId) {
		logger.debug("Session: \"" + sessionId + "\"");

		ClientStatusSummaryDto clientSummary = new ClientStatusSummaryDto();
		clientSummary.setClientVersion(clientVersionId);
		clientSummary.setSessionId(sessionId);
		clientSummary.setMessageCount(fhirLogDao.countSuccessfulRequests(sessionId, start, end));
		clientSummary.setErrorMessageCount(fhirLogDao.countRequestErrors(sessionId, start, end));
		return clientSummary;
	}

	public LogEntriesDto getSuccessfulMessageLog(String deviceId, Date start, Date end) {
		String logMessages = messagesDao.findSuccessfulMessages(deviceId, start, end);

		LogEntriesDto deviceLogMessagesDto = new LogEntriesDto();
		deviceLogMessagesDto.setDeviceId(deviceId);
		deviceLogMessagesDto.setMessageLog(logMessages);
		deviceLogMessagesDto.setType(LogEntriesDto.LogType.SUCCESSFUL);

		return deviceLogMessagesDto;
	}

	public LogEntriesDto getErrorMessageLog(String deviceId, Date start, Date end) {
		String logMessages = messagesDao.findErrorAckMessages(deviceId, start, end);

		LogEntriesDto deviceLogMessagesDto = new LogEntriesDto();
		deviceLogMessagesDto.setDeviceId(deviceId);
		deviceLogMessagesDto.setMessageLog(logMessages);
		deviceLogMessagesDto.setType(LogEntriesDto.LogType.ERRORS);

		return deviceLogMessagesDto;
	}

	public LogEntriesDto getAllMessages(String deviceId, Date start, Date end) {
		String successMessages = messagesDao.findSuccessfulMessages(deviceId, start, end);
		String errorMessages = messagesDao.findErrorAckMessages(deviceId, start, end);

		String logMessages = mergeSuccessAndErrorMessages(successMessages, errorMessages);

		LogEntriesDto deviceLogMessagesDto = new LogEntriesDto();
		deviceLogMessagesDto.setDeviceId(deviceId);
		deviceLogMessagesDto.setMessageLog(logMessages);
		deviceLogMessagesDto.setType(LogEntriesDto.LogType.ALL);

		return deviceLogMessagesDto;
	}

	private class MessageContainer {
		private String msg;
		private Date timestamp;

		MessageContainer(String msg, Date timestamp) {
			this.msg = msg;
			this.timestamp = timestamp;
		}

		public String getMsg() {
			return msg;
		}

		public Date getTimestamp() {
			return timestamp;
		}
	}

	private String mergeSuccessAndErrorMessages(String successMessagesStr, String errorMessagesStr) {
		Stream<MessageContainer> msgStream = Stream.empty();

		// Extract timestamps from successful messages
		if (!StringUtils.isBlank(successMessagesStr)) {
			msgStream = Stream.concat(
					msgStream,
					Stream.of(successMessagesStr.split("\n")).map(
							msg -> new MessageContainer(msg, extractTimestampFromPCD(msg))));
		}

		// Extract timestamps from error messages
		if (!StringUtils.isBlank(errorMessagesStr)) {
			msgStream = Stream.concat(
					msgStream,
					Stream.of(errorMessagesStr.split("\n")).map(
							msg -> new MessageContainer(msg, extractTimestampFromACK(msg))));
		}

		List<String> mergedMessages = msgStream.sorted((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
				.map(m -> m.getMsg()).collect(Collectors.toList());
		return String.join("\n", mergedMessages);
	}

	public LogEntriesDto getRequestLog(String sessionId, Date start, Date end) {
		String logMessages = fhirLogDao.findAllRequests(sessionId, start, end);

		LogEntriesDto logEntriesDto = new LogEntriesDto();
		logEntriesDto.setSessionId(sessionId);
		logEntriesDto.setMessageLog(logMessages);
		logEntriesDto.setType(LogEntriesDto.LogType.ALL);

		return logEntriesDto;
	}

	public LogEntriesDto getSuccessfulRequestLog(String sessionId, Date start, Date end) {
		String logMessages = fhirLogDao.findSuccessfulRequests(sessionId, start, end);

		LogEntriesDto logEntriesDto = new LogEntriesDto();
		logEntriesDto.setSessionId(sessionId);
		logEntriesDto.setMessageLog(logMessages);
		logEntriesDto.setType(LogEntriesDto.LogType.SUCCESSFUL);

		return logEntriesDto;
	}

	public LogEntriesDto getRequestErrorLog(String sessionId, Date start, Date end) {
		String logMessages = fhirLogDao.findRequestErrors(sessionId, start, end);

		LogEntriesDto logEntriesDto = new LogEntriesDto();
		logEntriesDto.setSessionId(sessionId);
		logEntriesDto.setMessageLog(logMessages);
		logEntriesDto.setType(LogEntriesDto.LogType.ERRORS);

		return logEntriesDto;
	}

	public Date extractTimestampFromACK(String ackMsg) {
		String tsPath = "/ACK/MSH/MSH.7";
		String ts = extractTimestampFromXML(ackMsg, tsPath);
		return T5FHIRUtils.convertHL7DateTypeToDate(ts);
	}

	public Date extractTimestampFromPCD(String pcdMsg) {
		String tsPath = "/PCD_01_Message/@timeStamp";
		String ts = extractTimestampFromXML(pcdMsg, tsPath);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.ISO_DATE_FORMAT).withZone(ZoneId.of("UTC"));
		ZonedDateTime zdt = ZonedDateTime.parse(ts, formatter);
		return Date.from(zdt.toInstant());
	}

	/**
	 * Extracts a timestamp with format yyyy-MM-dd'T'HH:mm:ss.SSS from a XML blob at a specified XPath location
	 * 
	 * @param xmlStr
	 *            xml string
	 * @param tsPath
	 *            path where to find the timestamp
	 * @return
	 */
	private String extractTimestampFromXML(String xmlStr, String tsPath) {
		if (StringUtils.isBlank(xmlStr)) {
			return null;
		}
		InputSource source = new InputSource(new StringReader(xmlStr));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document document = db.parse(source);

			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			return xpath.evaluate(tsPath, document);
		} catch (ParserConfigurationException e) {
			throw new XmlParsingException("Could not instantiate a DocumentBuilder", e);
		} catch (SAXException | IOException e) {
			logger.error("Unparsable XML string:\n" + xmlStr);
			throw new XmlParsingException("XML String could not be parsed to DOM.", e);
		} catch (XPathExpressionException e) {
			throw new XmlParsingException("The XPath '" + tsPath + "' is not valid", e);
		}
	}
}
