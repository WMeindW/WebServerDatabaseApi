package cz.meind.main;

import cz.meind.service.ObjectMapper;

import java.sql.DriverManager;


public class Main {
    /**
     * The entry point of the application.
     * <p>
     * This class contains the main method which is the starting point of the application.
     * It initializes the application and passes the command line arguments to it.
     *
     * @param args The command line arguments passed to the application.
     */
    public static void main(String[] args) throws Exception {
        //Application.run(args);
        ObjectMapper mapper = new ObjectMapper(DriverManager.getConnection("jdbc:mysql://sql.daniellinda.net:3306/synchro", "remote", "hm3C4iLL+"));


    }
}