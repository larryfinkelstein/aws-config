import com.typesafe.config.{Config, ConfigFactory, ConfigValueType}
import scala.jdk.CollectionConverters._

// Example TemplateParser implementation.
// If your implementation differs, substitute accordingly.
object TemplateParser {
  // This regex matches strings of the form: ${aws:secret:MySecretId} or ${aws:ssm:MyParameterName}
  // The first capture group is the lookup type ("secret" or "ssm"),
  // and the second capture group is the identifier.
  private val escapePattern = """\$\{aws:(secret|ssm|tag|region):([^}]+)\}""".r
  // private val escapePattern = "\\{\\{(.*?)\\}\\}".r

  /**
   * Parses a string value and returns an option containing a tuple (lookupType, lookupId)
   * if the value matches the escape pattern.
   */
  def parse(value: String): Option[(String, String)] = value match {
    case escapePattern(lookupType, lookupId) => Some((lookupType, lookupId))
    case _                                   => None
  }
}

