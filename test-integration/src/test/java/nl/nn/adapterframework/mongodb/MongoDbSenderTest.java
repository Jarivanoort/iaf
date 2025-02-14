package nl.nn.adapterframework.mongodb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.hamcrest.core.StringContains;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.mongodb.MongoDbSender.MongoAction;
import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.senders.SenderTestBase;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.stream.document.DocumentFormat;

public class MongoDbSenderTest extends SenderTestBase<MongoDbSender> {

	private String host="localhost";
	private String user="testiaf_user";
	private String password="testiaf_user00";
	private String database="testdb";
	private String collection="Students";

	private JndiMongoClientFactory mongoClientFactory;

	@Override
	public void setUp() throws Exception {
		String url = "mongodb://"+ user+":"+password+"@" + host;
		MongoClient mongoClient = MongoClients.create(url);
		mongoClientFactory = new JndiMongoClientFactory();
		mongoClientFactory.add(mongoClient, JndiMongoClientFactory.GLOBAL_DEFAULT_DATASOURCE_NAME);
		super.setUp();
	}


	@Override
	public MongoDbSender createSender() throws Exception {
		MongoDbSender result = new MongoDbSender();
		result.setMongoClientFactory(mongoClientFactory);
		result.setDatabase(database);
		result.setCollection(collection);
		return result;
	}

	@Test
	public void testConfigure() throws ConfigurationException {
		sender.setAction(MongoAction.FINDONE);
		sender.configure();
	}

	@Test
	public void testOpen() throws Exception {
		sender.setAction(MongoAction.FINDONE);
		sender.configure();
		sender.open();
	}

	@Test
	public void testInsertOne() throws Exception {
		sender.setAction(MongoAction.INSERTONE);
		sender.setCollection("Students");
		sender.configure();
		sender.open();

		JsonObject stud = createStudent("Evert","1c", 4,4,3);
		Message result = sendMessage(stud.toString());
		System.out.println(result.asString());
		assertThat(result.asString(),StringContains.containsString("\"insertedId\":"));
	}

	@Test
	public void testInsertOneNoObjectId() throws Exception {
		sender.setAction(MongoAction.INSERTONE);
		sender.setCollection("Students");
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"KarelV\", \"class_id\": \"first\", \"grades\": [ 4, 5, 6] }");
		System.out.println(result.asString());
		assertThat(result.asString(),StringContains.containsString("\"insertedId\":"));
	}

	@Test
	public void testInsertMany() throws Exception {
		sender.setAction(MongoAction.INSERTMANY);
		sender.setCollection("Students");
		sender.configure();
		sender.open();

		JsonArrayBuilder students = Json.createArrayBuilder();
		students.add(createStudent("Harry","1a", 4,5,6));
		students.add(createStudent("Klaas","1b", 5,7,9));
		Message result = sendMessage(students.build().toString());
		assertThat(result.asString(),StringContains.containsString("\"insertedIds\":"));
	}

	@Test
	public void testFindOne() throws Exception {
		sender.setAction(MongoAction.FINDONE);
		sender.setCollection("Students");
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"Evert\" }");
		System.out.println("FindOne: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("\"student_id\":\"Evert\",\"class_id\":\"1c\""));
	}

	@Test
	public void testFindOneXml() throws Exception {
		sender.setAction(MongoAction.FINDONE);
		sender.setCollection("Students");
		sender.setOutputFormat(DocumentFormat.XML);
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"Evert\" }");
		System.out.println("FindOne: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("<student_id>Evert</student_id><class_id>1c</class_id><scores><item>4</item><item>4</item><item>3</item></scores><seatno>10</seatno>"));
	}

	@Test
	public void testFindMany() throws Exception {
		sender.setAction(MongoAction.FINDMANY);
		sender.setCollection("Students");
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"Evert\" }");
//		System.out.println("FindMany: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("\"student_id\":\"Evert\",\"class_id\":\"1c\""));
	}

	@Test
	public void testFindManyXml() throws Exception {
		sender.setAction(MongoAction.FINDMANY);
		sender.setCollection("Students");
		sender.setOutputFormat(DocumentFormat.XML);
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"Evert\" }");
		System.out.println("FindManyXml: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("<student_id>Evert</student_id><class_id>1c</class_id><scores><item>4</item><item>4</item><item>3</item></scores><seatno>10</seatno></item><item>"));
	}

	@Test
	public void testFindManyUsingParameter() throws Exception {
		sender.setAction(MongoAction.FINDMANY);
		sender.setCollection("Students");
		Parameter param = new Parameter();
		param.setName("searchTarget");
		param.setValue("Evert");
		sender.addParameter(param);
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"?{searchTarget}\" }");
//		System.out.println("FindMany: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("\"student_id\":\"Evert\",\"class_id\":\"1c\""));
	}

	@Test
	public void testFindManyCountOnly() throws Exception {
		sender.setAction(MongoAction.FINDMANY);
		sender.setCollection("Students");
		sender.setCountOnly(true);
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"Evert\" }");
		System.out.println("FindMany: ["+result.asString()+"]");
		int count = Integer.parseInt(result.asString());
		assertTrue(count>0);
	}

	@Test
	public void testFindManyLimit() throws Exception {
		sender.setAction(MongoAction.FINDMANY);
		sender.setCollection("Students");
		sender.setCountOnly(true);
		sender.setLimit(1);
		sender.configure();
		sender.open();

		Message result = sendMessage("{ \"student_id\": \"Evert\" }");
		System.out.println("FindMany: ["+result.asString()+"]");
		int count = Integer.parseInt(result.asString());
		assertTrue(count==1);
	}
	@Test
	public void testUpdateOne() throws Exception {
		String filter = "{ \"student_id\": \"Evert\" }";
		String update = "{\"$set\": {\"seatno\":"+10+"}}";

		sender.setAction(MongoAction.UPDATEONE);
		sender.setCollection("Students");
		sender.setFilter(filter);
		sender.configure();
		sender.open();

		Message result = sendMessage(update);
		System.out.println("UpdateOne: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("\"modifiedCount\":"));
	}

	@Test
	public void testUpdateMany() throws Exception {
		String filter = "{ \"student_id\": \"Evert\" }";
		String update = "{\"$set\": {\"seatno\":"+10+"}}";

		sender.setAction(MongoAction.UPDATEMANY);
		sender.setCollection("Students");
		sender.setFilter(filter);
		sender.configure();
		sender.open();

		Message result = sendMessage(update);
		System.out.println("UpdateMany: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("\"modifiedCount\":"));
	}

	@Test
	public void testUpdateManyXml() throws Exception {
		String filter = "{ \"student_id\": \"Evert\" }";
		String update = "{\"$set\": {\"seatno\":"+10+"}}";

		sender.setAction(MongoAction.UPDATEMANY);
		sender.setCollection("Students");
		sender.setFilter(filter);
		sender.setOutputFormat(DocumentFormat.XML);
		sender.configure();
		sender.open();

		Message result = sendMessage(update);
		System.out.println("UpdateMany: ["+result.asString()+"]");
		assertThat(result.asString(),StringContains.containsString("<modifiedCount>"));
	}

	public JsonObject createStudent(String studentId, String classId, Integer... grades) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("student_id", studentId).add("class_id", classId);
		builder.add("scores", getScores(grades));
		JsonObject doc = builder.build();
		return doc;
	}

	public JsonArray getScores(Integer... grades) {
		JsonArrayBuilder scores = Json.createArrayBuilder();
		for (int grade: grades) {
			scores.add(grade);
		}
		return scores.build();
	}

}
