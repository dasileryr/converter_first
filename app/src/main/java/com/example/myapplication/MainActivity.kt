package com.example.myapplication // Убедитесь что пакет ваш

import android.os.Bundle
import androidx.activity.ComponentActivity // Используем ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Используем Material 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale // Для форматирования

class MainActivity : ComponentActivity() { // Наследуемся от ComponentActivity

    // Карта конверсий остается той же
    private val conversions = mapOf(
        "Км → Мили" to { v: Double -> v * 0.621371 },
        "Мили → Км" to { v: Double -> v / 0.621371 },
        "°C → °F" to { v: Double -> v * 9 / 5 + 32 },
        "°F → °C" to { v: Double -> (v - 32) * 5 / 9 },
        "Руб → USD (курс 90)" to { v: Double -> v / 90 },
        "USD → Руб (курс 90)" to { v: Double -> v * 90 }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { // Определяем UI внутри setContent
            ConverterApp(conversions) // Вызываем главную Composable-функцию
        }
    }
}

// Главная Composable-функция, описывающая UI
@OptIn(ExperimentalMaterial3Api::class) // Нужно для ExposedDropdownMenuBox
@Composable
fun ConverterApp(conversions: Map<String, (Double) -> Double>) {
    // --- Состояния для хранения UI данных ---
    var inputValue by remember { mutableStateOf("") }
    val conversionOptions = remember { conversions.keys.toList() } // Список опций
    var selectedConversion by remember { mutableStateOf(conversionOptions.firstOrNull() ?: "") } // Выбранная опция
    var resultText by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) } // Состояние выпадающего списка

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Используем ваш padding из XML
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Центрируем содержимое
    ) {
        // --- Текстовое поле для ввода ---
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it.replace(',', '.') },
            label = { Text("Введите значение") },
            // keyboardOptions = ..., // <-- ЭТА ЧАСТЬ УДАЛЕНА
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp)) // Отступ поменьше

        // --- Выпадающий список (Аналог Spinner) ---
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField( // Используем OutlinedTextField для единообразия
                value = selectedConversion,
                onValueChange = {}, // Нельзя менять вручную
                readOnly = true,
                label = { Text("Выберите конверсию") }, // Можно добавить label
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth() // Привязка меню
                    .height(IntrinsicSize.Min) // Подгоняем высоту
            )
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                conversionOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedConversion = selectionOption
                            isDropdownExpanded = false
                            resultText = "" // Сбрасываем результат при смене конверсии
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(20.dp)) // Отступ как в XML

        // --- Кнопка ---
        Button(
            onClick = {
                val input = inputValue.toDoubleOrNull() // Безопасное преобразование
                // Проверяем, что выбрана валидная опция и ввод корректен
                if (input != null && conversions.containsKey(selectedConversion)) {
                    val conversionFunction = conversions[selectedConversion]
                    if (conversionFunction != null) {
                        val result = conversionFunction(input)
                        // Используем String.format для контроля над форматированием
                        resultText = "Результат: ${String.format(Locale.US, "%.2f", result)}"
                    } else {
                        resultText = "Ошибка: Функция конверсии не найдена" // Маловероятно, но для полноты
                    }

                } else {
                    resultText = "Ошибка ввода или выбора конверсии"
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp) // Зададим высоту кнопки
        ) {
            Text("Конвертировать")
        }

        Spacer(modifier = Modifier.height(16.dp)) // Отступ как в XML

        // --- Текст результата ---
        Text(
            text = resultText,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth() // Растянем по ширине
        )
    }
}

// Функция для предпросмотра в Android Studio
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // Предоставляем моковые данные для превью
    val conversionsPreview = mapOf(
        "Км → Мили" to { v: Double -> v * 0.621371 },
        "Мили → Км" to { v: Double -> v / 0.621371 }
    )
    // Можно обернуть в тему Material, если используется
    // MaterialTheme {
    ConverterApp(conversions = conversionsPreview)
    // }
}