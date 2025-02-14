package nl.nn.adapterframework.extensions.cmis;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.nn.adapterframework.extensions.cmis.CmisSender.CmisAction;
import nl.nn.adapterframework.extensions.cmis.CmisSessionBuilder.BindingTypes;
import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.stream.UrlMessage;
import nl.nn.adapterframework.testutil.TestAssertions;
import nl.nn.adapterframework.testutil.TestFileUtils;
import nl.nn.adapterframework.util.StreamUtil;

public class TestCreateAction extends CmisSenderTestBase {
	private static final AtomicInteger atomicInt = new AtomicInteger();

	private static final String EMPTY_INPUT = "";
	private static final String EMPTY_RESULT = "[unknown]";

	private static final String INPUT = "<cmis><objectId>random</objectId><objectTypeId>cmis:document</objectTypeId><fileName>${filename}</fileName>"
			+ "<properties><property name=\"cmis:description\" type=\"string\">123456789</property>"
			+ "<property name=\"cmis:lastModificationDate\" type=\"datetime\">2019-02-26T16:31:15</property>"
			+ "<property name=\"cmis:creationDate\" type=\"boolean\">true</property></properties></cmis>";

	private static final String FILE_INPUT = "/fileInput.txt";

	private Message input;
	private String expectedResult;

	public static Stream<Arguments> allImplementations() {
		return Stream.of(
				Arguments.of(BindingTypes.ATOMPUB, EMPTY_INPUT),
				Arguments.of(BindingTypes.ATOMPUB, INPUT),
				Arguments.of(BindingTypes.WEBSERVICES, EMPTY_INPUT),
				Arguments.of(BindingTypes.WEBSERVICES, INPUT),
				Arguments.of(BindingTypes.BROWSER, EMPTY_INPUT),
				Arguments.of(BindingTypes.BROWSER, INPUT));
	}

	@ParameterizedTest(name = "{0} - {1}")
	@MethodSource("allImplementations")
	@Retention(RetentionPolicy.RUNTIME)
	private @interface TestAllImplementations {
	}

	public void setup(String input) {
		if(EMPTY_INPUT.equals(input)) {
			assumeTrue(STUBBED); //Only test empty named objects when stubbed
			this.expectedResult = EMPTY_RESULT;
			this.input = new Message(EMPTY_INPUT);
		} else {
			String filename = "/fileInput-"+atomicInt.getAndIncrement()+".txt";
			this.input = new Message(input.replace("${filename}", filename));
			this.expectedResult = filename;
		}
	}

	private void configure(BindingTypes bindingType) throws Exception {
		sender.setBindingType(bindingType);
		sender.setAction(CmisAction.CREATE);
		sender.configure();

		if(!STUBBED) {
			sender.open();
		}
	}

	private Message getTestFile(boolean base64Encoded) throws Exception {
		URL testFile = TestFileUtils.getTestFileURL(FILE_INPUT);
		assertNotNull(testFile);
		byte[] bytes = StreamUtil.streamToBytes(testFile.openStream());

		return new Message(base64Encoded ? Base64.encodeBase64(bytes) : bytes);
	}

	private String base64Decode(Message message) throws IOException {
		return new String(Base64.decodeBase64(message.asByteArray()));
	}

	@TestAllImplementations
	public void fileFromSessionKeyAsString(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fileContent", getTestFile(false).asString());
		sender.setFileSessionKey("fileContent");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileFromSessionKeyAsStringParameter(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fileContent", getTestFile(false).asString());
		Parameter fileSessionKey = new Parameter("fileSessionKey", "fileContent");
		sender.addParameter(fileSessionKey);
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileFromSessionKeyAsByteArray(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fileContent", getTestFile(false).asByteArray());
		sender.setFileSessionKey("fileContent");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileFromSessionKeyAsByteArrayParameter(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fileContent", getTestFile(false).asByteArray());
		Parameter fileSessionKey = new Parameter("fileSessionKey", "fileContent");
		sender.addParameter(fileSessionKey);
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileFromSessionKeyAsInputStream(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		URL testFile = TestFileUtils.getTestFileURL(FILE_INPUT);
		assertNotNull(testFile);

		session.put("fileContent", testFile.openStream());
		sender.setFileSessionKey("fileContent");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileFromSessionKeyAsInputStreamParameter(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		URL testFile = TestFileUtils.getTestFileURL(FILE_INPUT);
		assertNotNull(testFile);

		session.put("fileContent", testFile.openStream());
		Parameter fileSessionKey = new Parameter("fileSessionKey", "fileContent");
		sender.addParameter(fileSessionKey);
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations //should base64 decode the fileContent string
	public void fileContentFromSessionKeyAsString(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fileContent", getTestFile(true).asString());
		sender.setFileContentSessionKey("fileContent");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileContentFromSessionKeyAsByteArray(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fileContent", getTestFile(false).asByteArray());
		sender.setFileContentSessionKey("fileContent");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileContentFromSessionKeyAsInputStream(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		URL testFile = TestFileUtils.getTestFileURL(FILE_INPUT);
		assertNotNull(testFile);

		session.put("fileContent", testFile.openStream());
		sender.setFileContentSessionKey("fileContent");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileStreamFromSessionKeyAsString(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fis", getTestFile(false).asString());
		sender.setFileInputStreamSessionKey("fis");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileStreamFromSessionKeyAsByteArray(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		session.put("fis", getTestFile(false).asByteArray());
		sender.setFileInputStreamSessionKey("fis");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileStreamFromSessionKeyAsInputStream(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
		URL testFile = TestFileUtils.getTestFileURL(FILE_INPUT);
		assertNotNull(testFile);

		session.put("fis", testFile.openStream());
		sender.setFileInputStreamSessionKey("fis");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

	@TestAllImplementations
	public void fileStreamFromSessionKeyWithIllegalType(BindingTypes bindingType, String inputString) throws Exception {
		setup(inputString);
//		exception.expect(SenderException.class);
//		exception.expectMessage("expected InputStream, ByteArray or Base64-String but got");
		URL testFile = TestFileUtils.getTestFileURL(FILE_INPUT);
		assertNotNull(testFile);
		session.put("fis", new UrlMessage(testFile));
		sender.setFileInputStreamSessionKey("fis");
		configure(bindingType);

		Message actualResult = sender.sendMessageOrThrow(input, session);
		TestAssertions.assertEqualsIgnoreRNTSpace(expectedResult, base64Decode(actualResult));
	}

}
