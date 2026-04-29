package com.example.weatherappstate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

@Composable
fun CityWeatherScreen(city: City) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = city.name,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val weatherEmoji = when {
            city.temperature >= 70 -> "☀️"
            city.temperature >= 50 -> "⛅"
            else -> "❄️"
        }
        
        Text(
            text = weatherEmoji,
            fontSize = 120.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "${city.temperature}\u2109",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "H: ${city.temperature + 5}\u2109",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
            Text(
                text = "L: ${city.temperature - 5}\u2109",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
        }
    }
}

@Serializable
data class City(val name: String, val temperature: Int, val latitude: Double = 0.0, val longitude: Double = 0.0)
