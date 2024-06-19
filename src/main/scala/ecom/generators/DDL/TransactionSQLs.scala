package ecom.generators.DDL

object TransactionSQLs {
  val createTable = "CREATE TABLE IF NOT EXISTS transactions (" +
    "transaction_id VARCHAR(255) PRIMARY KEY, " +
    "product_id VARCHAR(255), " +
    "product_name VARCHAR(255), " +
    "product_category VARCHAR(255), " +
    "product_price DOUBLE PRECISION, " +
    "product_quantity INTEGER, " +
    "product_brand VARCHAR(255), " +
    "total_amount DOUBLE PRECISION, " +
    "currency VARCHAR(255), " +
    "customer_id VARCHAR(255), " +
    "transaction_date TIMESTAMP, " +
    "payment_method VARCHAR(255) " +
    ")"

  val insertStmt = "INSERT INTO transactions(transaction_id, product_id, product_name, product_category, product_price, " +
    "product_quantity, product_brand, total_amount, currency, customer_id, transaction_date, payment_method) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
    "ON CONFLICT (transaction_id) DO UPDATE SET " +
    "product_id = EXCLUDED.product_id, " +
    "product_name  = EXCLUDED.product_name, " +
    "product_category  = EXCLUDED.product_category, " +
    "product_price = EXCLUDED.product_price, " +
    "product_quantity = EXCLUDED.product_quantity, " +
    "product_brand = EXCLUDED.product_brand, " +
    "total_amount  = EXCLUDED.total_amount, " +
    "currency = EXCLUDED.currency, " +
    "customer_id  = EXCLUDED.customer_id, " +
    "transaction_date = EXCLUDED.transaction_date, " +
    "payment_method = EXCLUDED.payment_method " +
    "WHERE transactions.transaction_id = EXCLUDED.transaction_id"
}
