package com.example.labelprinter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.labelprinter.print.SystemLabelPrinter
import com.example.labelprinter.ui.LabelScreen
import com.example.labelprinter.ui.theme.LabelPrinterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val printer = SystemLabelPrinter(this)
        setContent {
            LabelPrinterTheme {
                LabelScreen(printer = printer)
            }
        }
    }
}
