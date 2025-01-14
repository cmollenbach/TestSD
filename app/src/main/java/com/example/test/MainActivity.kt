package com.example.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.lifecycleScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import kotlin.math.*
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.Intent
import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope

// Extension property on Context to access DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


object PreferencesKeys {
    val TARGET_DOUGH_WEIGHT = intPreferencesKey("target_dough_weight")
    val HYDRATION_PERCENTAGE = intPreferencesKey("hydration_percentage")
    val STARTER_PERCENTAGE = intPreferencesKey("starter_percentage")
    val STARTER_HYDRATION = intPreferencesKey("starter_hydration")
    val SALT_PERCENTAGE = intPreferencesKey("salt_percentage")
    // New keys for timings
    val MIX_TIME = stringPreferencesKey("mix_time")
    val AUTOLYSE_TIME = intPreferencesKey("autolyse_time")
    val ADD_STARTER_SALT_TIME = stringPreferencesKey("add_starter_salt_time")
    val BULK_FERMENTATION_TIME = intPreferencesKey("bulk_fermentation_time")
}

// Data Classes
data class RecipeResult(
    val flourWeight: Double,
    val waterWeight: Double,
    val starterWeight: Double,
    val saltWeight: Double
)

data class RecipeInputs(
    val targetDoughWeight: Int = 0,
    val hydrationPercentage: Int = 0,
    val starterPercentage: Int = 0,
    val starterHydration: Int = 0,
    val saltPercentage: Int = 0
)

data class RecipeInputsErrors(
    val targetDoughWeightError: Boolean = false,
    val hydrationPercentageError: Boolean = false,
    val starterPercentageError: Boolean = false,
    val starterHydrationError: Boolean = false,
    val saltPercentageError: Boolean = false
)

// Timing Data Class
data class TimingInputsData(
    val mixTime: LocalTime = LocalTime.now(),
    val autolyseTime: Int = 30,
    val addStarterSaltTime: LocalTime = LocalTime.now(),
    val bulkFermentationTime: Int = 240
)

// Saver object for RecipeInputs
val RecipeInputsSaver = Saver<RecipeInputs, Map<String, Any>>(
    save = { recipeInputs ->
        mapOf(
            "targetDoughWeight" to recipeInputs.targetDoughWeight,
            "hydrationPercentage" to recipeInputs.hydrationPercentage,
            "starterPercentage" to recipeInputs.starterPercentage,
            "starterHydration" to recipeInputs.starterHydration,
            "saltPercentage" to recipeInputs.saltPercentage
        )
    },
    restore = { map ->
        RecipeInputs(
            targetDoughWeight = map["targetDoughWeight"] as Int,
            hydrationPercentage = map["hydrationPercentage"] as Int,
            starterPercentage = map["starterPercentage"] as Int,
            starterHydration = map["starterHydration"] as Int,
            saltPercentage = map["saltPercentage"] as Int
        )
    }
)
// Saver object for RecipeInputsErrors
val RecipeInputsErrorsSaver = Saver<RecipeInputsErrors, Map<String, Any>>(
    save = { recipeInputsErrors ->
        mapOf(
            "targetDoughWeightError" to recipeInputsErrors.targetDoughWeightError,
            "hydrationPercentageError" to recipeInputsErrors.hydrationPercentageError,
            "starterPercentageError" to recipeInputsErrors.starterPercentageError,
            "starterHydrationError" to recipeInputsErrors.starterHydrationError,
            "saltPercentageError" to recipeInputsErrors.saltPercentageError
        )
    },
    restore = { map ->
        RecipeInputsErrors(
            targetDoughWeightError = map["targetDoughWeightError"] as Boolean,
            hydrationPercentageError = map["hydrationPercentageError"] as Boolean,
            starterPercentageError = map["starterPercentageError"] as Boolean,
            starterHydrationError = map["starterHydrationError"] as Boolean,
            saltPercentageError = map["saltPercentageError"] as Boolean
        )
    }
)

// Saver object for TimingInputsData
val TimingInputsDataSaver = Saver<TimingInputsData, Map<String, Any>>(
    save = { timingInputs ->
        mapOf(
            "mixTime" to timingInputs.mixTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            "autolyseTime" to timingInputs.autolyseTime,
            "addStarterSaltTime" to timingInputs.addStarterSaltTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            "bulkFermentationTime" to timingInputs.bulkFermentationTime
        )
    },
    restore = { map ->
        TimingInputsData(
            mixTime = LocalTime.parse(map["mixTime"] as String),
            autolyseTime = map["autolyseTime"] as Int,
            addStarterSaltTime = LocalTime.parse(map["addStarterSaltTime"] as String),
            bulkFermentationTime = map["bulkFermentationTime"] as Int
        )
    }
)

