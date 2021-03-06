package com.coroutinesflow.frameworks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coroutinesflow.features.hero_details.data.entities.MarvelHeroDetailsTable
import com.coroutinesflow.features.hero_details.data.local_datastore.MarvelHeroDerailsDao
import com.coroutinesflow.features.heroes_home.data.entities.MarvelHomeTable
import com.coroutinesflow.features.heroes_home.data.local_datastore.db.MarvelCharactersDao

@Database(
    entities = [MarvelHomeTable::class, MarvelHeroDetailsTable::class],
    version = 1
)
@TypeConverters(
    URlsDataConverter::class,
    ItemsDataConverter::class, ResultsDataConverter::class
)
abstract class MarvelCharactersDB : RoomDatabase() {

    abstract fun marvelCharactersDao(): MarvelCharactersDao
    abstract fun marvelHeroDerailsDao(): MarvelHeroDerailsDao

    companion object {
        @Volatile
        private var instance: MarvelCharactersDB? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
                instance
                    ?: buildDatabase(
                        context
                    ).also { instance = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                MarvelCharactersDB::class.java,
                "MarvelCharactersDB.db"
            )
                .allowMainThreadQueries()
                .build()
    }
}