package ok.demo.aptweather.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import ok.demo.aptweather.MainViewModel
import ok.demo.aptweather.R
import ok.demo.aptweather.adapters.VpAdapter
import ok.demo.aptweather.databinding.FragmentMainBinding
import ok.demo.aptweather.datas.WeatherModel
import ok.demo.aptweather.isPermissionGranted
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


const val API_KEY = "e74f09ba234c4065ab8175633221510"

const val CITY_NAME = "Saint Petersburg"

class MainFragment : Fragment() {


    private val fragList = listOf<Fragment>(
        HoursFragment.newInstance(),

        )

    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding

    //Для обмена ViewModel между разными фрагментами в рамках одного и того же activity
    //Для этого добавить зависимость в build.gradle
    //implementation 'androidx.fragment:fragment-ktx:1.3.6'
    private val model: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        updateCurrentCard()
    }

    //Если после включения локации возврат к приложению
    override fun onResume() {
        super.onResume()
        requestWeatherData(CITY_NAME)
    }

    //Инициализация адаптера
    private fun init() = with(binding) {
        val adapter = VpAdapter(activity as FragmentActivity, fragList)
        vp.adapter = adapter //используется with(binding)

        isInternetConnected()

        if (!isInternetConnected()) {
            Toast.makeText(activity, R.string.inet_connection_prompt, Toast.LENGTH_LONG)
                .show()
        }

        //По кнопке обновления ibSync
        btUpdate.setOnClickListener {
            vp.adapter = adapter //используется with(binding)
            requestWeatherData(CITY_NAME, true)
        }

    }

    @SuppressLint("NewApi")
    private fun updateCurrentCard() = with(binding) {
        //Следить за циклом activity - когда нужно его обновлять
        model.liveDataCurrent.observe(viewLifecycleOwner) {

            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MM-yyyy HH:mm")
            val curLocalDateTime = LocalDateTime.now().format(dateTimeFormatter)

            //Log.d("MyLog", curLocalDateTime.toString())

            val maxMinTemp = "${it.maxTemp}°C / ${it.minTemp}°C"
            //tvData.text = it.time
            tvData.text = curLocalDateTime.toString()
            tvCity.text = it.city
            tvCurrentTemp.text = if (it.currentTemp.isEmpty()) maxMinTemp else "${it.currentTemp}°C"
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMinTemp
            //Отображение картинки по ссылке в view в activity
            //с помощью библиотеки Picasso
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
        }
    }


    //Для проверки подключения интернет
    private fun isInternetConnected(): Boolean {

        val connectionManager: ConnectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectionManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }



    private fun requestWeatherData(city: String, afterUpdateMsg: Boolean = false) {
        val forecastDays: Int = 6
        val url: String = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=$city" +
                "&days=$forecastDays" +
                "&aqi=no&alerts=no"
        //Log.d("MyLog", url.toString())

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { resultResponse ->
                parseWeatherData(resultResponse)
                //Log.d("MyLog", "Строка ответа: $resultResponse")
                if (afterUpdateMsg) {
                    Toast.makeText(activity, getString(R.string.updateResult), Toast.LENGTH_SHORT)
                        .show()
                }
            },
            { errorResponse ->
                Toast.makeText(activity, "Error: $errorResponse", Toast.LENGTH_LONG).show()
                //Log.d("MyLog", "Ошибка: $errorResponse")
                if (!isInternetConnected()) {
                    Toast.makeText(activity, R.string.inet_connection_prompt, Toast.LENGTH_LONG)
                        .show()
                }
            }
        )
        queue.add(request)
    }

    //Заполнить данные погоды
    private fun parseWeatherData(requestResult: String) {
        val mainObject = JSONObject(requestResult)

        val list = parseDay(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    //Получить данные погоды по дням
    private fun parseDay(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")

        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    //Получить данные погоды текущего дня
    @SuppressLint("NewApi")
    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")
        val curTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        val last_updated = mainObject.getJSONObject("current").getString("last_updated")
        val date = LocalDateTime.parse(last_updated, curTimeFormatter).format(dateTimeFormatter)
        //Log.d("MyLog", date.toString())

        val item = WeatherModel(
            mainObject.getJSONObject("location")
                .getString("name") + "\n" + mainObject.getJSONObject("location")
                .getString("country"),
            date,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c").toFloat().toInt().toString(),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours
        )
        //Передать новые данные в модель
        //для обновления
        model.liveDataCurrent.value = item
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}