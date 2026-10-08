package com.dayaonweb.quoter.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "quotes")
data class StoredQuote(@PrimaryKey val id: String, val text: String, val author: String, val tags: String)

@Entity(tableName = "saved_quotes")
data class SavedQuote(@PrimaryKey val id: String)

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY rowid") fun observeQuotes(): Flow<List<StoredQuote>>
    @Query("SELECT id FROM saved_quotes") fun observeSaved(): Flow<List<String>>
    @Query("SELECT COUNT(*) FROM quotes") suspend fun count(): Int
    @Upsert suspend fun insertQuotes(quotes: List<StoredQuote>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun save(quote: SavedQuote)
    @Query("DELETE FROM saved_quotes WHERE id = :id") suspend fun unsave(id: String)
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1") suspend fun randomQuote(): StoredQuote?
}

@Database(entities = [StoredQuote::class, SavedQuote::class], version = 1, exportSchema = true)
abstract class QuoteDatabase : RoomDatabase() { abstract fun quotes(): QuoteDao }
