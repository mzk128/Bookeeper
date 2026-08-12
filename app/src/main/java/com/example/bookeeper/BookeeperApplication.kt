package com.example.bookeeper

import android.app.Application
import com.example.bookeeper.data.AppContainer
import com.example.bookeeper.data.DefaultAppContainer

class BookeeperApplication : Application() {
    val container: AppContainer by lazy { DefaultAppContainer(this) }
}
