package net.sllmdilab.debugtool.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sllmdilab.commons.database.MLDBClient;
import net.sllmdilab.commons.util.T5FHIRUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceUseStatementDao {

	@Autowired
	private MLDBClient mldbClient;

	private String createFindActiveDeviceIdsForPatientXQuery(String patientId) {
		//@formatter:off
		String xQuery = 
			"xquery version \"1.0-ml\";\n"+
			"declare default element namespace 'http://hl7.org/fhir';\n"+
			"distinct-values(doc()/DeviceUseStatement[subject/reference/@value = 'Patient/" + StringEscapeUtils.escapeXml10(patientId)+"' and not(whenUsed/end)]/device/reference/@value)";
		//@formatter:on
		return xQuery;
	}

	private String createFindActiveDeviceIdsDuringPeriodForPatientXQuery(
			String patientId, Date start, Date end) {
		//@formatter:off
		String xQuery = 
			"xquery version \"1.0-ml\";\n" +
			"declare default element namespace 'http://hl7.org/fhir';\n" +
			"distinct-values(doc()/DeviceUseStatement[subject/reference/@value = " +
			"'Patient/" + StringEscapeUtils.escapeXml10(patientId) +
			"' and whenUsed/start < '" + T5FHIRUtils.convertDateToXMLType(end) + 
			"' and ( whenUsed/end > '" + T5FHIRUtils.convertDateToXMLType(start) +
			"' or not(whenUsed/end))]/device/reference/@value)";
		//@formatter:on
		return xQuery;
	}

	public List<String> findActiveDeviceIdsForPatient(String patientId) {
		return parseResult(mldbClient
				.sendQuery(createFindActiveDeviceIdsForPatientXQuery(patientId)));
	}

	public List<String> findActiveDeviceIdsDuringPeriodForPatient(
			String patientId, Date start, Date end) {
		return parseResult(mldbClient
				.sendQuery(createFindActiveDeviceIdsDuringPeriodForPatientXQuery(
						patientId, start, end)));
	}

	private List<String> parseResult(String result) {
		return Stream.of(result.split("(\n*)Device/"))
				.filter(s -> !s.isEmpty()).collect(Collectors.toList());
	}
}
