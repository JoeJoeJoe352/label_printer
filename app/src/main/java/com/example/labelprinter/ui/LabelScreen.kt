package com.example.labelprinter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.labelprinter.R
import com.example.labelprinter.model.LabelData
import com.example.labelprinter.print.LabelPrinter
import com.example.labelprinter.print.LabelRenderer
import com.example.labelprinter.print.LabelSpec
import com.example.labelprinter.print.PrintResult
import com.example.labelprinter.ui.theme.LabelPrinterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelScreen(
    printer: LabelPrinter,
    initialLabel: LabelData = LabelData.Sample,
) {
    // rememberSaveable: képernyőforgatáskor sem vesznek el a beírt adatok.
    var name by rememberSaveable { mutableStateOf(LabelData.sanitize(initialLabel.name)) }
    var city by rememberSaveable { mutableStateOf(LabelData.sanitize(initialLabel.city)) }
    var street by rememberSaveable { mutableStateOf(LabelData.sanitize(initialLabel.street)) }

    // Hibaüzenet csak akkor jelenik meg, ha a mezőhöz már hozzányúltak,
    // vagy nyomtatni próbáltak – induláskor nem piroslik semmi.
    var nameTouched by rememberSaveable { mutableStateOf(false) }
    var cityTouched by rememberSaveable { mutableStateOf(false) }
    var streetTouched by rememberSaveable { mutableStateOf(false) }
    var printAttempted by rememberSaveable { mutableStateOf(false) }

    // Az előnézet és a nyomtatás ugyanebből a példányból dolgozik.
    val label = LabelData(name, city, street).trimmed()

    val snackbarHostState = remember { SnackbarHostState() }
    // A legutóbbi nyomtatási hiba; a snackbar ebből az állapotból jelenik meg.
    var printError by remember { mutableStateOf<PrintResult.Failed?>(null) }
    val focusManager = LocalFocusManager.current

    fun printLabel() {
        focusManager.clearFocus()
        printAttempted = true
        if (!label.isComplete) return // a mezők alatt megjelenik, mi hiányzik
        val result = printer.print(label)
        if (result is PrintResult.Failed) printError = result
    }

    printError?.let { error ->
        // A szöveg a kompozícióban, stringResource-szal készül, így nyelv- vagy
        // konfigurációváltáskor is a helyes szöveg jelenik meg.
        val message = when (error.reason) {
            PrintResult.Reason.PRINTING_UNAVAILABLE ->
                stringResource(R.string.error_printing_unavailable)
            PrintResult.Reason.UNEXPECTED_ERROR ->
                stringResource(R.string.error_print_failed, error.details ?: "-")
        }
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(message)
            printError = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Fejléc az app nevével. Csak a képernyőn látszik: a nyomtatás kizárólag
        // a LabelRenderer által rajzolt címkét tartalmazza.
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = Color.Unspecified
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val fields: @Composable () -> Unit = {
            LabelFields(
                name = name,
                city = city,
                street = street,
                nameError = label.name.isBlank() && (nameTouched || printAttempted),
                cityError = label.city.isBlank() && (cityTouched || printAttempted),
                streetError = label.street.isBlank() && (streetTouched || printAttempted),
                onNameChange = { name = LabelData.sanitize(it); nameTouched = true },
                onCityChange = { city = LabelData.sanitize(it); cityTouched = true },
                onStreetChange = { street = LabelData.sanitize(it); streetTouched = true },
                onDone = { focusManager.clearFocus() },
            )
        }
        val printButton: @Composable () -> Unit = {
            PrintButton(onClick = ::printLabel)
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Széles kijelzőn (tablet, fekvő telefon) két oszlop: bal oldalt a
            // mezők és alattuk a gomb, jobb oldalt az előnézet. Keskenyen egymás alatt.
            val wide = maxWidth >= WIDE_LAYOUT_MIN_WIDTH
            val scrollModifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)

            if (wide) {
                Row(
                    modifier = scrollModifier.widthIn(max = 1100.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        fields()
                        Spacer(Modifier.height(4.dp))
                        printButton()
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { PreviewSection(label = label) }
                }
            } else {
                Column(
                    modifier = scrollModifier.widthIn(max = 560.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    fields()
                    Spacer(Modifier.height(8.dp))
                    PreviewSection(label = label)
                    Spacer(Modifier.height(12.dp))
                    printButton()
                }
            }
        }
    }
}

/** Ennél szélesebb tartalomterületen az előnézet a mezők mellé kerül. */
private val WIDE_LAYOUT_MIN_WIDTH = 600.dp

@Composable
private fun LabelFields(
    name: String,
    city: String,
    street: String,
    nameError: Boolean,
    cityError: Boolean,
    streetError: Boolean,
    onNameChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStreetChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    LabelField(
        value = name,
        onValueChange = onNameChange,
        label = stringResource(R.string.field_name),
        isError = nameError,
        capitalization = KeyboardCapitalization.Words,
    )
    LabelField(
        value = city,
        onValueChange = onCityChange,
        label = stringResource(R.string.field_city),
        isError = cityError,
        capitalization = KeyboardCapitalization.Words,
    )
    LabelField(
        value = street,
        onValueChange = onStreetChange,
        label = stringResource(R.string.field_street),
        isError = streetError,
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
        onDone = onDone,
    )
}

@Composable
private fun PreviewSection(label: LabelData) {
    Text(
        text = stringResource(R.string.preview_title),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(
            R.string.label_size,
            LabelSpec.WIDTH_MM.toInt(),
            LabelSpec.HEIGHT_MM.toInt(),
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    LabelPreview(label = label, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PrintButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(
            text = stringResource(R.string.print_button),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** Egysoros beviteli mező karakterszámlálóval (pl. „12 / 64”). */
@Composable
private fun LabelField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    capitalization: KeyboardCapitalization,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        // Balra a hibaüzenet (ha van), jobbra a karakterszámláló.
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = if (isError) stringResource(R.string.error_required) else "",
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.char_counter, value.length, LabelData.MAX_LENGTH),
                    textAlign = TextAlign.End,
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * A címke előnézete. Ugyanazt a [LabelRenderer]-t használja, mint a nyomtatás,
 * ezért mindig fehér „papíron” fekete szöveg, sötét témában is.
 */
@Composable
fun LabelPreview(label: LabelData, modifier: Modifier = Modifier) {
    val description = label.lines.joinToString(", ")
    Surface(
        modifier = modifier.aspectRatio(LabelSpec.ASPECT_RATIO),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = description },
        ) {
            drawIntoCanvas { canvas ->
                LabelRenderer.draw(
                    canvas.nativeCanvas,
                    android.graphics.RectF(0f, 0f, size.width, size.height),
                    label,
                )
            }
        }
    }
}

@Preview(name = "Telefon", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Tablet", showBackground = true, widthDp = 1024, heightDp = 768)
@Composable
private fun LabelScreenPreview() {
    LabelPrinterTheme {
        LabelScreen(printer = { PrintResult.Started })
    }
}
