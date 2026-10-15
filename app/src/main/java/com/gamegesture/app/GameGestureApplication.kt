package com.gamegesture.app

import android.app.Application
import com.gamegesture.app.data.repository.GestureRepository
import com.gamegesture.app.data.repository.ProfileRepository
import com.gamegesture.app.data.repository.SettingsRepository

class GameGestureApplication : Application() {

    companion object {
        lateinit var instance: GameGestureApplication
            private set
    }

    lateinit var gestureRepository: GestureRepository
        private set

    lateinit var profileRepository: ProfileRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        gestureRepository = GestureRepository(this)
        profileRepository = ProfileRepository(this)
        settingsRepository = SettingsRepository(this)
    }
}
