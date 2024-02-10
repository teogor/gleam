package dev.teogor.gleam.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.teogor.gleam.ExperimentalGleamApi
import dev.teogor.gleam.Gleam
import dev.teogor.gleam.GleamDefaults
import dev.teogor.gleam.demo.theme.ModalistTheme
import dev.teogor.gleam.demo.ui.Country
import dev.teogor.gleam.demo.ui.CountryDetails
import dev.teogor.gleam.demo.ui.CountryList
import dev.teogor.gleam.utils.none

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalGleamApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    setContent {
      ModalistTheme {
        var showGleam by remember { mutableStateOf(false) }
        var selectedCountry: Country? by remember { mutableStateOf(null) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (showGleam) {
            Gleam(
              onDismissRequest = { showGleam = false },
              windowInsets = WindowInsets.none,
              properties = GleamDefaults.properties(
                animateCorners = true,
                animateHorizontalEdge = true,
                maxHorizontalEdge = 14.dp,
              ),
            ) {
              CountryList {
                selectedCountry = it
                showGleam = false
              }
            }
          }

          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            selectedCountry?.let { country ->
              CountryDetails("Country", country.name)
              CountryDetails("Code", country.code)
              CountryDetails("Flag", country.emoji)
            }
            Button(
              onClick = {
                showGleam = true
              },
            ) {
              Text(text = "Select Country")
            }
          }
        }
      }
    }
  }
}

