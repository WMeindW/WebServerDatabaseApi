package cz.meind.main;

import cz.meind.application.Application;


public class Main {
    /**
     * The entry point of the application.
     * <p>
     * This class contains the main method which is the starting point of the application.
     * It initializes the application and passes the command line arguments to it.
     *
     * @param args The command line arguments passed to the application.
     */
    public static void main(String[] args) {
        Application.run(args);
    }
}