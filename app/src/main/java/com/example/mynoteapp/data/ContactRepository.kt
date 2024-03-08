package com.example.mynoteapp.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.example.mynoteapp.database.NoteAppDatabase
import com.example.mynoteapp.database.ContactLocalDataSource
import com.example.mynoteapp.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ContactRepository(context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
    private val contactLocalDataSource: ContactLocalDataSource = NoteAppDatabase.getInstance(context).contactLocalDataSource()

    suspend fun importContacts() {
        withContext(Dispatchers.IO) {
            val contactsFromProvider = queryContactsFromProvider()
            saveContactsToDatabase(contactsFromProvider)
        }
    }

    @SuppressLint("Range")
    private fun queryContactsFromProvider(): List<Contact> {
        val contactsList = mutableListOf<Contact>()

        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val contact = Contact(id, name, number)
                contactsList.add(contact)
            }
        }

        return contactsList
    }

    private suspend fun saveContactsToDatabase(contacts: List<Contact>) {
        val existingContacts: List<Contact> = contactLocalDataSource.getAllContacts()
        for (contact in contacts) {
            if (!existingContacts.contains(contact)) {
                contactLocalDataSource.insertContact(contact)
            }
        }
        for (econtact in existingContacts) {
            if (!contacts.contains(econtact)) {
                contactLocalDataSource.deleteContact(econtact)
            }
        }
    }

    suspend fun getAllContacts(): List<Contact> {
        return contactLocalDataSource.getAllContacts()
    }

    suspend fun updateContact(contact: Contact) {
        contactLocalDataSource.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        contactLocalDataSource.deleteContact(contact)
    }


}