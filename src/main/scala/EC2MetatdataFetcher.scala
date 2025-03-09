import java.net.{HttpURLConnection, URL}
import scala.io.Source

object EC2MetadataFetcher {
  private val EC2_METADATA_URL = "http://169.254.169.254/latest"

  def getInstanceIDDocument(): String = {
    val connection = new URL(EC2_METADATA_URL + "/dynamic/instance-identity/document").openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setConnectTimeout(5000)
    connection.setReadTimeout(5000)

    try {
      val inputStream = connection.getInputStream
      val document = Source.fromInputStream(inputStream).mkString
      inputStream.close()
      document
    } catch {
      case e: Exception =>
        throw new RuntimeException("Failed to fetch EC2 instance identity document", e)
    } finally {
      connection.disconnect()
    }
  }

  /**
   * Retrieves the current region of the running EC2 instance by querying
   * the instance metadata service for the availability zone and then
   * stripping off the trailing letter.
   *
   * @return Some(region) if the lookup succeeds, otherwise None.
   */
  def getCurrentRegion: Option[String] = {
    try {
      // Query the metadata endpoint for the availability zone.
      val availabilityZone = Source.fromURL(EC2_METADATA_URL + "/meta-data/placement/availability-zone").mkString.trim
      if (availabilityZone.nonEmpty) {
        // The region is the availability zone with the last character removed.
        Some(availabilityZone.dropRight(1))
      } else {
        None
      }
    } catch {
      case ex: Exception =>
        // Logging the error is a good practice.
        println(s"Error retrieving instance metadata: ${ex.getMessage}")
        None
    }
  }
}
