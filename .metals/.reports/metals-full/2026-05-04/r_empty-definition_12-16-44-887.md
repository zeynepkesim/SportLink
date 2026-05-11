error id: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/booking-service/src/main/java/com/sportlink/booking/DatabaseInitializer.java:org/springframework/jdbc/core/JdbcTemplate#
file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/booking-service/src/main/java/com/sportlink/booking/DatabaseInitializer.java
empty definition using pc, found symbol in pc: org/springframework/jdbc/core/JdbcTemplate#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 138
uri: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/booking-service/src/main/java/com/sportlink/booking/DatabaseInitializer.java
text:
```scala
package com.sportlink.booking;


import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTem@@plate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseInitializer {

    public static void initialize(String nameOfDatabase) {
        String url = "jdbc:postgresql://localhost:5432/";
        String username = "postgres";
        String password = "postgres";
        String databaseName = nameOfDatabase;

        DataSource dataSource = new DriverManagerDataSource(url, username, password);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // this will create the database if it doesn't exist in postgres
        try {
            jdbcTemplate.execute("CREATE DATABASE " + databaseName);
        } catch (Exception e) {
            System.err.println("Database already exists");
        }

    }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/jdbc/core/JdbcTemplate#