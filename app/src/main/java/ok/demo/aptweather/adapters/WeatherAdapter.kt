package ok.demo.aptweather.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ok.demo.aptweather.R
import ok.demo.aptweather.databinding.ListItemBinding
import ok.demo.aptweather.datas.WeatherModel


class WeatherAdapter(val listener: Listener?) : ListAdapter<WeatherModel, WeatherAdapter.Holder>(ItemComparator()) {

    //Заполнение разметки активити
    //Хранит логику заполнения элемента данных (item)
    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        var itemTmp: WeatherModel? = null
        init {
            itemView.setOnClickListener {
                itemTmp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: WeatherModel) = with(binding) {
            itemTmp = item
            tvDate.text = item.time
            tvCondition.text = item.condition
            tvTemp.text = item.currentTemp+"°C"
           /* tvTemp.text = item.currentTemp.ifEmpty {
                "${item.maxTemp}°C / ${item.minTemp}°C"
            }*/
            //Отображение картинки по ссылке в view в activity
            //с помощью библиотеки Picasso
            Picasso.get().load("https:" + item.imageUrl).into(im)
        }
    }

    //Сравнение двух списков (items старого и нового)
    class ItemComparator : DiffUtil.ItemCallback<WeatherModel>() {
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener{
        fun onClick(item: WeatherModel)
    }
}