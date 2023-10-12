package com.example.test_radio

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {

        private lateinit var exoPlayer: ExoPlayer
        private lateinit var playerView: PlayerView
        private lateinit var listView: ListView
        private lateinit var adapter: ArrayAdapter<String>
        private val playlistUrl = "https://raw.githubusercontent.com/sacuar/MyIPTV/main/radio.m3u" // Replace with your M3U playlist URL
        private val audioStreamUrls = mutableListOf<String>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            playerView = findViewById(R.id.player_view)
            listView = findViewById(R.id.listView)
            exoPlayer = ExoPlayer.Builder(this).build()
            playerView.player = exoPlayer

            fetchChannelList(playlistUrl)

            listView.setOnItemClickListener { _, _, position, _ ->
                // Handle channel selection here
                val selectedStreamUrl = audioStreamUrls[position]
                val mediaItem = MediaItem.fromUri(Uri.parse(selectedStreamUrl))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }

        private fun fetchChannelList(playlistUrl: String) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(playlistUrl)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val playlist = response.body?.string()
                    val parsedUrls = parsePlaylist(playlist)

                    runOnUiThread {
                        audioStreamUrls.clear()
                        audioStreamUrls.addAll(parsedUrls)
                        displayChannels()
                    }
                }
            })
        }

        private fun parsePlaylist(playlist: String?): List<String> {
            val urls = mutableListOf<String>()

            playlist?.let {
                val lines = it.lines()
                for (line in lines) {
                    if (line.startsWith("http")) {
                        urls.add(line)
                    }
                }
            }

            return urls
        }

        private fun displayChannels() {
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, audioStreamUrls)
            listView.adapter = adapter
        }

        override fun onDestroy() {
            super.onDestroy()
            exoPlayer.release()
        }
    }
