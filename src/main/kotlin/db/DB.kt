package db

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

class DB {
    fun connect(): Connection {
        try {
            val props = Properties()
            props.load(DB::class.java.getResourceAsStream("/postgres.properties"))
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