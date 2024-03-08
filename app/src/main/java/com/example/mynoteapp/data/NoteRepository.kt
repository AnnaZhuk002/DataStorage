package com.example.mynoteapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.mynoteapp.database.NoteAppDatabase
import com.example.mynoteapp.database.NoteLocalDataSource
import com.example.mynoteapp.model.Note

class NoteRepository(context: Context) {

    private val noteDataSource: NoteLocalDataSource =
        NoteAppDatabase.getInstance(context).noteLocalDataSource()

    suspend fun getAllNotes(): List<Note> = noteDataSource.fetchAllNotes()

    suspend fun createNote(note: Note): Long = noteDataSource.saveNote(note)

    suspend fun updateNote(note: Note) = noteDataSource.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDataSource.deleteNote(note)

    suspend fun getNoteForContact(contactId: Long): Note? =
        noteDataSource.getNoteForContact(contactId)

    suspend fun getNoteIdForContact(contactId: Long): Long? =
        noteDataSource.getNoteIdForContact(contactId)
}
