package ok.demo.aptweather.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ok.demo.aptweather.MainViewModel
import ok.demo.aptweather.adapters.WeatherAdapter
import ok.demo.aptweather.databinding.FragmentHoursBinding
import ok.demo.aptweather.datas.WeatherModel
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycleView()
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            //Log.d("MyLog", "часы${it.hours}")
            adapter.submitList(getHoursList(it))
        }
    }

    //Инициализация данных (для примера)
    fun initRecycleView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter

    }

    @SuppressLint("NewApi")
    private fun getHoursList(wItem: WeatherModel): List<WeatherModel> {

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        //var time = LocalDateTime.parse("2022-10-18 11:25",DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).format(timeFormatter)
       // Log.d("MyLog", "Время: $time")

        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()) {

            var curLocalDateTime = LocalDateTime.now().toLocalTime()
            var curDateTime = (hoursArray[i] as JSONObject).getString("time")
            var timeTmp = LocalDateTime.parse(curDateTime, dateTimeFormatter)
            var time = LocalDateTime.parse(curDateTime, dateTimeFormatter).format(timeFormatter)

            //Показать только актуальный почасовой прогноз
            if(curLocalDateTime > timeTmp.toLocalTime()) continue

            val item = WeatherModel(
                wItem.city,
                (time.toString()),
                (hoursArray[i] as JSONObject).getJSONObject("condition")
                    .getString("text"),
                (hoursArray[i] as JSONObject).getString("temp_c").toFloat().toInt().toString(),
                "",
                "",
                (hoursArray[i] as JSONObject).getJSONObject("condition")
                    .getString("icon"),
                ""
            )
            list.add(item)
        }
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}