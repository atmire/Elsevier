package com.atmire.import_citations.configuration;

/** Represents a problem with the input source: e.g. cannot connect to the source.
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 19/09/12
 * Time: 13:17
 */
public class SourceException extends Exception {
    public SourceException() {
    }

    public SourceException(String s) {
        super(s);
    }

    public SourceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SourceException(Throwable throwable) {
        super(throwable);
    }
}
