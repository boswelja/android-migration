package com.boswelja.migration

/**
 * A class for executing [Migration]s.
 * @param migrations The available [Migration]s to use.
 */
abstract class Migrator(
    private val currentVersion: Int,
    private val migrations: List<Migration>
) {

    /**
     * Get the old version to migrate from.
     */
    abstract suspend fun getOldVersion(): Int

    suspend fun migrate() {
        // Get versions
        val oldVersion = getOldVersion()

        // Build migration map
        val migrationMap = buildMigrationMap(migrations, oldVersion)

        migrationMap.forEach { migration ->
            migration.migrate()
        }
    }

    /**
     * A recursive function to build a list of [Migration]s to be run in a sequential order. Note
     * this will throw [IllegalArgumentException] if a migration cannot be found from a version.
     * @param migrations A [List] of available [Migration]s.
     * @param fromVersion The version to build a migration map from.
     * @return A [List] of ordered [Migration]s that can be run to reach [currentVersion].
     */
    internal fun buildMigrationMap(
        migrations: List<Migration>,
        fromVersion: Int
    ): List<Migration> {
        val migrationMap = mutableListOf<Migration>()

        // Get next available migrations, and all remaining migrations
        val (migrationsFromOldVersion, remainingMigrations) = migrations.separate {
            it.fromVersion == fromVersion
        }

        // Determine next migration and add to migration map
        val migration = migrationsFromOldVersion.maxByOrNull { it.toVersion }
            ?: throw IllegalArgumentException(
                "Couldn't find a migration from version $fromVersion"
            )
        migrationMap.add(migration)

        // If needed, continue building migration map
        if (migration.toVersion < currentVersion) {
            migrationMap.addAll(
                buildMigrationMap(remainingMigrations, migration.toVersion)
            )
        }

        return migrationMap
    }
}
