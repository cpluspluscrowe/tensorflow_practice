package com.linkedin.ads.reporting.demographics.topn

import org.apache.spark.storage.StorageLevel
import com.linkedin.ads.reporting.utils.AvroUtils
import com.linkedin.events.pinot.derived.ads.AdAnalyticsEvent
import com.linkedin.spark.common.lib.EncoderUtils._
import com.linkedin.spark.lms.functions.SparkEventHeader
import com.linkedin.spark.lms.implicits._
import com.linkedin.tscp.reporting.common.avro.{AdAnalyticsEventField, AdAnalyticsEventFields}
import org.apache.spark.DaliSpark
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, SparkSession, DataFrame}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.storage.StorageLevel
import scala.collection.JavaConverters._
import com.linkedin.spark.common.lib.{DateUtils, TestUtils}
import org.apache.spark.ml.clustering.KMeansModel

object AdAnalyticsTopNReport {
  val columns = """accountid,
memberid,
impressions,
landingpageclicks,
otherengagements,
externalwebsiteconversions,
externalwebsitepostviewconversions,
externalwebsitepostclickconversions,
likes,
commentlikes,
comments,
shares,
follows,
oneclickleadformopens,
oneclickleads,
companypageclicks,
datepartition"""
  import org.apache.hadoop.fs.{FileSystem, Path}
  import org.apache.spark.SparkContext
  def delete_group(path: String)(sc: SparkContext): Unit = {
    val fs = FileSystem.get(sc.hadoopConfiguration)

    val outPutPath = new Path(path)

    if (fs.exists(outPutPath)) {
      fs.delete(outPutPath, true)
    }
  }
  //  val modelPath = "/jobs/tscprep/test/hackathon/ml_model/KMeansStandardizedWithImpressionsAssembled"
  def run(i: Int, model: KMeansModel)(implicit spark: SparkSession): DataFrame = {
    val earlierDate = DateUtils.getDateAgoString(i + 30)
    val laterDate = DateUtils.getDateAgoString(i)
    val path = s"/jobs/tscprep/test/hackathon/groups/$earlierDate"
    val parameters = Map(
      DaliSpark.FILTER_EXP -> s"datepartition <= '$laterDate' and datepartition >= '$earlierDate'",
      DaliSpark.PROJECT_COLS -> columns
    )
    val aprilData = DaliSpark.createDataFrame(path, parameters)
    val withoutAccountOrMemberId = aprilData.drop("memberid").drop("accountid").drop("date")
    val assembler = new VectorAssembler().setInputCols(withoutAccountOrMemberId.columns).setOutputCol("assembled")
    val output = assembler.transform(aprilData).select("assembled", "accountid", "memberid").repartition(500).cache
    val scaler =
      new StandardScaler().setInputCol("assembled").setOutputCol("features").setWithStd(true).setWithMean(true)
    val transformed = scaler.fit(output).transform(output).drop("assembled").cache

    val predictions = model.transform(transformed)
    val predictionAndMemberIds = predictions.select("memberid", "accountid", "prediction").cache
    val withData = predictionAndMemberIds.join(aprilData, Seq("memberid", "accountid"))
    delete_group(path)(spark.sparkContext)
    withData
  }

  def createModel(i: Int, modelPath: String)(spark: SparkSession): Unit = {
    group(i + 30)(spark)
    val earlierDate = DateUtils.getDateAgoString(i + 30)
    val laterDate = DateUtils.getDateAgoString(i)
    val path = s"/jobs/tscprep/test/hackathon/groups/$earlierDate"
    val parameters = Map(
      DaliSpark.FILTER_EXP -> s"datepartition <= '$laterDate' and datepartition >= '$earlierDate'",
      DaliSpark.PROJECT_COLS -> columns
    )
    val aprilData = DaliSpark.createDataFrame(path, parameters)
    val withoutAccountOrMemberId = aprilData.drop("memberid").drop("accountid")
    val assembler = new VectorAssembler().setInputCols(withoutAccountOrMemberId.columns).setOutputCol("assembled")
    val output = assembler.transform(aprilData).select("assembled", "accountid", "memberid").repartition().cache
    val scaler =
      new StandardScaler().setInputCol("assembled").setOutputCol("features").setWithStd(true).setWithMean(true)
    val transformed = scaler.fit(output).transform(output).drop("assembled").cache
    val k = 10
    val kmeans = new KMeans().setK(k).setSeed(1L)
    val model = kmeans.fit(transformed)
    model.save(modelPath)
  }

  def group(i: Int)(implicit spark: SparkSession): Unit = {
    // changed to only process a single day at a time
    import spark.implicits._
    val earlierDate = DateUtils.getDateAgoString(i)
    val laterDate = DateUtils.getDateAgoString(i)

    val adStatisticsViewPath = "dalids:///tscp_reporting_dali_mp.adstatistics_views"
    val parameters = Map(
      DaliSpark.FILTER_EXP -> s"datepartition <= '$laterDate' and datepartition >= '$earlierDate'",
      DaliSpark.PROJECT_COLS -> columns
    )
    val dataset =
      DaliSpark.createDataFrame(adStatisticsViewPath, parameters).drop("datepartition").persist(StorageLevel.DISK_ONLY)

    val filtered = dataset
      .repartition(10000)
      .persist(StorageLevel.DISK_ONLY)

    val grouped = filtered
      .groupBy("accountid", "memberid")
      .agg(
        sum("impressions").alias("impressions"),
        sum("landingpageclicks").alias("landingpageclicks"),
        sum("otherengagements").alias("otherengagements"),
        sum("externalwebsiteconversions").alias("externalwebsiteconversions"),
        sum("externalwebsitepostviewconversions").alias("externalwebsitepostviewconversions"),
        sum("externalwebsitepostclickconversions").alias("externalwebsitepostclickconversions"),
        sum("likes").alias("likes"),
        sum("commentlikes").alias("commentlikes"),
        sum("comments").alias("comments"),
        sum("shares").alias("shares"),
        sum("follows").alias("follows"),
        sum("oneclickleadformopens").alias("oneclickleadformopens"),
        sum("oneclickleads").alias("oneclickleads"),
        sum("companypageclicks").alias("companypageclicks")
      )
      .repartition()
      .withColumn("date", lit(earlierDate)) // added date, matches with the safe file's name
      .persist(StorageLevel.DISK_ONLY)

    val savePath = s"/jobs/tscprep/test/hackathon/groups/$earlierDate"

    grouped.write.mode("overwrite").format("com.databricks.spark.avro").option("header", "true").save(savePath)
  }
}
