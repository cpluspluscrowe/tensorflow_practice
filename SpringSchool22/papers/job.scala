package com.linkedin.ads.reporting.demographics.topn

import java.time.LocalDate

import com.linkedin.ads.reporting.date.ReportDate._
import com.linkedin.ads.reporting.job.{AdsReportingSparkJob, BaseConfig, DateRangeJobRunner}
import com.linkedin.events.pinot.derived.ads.AdAnalyticsEvent
import com.linkedin.spark.common.lib.EncoderUtils._
import com.linkedin.spark.lms.implicits._
import com.linkedin.tscp.reporting.common.avro.AdAnalyticsEventField
import org.apache.spark.DaliSpark
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, SparkSession, DataFrame}
import scala.reflect.ClassTag
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import com.linkedin.spark.common.lib.{DateUtils, TestUtils}
import com.linkedin.spark.lms.hdfs.HdfsLogger

case class AdAnalyticsTopNJobConfig(
    granularity: String,
    adAnalyticsDailyPath: String,
    topNOutputPath: String,
    topNRowsPerCategory: Int = 100,
    numberOfOutputFiles: Int = 2)
    extends BaseConfig

object AdAnalyticsTopNJob extends AdsReportingSparkJob[AdAnalyticsTopNJobConfig] {
  override val jobClass: Class[_] = getClass
  override val configClass: ClassTag[AdAnalyticsTopNJobConfig] = ClassTag(classOf[AdAnalyticsTopNJobConfig])
  import spark.implicits._
  override def run()(implicit config: AdAnalyticsTopNJobConfig): Unit = {
    val modelPath = "/jobs/tscprep/test/hackathon/ml_model/KMeansImpressionsAssembler3"
    val i = 1
//    val firstGroup = AdAnalyticsTopNReport.group(i)
//    AdAnalyticsTopNReport.createModel(firstGroup, modelPath)
    val model = KMeansModel.load(modelPath)
//    AdAnalyticsTopNReport.discoverBestK(model, hdfsLogger)
    for (i <- 7 to 365 by 7) {
      val grouped = AdAnalyticsTopNReport.group(i) // create group data for day i
      val result = AdAnalyticsTopNReport.run(i, model) // predict on group
      val earlierDate = DateUtils.getDateAgoString(i + 6)
      val savePath = s"/jobs/tscprep/test/hackathon/predictions/$earlierDate"
      result
        .repartition(50)
        .write
        .mode("overwrite")
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .save(savePath)
    }
  }
}
