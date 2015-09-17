package net.sllmdilab.debugtool.dao;

import java.util.Date;

import net.sllmdilab.commons.database.MLDBClient;
import net.sllmdilab.commons.util.T5FHIRUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class T5MessagesDao {
	private Logger logger = LoggerFactory.getLogger(T5MessagesDao.class);

	@Autowired
	private MLDBClient mldbClient;

	private String createFindSuccessfulMessagesXQuery(String deviceId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $deviceId := '" + StringEscapeUtils.escapeXml10(deviceId) + "'\n"+
			"let $timeFrom := xs:dateTime('" + T5FHIRUtils.convertDateToXMLType(start) + "')\n"+
			"let $timeUntil := xs:dateTime('" + T5FHIRUtils.convertDateToXMLType(end) +"')\n" +
			"for $msg in /PCD_01_Message[//Observation/EquipmentIdentifier = $deviceId and @timeStamp >= $timeFrom and @timeStamp <= $timeUntil]\n"+
			"order by $msg/@timeStamp\n" +
			"return $msg";
		//@formatter:on
		return xQuery;
	}

	private String createCountSuccessfulMessagesXQuery(String deviceId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"let $deviceId := '" + StringEscapeUtils.escapeXml10(deviceId) + "'\n"+
			"let $timeFrom := xs:dateTime('" + T5FHIRUtils.convertDateToXMLType(start) + "')\n"+
			"let $timeUntil := xs:dateTime('" + T5FHIRUtils.convertDateToXMLType(end) +"')\n"+
			"return\n"+
			"count(doc()/PCD_01_Message[//Observation/EquipmentIdentifier = $deviceId and @timeStamp >= $timeFrom and @timeStamp <= $timeUntil])";
		//@formatter:on
		return xQuery;
	}

	private String createFindErrorMessagesXQuery(String deviceId, Date start, Date end) {
		//@formatter:off
			String xQuery =
				"declare default element namespace 'urn:hl7-org:v2xml';\n"+
				"let $deviceId := '" + StringEscapeUtils.escapeXml10(deviceId) + "'\n"+
				"let $timeFrom := '" + T5FHIRUtils.convertDateToHL7Type(start) + "'\n"+
				"let $timeUntil := '" + T5FHIRUtils.convertDateToHL7Type(end) + "'\n"+
				"for $ack in /ACK[(MSA/MSA.1 = 'AR' or MSA/MSA.1 = 'AE' ) and ERR/ERR.7 and ERR/ERR.4 = 'I' and ERR/ERR.3/CWE.2 = 'Device ID' and ERR/ERR.7 = $deviceId and MSH/MSH.7 >= $timeFrom and MSH/MSH.7 <= $timeUntil ]\n"+
				"order by $ack/MSH/MSH.7\n" +
				"return $ack\n";
			//@formatter:on
		return xQuery;
	}

	private String createCountErrorMessagesXQuery(String deviceId, Date start, Date end) {
		//@formatter:off
			String xQuery =
				"declare default element namespace 'urn:hl7-org:v2xml';\n"+
				"let $deviceId := '" + StringEscapeUtils.escapeXml10(deviceId) + "'\n"+
				"let $timeFrom := '" + T5FHIRUtils.convertDateToHL7Type(start) + "'\n"+
				"let $timeUntil := '" + T5FHIRUtils.convertDateToHL7Type(end) + "'\n"+
				"return\n"+
				"count(/ACK[(MSA/MSA.1 = 'AR' or MSA/MSA.1 = 'AE' ) and ERR/ERR.7 and ERR/ERR.4 = 'I' and ERR/ERR.3/CWE.2 = 'Device ID' and ERR/ERR.7 = $deviceId and MSH/MSH.7 >= $timeFrom and MSH/MSH.7 <= $timeUntil ])";
		//@formatter:on
		return xQuery;
	}

	public String findSuccessfulMessages(String deviceId, Date start, Date end) {
		String response = mldbClient.sendQuery(createFindSuccessfulMessagesXQuery(deviceId, start, end));

		logger.debug("Response: " + response);

		return response;
	}

	public int countSuccessfulMessages(String deviceId, Date start, Date end) {
		long startTime = System.currentTimeMillis();
		String response = mldbClient.sendQuery(createCountSuccessfulMessagesXQuery(deviceId, start, end));
		logger.debug("countSuccessfulMessages took " + (System.currentTimeMillis() - startTime));
		logger.debug("Response: " + response);

		return Integer.parseInt(response);
	}

	public String findErrorAckMessages(String deviceId, Date start, Date end) {
		String response = mldbClient.sendQuery(createFindErrorMessagesXQuery(deviceId, start, end));

		logger.debug("Response: " + response);

		return response;
	}

	public int countErrorMessages(String deviceId, Date start, Date end) {
		long startTime = System.currentTimeMillis();
		String response = mldbClient.sendQuery(createCountErrorMessagesXQuery(deviceId, start, end));
		logger.debug("countErrorMessages took " + (System.currentTimeMillis() - startTime));

		logger.debug("Response: " + response);

		return Integer.parseInt(response);
	}
}
