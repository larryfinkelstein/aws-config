import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.ssm.SsmClient

trait AwsLookup {
  def getCurrentRegion(ec2Client: Ec2Client): String
  def getAWSSecret(secretClient: SecretsManagerClient, secretName: String): String
  def getAWSParameter(ssmClient: SsmClient, parameterName: String): String
  def getEC2MetadataTag(ec2Client: Ec2Client, tag: String): String
}
