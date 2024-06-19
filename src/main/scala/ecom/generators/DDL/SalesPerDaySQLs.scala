package generators.DDL

object SalesPerDaySQLs {
  val createTable = "CREATE TABLE IF NOT EXISTS sales_per_day (" +
    "transaction_date DATE PRIMARY KEY, " +
    "total_sales DOUBLE PRECISION " +
    ")"

  val insertStmt = "INSERT INTO sales_per_day(transaction_date, total_sales) " +
    "VALUES (?,?) " +
    "ON CONFLICT (transaction_date) DO UPDATE SET " +
    "total_sales = EXCLUDED.total_sales " +
    "WHERE sales_per_day.transaction_date = EXCLUDED.transaction_date"

}
