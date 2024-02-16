package com.example.yandexmap
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.Interface.SuggestApi
import com.example.yandexmap.databinding.ActivityMainBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.Runtime.init
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    private lateinit var locationMapKit: UserLocationLayer
    private lateinit var searchEdit: AutoCompleteTextView
    private lateinit var searchManager: SearchManager
    private lateinit var searchSession: Session
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val searchQueryFlow = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("02813d8c-6a80-4d82-8557-6b7db5e30c83")
        binding = ActivityMainBinding.inflate(layoutInflater)
        MapKitFactory.initialize(this)
        setContentView(binding.root)
        mapView = binding.mapview
        mapView.isVisible = false
        requestLocationPermission()
        SearchFactory.initialize(this)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapView.map.addCameraListener(this)
        searchEdit = binding.searchEdit
        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitQuery(searchEdit.text.toString())
            }
            false
        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://dev.api.boosa.ru/v1/").client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SuggestApi::class.java)


        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        searchEdit.setAdapter(adapter)
        searchEdit.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            searchEdit.setText(selectedItem)
            Log.d("Autocomplete", "Selected item: $selectedItem")
        }


            lifecycleScope.launch {
               searchQueryFlow
                    .debounce(300) // Задержка в 300 мс
                    .filter { query ->
                        query.isNotEmpty() && query.length >= 2 // Игнорируем пустой запрос
                    }
                    .distinctUntilChanged() // Игнорируем повторяющиеся запросы
                    .onEach {
                        Log.d("DebounceCheck", "Выполняется запрос: $it")
                    }
                    .flatMapLatest { query ->
                        // Обертываем результат запроса в flow, чтобы можно было использовать с flatMapLatest
                        flow {
                            try {
                                Log.e("AutoCompleteError", "Данные отправил")
                                val response = service.getCity(query)
                                if (response.suggestion.isNotEmpty()) {
                                    emit(response.suggestion.map { it.address.formattedAddress }) // Отправляем список адресов
                                } else {
                                    emit(emptyList<String>()) // Отправляем пустой список, если нет данных
                                }
                            } catch (e: Exception) {
                                Log.e("AutoCompleteError", "Ошибка при получении данных", e)
                                emit(emptyList<String>()) // В случае ошибки также отправляем пустой список
                            }
                        }
                    }
                    .collect { suggestions ->
                        // Обновляем UI в главном потоке
                        withContext(Dispatchers.Main) {
                            adapter.clear()
                            adapter.addAll(suggestions)
                            adapter.notifyDataSetChanged()
                        }
                    }
            }


        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("DebounceCheck", "Текст изменен: $s")
                searchQueryFlow.value = s.toString() // Обновляем значение Flow
            }

            // beforeTextChanged и onTextChanged определены, но не используются
        })





















//        searchEdit.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable?) {
//                coroutineScope.launch(Dispatchers.IO) {
//                    try {
//                        val response = service.getCity(searchEdit.text.toString())
//                        Log.d("KEK", "$response")
//                        if (response != null && response.suggestion.isNotEmpty()) {
//                            val suggestions = response.suggestion.map { it.address.formattedAddress } // Преобразование в список строк
//                            withContext(Dispatchers.Main) {// Обновляем адаптер в главном потоке
//                                adapter.clear()
//                                adapter.addAll(suggestions)
//                                adapter.notifyDataSetChanged()
//                            }
//                        } else {
//                            withContext(Dispatchers.Main) {
//                                Toast.makeText(this@MainActivity, "Получен пустой ответ от сервера", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            // Более подробное логирование ошибок
//                            Log.e("AutoCompleteError", "Ошибка при получении данных", e)
//                            Toast.makeText(this@MainActivity, "Ошибка: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            }
//
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Не используется
//            }
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Не используется
//            }
//        })


    }

    private fun requestLocationPermission() { //Проверка разрешения на геолокацию, Запрос разрешения если нужно
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
            return
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }
    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) { //Настройка вида метки пользователя на карте
        locationMapKit.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )
        userLocationView.arrow.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon(
            "icon",
            ImageProvider.fromResource(this, R.drawable.nothing),
            IconStyle().setAnchor(PointF(0f, 0f))
                .setRotationType(RotationType.ROTATE).setZIndex(0f).setScale(1f)
        )
        picIcon.setIcon(
            "pin", ImageProvider.fromResource(this, R.drawable.nothing),
            IconStyle().setAnchor(PointF(0.5f, 0.5f)).setRotationType(RotationType.ROTATE)
                .setZIndex(1f).setScale(0.5f)
        )
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x6600001
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }
    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

override fun onSearchResponse(response: Response) {//Очистка существующих меток.Добавление новых меток по результатам поиска
    val mapObject: MapObjectCollection = mapView.map.mapObjects //Отображение карты. Перемещение камеры на результат
    mapObject.clear()
    for (searchResult in response.collection.children) {
        val resultLocation = searchResult.obj!!.geometry[0].point!! //geometry?.getOrNull(0)?.point уберет лишние круги
        mapObject.addPlacemark(
            resultLocation,
            ImageProvider.fromResource(this, R.drawable.search_result)
        )
    }
    mapView.isVisible = true
    // Перемещение камеры на найденную точку
    val resultLocation = response.collection.children[0].obj!!.geometry[0].point!!
    mapView.map.move(
        CameraPosition(resultLocation, 10.0f, 0.0f, 0.0f)
    )
}
    private fun submitQuery(query: String) { //Выполнение запроса поиска
        searchSession = searchManager.submit(
            query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion), SearchOptions(), this
        )
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Неизвестная ошибка!"
        if (error is RemoteError) {
            errorMessage = "Беспроводная ошибка!"
        } else if (error is NetworkError) {
            errorMessage = "Проблемма с интернетом!"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCameraPositionChanged(//Запуск нового запроса при смене области
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) {
            submitQuery(searchEdit.text.toString())
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Отменяем все запущенные корутины при уничтожении активности
    }
}