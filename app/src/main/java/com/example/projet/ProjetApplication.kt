package com.example.projet

import android.app.Application
import com.example.projet.data.AppDatabase

class ProjetApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
}
