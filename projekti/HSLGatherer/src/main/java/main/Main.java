package main;

import classes.Stop;
import classes.Trip;
import logic.Logic;
import org.w3c.dom.Text;
import ui.TextUI;

import java.io.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        TextUI ui = new TextUI();
        ui.start();
    }
}