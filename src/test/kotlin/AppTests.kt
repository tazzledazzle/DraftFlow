import db.DB
import io.kotest.core.annotation.AutoScan
import io.kotest.core.spec.style.FunSpec

@AutoScan
class AppTests: FunSpec({
    test("test") {
//        val app = App()
//        app.main()
    }

    test("dbConnection") {
        val db = DB()
        val connection = db.connect()
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM users")
        while (rs.next()) {
            println(rs.getString("name"))
        }
    }
})