val df = DecimalFormat("#")

// Main Activity
class MainActivity : ComponentActivity() , CoroutineScope by MainScope() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Permission denied, show a message or disable notifications
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySourdoughTheme {
                var showDialog by remember { mutableStateOf(false) }
                Surface(color = MaterialTheme.colorScheme.background) {
                    SourdoughCalculator(
                        loadRecipeInputs = { loadRecipeInputs(this) },
                        onSaveRecipeInputs = { context, recipeInputs ->
                            lifecycleScope.launch {
                                saveRecipeInputs(context, recipeInputs)
                            }
                        },
                        loadTimingInputs = { loadTimingInputs(this) },
                        onSaveTimingInputs = { context, timingInputs ->
                            lifecycleScope.launch {
                                saveTimingInputs(context, timingInputs)
                            }
                        }
                    )
                    if (showDialog) {
                        PermissionRationaleDialog(
                            onDismiss = { showDialog = false },
                            onConfirm = {
                                showDialog = false
                                openAppSettings()
                            }
                        )
                    }
                }
                // Check for permission and show dialog if needed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        showDialog = true
                    }
                }
            }
        }

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS permission on Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel the CoroutineScope when the activity is destroyed
    }

    private suspend fun saveRecipeInputs(context: Context, recipeInputs: RecipeInputs) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_DOUGH_WEIGHT] = recipeInputs.targetDoughWeight
            preferences[PreferencesKeys.HYDRATION_PERCENTAGE] = recipeInputs.hydrationPercentage
            preferences[PreferencesKeys.STARTER_PERCENTAGE] = recipeInputs.starterPercentage
            preferences[PreferencesKeys.STARTER_HYDRATION] = recipeInputs.starterHydration
            preferences[PreferencesKeys.SALT_PERCENTAGE] = recipeInputs.saltPercentage
        }
    }

    private fun loadRecipeInputs(context: Context): Flow<RecipeInputs> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            RecipeInputs(
                targetDoughWeight = preferences[PreferencesKeys.TARGET_DOUGH_WEIGHT] ?: 0,
                hydrationPercentage = preferences[PreferencesKeys.HYDRATION_PERCENTAGE] ?: 0,
                starterPercentage = preferences[PreferencesKeys.STARTER_PERCENTAGE] ?: 0,
                starterHydration = preferences[PreferencesKeys.STARTER_HYDRATION] ?: 0,
                saltPercentage = preferences[PreferencesKeys.SALT_PERCENTAGE] ?: 0
            )
        }

    // Load Timing Inputs from DataStore
    private fun loadTimingInputs(context: Context): Flow<TimingInputsData> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            TimingInputsData(
                mixTime = LocalTime.parse(preferences[PreferencesKeys.MIX_TIME] ?: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))),
                autolyseTime = preferences[PreferencesKeys.AUTOLYSE_TIME] ?: 30,
                addStarterSaltTime = LocalTime.parse(preferences[PreferencesKeys.ADD_STARTER_SALT_TIME] ?: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))),
                bulkFermentationTime = preferences[PreferencesKeys.BULK_FERMENTATION_TIME] ?: 240
            )
        }

    // Save Timing Inputs to DataStore
    private suspend fun saveTimingInputs(context: Context, timingInputs: TimingInputsData) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIX_TIME] = timingInputs.mixTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            preferences[PreferencesKeys.AUTOLYSE_TIME] = timingInputs.autolyseTime
            preferences[PreferencesKeys.ADD_STARTER_SALT_TIME] = timingInputs.addStarterSaltTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            preferences[PreferencesKeys.BULK_FERMENTATION_TIME] = timingInputs.bulkFermentationTime
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}

// -------------------- Reusable UI Components --------------------

