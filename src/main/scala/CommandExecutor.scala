object CommandExecutor {
//  def execute(command: String): String = command match {
//    case "GetRegion" => AWSUtil.getRegion()
//    case "GetInstanceID" => AWSUtil.getInstanceID()
//    case "GetAccountID" => AWSUtil.getAccountID()
//    case "GetArchitecture" => AWSUtil.getArchitecture()
//    case "getInstanceIDDocument" => AWSUtil.getInstanceIDDocument()
//    case cmd if cmd.startsWith("GetEC2MetadataTag") => AWSUtil.getEC2MetadataTag(tag=cmd.split(":")(1))
//    case cmd if cmd.startsWith("GetAWSSecret") => AWSUtil.getAWSSecret(secretName=cmd.split(":")(1))
//    case cmd if cmd.startsWith("GetAWSParameter") => AWSUtil.getAWSParameter(parameterName=cmd.split(":")(1))
//    case _ => throw new IllegalArgumentException(s"Unknown command: $command")
//  }

//  def processTemplate(template: String): String = {
//    TemplateParser.parse(template).map(execute).mkString(", ")
//  }
}
