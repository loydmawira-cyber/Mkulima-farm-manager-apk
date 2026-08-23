package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auth.AuthManager
import com.example.data.FarmRepository
import com.example.data.FirestoreSyncEngine
import com.example.data.MkulimaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FarmViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val scope = CoroutineScope(Dispatchers.IO)
        val database = MkulimaDatabase.getDatabase(context.applicationContext, scope)
        val farmDao = database.farmDao()
        val syncEngine = FirestoreSyncEngine(context.applicationContext, farmDao)
        val repository = FarmRepository(farmDao, syncEngine)
        val authManager = AuthManager(context.applicationContext, repository)
        return FarmViewModel(repository, authManager) as T
    }
}
