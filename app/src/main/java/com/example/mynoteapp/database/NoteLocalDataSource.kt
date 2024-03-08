package com.example.mynoteapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mynoteapp.model.Note


@Dao
interface NoteLocalDataSource {

    @Query("SELECT * FROM notes")
    suspend fun fetchAllNotes(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE contactId = :contactId")
    suspend fun getNoteForContact(contactId: Long) : Note?


    @Query("SELECT id FROM notes WHERE contactId =:contactId")
    suspend fun getNoteIdForContact(contactId: Long): Long?


}