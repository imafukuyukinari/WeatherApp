package com.example.wetherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // APIキーとURLを定義
        val apiKey = "b5cc2b9f6440d1d304caa03d273add4c"
        val mainUrl = "https://api.openweathermap.org/data/2.5/weather?lang=ja"

        // viewを取得
        val btnTokyo:Button = findViewById(R.id.btnTokyo)
        val btnOkinawa:Button = findViewById(R.id.btnOkinawa)
        val tvCityName:TextView = findViewById(R.id.tvCityName)
        val tvCityWeather:TextView = findViewById(R.id.tvCityWeather)
        val tvMax:TextView = findViewById(R.id.tvMax)
        val tvMin:TextView = findViewById(R.id.tvMin)
        val btnClear:Button = findViewById(R.id.btnClear)

        // btnTokyoが押されたら
        btnTokyo.setOnClickListener {
            // 東京のお天気URLを取得
            // https://api.openweathermap.org/data/2.5/weather?lang=ja&q=tokyo&appid=b5cc2b9f6440d1d304caa03d273add4c
            val weatherUrl = "$mainUrl&q=tokyo&appid=$apiKey"

            // URLを元に得られた情報の結果を表示
            // コルーチンを作る＝>HTTP通信(ワーカースレッド)=>お天気データ表示(メインスレッド)
            weatherTask(weatherUrl)
        }

        // btnOkinawaが押されたら
        btnOkinawa.setOnClickListener {
            // 東京のお天気URLを取得
            val weatherUrl = "$mainUrl&q=okinawa&appid=$apiKey"

            // URLを元に得られた情報の結果を表示
            // コルーチンを作る＝>HTTP通信(ワーカースレッド)=>お天気データ表示(メインスレッド)
            weatherTask(weatherUrl)
        }

        // CLEARボタンが押されたら
        btnClear.setOnClickListener {
            tvCityName.text = "都市名"
            tvCityWeather.text = "都市の天気"
            tvMax.text = "最高気温"
            tvMin.text = "最低気温"
        }

    }

    private fun weatherTask(weatherUrl:String) {
        // コルーチンスコープ(非同期処理領域の用意)
        lifecycleScope.launch {
            // HTTP通信(ワーカースレッド)
            val result = weatherBackgroundTask(weatherUrl)
            // お天気データ表示(メインスレッド)
            weatherJsonTask(result)
        }
    }

    // HTTP通信(ワーカースレッド)の中身(suspend=中断する可能性がある関数)
    private suspend fun weatherBackgroundTask(weatherUrl:String):String {
        // withContext=スレッドの分離　Dispatchers.IO=ワーカースレッド
        val response = withContext(Dispatchers.IO) {
            // 天気情報サービスから取得した結果情報(JSON文字列)を入れるための変数を用意
            var httpResult = ""

            try{
                // URL文字列をURLオブジェクトに変換(文字列にリンク付加)
                val urlObj = URL(weatherUrl)
                // アクセスしたAPIから情報を取得
                // テキストファイルを読み込むクラス(文字コードを読めるようにする準備)
                val br = BufferedReader(InputStreamReader(urlObj.openStream()))
                httpResult = br.readText()

            }catch(e:IOException) {
                e.printStackTrace()
            }catch(e:JSONException) { // JSONデータ構造に問題が発生した場合の例外
                e.printStackTrace()
            }
            // HTTP接続の結果、取得したJSON文字列httpResultを戻り値とする
            return@withContext httpResult
        }

        return response
    }

    // HTTP通信を受けて、お天気データ(JSONデータ)を表示
    private fun weatherJsonTask(result:String) {
        val tvCityName:TextView = findViewById(R.id.tvCityName)
        val tvCityWeather:TextView = findViewById(R.id.tvCityWeather)
        val tvMax:TextView = findViewById(R.id.tvMax)
        val tvMin:TextView = findViewById(R.id.tvMin)

        // JSONオブジェクト一式を生成
        val jsonObj = JSONObject(result)

        // JSONオブジェクトの都市名のキーを取得
        val cityName = jsonObj.getString("name")
        tvCityName.text = cityName

        // JSONオブジェクトの天気情報JSON配列オブジェクトを取得
        val weatherJSONArray = jsonObj.getJSONArray("weather")
        // 現在の天気情報JSONオブジェクト(配列の0番目)を取得
        val weatherJSON = weatherJSONArray.getJSONObject(0)
        // お天気の説明を取得
        val weather = weatherJSON.getString("description")
        // TextViewにお天気結果を表示
        tvCityWeather.text = weather

        // JSONオブジェクトのmainオブジェクトを取得
        val main = jsonObj.getJSONObject("main")
        // TextViewに最高気温を表示
        tvMax.text = "最高気温：${main.getInt("temp_max")-273}℃"
        // TextViewに最低気温を表示
        tvMin.text = "最低気温：${main.getInt("temp_min")-273}℃"
    }
}