@Composable
fun PermissionRationaleDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("This app requires the permission to post notifications to remind you about the steps in the sourdough making process. Please grant the permission in the app settings.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun TwoColumnRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    output: String,
    outputLabel: String,
    isError: Boolean,
    errorMsg: String,
    inputWeight: Float,
    outputWeight: Float,
    recipeInputs: RecipeInputs,
    onRecipeInputsChange: (RecipeInputs) -> Unit,
    onSaveRecipeInputs: (Context, RecipeInputs) -> Unit,
    previousRecipeInputs: RecipeInputs,
    updatePreviousRecipeInputs: (RecipeInputs) -> Unit,
    df: DecimalFormat
) {
    val context = LocalContext.current
    //val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppPadding.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val filteredValue = if (keyboardType == KeyboardType.Number) {
                    newValue.filter { it.isDigit() }
                } else {
                    newValue
                }
                onValueChange(filteredValue)

                val updatedRecipeInputs = when (label) {
                    "Target dough weight" -> recipeInputs.copy(targetDoughWeight = filteredValue.toIntOrNull() ?: 0)
                    "Hydration" -> recipeInputs.copy(hydrationPercentage = filteredValue.toIntOrNull() ?: 0)
                    "Starter" -> recipeInputs.copy(starterPercentage = filteredValue.toIntOrNull() ?: 0)
                    "Salt" -> recipeInputs.copy(saltPercentage = filteredValue.toIntOrNull() ?: 0)
                    "Starter hydration" -> recipeInputs.copy(starterHydration = filteredValue.toIntOrNull() ?: 0)
                    else -> recipeInputs
                }
                onRecipeInputsChange(updatedRecipeInputs)
            },
            label = {Text(
                text = label,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = AppFontSize.medium
            )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .weight(inputWeight)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && recipeInputs != previousRecipeInputs) {
                        // Use the CoroutineScope from MainActivity:
                        lifecycleScope.launch {
                            onSaveRecipeInputs(context, recipeInputs)
                            updatePreviousRecipeInputs(recipeInputs)
                        }
                    }
                },
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = AppFontSize.medium),
            isError = isError,
            trailingIcon = {
                if (keyboardType == KeyboardType.Number && label != "Target dough weight") {
                    Text("%")
                }
            }
        )
        Spacer(modifier = Modifier.width(AppPadding.large))
        OutputBox(
            text = output,
            outputLabel = outputLabel,
            modifier = Modifier.weight(outputWeight)
        )
    }
    if (isError) {
        Text(
            text = errorMsg,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            fontSize = AppFontSize.small,
            modifier = Modifier.padding(start = AppPadding.extraLarge)
        )
    }
}

@Composable
fun OutputBox(
    text: String,
    outputLabel: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = {},
        label = {
            if (outputLabel.isNotEmpty()) {
                Text(
                    text = outputLabel,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = AppFontSize.medium
                )
            }
        },
        modifier = modifier,
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = AppFontSize.medium,
            fontWeight = FontWeight.Bold
        ),
        readOnly = true,
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.Gray,
        )
    )
}

// -------------------- Sourdough Calculator Composable --------------------

