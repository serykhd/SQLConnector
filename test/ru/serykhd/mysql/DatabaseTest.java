package ru.serykhd.mysql;

import ru.serykhd.mysql.impl.H2;
import ru.serykhd.mysql.impl.MySQL;

public class DatabaseTest {

    public static void main(String[] args) {
        Database database = new MySQL(new DatabaseCredentials().setDatabase("sqlconnector").setPassword("root"));

        database.executeQuery("SELECT 1;").thenAccept(s -> {
            System.out.println(s);
        }).join();

        database.executeVoidUpdate("CREATE TABLE IF NOT EXISTS `ttt`(`sss` varchar(32) NOT NULL);").join();

        for (int i = 0; i < 1000; i++) {;
            database.executeVoidUpdate(String.format("INSERT INTO `ttt`(`sss`) VALUES ('payload')")).join();
        }

        System.out.println(database.executeQuery("SELECT COUNT(*) FROM ttt;").join());
    }
}
