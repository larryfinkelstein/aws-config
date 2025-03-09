import com.typesafe.config.{Config, ConfigFactory}
import scala.jdk.CollectionConverters._

object AwsConfigLoader {

  /**
   * Loads the base configuration and then applies AWS lookups for any
   * keys containing escape placeholders as defined by TemplateParser.
   *
   * Example application.conf:
   *
   * my {
   *   secret {
   *     key = "${aws:secret:MySecretId}"
   *   }
   *   param {
   *     key = "${aws:ssm:MyParameterName}"
   *   }
   * }
   *
   * In the above, the placeholder for `my.secret.key` will be replaced by the value fetched
   * from AWS Secrets Manager and `my.param.key` from the SSM Parameter Store.
   */
  def load(): Config = {
    val baseConfig = ConfigFactory.load()
//    println(baseConfig.getConfig("my").root().render()) // Prints the entire configuration tree
    val awsOverrides = createAwsOverrideConfig(baseConfig)
    val awsConfig = awsOverrides.withFallback(baseConfig).resolve()
//    println(awsConfig.getConfig("my").root().render()) // Prints the entire configuration tree
    awsConfig
  }

  def load(config: Config): Config = {
    val baseConfig = config
    val awsOverrides = createAwsOverrideConfig(baseConfig)
    awsOverrides.withFallback(baseConfig).resolve()
  }


  private def createAwsOverrideConfig(config: Config): Config = {
    // Go through each configuration entry that is a string, look for escape placeholders,
    // and produce a mapping of keys to their AWS-fetched values.
    val overrides = config.entrySet().asScala
      .filter(_.getValue.valueType() == com.typesafe.config.ConfigValueType.STRING)
      .flatMap { entry =>
        val key = entry.getKey
        val value = config.getString(key)
        TemplateParser.parse(value) match {
          case Some((lookupType, lookupId)) =>
            // Invoke the appropriate AWS lookup. You could also consider caching lookups,
            // handling errors, or logging if a lookup fails.
            val updatedValue = lookupType match {
              case "secret" =>
                AWSUtil.getAWSSecret(secretName = lookupId)
              case "ssm" =>
                AWSUtil.getAWSParameter(parameterName = lookupId)
              case "tag" =>
                AWSUtil.getEC2MetadataTag(tag = lookupId)
              case "region" =>
                AWSUtil.getRegion()
              case _ =>
                // If an unknown lookup type is provided, leave the original value
                value
            }
            Some(key -> updatedValue)
          case None =>
            None
        }
      }.toMap
    // Create a new Config from the overrides map.
    ConfigFactory.parseMap(overrides.asJava)
  }
}
