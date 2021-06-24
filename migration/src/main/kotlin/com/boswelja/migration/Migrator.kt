package com.boswelja.migration

/**
 * A class for executing [Migration]s.
 * @param migrations The available [Migration]s to use.
 */
abstract class Migrator(
    private val migrations: List<Migration>
) {

    /**
     * Get the old version to migrate from.
     */
    abstract suspend fun getOldVersion(): Int

    /**
     * Gets the new version to migrate to.
     */
    abstract suspend fun getNewVersion(): Int

    suspend fun migrate() {
        // Get versions
        val oldVersion = getOldVersion()
        val newVersion = getNewVersion()

        // Build migration map
        val migrationMap = buildMigrationMap(migrations, oldVersion, newVersion)

        migrationMap.forEach { migration ->
            migration.migrate()
        }
    }

    /**
     * A recursive function to build a list of [Migration]s to be run in a sequential order.
     */
    internal fun buildMigrationMap(
        migrations: List<Migration>,
        oldVersion: Int,
        newVersion: Int
    ): List<Migration> {
        val migrationMap = mutableListOf<Migration>()
        val (migrationsFromOldVersion, remainingMigrations) = migrations.separate {
            it.fromVersion == oldVersion
        }
        if (migrationsFromOldVersion.count() != 1) {
            throw IllegalArgumentException(
                "Error getting a migration from version $oldVersion"
            )
        } else {
            val migration = migrationsFromOldVersion.first()
            migrationMap.add(migration)
            if (migration.toVersion < newVersion) {
                migrationMap.addAll(
                    buildMigrationMap(remainingMigrations, migration.toVersion, newVersion)
                )
            }
        }
        return migrationMap
    }
}
