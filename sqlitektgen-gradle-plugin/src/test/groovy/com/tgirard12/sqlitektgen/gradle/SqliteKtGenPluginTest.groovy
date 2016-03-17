package com.tgirard12.sqlitektgen.gradle

import org.gradle.api.Project
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before;
import org.junit.Test;

/**
 */
public class SqliteKtGenPluginTest {

    Project project
    Task task

    @Before
    public void before() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.tgirard12.sqlitektgen'
        task = project.getTasks().findByName('generateSqliteKtClasses')
    }

    @Test
    public void test_pluginId_and_taskName() {
        assert this.task != null
    }


    @Test
    public void test_task_param_change() {
        project.configure(project) {
            sqlitektgen {
                databaseFile = "other/databaseFile.json"
                outputDir = "other/outputDir"
            }
        }
        assert project.getExtensions().findByName('sqlitektgen').databaseFile.equals("other/databaseFile.json")
        assert project.getExtensions().findByName('sqlitektgen').outputDir.equals("other/outputDir")
    }
}