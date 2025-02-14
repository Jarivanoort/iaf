package nl.nn.adapterframework.extensions.cmis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.nn.adapterframework.extensions.cmis.CmisSender.CmisAction;
import nl.nn.adapterframework.extensions.cmis.CmisSessionBuilder.BindingTypes;
import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.testutil.TestAssertions;

public class TestGetAction extends CmisSenderTestBase {

	private static final Message INPUT_WITH_PROPERTIES = new Message("<cmis><id>id</id><objectId>dummy</objectId>"
			+ "<objectTypeId>cmis:document</objectTypeId><fileName>fileInput.txt</fileName>"
			+ "<properties><property name=\"cmis:description\" type=\"string\">123456789</property>"
			+ "<property name=\"cmis:lastModificationDate\" type=\"datetime\">2019-02-26T16:31:15</property>"
			+ "<property name=\"cmis:creationDate\" type=\"boolean\">true</property></properties></cmis>");
	
	private static final String GET_RESULT_FOR_INPUT= "dummy_stream";

	private static final String GET_RESULT_TO_SERVLET= null;

	private static final String GET_RESULT_FOR_GET_PROPERTIES = "<cmis><properties>"
			+ "<property name=\"cmis:name\" type=\"id\">dummy</property>"
			+ "<property name=\"project:number\" type=\"integer\">123456789</property>"
			+ "<property name=\"project:lastModified\" type=\"datetime\">2019-02-26T16:31:15</property>"
			+ "<property name=\"project:onTime\" type=\"boolean\">true</property></properties></cmis>";

	public static Stream<Arguments> allImplementations() {
		return Stream.of(
				Arguments.of(BindingTypes.ATOMPUB, GET_RESULT_FOR_INPUT, false, false, false),
				Arguments.of(BindingTypes.ATOMPUB, GET_RESULT_TO_SERVLET, true, false, false),
				Arguments.of(BindingTypes.ATOMPUB, GET_RESULT_FOR_GET_PROPERTIES, false, true, false),
				Arguments.of(BindingTypes.ATOMPUB, GET_RESULT_FOR_GET_PROPERTIES, false, true, true),

				Arguments.of(BindingTypes.WEBSERVICES, GET_RESULT_FOR_INPUT, false, false, false),
				Arguments.of(BindingTypes.WEBSERVICES, GET_RESULT_TO_SERVLET, true, false, false),
				Arguments.of(BindingTypes.WEBSERVICES, GET_RESULT_FOR_GET_PROPERTIES, false, true, false),
				Arguments.of(BindingTypes.WEBSERVICES, GET_RESULT_FOR_GET_PROPERTIES, false, true, true),

				Arguments.of(BindingTypes.BROWSER, GET_RESULT_FOR_INPUT, false, false, false),
				Arguments.of(BindingTypes.BROWSER, GET_RESULT_TO_SERVLET, true, false, false),
				Arguments.of(BindingTypes.BROWSER, GET_RESULT_FOR_GET_PROPERTIES, false, true, false),
				Arguments.of(BindingTypes.BROWSER, GET_RESULT_FOR_GET_PROPERTIES, false, true, true)
		);
	}

	@ParameterizedTest(name = "{0} - {1} - toServlet = {2} - getProperties = {3} - getDocumentContent = {4}")
	@MethodSource("allImplementations")
	@Retention(RetentionPolicy.RUNTIME)
	private @interface TestAllImplementations {
	}

	private void configure(BindingTypes bindingType, Boolean resultToServlet, Boolean getProperties, Boolean getDocumentContent) throws Exception {
		sender.setGetProperties(getProperties);
		sender.setGetDocumentContent(getDocumentContent);

		sender.setStreamResultToServlet(resultToServlet);
		sender.setBindingType(bindingType);
		sender.setAction(CmisAction.GET);
		sender.configure();

		if(!STUBBED) {
			sender.open();
		}
	}

