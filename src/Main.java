import Utilities.ReaderWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, CsvValidationException {
        final int MAX_ROWS = 20_000;
        final int MAX_COLUMNS = 7;
        String inFile;
        String outFile;
        int stations;
        final String CSV_EXT = ".csv";
        final String CLEANED_EXT = "_cleaned";
        String directoryName, continueResponse, tempInFile;
        var userInput = new Scanner(System.in);
        var checkPointNames = new ArrayList<String>();
        var checkPointIDs = new ArrayList<Integer>();
        var rawData = new ArrayList<RawField>();
        var cleanedData = new ArrayList<ArrayList<CleanedField>>();
        boolean continueScanning;

        //Folder where the CSV files are saved
        System.out.println("Enter the name of the directory (skip by pressing enter if the file is in the main directory):");
        directoryName = userInput.nextLine();
        directoryName += "/";

        do {
            //Update incoming and outgoing file names
            System.out.println("Enter the name of the CSV file to read (DO NOT include .csv extension):");
            inFile = userInput.nextLine();
            tempInFile = inFile;
            inFile += CSV_EXT;
            outFile = tempInFile + CLEANED_EXT;

            //Collect the number of stations
            System.out.println("Enter the number of stations (each station has two checkpoints, i.e.: Check-in and Check-out):");
            stations = userInput.nextInt();
            userInput.nextLine();

            //Collect all the stations' information
            for (int i = 1; i <= stations; i++) {
                //Update first_transaction and second_transaction column names
                System.out.println("Enter the name of the first transaction column in station " + i + ":");
                checkPointNames.add(userInput.nextLine());
                System.out.println("Enter the name of the second transaction column in station " + i + ":");
                checkPointNames.add(userInput.nextLine());

                //Input for first and second checkpoint IDs
                System.out.println("Enter the first transaction checkpoint ID in station " + i + ":");
                checkPointIDs.add(userInput.nextInt());
                userInput.nextLine();
                System.out.println("Enter the second transaction checkpoint ID in station " + i + ":");
                checkPointIDs.add(userInput.nextInt());
                userInput.nextLine();
            }

            //Perform reading, cleaning and writing
            rawData = ReaderWriter.csvReader(directoryName + inFile, MAX_ROWS, MAX_COLUMNS);
            cleanedData = ReaderWriter.dataCleanerIterator(rawData, checkPointIDs);
            ReaderWriter.csvWriterIterator(directoryName + outFile, cleanedData, MAX_ROWS, checkPointNames);
            //Clear ArrayLists for new use
            rawData.clear();
            cleanedData.clear();
            checkPointNames.clear();
            checkPointIDs.clear();

            //Ask if there are more CSV files to process
            System.out.println("Do you need to scan another file? y/n");
            continueResponse = userInput.nextLine();
            switch (continueResponse) {
                case "y", "Y" -> continueScanning = true;
                case "n", "N" -> {
                    continueScanning = false;
                    System.out.println("Goodbye");
                    userInput.close();
                }
                default -> {
                    System.out.println("That's not a valid response. Program terminates now");
                    continueScanning = false;
                    userInput.close();
                }
            }
        } while(continueScanning);
    }
}
