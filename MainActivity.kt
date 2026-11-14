package com.amarkhamar.app

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class FarmEntry(val id: Long, val title: String, val detail: String)

class FarmDb(context: android.content.Context) : SQLiteOpenHelper(context, "amar_khamar.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE cattle (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, detail TEXT NOT NULL)")
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, detail TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(table: String, title: String, detail: String) {
        val values = ContentValues().apply { put("title", title); put("detail", detail) }
        writableDatabase.insert(table, null, values)
    }
    fun get(table: String): List<FarmEntry> {
        val rows = mutableListOf<FarmEntry>()
        readableDatabase.rawQuery("SELECT id,title,detail FROM $table ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) rows.add(FarmEntry(c.getLong(0), c.getString(1), c.getString(2)))
        }
        return rows
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { FarmApp() } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmApp() {
    val db = remember { FarmDb(LocalContext.current) }
    var tab by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    val table = if (tab == 0) "cattle" else "transactions"
    val entries = remember(tab, refresh) { db.get(table) }
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("আমার খামার") }) }) { pad ->
            Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
                Text("খামার ব্যবস্থাপনা • অফলাইন", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("গরুর তালিকা") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("আয়-ব্যয়") })
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(title, { title = it }, label = { Text(if (tab == 0) "গরুর নাম / নম্বর" else "খাতের নাম") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(detail, { detail = it }, label = { Text(if (tab == 0) "জাত / বিবরণ" else "টাকার পরিমাণ ও বিবরণ") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (title.isNotBlank() && detail.isNotBlank()) {
                        db.add(table, title.trim(), detail.trim()); title = ""; detail = ""; refresh++
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("সংরক্ষণ করুন") }
                Spacer(Modifier.height(16.dp))
                Text(if (tab == 0) "সংরক্ষিত গরু (${entries.size})" else "সংরক্ষিত হিসাব (${entries.size})")
                LazyColumn { items(entries, key = { it.id }) { item ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) { Text(item.title); Text(item.detail) }
                    }
                } }
            }
        }
    }
}
