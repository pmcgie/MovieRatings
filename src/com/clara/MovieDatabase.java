package com.clara;
import java.sql.*;

public class MovieDatabase {

    private static String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "movies";   //TODO CHANGE
    private static final String USER = "root";
    private static final String PASS = "";

    static Statement statement = null;
    static Connection conn = null;
    static ResultSet rs = null;

    public final static String MOVIE_TABLE_NAME = "movie_reviews";
    public final static String PK_COLUMN = "id";                   //Primary key column. Each movie will have a unique ID.
                                                                    //A primary key is needed to allow updates to the database on modifications to ResultSet
    public final static String TITLE_COLUMN = "title";
    public final static String YEAR_COLUMN = "year_released";
    public final static String RATING_COLUMN = "rating";

    public final static int MOVIE_MIN_RATING = 1;
    public final static int MOVIE_MAX_RATING = 5;

    private static MovieDataModel movieDataModel;

    public static void main(String args[]) {

        //setup creates database (if it doesn't exist), opens connection, and adds sample data


        if (!setup()) {

            System.exit(-1);
        }

        if (!loadAllMovies()) {
            System.exit(-1);
        }

        //If no errors, then start GUI
        MovieForm tableGUI = new MovieForm(movieDataModel);

    }

    //Create or recreate a ResultSet containing the whole database, and give it to movieDataModel
    public static boolean loadAllMovies(){

        try{

            if (rs!=null) {
                rs.close();
            }

            String getAllData = "SELECT * FROM " + MOVIE_TABLE_NAME;
            rs = statement.executeQuery(getAllData);

            if (movieDataModel == null) {
                //If no current movieDataModel, then make one
                movieDataModel = new MovieDataModel(rs);
            } else {
                //Or, if one already exists, update its ResultSet
                movieDataModel.updateResultSet(rs);
            }

            return true;

        } catch (Exception e) {
            System.out.println("Error loading or reloading movies");
            System.out.println(e);
            e.printStackTrace();
            return false;
        }

    }

    public static boolean setup(){
        try {

            //Load driver class
            try {
                String Driver = "com.mysql.jdbc.Driver";
                Class.forName(Driver);
            } catch (ClassNotFoundException cnfe) {
                System.out.println("No database drivers found. Quitting");
                return false;
            }

            conn = DriverManager.getConnection(DB_CONNECTION_URL + DB_NAME, USER, PASS);

            // The first argument ResultSet.TYPE_SCROLL_INSENSITIVE
            // allows us to move the cursor both forward and backwards through the RowSet
            // we get from this statement.

            // (Some databases support TYPE_SCROLL_SENSITIVE, which means the ResultSet will be updated when
            // something else changes the database. Since Derby is embedded we don't need to worry about anything
            // else updating the database. If you were using a server DB you might need to be concerned about this.)

            // The TableModel will need to go forward and backward through the ResultSet.
            // by default, you can only move forward - it's less
            // resource-intensive than being able to go in both directions.            
            // If you set one argument, you need the other. 
            // The second one (CONCUR_UPDATABLE) means you will be able to change the ResultSet and see the changes in the DB
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            //Does the table exist? If not, create it.

            if (!movieTableExists()) {

                //Create a table in the database with 3 columns: Movie title, year and rating
                String createTableSQL = "CREATE TABLE " + MOVIE_TABLE_NAME + " (" + PK_COLUMN + " int NOT NULL AUTO_INCREMENT, " + TITLE_COLUMN + " varchar(50), " + YEAR_COLUMN + " int, " + RATING_COLUMN + " int, PRIMARY KEY(" + PK_COLUMN + "))";
                System.out.println(createTableSQL);
                statement.executeUpdate(createTableSQL);

                System.out.println("Created movie_reviews table");
                //Add some test data - change to some movies you like, if desired
                String addDataSQL = "INSERT INTO " + MOVIE_TABLE_NAME + " VALUES ('Back to the future', 1985, 5)";
                statement.executeUpdate(addDataSQL);
                addDataSQL = "INSERT INTO " + MOVIE_TABLE_NAME + " VALUES ('Back to the Future II', 1989, 4)";
                statement.executeUpdate(addDataSQL);
                addDataSQL = "INSERT INTO " + MOVIE_TABLE_NAME + " VALUES ('Back to the Future III', 1990, 3)";
                statement.executeUpdate(addDataSQL);
            }
            return true;

        } catch (SQLException se) {
            System.out.println(se);
            se.printStackTrace();
            return false;
        }
    }

    private static boolean movieTableExists() throws SQLException {

            String checkTablePresentQuery = "SHOW TABLES LIKE '" + MOVIE_TABLE_NAME + "'";   //Can query the database schema
            ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
            if (tablesRS.next()) {    //If ResultSet has a next row, it has at least one row... that must be our table
                return true;
            }
            return false;

    }

    //Close the ResultSet, statement and connection, in that order.
    public static void shutdown(){
        try {
            if (rs != null) {
                rs.close();
                System.out.println("Result set closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            if (statement != null) {
                statement.close();
                System.out.println("Statement closed");
            }
        } catch (SQLException se){
            //Closing the connection could throw an exception too         
            se.printStackTrace();
        }

        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed");
            }
        }
        catch (SQLException se) {
            se.printStackTrace();
        }
    }
}