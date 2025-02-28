package db

import java.util.Properties

class DatabaseConfig {
    val properties: Properties = Properties()
    companion object {
        val url = "jdbc:mysql://localhost:3306/timesheet"
        val driver = "com.mysql.jdbc.Driver"

        fun getProperties(dbConfig: DatabaseConfig): Properties {
            dbConfig.properties.load(DatabaseConfig::class.java.getResourceAsStream("/postgres.properties"))

            return dbConfig.properties
        }
    }
}