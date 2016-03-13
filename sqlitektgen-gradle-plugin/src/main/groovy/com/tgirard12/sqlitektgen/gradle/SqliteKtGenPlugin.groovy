package com.tgirard12.sqlitektgen.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 */
class SqliteKtGenPlugin implements Plugin<Project> {

    void apply(Project project) {

        // Add the 'sqlitektgen' extension object
        project.extensions.create("sqlitektgen", SqliteKtGenExtension)
        def task = project.tasks.create('generateSqliteKtClasses', SqliteKtGenTask.class)

        task.group = "sqlitektgen"
        task.description = "Generate Kotlin class for a better SQLite usage"
    }
}
