package com.boswelja.migration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MigratorTest {

    @Test
    fun runConstantMigrationsDoesNothingWithNoMigrations(): Unit = runBlocking {
        val migrationCount = 10
        // Create a migrator for running tests
        val migrator = ConcreteMigrator(
            1,
            1,
            false,
            emptyList()
        )

        // Check with empty migration list
        assertEquals(migrator.runConstantMigrations(1, emptyList()), Result.NOT_NEEDED)

        // Check with migrations whose shouldMigrate returns false
        val migrations = createConstantMigrations(
            count = migrationCount,
            shouldMigrate = { false },
            migrateResult = { false }
        )
        assertEquals(migrator.runConstantMigrations(1, migrations), Result.NOT_NEEDED)
    }

    @Test
    fun runConstantMigrationsAbortsWhenAbortOnErrorIsTrue(): Unit = runBlocking {
        val migrationCount = 10

        // Create a migrator for running tests
        val migrator = ConcreteMigrator(
            1,
            1,
            true,
            emptyList()
        )

        // Run migrations
        var migrationExecutionCount = 0
        val migrations = createConstantMigrations(
            count = migrationCount,
            shouldMigrate = { true },
            migrateResult = {
                migrationExecutionCount++
                false
            }
        )
        migrator.runConstantMigrations(1, migrations)

        // Verify only one migration was called
        assertEquals(migrationExecutionCount, 1)
    }

    @Test
    fun runConstantMigrationsReturnsFAILEDOnError(): Unit = runBlocking {
        val migrationCount = 10

        // Create migrations
        val migrations = createConstantMigrations(
            count = migrationCount,
            shouldMigrate = { true },
            migrateResult = { false }
        )

        // Check with abortOnError = true
        ConcreteMigrator(
            1,
            1,
            true,
            emptyList()
        ).also { migrator ->
            // Check result
            assertEquals(migrator.runConstantMigrations(1, migrations), Result.FAILED)
        }

        // Check with abortOnError = false
        ConcreteMigrator(
            1,
            1,
            false,
            emptyList()
        ).also { migrator ->
            // Check result
            assertEquals(migrator.runConstantMigrations(1, migrations), Result.FAILED)
        }
    }

    @Test
    fun runConstantMigrationsThrowsExceptionIfNonConstantMigrationsAreGiven(): Unit = runBlocking {
        // Create migrations
        val migrations = createVersionedMigrations(
            1,
            2,
            migrateResult = { true }
        )

        // Check with abortOnError = true
        ConcreteMigrator(
            1,
            1,
            true,
            emptyList()
        ).also { migrator ->
            // Check result
            assertFailsWith<IllegalArgumentException> {
                migrator.runConstantMigrations(1, migrations)
            }
        }
    }

    @Test
    fun runConstantMigrationsReturnsSUCCESSOnSuccessfulMigrations(): Unit = runBlocking {
        val migrationCount = 10

        // Create migrations
        val migrations = createConstantMigrations(
            count = migrationCount,
            shouldMigrate = { true },
            migrateResult = { true }
        )

        // Create a migrator to use
        ConcreteMigrator(
            1,
            1,
            true,
            emptyList()
        ).also { migrator ->
            // Check result
            assertEquals(migrator.runConstantMigrations(1, migrations), Result.SUCCESS)
        }
    }

    @Test
    fun runVersionedMigrationsDoesNothingWithNoMigrations(): Unit = runBlocking {
        val fromVersion = 1
        val toVersion = 10
        // Create a migrator for running tests
        val migrator = ConcreteMigrator(
            toVersion,
            toVersion,
            false,
            emptyList()
        )

        // Check with empty migration list
        assertEquals(migrator.runVersionedMigrations(toVersion, emptyList()), Result.NOT_NEEDED)

        // Check with migrations that won't change the version
        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion,
            migrateResult = { true }
        )
        assertEquals(migrator.runVersionedMigrations(toVersion, migrations), Result.NOT_NEEDED)
    }

    @Test
    fun runVersionedMigrationsAbortsWhenAbortOnErrorIsTrue(): Unit = runBlocking {
        val fromVersion = 1
        val toVersion = 10

        // Create a migrator for running tests
        val migrator = ConcreteMigrator(
            fromVersion,
            toVersion,
            true,
            emptyList()
        )

        // Run migrations
        var migrationExecutionCount = 0
        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion,
            migrateResult = {
                migrationExecutionCount++
                false
            }
        )
        migrator.runVersionedMigrations(1, migrations)

        // Verify only one migration was called
        assertEquals(migrationExecutionCount, 1)
    }

    @Test
    fun runVersionedMigrationsReturnsFAILEDOnError(): Unit = runBlocking {
        val fromVersion = 1
        val toVersion = 10

        // Create migrations
        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion,
            migrateResult = { false }
        )

        // Check with abortOnError = true
        ConcreteMigrator(
            fromVersion,
            toVersion,
            true,
            emptyList()
        ).also { migrator ->
            // Check result
            assertEquals(migrator.runVersionedMigrations(1, migrations), Result.FAILED)
        }

        // Check with abortOnError = false
        ConcreteMigrator(
            fromVersion,
            toVersion,
            false,
            emptyList()
        ).also { migrator ->
            // Check result
            assertEquals(migrator.runVersionedMigrations(1, migrations), Result.FAILED)
        }
    }

    @Test
    fun runVersionedMigrationsThrowsExceptionIfConstantMigrationsAreGiven(): Unit = runBlocking {
        val migrationCount = 10
        // Create migrations
        val migrations = createConstantMigrations(
            migrationCount,
            shouldMigrate = { true },
            migrateResult = { true }
        )

        // Check with abortOnError = true
        ConcreteMigrator(
            1,
            1,
            true,
            emptyList()
        ).also { migrator ->
            // Check result
            assertFailsWith<IllegalArgumentException> {
                migrator.runVersionedMigrations(1, migrations)
            }
        }
    }

    @Test
    fun runVersionedMigrationsReturnsSUCCESSOnSuccessfulMigrations(): Unit = runBlocking {
        val fromVersion = 1
        val toVersion = 10

        // Create migrations
        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion,
            migrateResult = { true }
        )

        // Create a migrator to use
        ConcreteMigrator(
            fromVersion,
            toVersion,
            true,
            emptyList()
        ).also { migrator ->
            // Check result
            assertEquals(
                migrator.runVersionedMigrations(fromVersion, migrations), Result.SUCCESS
            )
        }
    }

    @Test
    fun onMigratedToIsCalledAfterSuccessfulMigration() {
        val fromVersion = 1
        val toVersion = 3

        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion
        ) { true }

        val migrator = ConcreteMigrator(
            oldVersion = fromVersion,
            currentVersion = toVersion,
            migrations = migrations
        )

        runBlocking { migrator.migrate() }

        assertEquals(migrator.migratedTo, 3)
    }

    @Test
    fun onMigratedToIsCalledAfterFailedMigration() {
        val fromVersion = 1
        val toVersion = 3

        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion
        ) { false }

        val migrator = ConcreteMigrator(
            oldVersion = fromVersion,
            currentVersion = toVersion,
            migrations = migrations
        )

        runBlocking { migrator.migrate() }

        assertEquals(migrator.migratedTo, fromVersion)
    }

    @Test
    fun onMigratedToIsNotCalledIfNoMigrationsWereRun() {
        val fromVersion = 1
        val toVersion = 3

        val migrations = createVersionedMigrations(
            fromVersion,
            toVersion
        ) { false }

        val migrator = ConcreteMigrator(
            oldVersion = toVersion,
            currentVersion = toVersion,
            migrations = migrations
        )

        runBlocking { migrator.migrate() }

        assertEquals(migrator.migratedTo, null)
    }

    @Test
    fun migrateThrowsIllegalStateExceptionIfGetOldVersionIsHigherThanCurrentVersion(): Unit =
        runBlocking {
            val migrator = ConcreteMigrator(
                oldVersion = 2,
                currentVersion = 1,
                migrations = emptyList()
            )
            assertFailsWith<IllegalStateException> {
                migrator.migrate()
            }
        }
}
