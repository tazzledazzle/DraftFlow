import db.DatabaseConfig
import java.sql.Connection
import java.sql.DriverManager


class App {
    fun main(array: Array<String>) {
        val url = "jdbc:postgresql://localhost:5432/postgres"
        val username = "terenceschumacher"
        val password = "password"

        try {
            val connection: Connection? = DriverManager.getConnection(url, username, password)
            var stmt = connection?.createStatement()!!
            var rs = stmt.executeQuery("SELECT * FROM projects")
            val props = DatabaseConfig.getProperties(DatabaseConfig())
            props.keys.forEach {
                println(it)
            }
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
    }
}