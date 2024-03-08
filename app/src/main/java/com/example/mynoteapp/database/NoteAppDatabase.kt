package com.example.mynoteapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mynoteapp.model.Contact
import com.example.mynoteapp.model.Note


@Database(entities = [Contact::class, Note::class], version = 1, exportSchema = false)
abstract class NoteAppDatabase : RoomDatabase() {

    abstract fun contactLocalDataSource(): ContactLocalDataSource

    abstract fun noteLocalDataSource(): NoteLocalDataSource

    companion object {
        private const val DATABASE_NAME = "mynoteapp_database"

        @Volatile
        private var INSTANCE: NoteAppDatabase? = null

        fun getInstance(context: Context): NoteAppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteAppDatabase::class.java,
                        DATABASE_NAME
                    )
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
