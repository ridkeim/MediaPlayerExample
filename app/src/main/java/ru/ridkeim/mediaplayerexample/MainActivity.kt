package ru.ridkeim.mediaplayerexample

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.MediaController
import android.widget.Toast
import ru.ridkeim.mediaplayerexample.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(){
    private val requestAudioCode = 42
    private lateinit var viewBinding: ActivityMainBinding
    private val resourcesUris = arrayOf(
        "http://techslides.com/demos/sample-videos/small.mp4"
    )
    private val randomUriString : String
     get() {
         return resourcesUris[(resourcesUris.size * Math.random()).toInt()]
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.chooseFile.setOnClickListener {
            startActivityForResult(
                Intent.createChooser(
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "video/*"
                    }, "select video"
                ),requestAudioCode
            )
        }
        viewBinding.videoView.setMediaController(MediaController(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            requestAudioCode -> {
                if(resultCode == RESULT_OK){
                    val uri = data?.data ?: Uri.parse(randomUriString)
                    viewBinding.videoView.setVideoURI(uri)
                    viewBinding.videoView.start()
                }
            }
        }
    }


}