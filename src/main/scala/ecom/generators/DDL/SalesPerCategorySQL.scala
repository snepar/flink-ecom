package generators.DDL

object SalesPerCategorySQL {
  val createTable = "CREATE TABLE IF NOT EXISTS sales_per_category (" +
    "transaction_date DATE, " +
    "category VARCHAR(255), " +
    "total_sales DOUBLE PRECISION, " +
    "PRIMARY KEY (transaction_date, category)" +
    ")"

  val insertStmt = "INSERT INTO sales_per_category(transaction_date, category, total_sales) " +
  "VALUES (?, ?, ?) " +
    "ON CONFLICT (transaction_date, category) DO UPDATE SET " +
    "total_sales = EXCLUDED.total_sales " +
    "WHERE sales_per_category.category = EXCLUDED.category " +
    "AND sales_per_category.transaction_date = EXCLUDED.transaction_date"
}
