package com.example.mynoteapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mynoteapp.model.Contact
import com.example.mynoteapp.data.ContactRepository
import com.example.mynoteapp.model.Note
import com.example.mynoteapp.data.NoteRepository
import com.example.mynoteapp.ui.theme.MyNoteAppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var contactRepository: ContactRepository
    private lateinit var noteRepository: NoteRepository

    private var contacts by mutableStateOf(emptyList<Contact>())
    private var notes by mutableStateOf(emptyList<Note>())

    private var selectedContactId by mutableStateOf(0L)
    private var isEditDialogVisible by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactRepository = ContactRepository(this)
        noteRepository = NoteRepository(this)

        importContacts()
        importNotes()

        setContent {
            MyNoteAppTheme(content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(context = this)
                }
            }
            )
        }
        checkAndRequestContactsPermission()
    }

    private var isContactsImported by mutableStateOf(false)
    private var isNotesImported by mutableStateOf(false)


    private fun importContacts() {
        if (!isContactsImported) {
            lifecycleScope.launch {
                contactRepository.importContacts()
                contacts = contactRepository.getAllContacts()
            }
            isContactsImported = true
        }
    }

    private fun importNotes() {
        if (!isNotesImported) {
            lifecycleScope.launch {
                notes = noteRepository.getAllNotes()
            }
//            Log.d("ImportNodes", notes[0].toString())
            isNotesImported = true;
        }
    }

    private val READ_CONTACTS_PERMISSION_REQUEST_CODE = 1001

    private fun checkAndRequestContactsPermission() {
        // Check if the READ_CONTACTS permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the READ_CONTACTS permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_REQUEST_CODE
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_CONTACTS_PERMISSION_REQUEST_CODE -> {
                // Check if the permission is granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with accessing the contacts provider
                    importContacts()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message to the user)
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    @Composable
    private fun MainScreen(context: Context) {
        Text(
            text = "Contacts",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Column {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                state = rememberLazyListState(), // Add state for scroll position restoration
            ) {
                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
//                Log.d("checkNotes", notes[0].toString())
                itemsIndexed(contacts) { index, contact ->
                    val note = notes.find { it.contactId == contact.id }
                    ContactItem(
                        contact = contact,
                        note = note,
                        onEditClick = {
                            selectedContactId = contact.id
                            isEditDialogVisible = true
                        },
                        onDeleteClick = {
                            lifecycleScope.launch {
                                val note = noteRepository.getNoteForContact(contact.id)
                                note?.let { noteRepository.deleteNote(it) }
                                notes = noteRepository.getAllNotes()
                            }
                        }
                    )
                }
            }

            if (isEditDialogVisible) {
                val note = notes.find { it.contactId == selectedContactId }
                val oldNoteDescription = note?.description ?: "none"

                EditContactDialog(
                    oldNote = oldNoteDescription,
                    onDismiss = { isEditDialogVisible = false },
                    onSave = { newNote ->
                        lifecycleScope.launch {
                            val idNote = noteRepository.getNoteIdForContact(selectedContactId)

                            if (idNote != null) {
                                val tmpNote = Note(
                                    id = idNote,
                                    description = newNote,
                                    contactId = selectedContactId
                                )
                                noteRepository.updateNote(tmpNote)
                            } else {
                                noteRepository.createNote(
                                    Note(
                                        description = newNote,
                                        contactId = selectedContactId
                                    )
                                )

                            }

                            notes = noteRepository.getAllNotes()
                        }
                        isEditDialogVisible = false
                    }
                )
            }

        }
    }


    @Composable
    private fun ContactItem(
        contact: Contact,
        note: Note?,
        onEditClick: () -> Unit,
        onDeleteClick: () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (note != null) {
                        Text(
                            text = "Note: ${note.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditContactDialog(
        oldNote: String,
        onDismiss: () -> Unit,
        onSave: (newNote: String) -> Unit
    ) {
        var newNote by remember { mutableStateOf(TextFieldValue(oldNote)) }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Edit Note",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = newNote,
                    onValueChange = { newNote = it },
                    label = { Text("New Note") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onSave(newNote.text) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Save")
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
