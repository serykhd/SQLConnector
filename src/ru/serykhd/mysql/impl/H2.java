package ru.serykhd.mysql.impl;

import com.zaxxer.hikari.HikariDataSource;
import ru.serykhd.mysql.Database;
import ru.serykhd.mysql.DatabaseCredentials;

import java.sql.SQLException;

@Deprecated
public class H2 extends Database {

    public H2(DatabaseCredentials credentials) {
        super(credentials);
    }

    private static final String JDBC_URL = "jdbc:h2:file:/home/........./a3.db";

    @Override
    public HikariDataSource initSource(DatabaseCredentials credentials) {
        HikariDataSource source = new HikariDataSource();
        source.setUsername(credentials.getUser());
        source.setPassword(credentials.getPassword());
        source.setDriverClassName("org.h2.Driver");
        source.setJdbcUrl(JDBC_URL);

        try {
            source.setLoginTimeout(60);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return source;
    }
}