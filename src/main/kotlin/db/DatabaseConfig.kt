package db

import org.hibernate.dialect.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Configuration
import java.util.Properties
import javax.sql.DataSource

@Configuration
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

    constructor(
        @Value("\${spring.datasource.url}") url: String?,
        @Value("\${spring.datasource.username}") username: String?,
        @Value("\${spring.datasource.password}") password: String?
    ) {
        properties.load(DatabaseConfig::class.java.getResourceAsStream("/postgres.properties"))
    }

    fun dataSource(): DataSource {
        var dcBuildUrl = properties["db.url"].toString()
        if (url != null) {
            dcBuildUrl = url
        }
        var dcBuildUser = properties["db.username"].toString()
//        if (username != null) {
//            dcBuildUser = username
//        }
        val dataSourceBuilder = DataSourceBuilder.create()
        return dataSourceBuilder.url(dcBuildUrl).username(dcBuildUser).password(properties["db.password"].toString()).build()
    }
}