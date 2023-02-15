package ru.serykhd.mysql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Database {

    private static final Executor executor = Executors.newWorkStealingPool(16);

    private static final Map<DatabaseCredentials, DatabaseCredentialsManager> REGISTRY
            = new ConcurrentHashMap<>();

    private final HikariDataSource src;
    private Connection con;

    public Database(final DatabaseCredentials credentials) {
        DatabaseCredentialsManager manager = REGISTRY.computeIfAbsent(credentials, this::initManager);
        src = manager.getSource();

        int count = manager.getUsages().incrementAndGet();
        src.setMaximumPoolSize(credentials.getMaximumPoolSize());
    }

    private DatabaseCredentialsManager initManager(final DatabaseCredentials credentials) {
        return new DatabaseCredentialsManager(initSource(credentials), new AtomicInteger());
    }

    protected abstract HikariDataSource initSource(DatabaseCredentials credentials);

    private Executor getExecutor() {
        return executor;
    }

    /**
     * Close connection
     */
    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException t) {
                throw new IllegalStateException(t);
            }

            con = null;
        }
    }

    /**
     * Get or create connection
     */
    public Connection getConnection() throws SQLException {
        synchronized (this) {
            return src.getConnection();
        }
    }

    private DatabaseStatement makeStatement(final int keys, final String query, final Object... objects) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query, keys);

        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                Object value = objects[i];

                if (value instanceof LocalDate) {
                    value = Date.valueOf((LocalDate) value);
                }

                if (value == null) {
                    statement.setNull(i + 1, Types.NULL);
                } else {
                    statement.setObject(i + 1, value);
                }
            }
        }

        return new DatabaseStatement(connection, statement);
    }

    /**
     * Execute <code>async</code> query to the database
     *
     * @return Future
     */
    public CompletableFuture<BufferedQuery> executeQuery(final String sql, final Object... objects) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseStatement statement = makeStatement(Statement.NO_GENERATED_KEYS, sql, objects);
                 ResultSet rs = statement.executeQuery()) {
                return new BufferedQuery(rs);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }, getExecutor()).exceptionally(ex -> {
            ex.printStackTrace();

            throw new RuntimeException(ex);
        });
    }

    /**
     * Execute <code>async</code> update to the database and return generated keys
     *
     * @return Future
     */
    public CompletableFuture<BufferedExecutionWithGeneratedKeys> executeUpdate(final String sql, final Object... objects) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseStatement statement = makeStatement(Statement.RETURN_GENERATED_KEYS, sql, objects)) {
                int affectedRows = statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    return new BufferedExecutionWithGeneratedKeys(
                            rs.next() ? rs.getInt(1) : 0,
                            affectedRows
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }, getExecutor()).exceptionally(ex -> {
            ex.printStackTrace();

            throw new RuntimeException(ex);
        });
    }

    /**
     * Execute <code>async</code> update to the database
     *
     * @return Future
     */
    public CompletableFuture<BufferedExecution> executeVoidUpdate(final String sql, final Object... objects) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseStatement statement = makeStatement(Statement.NO_GENERATED_KEYS, sql, objects)) {
                return new BufferedExecution(statement.executeUpdate());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }, getExecutor()).exceptionally(ex -> {
            ex.printStackTrace();

            throw new RuntimeException(ex);
        });
    }

    /**
     * Truncate table
     */
    @Deprecated
    public CompletableFuture<BufferedExecutionWithGeneratedKeys> trucateTable(String tableName) {
        return executeUpdate("TRUNCATE " + tableName);
    }
}
