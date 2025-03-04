package db

import java.sql.Connection
import java.sql.DriverManager

class DB {
    fun connect(): Connection {
        try {
            val props = DatabaseConfig.getProperties(DatabaseConfig())
            props.keys.forEach {
                println(it)
            }

            return DriverManager.getConnection(props["db.url"].toString())
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
        println("Connected to database")
    }
}