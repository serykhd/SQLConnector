package ru.serykhd.mysql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class DatabaseCredentialsManager {

    private final HikariDataSource source;
    private final AtomicInteger usages;

}
