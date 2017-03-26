package com.earldouglas.xwp

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier
import com.amazonaws.services.elasticbeanstalk.model.S3Location
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.io.File
import java.net.URLEncoder
import java.util.Arrays
import sbt.Keys._
import sbt.Keys.{`package` => pkg}
import sbt._

object ElasticBeanstalkDeployPlugin extends AutoPlugin {

  object autoImport {
    lazy val elasticBeanstalkDeploy  = taskKey[Unit]("Deploy .war file to Elastic Beanstalk")
    lazy val elasticBeanstalkRegion  = settingKey[String]("Elastic Beanstalk region, e.g. us-west-1")
    lazy val elasticBeanstalkAppName = settingKey[String]("Elastic Beanstalk application name, e.g. my-app")
    lazy val elasticBeanstalkEnvName = settingKey[String]("Elastic Beanstalk environment name, e.g. my-env")
  }

  import autoImport._

  override def projectSettings =
    Seq(elasticBeanstalkDeploy := deploy( (packagedArtifact in (Compile, pkg), pkg)._2.value
                                         , elasticBeanstalkRegion.value
                                         , elasticBeanstalkAppName.value
                                         , elasticBeanstalkEnvName.value
                                         , version.value
                                         )
  )

  def deploy( warFile: File
            , region: String
            , appName: String
            , envName: String
            , version: String
            ): Unit = {

    val creds = new EnvironmentVariableCredentialsProvider

    val eb: AWSElasticBeanstalk =
      AWSElasticBeanstalkClientBuilder.
          standard().
          withRegion(region).
          withCredentials(creds).
          build()

    val s3: AmazonS3 =
      AmazonS3ClientBuilder.
          standard().
          withRegion(region).
          withCredentials(creds).
          build()

    val s3Location = uploadWarFile(eb, s3, warFile)
    deleteApplicationVersion(eb, appName, version)
    createApplicationVersion(eb, appName, version, s3Location)
    updateEnvironment(eb, envName, version)

  }

  def uploadWarFile( eb: AWSElasticBeanstalk
                   , s3: AmazonS3
                   , warFile: File
                   ): S3Location = {
    val bucketName = eb.createStorageLocation().getS3Bucket()
    val key = URLEncoder.encode(warFile.getName(), "UTF-8")
    val s3Result = s3.putObject(bucketName, key, warFile)
    new S3Location(bucketName, key)
  }

  def deleteApplicationVersion( eb: AWSElasticBeanstalk
                              , appName: String
                              , version: String
                              ): Unit = {
    val describeApplicationVersionsRequest = new DescribeApplicationVersionsRequest()
    describeApplicationVersionsRequest.setApplicationName(appName)
    describeApplicationVersionsRequest.setVersionLabels(Arrays.asList(version))

    val result = eb.describeApplicationVersions(describeApplicationVersionsRequest)
    if (!result.getApplicationVersions().isEmpty()) {
      val deleteRequest = new DeleteApplicationVersionRequest(appName, version)
      deleteRequest.setDeleteSourceBundle(true)
      eb.deleteApplicationVersion(deleteRequest)
    }
  }

  def createApplicationVersion( eb: AWSElasticBeanstalk                  
                              , appName: String
                              , version: String
                              , s3Location: S3Location
                              ): Unit = {
    val createApplicationVersionRequest =
      new CreateApplicationVersionRequest(appName , version)
    createApplicationVersionRequest.setAutoCreateApplication(true)
    createApplicationVersionRequest.setSourceBundle(s3Location)

    eb.createApplicationVersion(createApplicationVersionRequest)
  }

  def updateEnvironment( eb: AWSElasticBeanstalk                  
                       , envName: String
                       , version: String
                       ): Unit = {
    val updateEnviromentRequest = new UpdateEnvironmentRequest()
    updateEnviromentRequest.setEnvironmentName(envName)
    updateEnviromentRequest.setVersionLabel(version)

    eb.updateEnvironment(updateEnviromentRequest)
  }

}
