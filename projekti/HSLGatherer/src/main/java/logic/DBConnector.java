package logic;

import java.sql.*;
import java.util.*;

public class DBConnector {

    private Connection db;

    public DBConnector() throws SQLException {
        try {
            this.db = DriverManager.getConnection("jdbc:sqlite:hsldatabase.db");
            createTables();
        } catch(SQLException e) {
        }
    }

    public void createTables() throws SQLException {

        Statement stmt = db.createStatement();

        try {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("CREATE TABLE Stops (id INTEGER PRIMARY KEY, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE Trips (id INTEGER PRIMARY KEY, sign TEXT, stop_id INTEGER REFERENCES Stops(id) ON DELETE CASCADE, route TEXT, UNIQUE(sign, route))");
        } catch(SQLException e) {
        }
    }

    public boolean addStop(String stop) throws SQLException {
        Statement stmt = db.createStatement();
        stmt.execute("BEGIN TRANSACTION");

        boolean saved = false;

        try {
            stmt.execute("INSERT INTO Stops (name) VALUES ('" + stop + "')");
            saved = true;
        } catch (SQLException e) {
            System.out.println("\nStop is already saved! Continuing..");
        }

        stmt.execute("COMMIT");

        return saved;
    }

    public boolean addTrip(String sign, String route, String stop) throws SQLException {
        Statement stmt = db.createStatement();
        stmt.execute("BEGIN TRANSACTION");

        boolean saved = false;

        try {
            stmt.execute("INSERT INTO Trips (sign, stop_id, route) " +
                    "VALUES ('" + sign + "', (SELECT id FROM Stops WHERE name = '" + stop + "'), '" + route + "')");
            saved = true;
        } catch (Exception e) {
            System.out.println("\nRoute is already saved! Continuing..");
        }

        stmt.execute("COMMIT");

        return saved;
    }

    public ResultSet getSavedStops() throws SQLException {
        Statement stmt = db.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM Stops");

        return rs;
    }

    public ResultSet getSavedRoutes() throws SQLException {
        Statement stmt = db.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Trips");

        return rs;
    }

    public boolean deleteStop(String stop) throws SQLException {
        Statement stmt = db.createStatement();
        boolean deleted = false;

        try {
            stmt.execute("DELETE FROM Stops WHERE name = '" + stop + "'");
            deleted = true;
        } catch (Exception e) {
            deleted = false;
        }

        return deleted;
    }

    public boolean deleteRoute(String sign, String route) throws SQLException {
        Statement stmt = db.createStatement();
        boolean deleted = false;

        try {
            stmt.execute("DELETE FROM Trips WHERE sign = '" + sign + "' AND route = '" + route + "'");
            deleted = true;
        } catch (Exception e) {
        }

        return deleted;
    }
}