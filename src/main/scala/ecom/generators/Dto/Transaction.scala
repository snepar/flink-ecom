package ecom.generators.Dto

case class Transaction(
                        transactionId: String,
                        productId: String,
                        productName: String,
                        productCategory: String,
                        productPrice: Double,
                        productQuantity: Int,
                        productBrand: String,
                        totalAmount: Double,
                        currency: String,
                        customerId: String,
                        transactionDate: java.sql.Timestamp, // Use java.sql.Timestamp for Scala
                        paymentMethod: String
                      )
