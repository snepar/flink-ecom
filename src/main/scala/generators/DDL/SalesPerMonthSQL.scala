package generators.DDL

object SalesPerMonthSQL {
  val createTable = "CREATE TABLE IF NOT EXISTS sales_per_month (" +
    "year INTEGER, " +
    "month INTEGER, " +
    "total_sales DOUBLE PRECISION, " +
    "PRIMARY KEY (year, month)" +
    ")"

  val insertStmt = "INSERT INTO sales_per_month(year, month, total_sales) " +
    "VALUES (?,?,?) " +
    "ON CONFLICT (year, month) DO UPDATE SET " +
    "total_sales = EXCLUDED.total_sales " +
    "WHERE sales_per_month.year = EXCLUDED.year " +
    "AND sales_per_month.month = EXCLUDED.month "
}
