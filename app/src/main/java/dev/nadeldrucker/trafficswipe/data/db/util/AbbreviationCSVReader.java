package dev.nadeldrucker.trafficswipe.data.db.util;

import dev.nadeldrucker.trafficswipe.data.db.entities.Abbreviation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Helper class for reading csv
 */
public class AbbreviationCSVReader {

    /**
     * Reads a abbreviations csv file into an array.
     * @param inputStream {@link InputStream} to read from
     * @return array of {@link Abbreviation}
     */
    public static Abbreviation[] readAbbreviationsFromStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            return reader.lines()
                    .map(AbbreviationCSVReader::readCSVLine)
                    .toArray(Abbreviation[]::new);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Abbreviation readCSVLine(String line) {
        final String[] contents = line.split(";");

        if (contents.length != 2) throw new IllegalStateException("Line with less than 2 entries!");

        return new Abbreviation(contents[1], contents[0]);
    }

}
