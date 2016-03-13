package com.tgirard12.sqlitektgen.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class SqliteKtGenTask extends DefaultTask {

    @TaskAction
    def sqlitektenTask() {

        println 'my Task'

    }
}
