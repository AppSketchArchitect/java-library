package fr.appsketch.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteInitScript {
    private static final Logger log = LoggerFactory.getLogger(SQLiteInitScript.class);

    public static void main(String[] args) {
        String url = "jdbc:sqlite:test.db";

        try (Connection ignored = DriverManager.getConnection(url)) {
            System.out.println("Base SQLite et table créées avec succès !");

        } catch (Exception e) {
            log.error("Database Exception: ", e);
        }
    }
}
