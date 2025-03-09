import AWSUtil.ec2Client
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.Try

object DefaultAwsLookup extends AwsLookup {
  lazy val secretsManagerClient: SecretsManagerClient =
    SecretsManagerClient.builder().build()

  lazy val ssmClient: SsmClient =
    SsmClient.builder().build()

  lazy val ec2Client: Ec2Client = Ec2Client.builder()
    .region(
      Try(Region.of(System.getenv("AWS_REGION"))).getOrElse(Region.US_EAST_1) // Fallback to default region
    )
    .build()

  override def getAWSSecret(secretClient: SecretsManagerClient, secretName: String): String = {
    val client = if (secretClient != null) secretClient else secretsManagerClient
    val request = GetSecretValueRequest.builder()
      .secretId(secretName)
      .build()
    client.getSecretValue(request).secretString()
  }

  override def getAWSParameter(ssmClientProvided: SsmClient, parameterName: String): String = {
    val client = if (ssmClientProvided != null) ssmClientProvided else ssmClient
    val request = GetParameterRequest.builder()
      .name(parameterName)
      .build()
    client.getParameter(request).toString
  }

  override def getEC2MetadataTag(ec2Client: Ec2Client, tag: String): String = {
    val client = if (ec2Client != null) ec2Client else AWSUtil.ec2Client
    val response: DescribeInstancesResponse = client.describeInstances()
    response.reservations().get(0).instances().get(0).tags().asScala
      .find(t => t.key().equals(tag))
      .map(_.value())
      .getOrElse("NotFound")
  }

  override def getCurrentRegion(ec2Client: Ec2Client): String = {
    val client = if (ec2Client != null) ec2Client else AWSUtil.ec2Client
    client.describeRegions().regions().get(0).regionName()
  }
}