	@TestAllImplementations
	public void sendMessageFileStream(BindingTypes bindingType, String expectedResult, Boolean resultToServlet, Boolean getProperties, Boolean getDocumentContent) throws Exception {
		sender.setFileInputStreamSessionKey("fis");
		configure(bindingType, resultToServlet, getProperties, getDocumentContent);

		String actualResult = sender.sendMessageOrThrow(INPUT_WITH_PROPERTIES, session).asString();
		if(!getProperties && !resultToServlet) {
			assertNull(actualResult);
		} else {
			TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, actualResult);
		}

		Message stream = session.getMessage(sender.getFileSessionKey());
		if((getProperties && getDocumentContent) || (!getProperties && !resultToServlet)) {
			assertEquals(GET_RESULT_FOR_INPUT, stream.asString());
		} else {
			assertTrue(stream.isNull());
		}
	}

	@TestAllImplementations
	public void sendMessageStreamResult(BindingTypes bindingType, String expectedResult, Boolean resultToServlet, Boolean getProperties, Boolean getDocumentContent) throws Exception {
		sender.setBindingType(bindingType);
		sender.setAction(CmisAction.GET);
		sender.configure();

		Message actualResult = sender.sendMessageOrThrow(INPUT_WITH_PROPERTIES, session);
		assertInstanceOf(InputStream.class, actualResult.asObject());
		assertEquals(GET_RESULT_FOR_INPUT, actualResult.asString());
	}

	@TestAllImplementations
	public void sendMessageFileContentSessionKey(BindingTypes bindingType, String expectedResult, Boolean resultToServlet, Boolean getProperties, Boolean getDocumentContent) throws Exception {
		sender.setFileContentSessionKey("fileContent");
		configure(bindingType, resultToServlet, getProperties, getDocumentContent);

		String actualResult = sender.sendMessageOrThrow(INPUT_WITH_PROPERTIES, session).asString();
		if(!getProperties && !resultToServlet) {
			assertNull(actualResult);
		} else {
			TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, actualResult);
		}

		String base64Content = (String) session.get(sender.getFileSessionKey());
		if((getProperties && getDocumentContent) || (!getProperties && !resultToServlet)) {
			assertEquals("ZHVtbXlfc3RyZWFt", base64Content);
		} else {
			assertNull(base64Content);
		}
	}

	@TestAllImplementations
	public void sendMessageFileStreamWithParameters(BindingTypes bindingType, String expectedResult, Boolean resultToServlet, Boolean getProperties, Boolean getDocumentContent) throws Exception {
		sender.setFileInputStreamSessionKey("fis");
		sender.addParameter(new Parameter("getProperties", getProperties.toString()));
		sender.addParameter(new Parameter("getDocumentContent", getDocumentContent.toString()));
		configure(bindingType, resultToServlet, getProperties, getDocumentContent);

		String actualResult = sender.sendMessageOrThrow(INPUT_WITH_PROPERTIES, session).asString();
		if(!getProperties && !resultToServlet) {
			assertNull(actualResult);
		} else {
			TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, actualResult);
		}

		Message stream = session.getMessage(sender.getFileSessionKey());
		if((getProperties && getDocumentContent) || (!getProperties && !resultToServlet)) {
			assertEquals(GET_RESULT_FOR_INPUT, stream.asString());
		} else {
			assertTrue(stream.isNull());
		}
	}

	@TestAllImplementations
	public void sendMessageFileContentWithParameters(BindingTypes bindingType, String expectedResult, Boolean resultToServlet, Boolean getProperties, Boolean getDocumentContent) throws Exception {
		sender.setFileContentSessionKey("fileContent");
		sender.addParameter(new Parameter("getProperties", getProperties.toString()));
		sender.addParameter(new Parameter("getDocumentContent", getDocumentContent.toString()));

		configure(bindingType, resultToServlet, getProperties, getDocumentContent);

		String actualResult = sender.sendMessageOrThrow(INPUT_WITH_PROPERTIES, session).asString();
		if(!getProperties && !resultToServlet) {
			assertNull(actualResult);
		} else {
			TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, actualResult);
		}

		String base64Content = (String) session.get(sender.getFileSessionKey());
		if((getProperties && getDocumentContent) || (!getProperties && !resultToServlet)) {
			assertEquals("ZHVtbXlfc3RyZWFt", base64Content);
		} else {
			assertNull(base64Content);
		}
	}
}
