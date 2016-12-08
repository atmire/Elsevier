package com.atmire.util.helper;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 16 Sep 2015
 */
public class ToStringStringifier<T> implements Stringifier<T> {

    @Override
    public String stringify(T o) {
        return o.toString();
    }
}