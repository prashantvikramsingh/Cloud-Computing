import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.amazonaws.regions.Region;

import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import com.amazonaws.services.dynamodbv2.document.Item;

import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;

import com.amazonaws.services.dynamodbv2.document.Table;

import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

import com.amazonaws.services.simpleemail.model.*;


import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.UUID;


public class LogEvent implements RequestHandler<SNSEvent, Object> {


  AmazonDynamoDBClient clientdb = new AmazonDynamoDBClient();

  private DynamoDB dynamoDb;

  private String DYNAMODB_TABLE_NAME = clientdb.listTables().toString().substring(14, clientdb.listTables().toString().length() - 3);

  private Regions REGION = Regions.US_EAST_1;

  // Replace sender@example.com with your "From" address.

  // This address must be verified with Amazon SES.

  //AmazonSimpleEmailServiceClient amazonSimpleEmailServiceClient = new AmazonSimpleEmailServiceClient(new InstanceProfileCredentialsProvider());

  AmazonSimpleEmailService client1 = new AmazonSimpleEmailServiceClient();

  ListIdentitiesRequest request1 = new ListIdentitiesRequest();

  ListIdentitiesResult response = client1.listIdentities(request1);


  String FROM = "mymail@" + response.toString().substring(14, response.toString().length() - 3) + "";
  //String FROM = "vipul.krishna@csye6225-spring2018-krishnav.me";


  // Replace recipient@example.com with a "To" address. If your account

  // is still in the sandbox, this address must be verified.

  //static final String TO = "recipient@example.com";


  // The configuration set to use for this email. If you do not want to use a

  // configuration set, comment the following variable and the

  // .withConfigurationSetName(CONFIGSET); argument below.

  //static final String CONFIGSET = "ConfigSet";


  // The subject line for the email.

  static final String SUBJECT = "Password Reset Link";


  // The HTML body for the email.


  // The email body for recipients with non-HTML email clients.

  static final String TEXTBODY = "This email was sent through Amazon SES "

          + "using the AWS SDK for Java.";


  public Object handleRequest(SNSEvent request, Context context) {


    this.initDynamoDbClient();


    Table table = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME);


    String uuid = String.valueOf(UUID.randomUUID());

    String username = request.getRecords().get(0).getSNS().getMessage();

    Calendar cal = Calendar.getInstance(); //current date and time

    cal.add(Calendar.MINUTE, 20); //add days

    Long ttl = (cal.getTimeInMillis() / 1000L);

    Item i = table.getItem("id", username);

    if (i == null) {

      Item item = new Item()

              .withPrimaryKey("id", username)

              .withString("token", uuid)

              .withLong("ttl", ttl);

      PutItemOutcome outcome = table.putItem(item);


      final String HTMLBODY = "<h1>Password Reset Link</h1>"

              + "<p>http://" + response.toString().substring(14, response.toString().length() - 3) + "/reset?email=" + username + "&token=" + uuid;


      try {

        System.out.println("Fetching Message ---------------");
        System.out.println(request.getRecords().get(0).getSNS().getMessage());
        System.out.println("Fetching username ----------------");
        System.out.println(username);
        System.out.println("end --------");

        AmazonSimpleEmailService client =

                AmazonSimpleEmailServiceClientBuilder.standard()

                        // Replace US_WEST_2 with the AWS Region you're using for

                        // Amazon SES.

                        .withRegion(Regions.US_EAST_1).build();

        SendEmailRequest req = new SendEmailRequest()

                .withDestination(new Destination().withToAddresses(username))

                .withMessage(new Message()

                        .withBody(new Body()

                                .withHtml(new Content()

                                        .withCharset("UTF-8").withData(HTMLBODY))

                                .withText(new Content()

                                        .withCharset("UTF-8").withData(TEXTBODY)))

                        .withSubject(new Content()

                                .withCharset("UTF-8").withData(SUBJECT)))

                .withSource(FROM);

        // Comment or remove the next line if you are not using a

        // configuration set


        client.sendEmail(req);

        System.out.println("Email sent!");

      } catch (Exception ex) {

        System.out.println("The email was not sent. Error message: "

                + ex.getMessage());

      }


    } else {

      System.out.println("Inside else logic");

      return null;

    }


    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());


    context.getLogger().log("Invocation started: " + timeStamp);


    context.getLogger().log("Dynamo db name: " + DYNAMODB_TABLE_NAME);


    context.getLogger().log("Number of Records: " + (request.getRecords().size()));


    context.getLogger().log("Record message: " + request.getRecords().get(0).getSNS().getMessage());


    timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());


    context.getLogger().log("Invocation completed: " + timeStamp);


    return null;

  }


  public void initDynamoDbClient() {

    AmazonDynamoDBClient client = new AmazonDynamoDBClient();

    client.setRegion(Region.getRegion(REGION));


    this.dynamoDb = new DynamoDB(client);

  }

}





