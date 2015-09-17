package net.sllmdilab.debugtool.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sllmdilab.commons.util.Constants;
import net.sllmdilab.debugtool.dao.DeviceUseStatementDao;
import net.sllmdilab.debugtool.dao.FhirLogDao;
import net.sllmdilab.debugtool.dao.T5MessagesDao;
import net.sllmdilab.debugtool.dto.ClientStatusSummaryDto;
import net.sllmdilab.debugtool.dto.DeviceStatusSummary;
import net.sllmdilab.debugtool.dto.LogEntriesDto;
import net.sllmdilab.debugtool.dto.StatusSummaryDto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DebugServiceTest {

	private static final int MOCK_NUM_FAILED_REQUESTS = 95;
	private static final int MOCK_NUM_REQUESTS = 78;
	private static final int MOCK_NUM_ERROR_MESSAGES = 99;
	private static final int MOCK_NUM_MESSAGES = 42;
	private static final String MOCK_PATIENT_ID = "121212121212";
	private static final String MOCK_DEVICE2 = "device2";
	private static final String MOCK_DEVICE1 = "device1";
	private static final String MOCK_SESSION1 = "session1";
	private static final String MOCK_CLIENT_VERSION = "client1";
	private static final String MOCK_SUCCESS_MESSAGE_LOG = "<PCD_01_Message id='5811120104160ee' timeStamp='2015-08-11T12:01:01.000'></PCD_01_Message>";
	private static final String MOCK_SUCCESS_MESSAGE_LOG_2 = "<PCD_01_Message id='5811120104160ee' timeStamp='2015-08-11T12:01:03.000'></PCD_01_Message>";
	//@formatter:off 
	// This one is taken from HL7 XML Encoding Rules for HL7v2 Messages with a modified date format.
	private static final String MOCK_ERROR_MESSAGE_LOG = "<ACK "
			+ "xmlns='urn:hl7-org:v2xml' "
			+ "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
			+ "xsi:schemaLocation='urn:hl7-org:v2xml "
			+ "ACK.xsd'>"
			+ 	"<MSH>"
			+ 		"<MSH.1>|</MSH.1>"
			+ 		"<MSH.2>^~\\&amp;</MSH.2>"
			+ 		"<MSH.3><HD.1>LAB</HD.1></MSH.3>"
			+ 		"<MSH.4><HD.1>767543</HD.1></MSH.4>"
			+ 		"<MSH.5><HD.1>ADT</HD.1></MSH.5>"
			+ 		"<MSH.6><HD.1>767543</HD.1></MSH.6>"
			+ 		"<MSH.7>20150811120102.000</MSH.7>"
			+ 	"</MSH>"
			+ "</ACK>";
	//@formatter:on
	private Date start = new Date(10000);
	private Date end = new Date(100000);
	
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.ISO_DATE_FORMAT).withZone(ZoneId.of("UTC"));

	@Mock
	private T5MessagesDao messagesDao;

	@Mock
	private FhirLogDao fhirLogDao;

	@Mock
	private DeviceUseStatementDao deviceUseStatementDao;

	@InjectMocks
	private DebugService debugService;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void statusSummaryIsReturned() {
		when(deviceUseStatementDao.findActiveDeviceIdsDuringPeriodForPatient(any(), any(), any())).thenReturn(
				Arrays.asList(MOCK_DEVICE1, MOCK_DEVICE2));

		when(fhirLogDao.findAllSessionsForPatient(any(), any(), any())).thenReturn(Arrays.asList(MOCK_SESSION1));

		when(messagesDao.countSuccessfulMessages(any(), any(), any())).thenReturn(MOCK_NUM_MESSAGES);
		when(messagesDao.countErrorMessages(any(), any(), any())).thenReturn(MOCK_NUM_ERROR_MESSAGES);

		when(fhirLogDao.countSuccessfulRequests(any(), any(), any())).thenReturn(MOCK_NUM_REQUESTS);
		when(fhirLogDao.countRequestErrors(any(), any(), any())).thenReturn(MOCK_NUM_FAILED_REQUESTS);
		when(fhirLogDao.findClientVersionForSession(any())).thenReturn(MOCK_CLIENT_VERSION);

		StatusSummaryDto statusSummary = debugService.getStatusSummary(MOCK_PATIENT_ID, start, end);

		verify(deviceUseStatementDao).findActiveDeviceIdsDuringPeriodForPatient(MOCK_PATIENT_ID, start, end);

		verify(messagesDao, times(2)).countSuccessfulMessages(any(), eq(start), eq(end));
		verify(messagesDao, times(2)).countErrorMessages(any(), eq(start), eq(end));

		List<DeviceStatusSummary> deviceSummaries = statusSummary.getDeviceStatusSummaries();
		assertEquals(2, deviceSummaries.size());

		assertEquals(MOCK_DEVICE1, deviceSummaries.get(0).getDeviceId());
		assertEquals(MOCK_NUM_MESSAGES, deviceSummaries.get(0).getMessageCount());
		assertEquals(MOCK_NUM_ERROR_MESSAGES, deviceSummaries.get(0).getErrorMessageCount());

		verify(fhirLogDao, times(1)).countSuccessfulRequests(any(), eq(start), eq(end));
		verify(fhirLogDao, times(1)).countRequestErrors(any(), eq(start), eq(end));

		List<ClientStatusSummaryDto> clientSummaries = statusSummary.getClientStatusSummaries();
		assertEquals(1, clientSummaries.size());

		assertEquals(MOCK_SESSION1, clientSummaries.get(0).getSessionId());
		assertEquals(MOCK_NUM_REQUESTS, clientSummaries.get(0).getMessageCount());
		assertEquals(MOCK_NUM_FAILED_REQUESTS, clientSummaries.get(0).getErrorMessageCount());
		assertEquals(MOCK_CLIENT_VERSION, clientSummaries.get(0).getClientVersion());
	}

	@Test
	public void deviceLogMessagesAreReturned() {
		when(messagesDao.findSuccessfulMessages(any(), any(), any())).thenReturn(MOCK_SUCCESS_MESSAGE_LOG);

		LogEntriesDto deviceLogMessagesDto = debugService.getSuccessfulMessageLog(MOCK_DEVICE1, start, end);

		verify(messagesDao).findSuccessfulMessages(eq(MOCK_DEVICE1), eq(start), eq(end));

		assertEquals(deviceLogMessagesDto.getMessageLog(), MOCK_SUCCESS_MESSAGE_LOG);
		assertEquals(deviceLogMessagesDto.getDeviceId(), MOCK_DEVICE1);
		assertEquals(LogEntriesDto.LogType.SUCCESSFUL, deviceLogMessagesDto.getType());
	}

	@Test
	public void sessionLogMessagesAreReturned() {
		when(fhirLogDao.findSuccessfulRequests(any(), any(), any())).thenReturn(MOCK_SUCCESS_MESSAGE_LOG);

		LogEntriesDto deviceLogMessagesDto = debugService.getSuccessfulRequestLog(MOCK_SESSION1, start, end);

		verify(fhirLogDao).findSuccessfulRequests(eq(MOCK_SESSION1), eq(start), eq(end));

		assertEquals(deviceLogMessagesDto.getMessageLog(), MOCK_SUCCESS_MESSAGE_LOG);
		assertEquals(deviceLogMessagesDto.getSessionId(), MOCK_SESSION1);
		assertEquals(LogEntriesDto.LogType.SUCCESSFUL, deviceLogMessagesDto.getType());
	}

	@Test
	public void deviceErrorLogMessagesAreReturned() {
		when(messagesDao.findErrorAckMessages(any(), any(), any())).thenReturn(MOCK_ERROR_MESSAGE_LOG);

		LogEntriesDto deviceLogMessagesDto = debugService.getErrorMessageLog(MOCK_DEVICE1, start, end);

		verify(messagesDao).findErrorAckMessages(eq(MOCK_DEVICE1), eq(start), eq(end));

		assertEquals(deviceLogMessagesDto.getMessageLog(), MOCK_ERROR_MESSAGE_LOG);
		assertEquals(deviceLogMessagesDto.getDeviceId(), MOCK_DEVICE1);
		assertEquals(LogEntriesDto.LogType.ERRORS, deviceLogMessagesDto.getType());
	}

	@Test
	public void sessionLogErrorMessagesAreReturned() {
		when(fhirLogDao.findRequestErrors(any(), any(), any())).thenReturn(MOCK_SUCCESS_MESSAGE_LOG);

		LogEntriesDto deviceLogMessagesDto = debugService.getRequestErrorLog(MOCK_SESSION1, start, end);

		verify(fhirLogDao).findRequestErrors(eq(MOCK_SESSION1), eq(start), eq(end));

		assertEquals(deviceLogMessagesDto.getMessageLog(), MOCK_SUCCESS_MESSAGE_LOG);
		assertEquals(deviceLogMessagesDto.getSessionId(), MOCK_SESSION1);
		assertEquals(LogEntriesDto.LogType.ERRORS, deviceLogMessagesDto.getType());
	}

	@Test
	public void shouldExtractTimestampFromPCD() throws ParseException {
		Date actualTs = debugService.extractTimestampFromPCD(MOCK_SUCCESS_MESSAGE_LOG);
		ZonedDateTime zdt = ZonedDateTime.parse("2015-08-11T12:01:01.000", dtf);
		assertEquals(zdt.toInstant().toEpochMilli(), actualTs.toInstant().toEpochMilli());
	}

	@Test
	public void shouldExtractTimestampFromACK() throws ParseException {
		Date actualTs = debugService.extractTimestampFromACK(MOCK_ERROR_MESSAGE_LOG);
		ZonedDateTime zdt = ZonedDateTime.parse("2015-08-11T12:01:02.000", dtf);
		assertEquals(zdt.toInstant().toEpochMilli(), actualTs.toInstant().toEpochMilli());
	}

	@Test
	public void successAndErrorMessagesShouldBeCorrectlyMerged() throws ParseException {
		String mockSuccessMsg = String.join("\n", MOCK_SUCCESS_MESSAGE_LOG, MOCK_SUCCESS_MESSAGE_LOG_2);

		when(messagesDao.findSuccessfulMessages(any(), any(), any())).thenReturn(mockSuccessMsg);
		when(messagesDao.findErrorAckMessages(any(), any(), any())).thenReturn(MOCK_ERROR_MESSAGE_LOG);

		LogEntriesDto allMessages = debugService.getAllMessages(MOCK_DEVICE1, start, end);

		String expectedMsgs = String.join("\n", MOCK_SUCCESS_MESSAGE_LOG, MOCK_ERROR_MESSAGE_LOG, MOCK_SUCCESS_MESSAGE_LOG_2);
		String actualMsgs = allMessages.getMessageLog();

		assertEquals(expectedMsgs, actualMsgs);
	}
}
