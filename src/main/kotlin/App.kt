import java.sql.Connection
import java.sql.DriverManager


class App {
    fun main() {
        val url = "jdbc:postgresql://your-database-url:5432/projects"
        val username = "tschumacher"
        val password = "password"

        try {
            val connection: Connection? = DriverManager.getConnection(url, username, password)
            var stmt = connection?.createStatement()!!
            var rs = stmt.executeQuery("SELECT * FROM projects")
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
    }
}