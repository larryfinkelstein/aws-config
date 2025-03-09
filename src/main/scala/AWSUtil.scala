import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.Try

object AWSUtil {
  // Expose the lookup functionality via a variable.
  // In production, this is set to the default implementation.
  var awsLookup: AwsLookup = DefaultAwsLookup

  // Convenience accessors for the default clients (if needed).
  def secretsManagerClient: SecretsManagerClient = DefaultAwsLookup.secretsManagerClient
  def ssmClient: SsmClient = DefaultAwsLookup.ssmClient
  def ec2Client: Ec2Client = DefaultAwsLookup.ec2Client

//  def getRegion(): String = ec2Client.describeRegions().regions().get(0).regionName()
  def getRegion(ec2Client: Ec2Client = null): String = {
    awsLookup.getCurrentRegion(ec2Client)
  }
  def getInstanceID(ec2Client: Ec2Client = null): String = {
    // Use the provided client or fall back to a default one
    val client = if (ec2Client != null) ec2Client else AWSUtil.ec2Client

    val response: DescribeInstancesResponse = client.describeInstances()
    response.reservations().get(0).instances().get(0).instanceId()
  }

  def getAccountID(): String = ec2Client.describeInstances().reservations().get(0).ownerId()

  def getArchitecture(ec2Client: Ec2Client = null): String = {
    // Use the provided client or fall back to a default one
    val client = if (ec2Client != null) ec2Client else AWSUtil.ec2Client

    val response: DescribeInstancesResponse = client.describeInstances()
    response.reservations().get(0).instances().get(0).architecture().toString
  }

  def getInstanceIDDocument(): String = EC2MetadataFetcher.getInstanceIDDocument().toString()

  def getEC2MetadataTag(ec2Client: Ec2Client = null, tag: String): String =
    awsLookup.getEC2MetadataTag(ec2Client, tag)

  def getAWSSecret(secretClient: SecretsManagerClient = null, secretName: String): String =
    awsLookup.getAWSSecret(secretClient, secretName)

  def getAWSParameter(ssmClientProvided: SsmClient = null, parameterName: String): String =
    awsLookup.getAWSParameter(ssmClientProvided, parameterName)

}
