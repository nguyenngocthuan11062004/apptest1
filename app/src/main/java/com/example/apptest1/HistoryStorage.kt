package com.example.apptest1

import android.content.Context
import com.example.apptest1.model.HistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryStorage {

    private const val PREF_NAME = "history_pref"
    private const val KEY_HISTORY = "history_list"

    private val gson = Gson()

    // ======================
    // SAVE 1 ITEM
    // ======================
    fun save(context: Context, item: HistoryItem) {
        val list = getAll(context).toMutableList()
        list.add(0, item) // newest on top
        saveAll(context, list)
    }

    // ======================
    // GET ALL
    // ======================
    fun getAll(context: Context): List<HistoryItem> {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = pref.getString(KEY_HISTORY, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ======================
    // CLEAR (OPTIONAL)
    // ======================
    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY)
            .apply()
    }

    // ======================
    // INTERNAL SAVE
    // ======================
    private fun saveAll(context: Context, list: List<HistoryItem>) {
        val json = gson.toJson(list)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, json)
            .apply()
    }
}
