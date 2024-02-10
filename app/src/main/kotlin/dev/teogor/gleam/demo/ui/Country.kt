package dev.teogor.gleam.demo.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun ColumnScope.CountryDetails(
  title: String,
  content: String,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 4.dp)
      .background(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .7f),
        shape = RoundedCornerShape(10.dp),
      )
      .padding(horizontal = 10.dp, vertical = 10.dp),
  ) {
    Text(
      text = "$title:",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    Text(
      text = content,
      fontSize = 16.sp,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
  }
}

@Composable
fun ColumnScope.CountryList(
  onSelected: (Country) -> Unit,
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf(TextFieldValue()) }
  var countries by remember { mutableStateOf(emptyList<Country>()) }
  val filteredCountries = remember(searchQuery, countries) {
    val isSearching = searchQuery.text.isNotEmpty()
    if (isSearching) {
      countries.filter {
        it.name.contains(searchQuery.text, ignoreCase = true) ||
          it.code.contains(searchQuery.text, ignoreCase = true)
      }
    } else {
      countries
    }
  }

  LaunchedEffect(Unit) {
    countries = parseCountriesFromJson(context)
  }

  TextField(
    value = searchQuery,
    onValueChange = { searchQuery = it },
    placeholder = { Text("Search by name, emoji, or code") },
    modifier = Modifier
      .fillMaxWidth(.9f)
      .align(Alignment.CenterHorizontally),
  )

  LazyColumn {
    items(filteredCountries) { country ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onSelected(country)
          }
          .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = country.emoji,
          fontSize = 26.sp,
          modifier = Modifier.padding(end = 10.dp),
        )
        Text(text = "${country.name} (${country.code})")
      }
    }
  }
}

private fun parseCountriesFromJson(
  context: Context,
): List<Country> {
  val jsonString = with(context.assets) {
    open("countries.json").bufferedReader().use {
      it.readText()
    }
  }

  val gson = Gson()
  val countriesList: List<Country> = gson.fromJson(
    jsonString,
    object : TypeToken<List<Country>>() {}.type,
  )

  return countriesList
}

data class Country(
  val name: String,
  val emoji: String,
  val code: String,
)
