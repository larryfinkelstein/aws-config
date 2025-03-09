import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, MockitoSugar}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentialsProvider}
import software.amazon.awssdk.regions.providers.AwsRegionProvider
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.{DescribeInstancesResponse, DescribeRegionsResponse, Instance, InstanceState, Reservation, Tag}
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.{GetSecretValueRequest, GetSecretValueResponse}
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.{GetParameterRequest, GetParameterResponse}

class AWSUtilTest extends AnyFlatSpec with MockitoSugar {

  "getRegion" should "return a mock Region" in {
    // Mock credentials provider
    val mockCredentialsProvider = mock[AwsCredentialsProvider]
    when(mockCredentialsProvider.resolveCredentials())
      .thenReturn(AwsBasicCredentials.create("mock-access-key", "mock-secret-key"))

    // Mock region provider
    val mockRegionProvider = new AwsRegionProvider {
      override def getRegion: Region = Region.US_EAST_1
    }

    // Test the getRegion method
    val region = AWSUtilWithMockRegionProvider.getRegion(mockRegionProvider)
    assert(region == Region.US_EAST_1)
  }

  "getInstance" should "return a mock instance" in {
    // Mock credentials provider
    val mockCredentialsProvider = mock[AwsCredentialsProvider]
    when(mockCredentialsProvider.resolveCredentials())
      .thenReturn(AwsBasicCredentials.create("mock-access-key", "mock-secret-key"))

    // Mock EC2 client
    val mockEc2Client = mock[Ec2Client]
    when(mockEc2Client.describeInstances())
      .thenReturn(
        DescribeInstancesResponse.builder()
          .reservations(
            Reservation.builder()
              .instances(
                Instance.builder()
                  .instanceId("i-0abc1234def5678gh")
                  .state(InstanceState.builder().name("running").build())
                  .architecture("x86_64")
                  .tags(
                    Tag.builder().key("Environment").value("Production").build(),
                    Tag.builder().key("Name").value("hostname.domain.com").build()
                  )
                  .build()
              )
              .build()
          )
          .build()
      )

    // Call the method with the mocked client
    val instanceID = AWSUtil.getInstanceID(mockEc2Client)
    assert(instanceID == "i-0abc1234def5678gh")

    val architecture = AWSUtil.getArchitecture(mockEc2Client)
    assert(architecture == "x86_64")

    val environment = AWSUtil.getEC2MetadataTag(mockEc2Client, "Environment")
    assert(environment == "dev")

    val name = AWSUtil.getEC2MetadataTag(mockEc2Client, "Name")
    assert(name == "hostname.domain.com")
  }

  "getAWSSecret" should "return the secret string from the client" in {
    // Arrange: Create a mock SecretsManagerClient.
    val mockClient = mock[SecretsManagerClient]
    val secretName = "MySecret"
    val expectedSecret = "SecretValue123"

    // Create a fake response that returns our expected secret.
    lazy val fakeResponse: GetSecretValueResponse = GetSecretValueResponse.builder()
      .secretString(expectedSecret)
      .build()

    // Stub getSecretValue to return fakeResponse when called with any GetSecretValueRequest.
    when(mockClient.getSecretValue(any(classOf[GetSecretValueRequest])))
      .thenReturn(fakeResponse)

    // Act: Call the function with the mocked client.
    val result = AWSUtil.getAWSSecret(mockClient, secretName)

    // Assert: Verify the returned secret matches what we expect.
    result shouldEqual expectedSecret
  }

  "getAWSParameter" should "return the string representation of the parameter response from the client" in {
    // Arrange: Create a mock SsmClient.
    val mockClient = mock[SsmClient]
    val parameterName = "TestParameter"
    val expectedResponseString = "ParameterValue from SSM"

    // Create a fake GetParameterResponse using the builder.
    val fakeResponse: GetParameterResponse = GetParameterResponse.builder().build()
    // We use a spy so we can override its toString behavior.
    val fakeResponseSpy = spy(fakeResponse)
    when(fakeResponseSpy.toString).thenReturn(expectedResponseString)

    // Stub getParameter to return our spied fakeResponse.
    when(mockClient.getParameter(ArgumentMatchers.any(classOf[GetParameterRequest])))
      .thenReturn(fakeResponseSpy)

    // Act: Call our function using the mocked client.
    val result = AWSUtil.getAWSParameter(mockClient, parameterName)

    // Assert: The result should match our expected fakeResponse's toString.
    result shouldEqual expectedResponseString
  }

}
