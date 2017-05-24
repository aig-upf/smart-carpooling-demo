package eu.fbk.das.adaptation;

public abstract class Loggable {

    abstract String toCsv(String commaDelimiter);

    abstract String getCsvFileHeader(String commaDelimiter);

}
