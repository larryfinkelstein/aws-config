import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

object AWSUtilWithMockRegionProvider {
  def createEc2Client(credentialsProvider: AwsCredentialsProvider, regionProvider: AwsRegionProvider): Ec2Client = {
    Ec2Client.builder()
      .credentialsProvider(credentialsProvider)
      .region(regionProvider.getRegion)
      .build()
  }

  def getRegion(regionProvider: AwsRegionProvider): Region = {
    regionProvider.getRegion // Directly return the region from the provider
  }
}
