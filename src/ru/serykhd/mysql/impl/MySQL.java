package ru.serykhd.mysql.impl;

import com.zaxxer.hikari.HikariDataSource;
import ru.serykhd.mysql.Database;
import ru.serykhd.mysql.DatabaseCredentials;

import java.sql.SQLException;

public class MySQL extends Database {

    public MySQL(DatabaseCredentials credentials) {
        super(credentials);
    }

    private static final String JDBC_URL = "jdbc:mysql://%s:%s/%s"
            + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF8&connectionCollation=UTF8&useUnicode=true"
            + "&allowMultiQueries=true";

    @Override
    public HikariDataSource initSource(DatabaseCredentials credentials) {
        HikariDataSource source = new HikariDataSource();
        source.setUsername(credentials.getUser());
        source.setPassword(credentials.getPassword());
        source.setJdbcUrl(String.format(JDBC_URL, credentials.getHost(), credentials.getPort(), credentials.getDatabase()));

        try {
            source.setLoginTimeout(60);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return source;
    }
}