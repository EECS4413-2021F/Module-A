# Module A - Microservices

- [Services](#services)
- [Database Access Examples](#database-access-examples)
- [Data Serialization Examples](#data-serialization-examples)
  - [Serialization](#serialization)
  - [De-serialization](#de-serialization)
- [Other Examples](#other-examples)

-----
## Services

- [SquareRootService](src/services/SquareRootService.java) from Lecture 1
- [TaxService](src/services/TaxService.java) from Lecture 2
  - [TaxBean](src/model/TaxBean.java), model class for a single Tax rate record (a single province)
  - [TaxCollection](src/model/TaxCollection.java), model class for a collection of Tax rate records (many provinces)
- [HTTPServer](src/services/HTTPServer.java) from end of Lecture 2
- [ExchangeRateService](src/services/ExchangeRateService.java) from the Lab, week 2
- [HTTPCalcService](src/services/HTTPCalcService.java) from the Lab, week 2

-----
## Database Access Examples

- [DerbyJDBCExample](src/miscs/DerbyJDBCExample.java)
- [SqliteJDBCExample](src/miscs/SqliteJDBCExample.java)

-----
## Data Serialization Examples

#### Serialization

- [ToJSONExample](src/miscs/ToJSONExample.java)
- [ToXMLExample](src/miscs/ToXMLExample.java)
- [ToXMLCollectionExample](src/miscs/ToXMLCollectionExample.java)

#### De-serialization

- [FromJSONExample](src/miscs/FromJSONExample.java)
- [FromXMLExample](src/miscs/FromXMLExample.java)

-----
## Other Examples

- [TCPClient](src/miscs/TCPClient.java)
