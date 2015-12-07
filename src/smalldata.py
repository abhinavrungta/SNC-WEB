import os
import sys

from pyspark import SparkConf
from pyspark.context import SparkContext
from pyspark.sql.context import SQLContext
from pyspark.sql.types import Row, StructType, StructField, FloatType, ArrayType, \
    StringType, IntegerType, DoubleType, LongType
from pyspark.storagelevel import StorageLevel
from sklearn import mixture

import numpy as np


class MainApp(object):
    def __init__(self):
        pass
    
    def init(self):
        os.environ["SPARK_HOME"] = "/Users/abhinavrungta/Desktop/setups/spark-1.5.2"
        # os.environ['AWS_ACCESS_KEY_ID'] = <YOURKEY>
        # os.environ['AWS_SECRET_ACCESS_KEY'] = <YOURKEY>
        conf = SparkConf()
        conf.setMaster("local[10]")
        conf.setAppName("PySparkShell")
        conf.set("spark.executor.memory", "2g")
        conf.set("spark.driver.memory", "1g")
        self.sc = SparkContext(conf=conf)
        self.sqlContext = SQLContext(self.sc)        

    def loadData(self):
        category_list = self.sc.textFile("/Users/abhinavrungta/Desktop/uf-study/snc/github/SNC-WEB/src/yahoo/ydata-ymovies-user-movie-ratings-train-v1_0.txt").map(lambda line: (int(line.split(',')[0]), int(line.split(',')[1]), float(line.split(',')[2]), long(line.split(',')[3])))
        category_schema = StructType([
            StructField("userid", IntegerType(), True),
            StructField("movieid", IntegerType(), True),
            StructField("rating", FloatType(), True),
            StructField("time", LongType(), True)
        ])
        category_list = self.sqlContext.createDataFrame(category_list, category_schema)
        category_list.registerTempTable("data")
        movie_list = self.sqlContext.sql("SELECT movieid, COUNT(movieid) AS ct FROM data GROUP BY movieid")
        movie_list.registerTempTable("movie")
        movieid = movie_list.sort(movie_list.ct.desc()).first().movieid
        # movieid = category_list.first().movieid
        category_list = self.sqlContext.sql("SELECT * FROM data WHERE movieid = {0}".format(movieid))
        category_list.registerTempTable("data")
        user_list = self.sqlContext.sql("SELECT DISTINCT userid FROM data LIMIT 50")
        print(user_list.count())
        user_list.show()
        user_list.registerTempTable("users")
        category_list = self.sqlContext.sql("SELECT d.userid AS userid, d.movieid AS movieid, d.rating AS rating, d.time AS time FROM data d, users u WHERE d.userid = u.userid").repartition(1)
        #category_list = self.sqlContext.createDataFrame(category_list, category_schema)
        category_list = category_list.map(lambda line: str(line.userid) + "," + str(line.movieid) + "," + str(line.rating) + "," + str(line.time))
        category_list = category_list.repartition(1)
        category_list.saveAsTextFile("data.txt")

app = MainApp()
app.init()
app.loadData()
