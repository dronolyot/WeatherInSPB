package ok.demo.aptweather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ok.demo.aptweather.datas.WeatherModel

class MainViewModel: ViewModel() {
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
}