@Composable
fun SourdoughCalculator(
    loadRecipeInputs: (Context) -> Flow<RecipeInputs>,
    onSaveRecipeInputs: suspend (Context, RecipeInputs) -> Unit,
    loadTimingInputs: (Context) -> Flow<TimingInputsData>,
    onSaveTimingInputs: suspend (Context, TimingInputsData) -> Unit
) {
    val context = LocalContext.current
    var recipeInputs by rememberSaveable(stateSaver = RecipeInputsSaver) { mutableStateOf(RecipeInputs()) }
    var recipeInputsErrors by rememberSaveable(stateSaver = RecipeInputsErrorsSaver) { mutableStateOf(RecipeInputsErrors()) }
    var timingInputs by rememberSaveable(stateSaver = TimingInputsDataSaver) { mutableStateOf(TimingInputsData()) }
    var previousRecipeInputs by rememberSaveable(stateSaver = RecipeInputsSaver) {
        mutableStateOf(RecipeInputs())
    }
    var previousTimingInputs by rememberSaveable(stateSaver = TimingInputsDataSaver) { mutableStateOf(TimingInputsData()) }

    // Load saved values when the composable is created
    LaunchedEffect(Unit) {
        loadRecipeInputs(context).collect { savedInputs ->
            recipeInputs = savedInputs
            previousRecipeInputs = savedInputs
        }
        loadTimingInputs(context).collect { savedInputs ->
            timingInputs = savedInputs
            previousTimingInputs = savedInputs
        }
    }

    // Update recipeInputsErrors whenever recipeInputs changes
    LaunchedEffect(recipeInputs) {
        recipeInputsErrors = RecipeInputsErrors(
            targetDoughWeightError = recipeInputs.targetDoughWeight < 100,
            hydrationPercentageError = recipeInputs.hydrationPercentage > 100,
            starterPercentageError = recipeInputs.starterPercentage > 100,
            starterHydrationError = recipeInputs.starterHydration > 100,
            saltPercentageError = recipeInputs.saltPercentage > 100
        )
    }

    // State variables for timing inputs
    var mixTime by rememberSaveable { mutableStateOf(timingInputs.mixTime) }
    var mixButtonEnabled by rememberSaveable { mutableStateOf(true) }
    var autolyseTime by rememberSaveable { mutableStateOf(timingInputs.autolyseTime) }
    var formattedAutolyseTime by rememberSaveable { mutableStateOf("") }
    var addStarterSaltTime by rememberSaveable { mutableStateOf(timingInputs.addStarterSaltTime) }
    var addStarterSaltButtonEnabled by rememberSaveable { mutableStateOf(false) }
    var formattedAddStarterSaltTime by rememberSaveable { mutableStateOf("") }
    var bulkFermentationTime by rememberSaveable { mutableStateOf(timingInputs.bulkFermentationTime) }
    var formattedBulkFermentationEndTime by rememberSaveable { mutableStateOf("") }

    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    // Function to add time
    val addTime: (LocalTime, Int) -> String = { time, minutesToAdd ->
        try {
            time.plusMinutes(minutesToAdd.toLong()).format(formatter)
        } catch (e: Exception) {
            "Invalid"
        }
    }

    // Calculated states using the addTime function
    val calculatedAutolyseTime by remember(mixTime, autolyseTime) {
        derivedStateOf { addTime(mixTime, autolyseTime) }
    }
    val calculatedBulkFermentationEndTime by remember(addStarterSaltTime, bulkFermentationTime) {
        derivedStateOf { addTime(addStarterSaltTime, bulkFermentationTime) }
    }

    // Observe the calculated times and update formatted times
    LaunchedEffect(calculatedAutolyseTime) {
        formattedAutolyseTime = calculatedAutolyseTime
    }
    LaunchedEffect(calculatedBulkFermentationEndTime) {
        formattedBulkFermentationEndTime = calculatedBulkFermentationEndTime
    }

    // Save Timing Inputs to DataStore when they change
    LaunchedEffect(mixTime, autolyseTime, addStarterSaltTime, bulkFermentationTime) {
        val newTimingInputs = TimingInputsData(
            mixTime = mixTime,
            autolyseTime = autolyseTime,
            addStarterSaltTime = addStarterSaltTime,
            bulkFermentationTime = bulkFermentationTime
        )
        if (newTimingInputs != timingInputs) {
            onSaveTimingInputs(context, newTimingInputs)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(AppPadding.extraLarge),
            verticalArrangement = Arrangement.spacedBy(AppPadding.large)
        ) {
            // App Title
            Text(
                text = "Sourdough Recipe",
                fontWeight = FontWeight.Bold,
                fontSize = AppFontSize.extraLarge,
                modifier = Modifier.padding(bottom = AppPadding.medium)
            )

            // Recipe Inputs Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(AppPadding.large)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Recipe Inputs",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppFontSize.large,
                        modifier = Modifier.padding(bottom = AppPadding.medium)
                    )

                    // Recipe Inputs
                    RecipeInputs(
                        recipeInputs,
                        recipeInputsErrors,
                        onRecipeInputsChange = { updatedInputs ->
                            recipeInputs = updatedInputs
                            // Calculate and update errors based on the new inputs
                            recipeInputsErrors = RecipeInputsErrors(
                                targetDoughWeightError = updatedInputs.targetDoughWeight < 100,
                                hydrationPercentageError = updatedInputs.hydrationPercentage > 100,
                                starterPercentageError = updatedInputs.starterPercentage > 100,
                                starterHydrationError = updatedInputs.starterHydration > 100,
                                saltPercentageError = updatedInputs.saltPercentage > 100
                            )
                        },
                        df = df,
                        onSaveRecipeInputs = { context, inputs ->
                            // Use the MainActivity's scope to launch:
                            lifecycleScope.launch {
                                onSaveRecipeInputs(context, inputs)
                            }
                        },
                        previousRecipeInputs = previousRecipeInputs,
                        updatePreviousRecipeInputs = { updatedInputs ->
                            previousRecipeInputs = updatedInputs
                        }
                    )
                }
            }

            // Timing Inputs Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(AppPadding.large)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Timing Inputs",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppFontSize.large,
                        modifier = Modifier.padding(bottom = AppPadding.medium)
                    )

                    // Timing Inputs
                    TimingInputs(
                        mixTime = mixTime,
                        mixButtonEnabled = mixButtonEnabled,
                        autolyseTime = autolyseTime,
                        formattedAutolyseTime = formattedAutolyseTime,
                        addStarterSaltTime = addStarterSaltTime,
                        addStarterSaltButtonEnabled = addStarterSaltButtonEnabled,
                        formattedAddStarterSaltTime = formattedAddStarterSaltTime,
                        bulkFermentationTime = bulkFermentationTime,
                        formattedBulkFermentationEndTime = formattedBulkFermentationEndTime,
                        formatter = formatter,
                        onMixTimeChange = { mixTime = it },
                        onMixButtonEnabledChange = { mixButtonEnabled = it },
                        onAutolyseTimeChange = { autolyseTime = it },
                        onFormattedAutolyseTimeChange = { formattedAutolyseTime = it },
                        onAddStarterSaltTimeChange = { addStarterSaltTime = it },
                        onAddStarterSaltButtonEnabledChange = { addStarterSaltButtonEnabled = it },
                        onFormattedAddStarterSaltTimeChange = { formattedAddStarterSaltTime = it },
                        onBulkFermentationTimeChange = { bulkFermentationTime = it },
                        onFormattedBulkFermentationEndTimeChange = { formattedBulkFermentationEndTime = it },
                        onSaveTimingInputs = { timingInputs ->
                            // Use the MainActivity's scope to launch:
                            lifecycleScope.launch {
                                onSaveTimingInputs(context, timingInputs)
                            }
                        },
                        timingInputs = timingInputs,
                        onTimingInputsChange = { updatedInputs ->
                            timingInputs = updatedInputs
                        },
                        previousTimingInputs = previousTimingInputs,
                        updatePreviousTimingInputs = { updatedInputs ->
                            previousTimingInputs = updatedInputs
                        }
                    )
                }
            }

            // Reset Button
            Button(
                onClick = {
                    // Reset ONLY these variables:
                    mixTime = LocalTime.now()
                    mixButtonEnabled = true
                    formattedAutolyseTime = ""
                    addStarterSaltTime = LocalTime.now()
                    addStarterSaltButtonEnabled = false
                    formattedAddStarterSaltTime = ""
                    formattedBulkFermentationEndTime = ""

                    // Do NOT reset autolyseTime and bulkFermentationTime
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Reset Timers",
                    fontSize = AppFontSize.medium
                )
            }
        }
    }
}

