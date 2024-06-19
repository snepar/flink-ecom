package ecom

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import deserializer.JSONValueDeserializationSchema
import generators.Dto.{SalesPerCategory, SalesPerDay, SalesPerMonth, Transaction}
import generators.DDL
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.elasticsearch.sink.{Elasticsearch7SinkBuilder, ElasticsearchSink}
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.elasticsearch7.shaded.org.apache.http.HttpHost
import org.apache.flink.elasticsearch7.shaded.org.elasticsearch.action.index.IndexRequest
import org.apache.flink.elasticsearch7.shaded.org.elasticsearch.client.Requests
import org.apache.flink.elasticsearch7.shaded.org.elasticsearch.common.xcontent.XContentType
import org.apache.flink.streaming.api.scala._

import java.sql.{Date, PreparedStatement}

object KafkaIntegrationEcom {
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
  val username = "postgres"
  val password = "postgres"

  val execOptions = new JdbcExecutionOptions.Builder()
    .withBatchSize(10)
    .withBatchIntervalMs(200)
    .withMaxRetries(5)
    .build()

  val connOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
    .withUrl(jdbcUrl)
    .withDriverName("org.postgresql.Driver")
    .withUsername(username)
    .withPassword(password)
    .build()


  def readCustomData() = {
    val topic = "financial_transactions"
    val source = KafkaSource.builder[Transaction]()
      .setBootstrapServers("localhost:9092")
      .setTopics(topic)
      .setGroupId("ecom-group")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new JSONValueDeserializationSchema())
      .build()

    val transactionStream: DataStream[Transaction] = env.fromSource(source, watermarkStrategy = WatermarkStrategy.noWatermarks(), "ecom")

