package eu.fbk.das.experiments;

public abstract class Loggable {

    abstract String toCsv(String commaDelimiter);

    abstract String getCsvFileHeader(String commaDelimiter);

}
