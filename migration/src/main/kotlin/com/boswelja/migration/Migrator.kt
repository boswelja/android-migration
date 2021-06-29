package com.boswelja.migration

/**
 * A class for executing [Migration]s.
 * @param currentVersion The current version to use when building migration maps.
 * @param abortOnError Whether [migrate] should abort when a migration fails.
 * @param migrations The available [Migration]s to use.
 */
abstract class Migrator(
    private val currentVersion: Int,
    private val abortOnError: Boolean = true,
    private val migrations: List<Migration>
) {

    /**
     * Get the old version to migrate from.
     */
    abstract suspend fun getOldVersion(): Int

    /**
     * Called when we've successfully migrated to a new version, and there are no pending migrations
     * left.
     * @param version The version we migrated to.
     */
    abstract suspend fun onMigratedTo(version: Int)

    suspend fun migrate(): Boolean {
        // Get versions
        val oldVersion = getOldVersion()

        // Check old version isn't greater than current version
        check(oldVersion <= currentVersion)

        // Build migration map
        val migrationMap = buildMigrationMap(migrations, oldVersion)

        var result = true
        var version = oldVersion
        migrationMap.forEach { migration ->
            result = migration.migrate()
            if (result) {
                version = migration.toVersion
            } else if (abortOnError) {
                onMigratedTo(version)
                return false
            }
        }

        onMigratedTo(version)

        return result
    }

    /**
     * A recursive function to build a list of [Migration]s to be run in a sequential order. Note
     * this will throw [IllegalArgumentException] if a migration cannot be found from a version.
     * @param migrations A [List] of available [Migration]s.
     * @param fromVersion The version to build a migration map from.
     * @return A [List] of ordered [Migration]s that can be run to reach [currentVersion].
     */
    internal suspend fun buildMigrationMap(
        migrations: List<Migration>,
        fromVersion: Int
    ): List<Migration> {
        val migrationMap = mutableListOf<Migration>()

        // Get next available migrations, and all remaining migrations
        val (migrationsFromOldVersion, remainingMigrations) = migrations.separate {
            it.shouldMigrate(fromVersion)
        }

        // Determine next migration and add to migration map
        val migration = migrationsFromOldVersion.maxByOrNull { it.toVersion }
        checkNotNull(migration) { "Couldn't find a migration from version $fromVersion" }

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
