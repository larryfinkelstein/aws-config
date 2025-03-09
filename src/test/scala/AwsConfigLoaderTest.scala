import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

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
class AwsConfigLoaderSpec extends AnyFlatSpec with Matchers with MockitoSugar {

  // Back-up the original lookup functions.
  // These functions are assumed defined as vars in AWSUtilFunctions.
  // For example:
  //   var getAWSSecret: (SecretsManagerClient, String) => String = { (client, secretName) => ... }
  //   var getAWSParameter: (SsmClient, String) => String = { (client, parameterName) => ... }
  private val originalGetAWSSecret = AWSUtil.getAWSSecret _
  private val originalGetAWSParameter = AWSUtil.getAWSParameter _

  override def withFixture(test: NoArgTest) = {
    // Override AWS lookups with our fake implementations.
    // This uses the same mocks already defined in AWSUtilTest.
    AWSUtil.getAWSSecret = (_: Any, secretName: String) => secretName match {
      case "MySecretId" => "FakeSecretValue"
      case other        => s"UnknownSecret($other)"
    }
    AWSUtil.getAWSParameter = (_: Any, parameterName: String) => parameterName match {
      case "MyParameterName" => "FakeParameterValue"
      case other             => s"UnknownParameter($other)"
    }

    try {
      super.withFixture(test)
    } finally {
      // Restore the original lookup implementations after each test.
      AWSUtil.getAWSSecret = originalGetAWSSecret
      AWSUtil.getAWSParameter = originalGetAWSParameter
    }
  }

  "AwsConfigLoader" should "substitute AWS escape placeholders with values from AWS" in {
    // Create an in-memory configuration (simulating application.conf)
    val testConfigStr =
      """
        |my {
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
  }
}
