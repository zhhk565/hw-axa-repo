package test
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object test {

  def main(args: Array[String]): Unit = {

    val prop = new java.util.Properties()
    //val warehouseLocation = new File("hdfs://hdp-master.com:8020/warehouse/tablespace/managed/hive").getAbsolutePath
    val hiveLocation = "hdfs://hdp-master.com:8020/warehouse"
    val sc = SparkSession.builder().appName("XML_Processing")
      .master("yarn")
      .config("hive.metastore.uris", "thrift://hdp-master.com:9083")
      .config("hive.metastore.warehouse.dir", "hdfs://hdp-master.com:8020/warehouse/tablespace/managed/hive")
      .config("spark.sql.warehouse.dir", hiveLocation)
      .config("spark.sql.hive.convertMetastoreParquet", "false")
      .config("hive.strict.managed.tables", "true")
      .enableHiveSupport()
      .getOrCreate

    var spark = sc.sqlContext


    ////*****************Branch Source*******************

    spark.sql("use axa_gulf_gulf_dc_ciris")

    spark.sql("SELECT " +
      "BR_CODE branch_code," +
      "voc_des branch_desc," +
      "time_stamp_create AS time_stamp," +
      "BR_POST1 AS branch_country " +
      "FROM UDW_BR,dri_voc " +
      "WHERE br_code = voc_code " +
      "AND voc_table_code = '00003' " +
      "AND br_inuse = 'Y' " +
      "AND voc_lang = 'E'").toDF()
      .withColumn("Branch_dk", row_number().over(Window.orderBy(lit(1))))
      .withColumn("Valid_From_Timestamp", lit("NULL"))
      .withColumn("Valid_To_Timestamp", lit("NULL"))
      .withColumn("Current_Indicator", lit(1))
      .withColumn("Effective_From_Date", lit(1))
      .withColumn("Effective_To_Date", lit("Null"))
      .withColumn("Load_Info_Dk", lit(-7777))
      .withColumn("Legal_Owner_Code_Dk", lit(-7777))
      .withColumn("Source_Code_Dk", lit(-7777))
      .withColumn("Load_Info_Code", lit("Unknown"))
      .withColumn("Load_Info_Description", lit("Unknown"))
      .withColumn("Legal_Owner_Code", lit("Unknown"))
      .withColumn("Legal_Owner_Description", lit("Unknown"))
      .withColumn("Source_Code", lit("Unknown"))
      .withColumn("Source_Code_Description", lit("Unknown"))
      .withColumn("Unique_ID_in_Source_System", lit("Unknown"))
      .withColumn("Insert_Timestamp", lit(1))
      .withColumn("Update_Timestamp", lit("NULL"))
      .withColumn("Updated_by_User_Dk", lit(-7777))
      .withColumn("Created_by_User_Dk", lit(-7777))
      .createOrReplaceTempView("Branch_Stage")


    //Sequence
    spark.sql("use axa_land")
    val data = spark.sql("SELECT Branch_Dk,Branch_Code AS Branch_Bk,Branch_code,branch_desc as Branch_Description,Branch_Country AS Branch_Country"+
      ",Valid_From_Timestamp,Valid_From_Timestamp Valid_From_Timestamp,Valid_To_Timestamp"+
      ",Current_Indicator,Effective_From_Date,Effective_To_Date,Load_Info_Dk,Load_Info_Code,Load_Info_Description"+
      ",Legal_Owner_Code_Dk,Legal_Owner_Code,Legal_Owner_Description,Source_Code_Dk,Source_Code,Source_Code_Description"+
      ",Unique_ID_in_Source_System,Insert_Timestamp,Update_Timestamp,Updated_by_User_Dk,Created_by_User_Dk FROM Branch_Stage")
    data.write.mode("Overwrite").insertInto("branch_test")

  }
}











