import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.ssm.SsmClient

/**
 * This test uses the escape pattern defined in TemplateParser
 * to override values in configuration entries that look like
 *
 *   "${aws:secret:MySecretId}"
 *   "${aws:ssm:MyParameterName}"
 *
 * For the test we rely on the existing mocks in AWSUtilTest by overriding
 * the AWSUtilFunctions.lookup methods. (It is assumed that AWSUtilFunctions
 * is implemented so that its lookup functions can be re-assigned via var.)
 */
class AwsConfigLoaderTest extends AnyFlatSpec with Matchers with MockitoSugar {

  AWSUtil.awsLookup = new AwsLookup {
    override def getAWSSecret(secretClient: SecretsManagerClient, secretName: String): String = {
      secretName match {
        case "MySecretId" => "FakeSecretValue"
        case "MySecret" => "SecretValue123"
        case "DatabasePassword" => "FakeDatabasePassword"
        case other => s"UnknownSecret($other)"
      }
    }

    override def getAWSParameter(ssmClient: SsmClient, parameterName: String): String = {
      parameterName match {
        case "MyParameterName" => "FakeParameterValue"
        case "LogFilePath" => "/var/log/FakeLogs"
        case "TestParameter" => "ParameterValue from SSM"
        case other => s"UnknownParameter($other)"
      }
    }

    override def getEC2MetadataTag(ec2Client: Ec2Client, tag: String): String = {
      tag match {
        case "Environment" => "dev"
        case "Port" => "1234"
        case "Name" => "hostname.domain.com"
        case other => s"UnknownTag($other)"
      }
    }

    override def getCurrentRegion(ec2Client: Ec2Client): String = {
      Region.US_EAST_1.toString
    }
  }

  "AwsConfigLoader" should "substitute AWS escape placeholders with values from AWS" in {
    // Create an in-memory configuration (simulating application.conf)
    val testConfigStr =
      """
        |my {
        |  region = "${aws:region:fake}"
        |
        |  secret {
        |    key = "${aws:secret:MySecretId}"
        |  }
        |  param {
        |    key = "${aws:ssm:MyParameterName}"
        |  }
        |  staticValue = "FixedValue"
        |}
        |""".stripMargin

    // Parse the test configuration.
    val testConfig: Config = ConfigFactory.parseString(testConfigStr)

    // Load the configuration using the test overload.
    // Note: AwsConfigLoader.load(config: Config) should take the provided configuration,
    // perform AWS lookups for escape placeholders, and merge overrides with it.
    val loadedConfig: Config = AwsConfigLoader.load(testConfig)

    // Verify that the keys with AWS escape patterns have been replaced
    // with the values provided by our mocked functions.
    loadedConfig.getString("my.secret.key") shouldEqual "FakeSecretValue"
    loadedConfig.getString("my.param.key") shouldEqual "FakeParameterValue"

    // Also verify that configuration entries without placeholders remain unchanged.
    loadedConfig.getString("my.staticValue") shouldEqual "FixedValue"
    loadedConfig.getString("my.region") shouldEqual "us-east-1"
  }

  "AwsConfigLoader" should "Load and parse configs from application.conf" in {
    val config: Config = AwsConfigLoader.load()

    config.getString("my.secret.key") shouldEqual "FakeSecretValue"
    config.getString("my.param.key") shouldEqual "FakeParameterValue"
    config.getString("logging.file") shouldEqual "/var/log/FakeLogs"
    config.getString("service.database.password") shouldEqual "FakeDatabasePassword"

    config.getString("app.host") shouldEqual "hostname.domain.com"
    config.getString("app.port") shouldEqual "1234"

    config.getString("my.region") shouldEqual "us-east-1"
    config.getString("my.environment") shouldEqual "dev"
  }
}
