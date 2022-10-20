package ok.demo.aptweather.datas

data class WeatherModel(
    val city: String,
    //Данные по дню
    val time: String,
    val condition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val imageUrl: String,
    //Данные информвции по часам
    val hours: String
)