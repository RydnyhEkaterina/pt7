package com.example.travel

import kotlinx.coroutines.delay
import kotlin.random.Random

// Сервис-заглушка, имитирующий работу с API
object MockApiService {

    // Имитация загрузки из API с задержкой
    suspend fun fetchCountries(): List<Country> {
        // Имитация сетевой задержки
        delay(1000L + Random.nextLong(500L))

        return getMockCountries()
    }

    suspend fun fetchToursForCountry(countryName: String): List<CurrentTour> {
        // Имитация сетевой задержки
        delay(800L + Random.nextLong(300L))

        return getMockToursForCountry(countryName)
    }

    suspend fun searchCountries(query: String): List<Country> {
        delay(500L)
        return getMockCountries().filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.capital.contains(query, ignoreCase = true)
        }
    }

    private fun getMockCountries(): List<Country> {
        return listOf(
            Country(
                name = "Россия",
                flagUrl = "https://flagcdn.com/w320/ru.png",
                capital = "Москва",
                description = "Крупнейшая страна мира с богатой культурой и историей. Посетите Кремль, Красную площадь и Золотое кольцо.",
                popularity = 5
            ),
            Country(
                name = "США",
                flagUrl = "https://flagcdn.com/w320/us.png",
                capital = "Вашингтон",
                description = "Страна небоскребов, Голливуда и инноваций. От Нью-Йорка до Калифорнии.",
                popularity = 5
            ),
            Country(
                name = "Франция",
                flagUrl = "https://flagcdn.com/w320/fr.png",
                capital = "Париж",
                description = "Страна любви, моды и кулинарии. Эйфелева башня, Лувр и Лазурный берег.",
                popularity = 5
            ),
            Country(
                name = "Германия",
                flagUrl = "https://flagcdn.com/w320/de.png",
                capital = "Берлин",
                description = "Страна пива, автомобилей и средневековых замков. Берлин, Мюнхен, Кёльнский собор.",
                popularity = 4
            ),
            Country(
                name = "Италия",
                flagUrl = "https://flagcdn.com/w320/it.png",
                capital = "Рим",
                description = "Колыбель искусства и архитектуры. Рим, Венеция, Флоренция и вкуснейшая кухня.",
                popularity = 5
            ),
            Country(
                name = "Испания",
                flagUrl = "https://flagcdn.com/w320/es.png",
                capital = "Мадрид",
                description = "Страна фламенко, корриды и солнечных пляжей. Барселона, Мадрид, Коста-Брава.",
                popularity = 4
            ),
            Country(
                name = "Япония",
                flagUrl = "https://flagcdn.com/w320/jp.png",
                capital = "Токио",
                description = "Страна восходящего солнца. Современные технологии и древние традиции.",
                popularity = 4
            ),
            Country(
                name = "Китай",
                flagUrl = "https://flagcdn.com/w320/cn.png",
                capital = "Пекин",
                description = "Древняя цивилизация с Великой стеной. Пекин, Шанхай, Гонконг.",
                popularity = 4
            ),
            Country(
                name = "Турция",
                flagUrl = "https://flagcdn.com/w320/tr.png",
                capital = "Анкара",
                description = "Мост между Европой и Азией. Стамбул, Каппадокия, Анталия.",
                popularity = 4
            ),
            Country(
                name = "Греция",
                flagUrl = "https://flagcdn.com/w320/gr.png",
                capital = "Афины",
                description = "Колыбель западной цивилизации. Афины, Санторини, Миконос.",
                popularity = 4
            ),
            Country(
                name = "Египет",
                flagUrl = "https://flagcdn.com/w320/eg.png",
                capital = "Каир",
                description = "Земля фараонов и пирамид. Каир, Луксор, Шарм-эль-Шейх.",
                popularity = 3
            ),
            Country(
                name = "Таиланд",
                flagUrl = "https://flagcdn.com/w320/th.png",
                capital = "Бангкок",
                description = "Страна улыбок и тропических островов. Бангкок, Пхукет, Паттайя.",
                popularity = 4
            ),
            Country(
                name = "Великобритания",
                flagUrl = "https://flagcdn.com/w320/gb.png",
                capital = "Лондон",
                description = "Королевство традиций. Лондон, Эдинбург, Стоунхендж.",
                popularity = 4
            ),
            Country(
                name = "Канада",
                flagUrl = "https://flagcdn.com/w320/ca.png",
                capital = "Оттава",
                description = "Страна кленового сиропа и природных красот. Торонто, Ванкувер, Ниагарский водопад.",
                popularity = 3
            ),
            Country(
                name = "Австралия",
                flagUrl = "https://flagcdn.com/w320/au.png",
                capital = "Канберра",
                description = "Континент кенгуру и коралловых рифов. Сидней, Мельбурн, Большой Барьерный риф.",
                popularity = 3
            ),
            Country(
                name = "Бразилия",
                flagUrl = "https://flagcdn.com/w320/br.png",
                capital = "Бразилиа",
                description = "Страна карнавала и футбола. Рио-де-Жанейро, водопады Игуасу, Амазонка.",
                popularity = 3
            )
        )
    }

    private fun getMockToursForCountry(countryName: String): List<CurrentTour> {
        val tours = mutableListOf<CurrentTour>()

        when (countryName) {
            "Россия" -> {
                tours.addAll(listOf(
                    CurrentTour(
                        name = "Москва: сердце России",
                        country = "Россия",
                        date = "15-05-2024",
                        price = 45000,
                        flagUrl = "https://flagcdn.com/w320/ru.png",
                        imageUrl = "",
                        description = "Тур по столице России: Кремль, Красная площадь, Третьяковская галерея",
                        availableSeats = 12,
                        durationDays = 7
                    ),
                    CurrentTour(
                        name = "Золотое кольцо",
                        country = "Россия",
                        date = "20-06-2024",
                        price = 38000,
                        flagUrl = "https://flagcdn.com/w320/ru.png",
                        imageUrl = "",
                        description = "Путешествие по древним городам: Суздаль, Владимир, Ярославль",
                        availableSeats = 8,
                        durationDays = 5
                    )
                ))
            }
            "Франция" -> {
                tours.addAll(listOf(
                    CurrentTour(
                        name = "Париж романтический",
                        country = "Франция",
                        date = "10-05-2024",
                        price = 95000,
                        flagUrl = "https://flagcdn.com/w320/fr.png",
                        imageUrl = "",
                        description = "Эйфелева башня, Лувр, Монмартр, Сена",
                        availableSeats = 6,
                        durationDays = 8
                    ),
                    CurrentTour(
                        name = "Лазурный берег",
                        country = "Франция",
                        date = "15-07-2024",
                        price = 120000,
                        flagUrl = "https://flagcdn.com/w320/fr.png",
                        imageUrl = "",
                        description = "Ницца, Канны, Монако",
                        availableSeats = 4,
                        durationDays = 10
                    )
                ))
            }
            "Италия" -> {
                tours.addAll(listOf(
                    CurrentTour(
                        name = "Рим и Ватикан",
                        country = "Италия",
                        date = "05-06-2024",
                        price = 85000,
                        flagUrl = "https://flagcdn.com/w320/it.png",
                        imageUrl = "",
                        description = "Колизей, Пантеон, Собор Святого Петра",
                        availableSeats = 10,
                        durationDays = 7
                    ),
                    CurrentTour(
                        name = "Венецианские каналы",
                        country = "Италия",
                        date = "12-09-2024",
                        price = 92000,
                        flagUrl = "https://flagcdn.com/w320/it.png",
                        imageUrl = "",
                        description = "Площадь Сан-Марко, Гранд-канал, Мурано",
                        availableSeats = 5,
                        durationDays = 6
                    )
                ))
            }
            else -> {
                // Генерация тестовых туров для других стран
                tours.add(
                    CurrentTour(
                        name = "Экскурсия по $countryName",
                        country = countryName,
                        date = "01-07-2024",
                        price = 50000 + Random.nextInt(50000),
                        flagUrl = "https://flagcdn.com/w320/${countryName.take(2).lowercase()}.png",
                        imageUrl = "",
                        description = "Увлекательное путешествие по $countryName",
                        availableSeats = Random.nextInt(5, 15),
                        durationDays = Random.nextInt(5, 10)
                    )
                )
            }
        }

        return tours
    }
}