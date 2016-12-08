package com.atmire.import_citations.configuration;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 27 Oct 2014
 */
public abstract interface SourceExceptionHandler<T extends Source> {

    public abstract void handle(T source);

}
