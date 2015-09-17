package net.sllmdilab.debugtool.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sllmdilab.commons.database.MLDBClient;
import net.sllmdilab.commons.util.T5FHIRUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FhirLogDao {
	private Logger logger = LoggerFactory.getLogger(FhirLogDao.class);

	@Autowired
	public MLDBClient mldbClient;

	public String findAllRequests(String sessionId, Date start, Date end) {
		String response = mldbClient.sendQuery(createFindAllRequestsXQuery(sessionId, start, end));

		logger.debug("Response: " + response);

		return response;
	}
	
	private String createFindAllRequestsXQuery(String sessionId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $sessionId := '" + StringEscapeUtils.escapeXml10(sessionId) + "'\n"+
			"let $timeFrom := '" + T5FHIRUtils.convertDateToXMLType(start) + "'\n"+
			"let $timeUntil := '" + T5FHIRUtils.convertDateToXMLType(end) +"'\n"+
			"for $entry in /LogEntry[requestHeaders/requestHeader[@name = 'session-id']/value = $sessionId " +
				"and timeStamp >= $timeFrom and timeStamp <= $timeUntil]\n" +
			"order by $entry/timeStamp\n" +
			"return $entry\n";
		//@formatter:on
		return xQuery;
	}
	
	public String findSuccessfulRequests(String sessionId, Date start, Date end) {
		String response = mldbClient.sendQuery(createFindSuccessfulRequestsXQuery(sessionId, start, end));

		logger.debug("Response: " + response);

		return response;
	}

	private String createFindSuccessfulRequestsXQuery(String sessionId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $sessionId := '" + StringEscapeUtils.escapeXml10(sessionId) + "'\n"+
			"let $timeFrom := '" + T5FHIRUtils.convertDateToXMLType(start) + "'\n"+
			"let $timeUntil := '" + T5FHIRUtils.convertDateToXMLType(end) +"'\n"+
			"for $entry in /LogEntry[not(exception and exception != '') " +
				"and requestHeaders/requestHeader[@name = 'session-id']/value = $sessionId " +
				"and timeStamp >= $timeFrom and timeStamp <= $timeUntil]\n" +
			"order by $entry/timeStamp\n" +
			"return $entry\n";
		//@formatter:on
		return xQuery;
	}

	public int countSuccessfulRequests(String sessionId, Date start, Date end) {
		long startTime = System.currentTimeMillis();
		String response = mldbClient.sendQuery(createCountSuccessfulRequestsXQuery(sessionId, start, end));
		logger.debug("countSuccessfulRequests took " + (System.currentTimeMillis() - startTime));

		logger.debug("Response: " + response);

		return Integer.parseInt(response);
	}

	private String createCountSuccessfulRequestsXQuery(String sessionId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $sessionId := '" + StringEscapeUtils.escapeXml10(sessionId) + "'\n"+
			"let $timeFrom := '" + T5FHIRUtils.convertDateToXMLType(start) + "'\n"+
			"let $timeUntil := '" + T5FHIRUtils.convertDateToXMLType(end) +"'\n"+
			"return\n"+
			"count(/LogEntry[not(exception and exception != '') and requestHeaders/requestHeader[@name = 'session-id']/value = $sessionId and timeStamp >= $timeFrom and timeStamp <= $timeUntil])";
		//@formatter:on
		return xQuery;
	}

	public String findRequestErrors(String sessionId, Date start, Date end) {
		String response = mldbClient.sendQuery(createFindRequestErrorsXQuery(sessionId, start, end));

		logger.debug("Response: " + response);

		return response;
	}

	private String createFindRequestErrorsXQuery(String sessionId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $sessionId := '" + StringEscapeUtils.escapeXml10(sessionId) + "'\n"+
			"let $timeFrom := '" + T5FHIRUtils.convertDateToXMLType(start) + "'\n"+
			"let $timeUntil := '" + T5FHIRUtils.convertDateToXMLType(end) +"'\n"+
			"return\n"+
			"for $entry in /LogEntry[exception and exception != '' and requestHeaders/requestHeader[@name = 'session-id']/value = $sessionId and timeStamp >= $timeFrom and timeStamp <= $timeUntil]" +
			"order by $entry/timeStamp\n" +
			"return $entry\n";
		//@formatter:on
		return xQuery;
	}

	public int countRequestErrors(String sessionId, Date start, Date end) {
		long startTime = System.currentTimeMillis();
		String response = mldbClient.sendQuery(createCountRequestErrorsXQuery(sessionId, start, end));
		logger.debug("countRequestErrors took " + (System.currentTimeMillis() - startTime));

		logger.debug("Response: " + response);

		return Integer.parseInt(response);
	}

	private String createCountRequestErrorsXQuery(String sessionId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $sessionId := '" + StringEscapeUtils.escapeXml10(sessionId) + "'\n"+
			"let $timeFrom := '" + T5FHIRUtils.convertDateToXMLType(start) + "'\n"+
			"let $timeUntil := '" + T5FHIRUtils.convertDateToXMLType(end) +"'\n"+
			"return\n"+
			"count(/LogEntry[exception and exception != '' and requestHeaders/requestHeader[@name = 'session-id']/value  = $sessionId and timeStamp >= $timeFrom and timeStamp <= $timeUntil])";
		//@formatter:on
		return xQuery;
	}

	public List<String> findAllSessionsForPatient(String patientId, Date start, Date end) {
		long startTime = System.currentTimeMillis();
		String response = mldbClient.sendQuery(createFindAllSessionsForPatientXQuery(patientId, start, end));
		logger.debug("findAllSessionsForPatient took " + (System.currentTimeMillis() - startTime));
		
		logger.debug("Response: " + response);

		return parseResult(response);
	}

	private String createFindAllSessionsForPatientXQuery(String patientId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $patientId := '" + StringEscapeUtils.escapeXml10(patientId) + "'\n"+
			"let $timeFrom := '" + T5FHIRUtils.convertDateToXMLType(start) + "'\n"+
			"let $timeUntil := '" + T5FHIRUtils.convertDateToXMLType(end) +"'\n"+
			"return\n"+
			"distinct-values(doc()/LogEntry[timeStamp >= $timeFrom and timeStamp <= $timeUntil and " +
				"(queryParameters/queryParameter[@name = 'patient'] = $patientId " +
				"or queryParameters/queryParameter[@name = 'subject'] = $patientId " +
				"or queryParameters/queryParameter[@name = 'patient.identifier'] = $patientId ) ]" + 
					"/requestHeaders/requestHeader[@name = 'session-id']/data(value))";
		//@formatter:on
		return xQuery;
	}

	public String findClientVersionForSession(String sessionId) {
		long startTime = System.currentTimeMillis();
		String response = mldbClient.sendQuery(createFindClientVersionForSession(sessionId));
		logger.debug("findClientVersionForSession took " + (System.currentTimeMillis() - startTime));

		logger.debug("Response: " + response);

		return response;
	}

	private String createFindClientVersionForSession(String sessionId) {
		//@formatter:off
		String xQuery = 
			"let $sessionId := '" + StringEscapeUtils.escapeXml10(sessionId) + "'\n" +
			"let $versions := doc()/LogEntry[requestHeaders/requestHeader[@name = 'session-id']/value = $sessionId ]/requestHeaders/requestHeader[@name = 'client-version']/data(value)\n" +
			"for $version in $versions[position() lt 2]\n" +
			"return $version";
		//@formatter:on
		return xQuery;
	}

	private List<String> parseResult(String result) {
		return Stream.of(result.split("\n")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
	}
}
