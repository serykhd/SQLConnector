package ru.serykhd.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Unidentified Person
 */
public class DatabaseStatement implements AutoCloseable {

    private Connection connection;
    private PreparedStatement statement;

    public DatabaseStatement(Connection connection, PreparedStatement statement) {
        this.connection = connection;
        this.statement = statement;
    }

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
        connection = null;
        statement.close();
        statement = null;
    }
}
