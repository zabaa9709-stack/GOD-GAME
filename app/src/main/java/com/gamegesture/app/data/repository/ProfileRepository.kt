package com.gamegesture.app.data.repository

import android.content.Context
import com.gamegesture.app.data.model.CalibrationProfile
import com.gamegesture.app.data.model.CalibrationTarget
import com.gamegesture.app.data.model.DeviceProfile
import com.gamegesture.app.data.model.ExportedProfilePackage
import com.gamegesture.app.data.model.GestureSequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProfileRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
        classDiscriminator = "actionClass"
    }

    private val profilesFile = File(context.filesDir, "device_profiles.json")
    private val calibrationsFile = File(context.filesDir, "calibrations.json")

    private val _profiles = MutableStateFlow<List<DeviceProfile>>(emptyList())
    val profiles: StateFlow<List<DeviceProfile>> = _profiles.asStateFlow()

    private val _calibrations = MutableStateFlow<List<CalibrationProfile>>(emptyList())
    val calibrations: StateFlow<List<CalibrationProfile>> = _calibrations.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            withContext(Dispatchers.IO) {
                if (profilesFile.exists()) {
                    try {
                        _profiles.value = json.decodeFromString(profilesFile.readText())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val defaults = createDefaultProfiles()
                        _profiles.value = defaults
                        saveProfiles(defaults)
                    }
                } else {
                    val defaults = createDefaultProfiles()
                    _profiles.value = defaults
                    saveProfiles(defaults)
                }

                if (calibrationsFile.exists()) {
                    try {
                        _calibrations.value = json.decodeFromString(calibrationsFile.readText())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _calibrations.value = listOf(createDefaultCalibration())
                    }
                } else {
                    val defaultCal = createDefaultCalibration()
                    _calibrations.value = listOf(defaultCal)
                    saveCalibrations(listOf(defaultCal))
                }
            }
        }
    }

    suspend fun saveProfile(profile: DeviceProfile) = withContext(Dispatchers.IO) {
        val current = _profiles.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
        } else {
            current.add(profile)
        }
        saveProfiles(current)
        _profiles.value = current
    }

    suspend fun saveCalibration(calibration: CalibrationProfile) = withContext(Dispatchers.IO) {
        val current = _calibrations.value.toMutableList()
        val index = current.indexOfFirst { it.id == calibration.id || it.profileId == calibration.profileId }
        if (index >= 0) {
            current[index] = calibration
        } else {
            current.add(calibration)
        }
        saveCalibrations(current)
        _calibrations.value = current
    }

    fun getCalibrationForProfile(profileId: String): CalibrationProfile? {
        return _calibrations.value.find { it.profileId == profileId } ?: _calibrations.value.firstOrNull()
    }

    fun exportProfilePackage(
        profileId: String,
        gestures: List<GestureSequence>
    ): String {
        val profile = _profiles.value.find { it.id == profileId } ?: _profiles.value.first()
        val cal = getCalibrationForProfile(profileId)
        val packageData = ExportedProfilePackage(
            profile = profile,
            calibration = cal,
            gestures = gestures.filter { it.profileId == profileId || it.profileId == "default" }
        )
        return json.encodeToString(packageData)
    }

    suspend fun importProfilePackage(jsonString: String): ExportedProfilePackage = withContext(Dispatchers.IO) {
        val imported = json.decodeFromString<ExportedProfilePackage>(jsonString)
        saveProfile(imported.profile)
        imported.calibration?.let { saveCalibration(it) }
        imported
    }

    private fun saveProfiles(list: List<DeviceProfile>) {
        try {
            profilesFile.writeText(json.encodeToString(list))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveCalibrations(list: List<CalibrationProfile>) {
        try {
            calibrationsFile.writeText(json.encodeToString(list))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDefaultProfiles(): List<DeviceProfile> {
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val dpi = displayMetrics.densityDpi

        val gcd = gcd(width, height)
        val aspect = "${width / gcd}:${height / gcd}"

        return listOf(
            DeviceProfile(
                id = "default",
                name = "Native Device",
                screenWidth = width,
                screenHeight = height,
                aspectRatio = aspect,
                densityDpi = dpi,
                isDefault = true,
                description = "Primary physical display detected from device metrics."
            ),
            DeviceProfile(
                id = "profile_tablet",
                name = "Gaming Tablet (16:10)",
                screenWidth = 1600,
                screenHeight = 2560,
                aspectRatio = "16:10",
                densityDpi = 320,
                isDefault = false,
                description = "High-res tablet configuration for spacious two-thumb controls."
            ),
            DeviceProfile(
                id = "profile_compact",
                name = "Compact Phone (20:9)",
                screenWidth = 1080,
                screenHeight = 2400,
                aspectRatio = "20:9",
                densityDpi = 440,
                isDefault = false,
                description = "Standard tall modern smartphone aspect ratio."
            )
        )
    }

    private fun createDefaultCalibration(): CalibrationProfile {
        return CalibrationProfile(
            id = "default_calibration",
            profileId = "default",
            profileName = "Default Screen Calibration",
            screenWidth = context.resources.displayMetrics.widthPixels,
            screenHeight = context.resources.displayMetrics.heightPixels,
            targets = listOf(
                CalibrationTarget(
                    id = "target_aim_origin",
                    label = "Aim Control Center",
                    description = "Center of touch rotation area for camera movements.",
                    normalizedX = 0.65f,
                    normalizedY = 0.55f
                ),
                CalibrationTarget(
                    id = "target_action_button",
                    label = "Action / Fire Button",
                    description = "Screen button to trigger tactical items or primary fire.",
                    normalizedX = 0.85f,
                    normalizedY = 0.75f
                )
            ),
            rotationalCenterNormalizedX = 0.65f,
            rotationalCenterNormalizedY = 0.55f,
            rotationalRadiusNormalized = 0.22f
        )
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = y
            y = x % y
            x = t
        }
        return if (x == 0) 1 else x
    }
}
