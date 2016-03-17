package com.tgirard12.sqlitektgen.gradle

/**
 *
 */
class SqliteKtGenException extends Exception {
    SqliteKtGenException(String message) {
        super(message)
    }

    SqliteKtGenException(String message, Throwable ex) {
        super(message, ex)
    }
}