    transactionStream
  }

  def writeToDB(transactionStream: DataStream[Transaction])= {
    transactionStream.addSink(JdbcSink.sink(
      DDL.TransactionSQLs.createTable,
      new JdbcStatementBuilder[Transaction] {
        override def accept(t: PreparedStatement, u: Transaction): Unit = {
        }
      },
      execOptions,
      connOptions
    )).name("Create Transaction Table")

    transactionStream.addSink(JdbcSink.sink(
      DDL.TransactionSQLs.insertStmt,
      new JdbcStatementBuilder[Transaction] {
        override def accept(preparedStatement: PreparedStatement, transaction: Transaction): Unit = {
          preparedStatement.setString(1, transaction.transactionId)
          preparedStatement.setString(2, transaction.productId)
          preparedStatement.setString(3, transaction.productName)
          preparedStatement.setString(4, transaction.productCategory)
          preparedStatement.setDouble(5, transaction.productPrice)
          preparedStatement.setInt(6, transaction.productQuantity)
          preparedStatement.setString(7, transaction.productBrand)
          preparedStatement.setDouble(8, transaction.totalAmount)
          preparedStatement.setString(9, transaction.currency)
          preparedStatement.setString(10, transaction.customerId)
          preparedStatement.setTimestamp(11, transaction.transactionDate)
          preparedStatement.setString(12, transaction.paymentMethod)
        }
      },
      execOptions,
      connOptions
    )).name("Insert into Transaction table")
  }

  def writeToDBsalesPerCategory(transactionStream:DataStream[Transaction]) = {
    transactionStream.addSink(JdbcSink.sink(
      DDL.SalesPerCategorySQL.createTable,
      new JdbcStatementBuilder[Transaction] {
        override def accept(t: PreparedStatement, u: Transaction): Unit = {
        }
      },
      execOptions,
      connOptions
    ))

    transactionStream.map(
      transaction => {
        SalesPerCategory(new Date(transaction.transactionDate.getTime), transaction.productCategory, transaction.totalAmount)
      }
    ).keyBy(_.category)
      .reduce((acc, curr) => {
        acc.copy(totalSales = acc.totalSales + curr.totalSales)
      })
      .addSink(JdbcSink.sink(
      DDL.SalesPerCategorySQL.insertStmt,
      new JdbcStatementBuilder[SalesPerCategory] {
        override def accept(preparedStatement: PreparedStatement, salesPerCategory: SalesPerCategory): Unit = {
          preparedStatement.setDate(1, new Date(salesPerCategory.transactionDate.getTime))
          preparedStatement.setString(2, salesPerCategory.category)
          preparedStatement.setDouble(3, salesPerCategory.totalSales)
        }
      },
      execOptions,
      connOptions
    ))
  }

  def writeToDBsalesPerDay(transactionStream: DataStream[Transaction]) = {
    transactionStream.addSink(JdbcSink.sink(
      DDL.SalesPerDaySQLs.createTable,
      new JdbcStatementBuilder[Transaction] {
        override def accept(t: PreparedStatement, u: Transaction): Unit = {
        }
      },
      execOptions,
      connOptions
    ))

    transactionStream.map(transaction =>
      SalesPerDay(new Date(transaction.transactionDate.getTime), transaction.totalAmount)
    ).keyBy(_.transactionDate).reduce((acc,curr) => acc.copy(totalSales = acc.totalSales + curr.totalSales))
      .addSink(JdbcSink.sink(
      DDL.SalesPerDaySQLs.insertStmt,
      new JdbcStatementBuilder[SalesPerDay] {
        override def accept(preparedStatement: PreparedStatement, salesPerDay: SalesPerDay): Unit = {
          preparedStatement.setDate(1, salesPerDay.transactionDate)
          preparedStatement.setDouble(2, salesPerDay.totalSales);
        }
      },
      execOptions,
      connOptions
    ))

  }

  def writeToDBsalesPerMonth(transactionStream: DataStream[Transaction]) = {
    transactionStream.addSink(JdbcSink.sink(
      DDL.SalesPerMonthSQL.createTable,
      new JdbcStatementBuilder[Transaction] {
        override def accept(t: PreparedStatement, u: Transaction): Unit = {
        }
      },
      execOptions,
      connOptions
    ))

    transactionStream.map(transaction =>
      {
        val transactionDate = new Date(transaction.transactionDate.getTime);
        val year = transactionDate.toLocalDate().getYear();
        val month = transactionDate.toLocalDate().getMonth().getValue();
        SalesPerMonth(year, month, totalSales = transaction.totalAmount)
      }
    ).keyBy(spm=>(spm.year,spm.month)).reduce((acc,curr) => acc.copy(totalSales = acc.totalSales + curr.totalSales))
      .addSink(JdbcSink.sink(
        DDL.SalesPerMonthSQL.insertStmt,
        new JdbcStatementBuilder[SalesPerMonth] {
          override def accept(preparedStatement: PreparedStatement, salesPerMonth: SalesPerMonth): Unit = {
            preparedStatement.setInt(1, salesPerMonth.year)
            preparedStatement.setInt(2, salesPerMonth.month)
            preparedStatement.setDouble(3, salesPerMonth.totalSales)
          }
        },
        execOptions,
        connOptions
      ))

  }

  def createIndexRequest(element:(Transaction)): IndexRequest = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val json: String = mapper.writeValueAsString(element)
    Requests.indexRequest.index("transactions").id(element.transactionId).source(json,XContentType.JSON)
  }

  def writeToElastic(transactionStream: DataStream[Transaction]) = {

    val sink: ElasticsearchSink[Transaction] = new Elasticsearch7SinkBuilder[Transaction]
      .setHosts(new HttpHost("localhost", 9200, "http"))
      .setEmitter[Transaction]{
        (transaction, context, indexer) => {
          val mapper = new ObjectMapper()
          mapper.registerModule(DefaultScalaModule)
          val json: String = mapper.writeValueAsString(transaction)

          val indexRequest = Requests.indexRequest()
            .index("transactions")
            .id(transaction.transactionId)
            .source(json, XContentType.JSON);
          indexer.add(indexRequest)
        }
      }.build()

    transactionStream.sinkTo(sink)
  }

  def main(args: Array[String]): Unit = {
    val transactionStream = readCustomData()
    transactionStream.print()

    writeToDB(transactionStream)
    writeToDBsalesPerCategory(transactionStream)
    writeToDBsalesPerDay(transactionStream)
    writeToDBsalesPerMonth(transactionStream)
    writeToElastic(transactionStream)
    
    env.execute()

  }
}