@Composable
fun RecipeInputs(
    recipeInputs: RecipeInputs,
    recipeInputsErrors: RecipeInputsErrors,
    onRecipeInputsChange: (RecipeInputs) -> Unit,
    df: DecimalFormat,
    onSaveRecipeInputs: (Context, RecipeInputs) -> Unit,
    previousRecipeInputs: RecipeInputs,
    updatePreviousRecipeInputs: (RecipeInputs) -> Unit
) {
    val context = LocalContext.current

    val results = calculateRecipe(
        recipeInputs.targetDoughWeight,
        recipeInputs.hydrationPercentage,
        recipeInputs.starterPercentage,
        recipeInputs.starterHydration,
        recipeInputs.saltPercentage
    )

    Column {
        TwoColumnRow(
            label = "Target dough weight",
            value = recipeInputs.targetDoughWeight.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = {
                val filteredInput = it.filter { it.isDigit() }
                val newTargetDoughWeight = filteredInput.toIntOrNull() ?: 0
                onRecipeInputsChange(recipeInputs.copy(targetDoughWeight = newTargetDoughWeight))
            },
            keyboardType = KeyboardType.Number,
            output = "${df.format(results.flourWeight)} g",
            outputLabel = "Flour",
            isError = recipeInputsErrors.targetDoughWeightError,
            errorMsg = "Invalid input",
            inputWeight = 0.5f,
            outputWeight = 0.5f,
            recipeInputs = recipeInputs,
            onRecipeInputsChange = onRecipeInputsChange,
            onSaveRecipeInputs = onSaveRecipeInputs,
            previousRecipeInputs = previousRecipeInputs,
            updatePreviousRecipeInputs = updatePreviousRecipeInputs,
            df = df
        )
        TwoColumnRow(
            label = "Hydration",
            value = recipeInputs.hydrationPercentage.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = {
                val filteredInput = it.filter { it.isDigit() }
                val newHydrationPercentage = filteredInput.toIntOrNull() ?: 0
                onRecipeInputsChange(recipeInputs.copy(hydrationPercentage = newHydrationPercentage))
            },
            keyboardType = KeyboardType.Number,
            output = "${df.format(results.waterWeight)} g",
            outputLabel = "Water",
            isError = recipeInputsErrors.hydrationPercentageError,
            errorMsg = "Invalid input",
            inputWeight = 0.5f,
            outputWeight = 0.5f,
            recipeInputs = recipeInputs,
            onRecipeInputsChange = onRecipeInputsChange,
            onSaveRecipeInputs = onSaveRecipeInputs,
            previousRecipeInputs = previousRecipeInputs,
            updatePreviousRecipeInputs = updatePreviousRecipeInputs,
            df = df
        )

        TwoColumnRow(
            label = "Starter",
            value = recipeInputs.starterPercentage.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = {
                val filteredInput = it.filter { it.isDigit() }
                val newStarterPercentage = filteredInput.toIntOrNull() ?: 0
                onRecipeInputsChange(recipeInputs.copy(starterPercentage = newStarterPercentage))
            },
            keyboardType = KeyboardType.Number,
            output = "${df.format(results.starterWeight)} g",
            outputLabel = "Starter",
            isError = recipeInputsErrors.starterPercentageError,
            errorMsg = "Invalid input",
            inputWeight = 0.5f,
            outputWeight = 0.5f,
            recipeInputs = recipeInputs,
            onRecipeInputsChange = onRecipeInputsChange,
            onSaveRecipeInputs = onSaveRecipeInputs,
            previousRecipeInputs = previousRecipeInputs,
            updatePreviousRecipeInputs = updatePreviousRecipeInputs,
            df = df
        )

        TwoColumnRow(
            label = "Salt",
            value = recipeInputs.saltPercentage.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = {
                val filteredInput = it.filter { it.isDigit() }
                val newSaltPercentage = filteredInput.toIntOrNull() ?: 0
                onRecipeInputsChange(recipeInputs.copy(saltPercentage = newSaltPercentage))
            },
            keyboardType = KeyboardType.Number,
            output = "${df.format(results.saltWeight)} g",
            outputLabel = "Salt",
            isError = recipeInputsErrors.saltPercentageError,
            errorMsg = "Invalid input",
            inputWeight = 0.5f,
            outputWeight = 0.5f,
            recipeInputs = recipeInputs,
            onRecipeInputsChange = onRecipeInputsChange,
            onSaveRecipeInputs = onSaveRecipeInputs,
            previousRecipeInputs = previousRecipeInputs,
            updatePreviousRecipeInputs = updatePreviousRecipeInputs,
            df = df
        )

        TwoColumnRow(
            label = "Starter hydration",
            value = recipeInputs.starterHydration.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = {
                val filteredInput = it.filter { it.isDigit() }
                val newStarterHydration = filteredInput.toIntOrNull() ?: 0
                onRecipeInputsChange(recipeInputs.copy(starterHydration = newStarterHydration))
            },
            keyboardType = KeyboardType.Number,
            output = "", // No output for this row
            outputLabel = "",
            isError = recipeInputsErrors.starterHydrationError,
            errorMsg = "Invalid input",
            inputWeight = 0.5f,
            outputWeight = 0.5f,
            recipeInputs = recipeInputs,
            onRecipeInputsChange = onRecipeInputsChange,
            onSaveRecipeInputs = onSaveRecipeInputs,
            previousRecipeInputs = previousRecipeInputs,
            updatePreviousRecipeInputs = updatePreviousRecipeInputs,
            df = df
        )
    }
}
@Composable
fun TimingInputs(
    mixTime: LocalTime,
    mixButtonEnabled: Boolean,
    autolyseTime: Int,
    formattedAutolyseTime: String,
    addStarterSaltTime: LocalTime,
    addStarterSaltButtonEnabled: Boolean,
    formattedAddStarterSaltTime: String,
    bulkFermentationTime: Int,
    formattedBulkFermentationEndTime: String,
    formatter: DateTimeFormatter,
    onMixTimeChange: (LocalTime) -> Unit,
    onMixButtonEnabledChange: (Boolean) -> Unit,
    onAutolyseTimeChange: (Int) -> Unit,
    onFormattedAutolyseTimeChange: (String) -> Unit,
    onAddStarterSaltTimeChange: (LocalTime) -> Unit,
    onAddStarterSaltButtonEnabledChange: (Boolean) -> Unit,
    onFormattedAddStarterSaltTimeChange: (String) -> Unit,
    onBulkFermentationTimeChange: (Int) -> Unit,
    onFormattedBulkFermentationEndTimeChange: (String) -> Unit,
    onSaveTimingInputs: suspend (TimingInputsData) -> Unit,
    timingInputs: TimingInputsData,
    onTimingInputsChange: (TimingInputsData) -> Unit,
    previousTimingInputs: TimingInputsData,
    updatePreviousTimingInputs: (TimingInputsData) -> Unit
) {
    val context = LocalContext.current
    val coroutine
    Scope = rememberCoroutineScope()

    // Calculate autolyse end time taking into account slider changes
    val autolyseEndTime = mixTime.plusMinutes(autolyseTime.toLong())
    val formattedAutolyseEndTime = autolyseEndTime.format(formatter)

    // Update formattedAutolyseTime whenever mixTime or autolyseTime changes
    LaunchedEffect(mixTime, autolyseTime) {
        onFormattedAutolyseTimeChange(formattedAutolyseEndTime)
    }

    // Mix Time Button and Output
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppPadding.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                val currentTime = LocalTime.now()
                onMixTimeChange(currentTime)
                onMixButtonEnabledChange(false)

                // Calculate delay for Autolyse notification
                val autolyseDurationMillis = autolyseTime * 60 * 1000L // Convert minutes to milliseconds

                // Schedule the Autolyse notification
                if (autolyseDurationMillis > 0) {
                    NotificationHelper.scheduleNotification(
                        context,
                        autolyseDurationMillis,
                        NotificationReceiver.NOTIFICATION_ID_AUTOLYSE,
                        "Autolyse is finished!"
                    )
                }

                // Save to DataStore after Mix Time button is clicked
                coroutineScope.launch {
                    onSaveTimingInputs(
                        TimingInputsData(
                            mixTime = currentTime,
                            autolyseTime = autolyseTime,
                            addStarterSaltTime = addStarterSaltTime,
                            bulkFermentationTime = bulkFermentationTime
                        )
                    )
                }
                // Use the CoroutineScope from MainActivity:
                lifecycleScope.launch { // Use lifecycleScope.launch
                    onSaveTimingInputs(
                        TimingInputsData(
                            // ...
                        )
                    )
                }
            },
            modifier = Modifier.weight(1f),
            enabled = mixButtonEnabled
        ) {
            Text(
                "Mix flour and water",
                fontSize = AppFontSize.medium
            )
        }
        Spacer(modifier = Modifier.width(AppPadding.large))
        OutputBox(
            text = mixTime.format(formatter),
            modifier = Modifier.weight(1f),
            outputLabel = "Mix time"
        )
    }

    // Autolyse Time Input with Slider
    InputWithSlider(
        label = "Autolyse Time",
        value = autolyseTime,
        onValueChange = {newValue ->
            onAutolyseTimeChange(newValue)
            onTimingInputsChange(timingInputs.copy(autolyseTime = newValue))
            // Save to DataStore after Autolyse Time changes
            coroutineScope.launch {
                onSaveTimingInputs(
                    timingInputs.copy(autolyseTime = newValue)
                )
            }
        },
        range = 0f..60f,
        steps = 60,
        timingInputs = timingInputs,
        onTimingInputsChange = onTimingInputsChange,
        previousTimingInputs = previousTimingInputs,
        updatePreviousTimingInputs = updatePreviousTimingInputs
    )

    // Display Autolyse End Time
    Text(
        text = "Autolyse end: $formattedAutolyseTime",
        fontSize = AppFontSize.large,
        modifier = Modifier.padding(top = AppPadding.large)
    )

    // Use LaunchedEffect to enable the Add Starter & Salt button after autolyse
    LaunchedEffect(formattedAutolyseTime) {
        if (formattedAutolyseTime.isNotEmpty() && formattedAutolyseTime != "Invalid input") {
            val autolyseEndTime = LocalTime.parse(formattedAutolyseTime, formatter)
            if (LocalTime.now() >= autolyseEndTime) {
                onAddStarterSaltButtonEnabledChange(true)
            }
        }
    }

    // Add 5 minutes for adding starter and salt
    val bulkFermentationStartTime = addStarterSaltTime.plusMinutes(5)

    // Add Starter and Salt Button and Output
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppPadding.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                val currentTime = LocalTime.now()
                onAddStarterSaltTimeChange(currentTime)
                onAddStarterSaltButtonEnabledChange(false)
                onFormattedAddStarterSaltTimeChange(currentTime.format(formatter))
                // Calculate and update bulk fermentation end time
                onFormattedBulkFermentationEndTimeChange(bulkFermentationStartTime.plusMinutes(bulkFermentationTime.toLong()).format(formatter))

                // Calculate delay for Bulk Fermentation notification
                val bulkFermentationDurationMillis = bulkFermentationTime * 60 * 1000L // Convert minutes to milliseconds

                // Schedule the Bulk Fermentation notification
                if (bulkFermentationDurationMillis > 0) {
                    NotificationHelper.scheduleNotification(
                        context,
                        bulkFermentationDurationMillis,
                        NotificationReceiver.NOTIFICATION_ID_BULK_FERMENT,
                        "Bulk fermentation is finished!"
                    )
                }

                // Save to DataStore after Add Starter and Salt button is clicked
                coroutineScope.launch {
                    onSaveTimingInputs(
                        TimingInputsData(
                            mixTime = mixTime,
                            autolyseTime = autolyseTime,
                            addStarterSaltTime = currentTime,
                            bulkFermentationTime = bulkFermentationTime
                        )
                    )
                }
                lifecycleScope.launch { // Use lifecycleScope.launch
                    onSaveTimingInputs(
                        TimingInputsData(
                            // ...
                        )
                    )
                }
            },
            modifier = Modifier.weight(1f),
            enabled = addStarterSaltButtonEnabled
        ) {
            Text(
                "Add starter & salt",
                fontSize = AppFontSize.medium
            )
        }
        Spacer(modifier = Modifier.width(AppPadding.large))
        OutputBox(
            text = addStarterSaltTime.format(formatter),
            modifier = Modifier.weight(1f),
            outputLabel = "Add time"
        )
    }

    // Display Add Starter and Salt Time (Now Bulk Fermentation Start Time)
    Text(
        text = "Bulk fermentation start: ${bulkFermentationStartTime.format(formatter)}",
        fontSize = AppFontSize.large,
        modifier = Modifier.padding(top = AppPadding.large)
    )

    // Bulk Fermentation Time Input with Slider
    InputWithSlider(
        label = "Bulk Fermentation Time",
        value = bulkFermentationTime,
        onValueChange = { newValue ->
            onBulkFermentationTimeChange(newValue)
            onTimingInputsChange(timingInputs.copy(bulkFermentationTime = newValue))
            // Save to DataStore after Bulk Fermentation Time changes
            coroutineScope.launch {
                onSaveTimingInputs(
                    timingInputs.copy(bulkFermentationTime = newValue)
                )
            }
        },
        range = 60f..600f,
        steps = 108,
        timingInputs = timingInputs,
        onTimingInputsChange = onTimingInputsChange,
        previousTimingInputs = previousTimingInputs,
        updatePreviousTimingInputs = updatePreviousTimingInputs
    )

    // Display Bulk Fermentation End Time
    Text(
        text = "Bulk fermentation end: $formattedBulkFermentationEndTime",
        fontSize = AppFontSize.large,
        modifier = Modifier.padding(top = AppPadding.large)
    )
}

