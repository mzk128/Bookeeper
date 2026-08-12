package com.example.bookeeper.data

import android.content.Context
import com.example.bookeeper.data.local.BookeeperDatabase
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.data.repository.OfflineBookeeperRepository

interface AppContainer {
    val repository: BookeeperRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database: BookeeperDatabase by lazy {
        BookeeperDatabase.getInstance(context)
    }

    override val repository: BookeeperRepository by lazy {
        OfflineBookeeperRepository(database)
    }
}
