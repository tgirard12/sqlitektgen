package com.tgirard12.sqlitektgen.gradle

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 */
@ToString
@EqualsAndHashCode
class Table {
    String name
    String ktClass
    String ktPackage

    List<Column> columns = []
    Map<String, String> queries = new HashMap<>()

    @EqualsAndHashCode
    @ToString
    static class Column {
        String name
        String ktField
        String ktType
        String typeAppend
        Boolean insertOrUpdate
        Boolean select
        String defaultValue

        boolean nullable

        def nameUpper() {
            return name.toUpperCase()
        }
    }
}
