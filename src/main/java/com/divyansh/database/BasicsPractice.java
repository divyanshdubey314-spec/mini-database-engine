package com.divyansh.database;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class BasicsPractice {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        // STEP 1: Write your name to a file
//        FileWriter writer = new FileWriter("name.txt");
//        writer.write("Divyansh");  // this text gets converted to bytes and saved
//        writer.close();            // always close when you're done writing
//        System.out.println("Wrote name to file.");

        // STEP 2: Read it back
        FileReader reader = new FileReader("name.txt");

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            System.out.println(line);
        }
        reader.close();
    }
}