@Composable
fun InputWithSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    timingInputs: TimingInputsData,
    onTimingInputsChange: (TimingInputsData) -> Unit,
    previousTimingInputs: TimingInputsData,
    updatePreviousTimingInputs: (TimingInputsData) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var sliderValue by remember { mutableStateOf(value.toFloat()) }

    // Ensure the slider updates when the value prop changes
    LaunchedEffect(value) {
        if (value.toFloat() != sliderValue) {
            sliderValue = value.toFloat()
        }
    }

    // Update the underlying value when the slider changes
    val onSliderChange: (Float) -> Unit = { newValue ->
        sliderValue = newValue
        val intValue = newValue.roundToInt()
        onValueChange(intValue)
        onTimingInputsChange(
            when (label) {
                "Autolyse Time" -> timingInputs.copy(autolyseTime = intValue)
                "Bulk Fermentation Time" -> timingInputs.copy(bulkFermentationTime = intValue)
                else -> timingInputs
            }
        )
    }

    Column(modifier = Modifier.padding(AppPadding.medium)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label:",
                fontSize = AppFontSize.medium,
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = "${value} min",
                fontSize = AppFontSize.medium,
                modifier = Modifier.weight(0.6f),
                textAlign = TextAlign.Center
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = onSliderChange,
            valueRange = range,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Moved outside of MainActivity to be accessible from other composables
fun calculateRecipe(
    targetDoughWeight: Int,
    hydrationPercentage: Int,
    starterPercentage: Int,
    starterHydration: Int,
    saltPercentage: Int
): RecipeResult {
    // Prevent division by zero and negative weights
    if (targetDoughWeight <= 0 || hydrationPercentage <= 0 || starterPercentage <= 0 || starterHydration <= 0 || saltPercentage <= 0) {
        return RecipeResult(0.0, 0.0, 0.0, 0.0)
    }

    // Convert inputs to Double for calculations
    val targetDoughWeightDouble = targetDoughWeight.toDouble()
    val hydrationPercentageDouble = hydrationPercentage.toDouble() / 100.0
    val starterPercentageDouble = starterPercentage.toDouble() / 100.0
    val starterHydrationDouble = starterHydration.toDouble() / 100.0
    val saltPercentageDouble = saltPercentage.toDouble() / 100.0

    // Calculate total weight excluding salt
    val totalWeightWithoutSalt =
        targetDoughWeightDouble / (1 + saltPercentageDouble * (1 / (1 + hydrationPercentageDouble)))

    // Calculate total flour weight
    val totalFlourWeight = totalWeightWithoutSalt / (1 + hydrationPercentageDouble)

    // Calculate flour and water from starter
    val flourFromStarter =
        (totalFlourWeight * starterPercentageDouble) / (1 + starterHydrationDouble)
    val waterFromStarter = flourFromStarter * starterHydrationDouble

    // Calculate final flour and water weights
    val finalFlourWeight = totalFlourWeight - flourFromStarter
    val totalWaterWeight = totalWeightWithoutSalt - totalFlourWeight
    val finalWaterWeight = totalWaterWeight - waterFromStarter

    // Calculate final starter and salt weights
    val finalStarterWeight = flourFromStarter + waterFromStarter
    val saltWeight = totalFlourWeight * saltPercentageDouble

    return RecipeResult(
        flourWeight = finalFlourWeight,
        waterWeight = finalWaterWeight,
        starterWeight = finalStarterWeight,
        saltWeight = saltWeight
    )
}