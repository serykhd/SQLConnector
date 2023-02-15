package ru.serykhd.mysql;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Setter
@Accessors(chain = true)
@Getter
public final class DatabaseCredentials {

    private String database;
    private String user = "root";
    private String password;
    private String host = "[::1]";
    private int port = 3306;
    private int maximumPoolSize = 16;

}
