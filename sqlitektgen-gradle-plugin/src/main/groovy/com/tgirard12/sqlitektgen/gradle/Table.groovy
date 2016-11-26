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
    Map<String, String> selectBy = new HashMap<>()

    @ToString
    @EqualsAndHashCode
    static class Column {
        String name
        String ktField
        String ktType
        String typeAppend
        Boolean insertOrUpdate
        Boolean select
        String defaultValue

        boolean nullable
        boolean isInTable

        def nameUpper() {
            return name.toUpperCase()
        }

        String columnNameFull(String table) {
            "$table.${this.name}"
        }
    }
}
