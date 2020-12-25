package ru.ridkeim.mediaplayerexample

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import ru.ridkeim.mediaplayerexample.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val requestAudioCode = 42
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var mediaPlayer : MediaPlayer
    private val resourcesUris = arrayOf(
        "https://sound-pack.net/download/Sound_17453_SynthWah.ogg",
        "https://sound-pack.net/download/Sound_17433_ShakerD.ogg",
        "https://sound-pack.net/download/Sound_22719_Tambourine_130_BPM.ogg",
        "https://ssound-pack.net/download/Sound_22721_Triangle_95_BPM.ogg",
        "https://sound-pack.net/download/Sound_11942_CharismaRhythmGuitB.ogg",
        "https://sound-pack.net/download/Sound_11920_HeavyD.ogg"
    )
    private val randomUriString : String
     get() {
         return resourcesUris[(resourcesUris.size * Math.random()).toInt()]
     }
    private var playerState = PlayerState.IDLE
    enum class PlayerState {
        STOPPED,STARTED,PAUSED,PREPARED,IDLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        volumeControlStream = AudioManager.STREAM_MUSIC
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
        mediaPlayer.setOnPreparedListener {
            viewBinding.buttonStop.isEnabled = true
            viewBinding.buttonPlay.isEnabled = true
            playerState = PlayerState.PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            viewBinding.buttonStop.performClick()
        }
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            val whatMsg = if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                "ServerDied"
            }else{
                "Unknown"
            }
            val extraMsg = when (extra) {
                MediaPlayer.MEDIA_ERROR_IO -> "Io"
                MediaPlayer.MEDIA_ERROR_MALFORMED -> "Malformed"
                MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "Unsupported"
                MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "Timed Out"
                -2147483648 -> "low-level system error"
                else -> "Unknown"
            }
            Toast.makeText(applicationContext,"Error: $whatMsg $extraMsg",Toast.LENGTH_SHORT).show()
            return@setOnErrorListener true
        }
        nextRandomTrack()
        viewBinding.buttonPlay.setOnClickListener {
            if(playerState == PlayerState.STOPPED){
                mediaPlayer.prepare()
                playerState = PlayerState.PREPARED
            }
            when(playerState){
                PlayerState.PREPARED, PlayerState.PAUSED->{
                    viewBinding.buttonPlay.setText(R.string.pause)
                    mediaPlayer.start()
                    playerState = PlayerState.STARTED
                    viewBinding.playerState.setText(R.string.playing)
                }
                PlayerState.STARTED -> {
                    viewBinding.buttonPlay.setText(R.string.play)
                    mediaPlayer.pause()
                    playerState = PlayerState.PAUSED
                    viewBinding.playerState.setText(R.string.paused)
                }
                else -> {}
            }
        }
        viewBinding.buttonStop.setOnClickListener {
            viewBinding.playerState.setText(R.string.press_play)
            viewBinding.buttonPlay.setText(R.string.play)
            mediaPlayer.stop()
            playerState = PlayerState.STOPPED
        }
        viewBinding.buttonNext.setOnClickListener {
            nextRandomTrack()
        }
        viewBinding.chooseFile.setOnClickListener {
            startActivityForResult(
                Intent.createChooser(
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "audio/*"
                    }, "select audio"
                ),requestAudioCode
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            requestAudioCode -> {
                if(resultCode == RESULT_OK){
                    val audioUri = data?.data ?: Uri.parse(randomUriString)
                    loadNextTrack(audioUri)
                }
            }
        }
    }

    private fun nextRandomTrack(){
        val uri = Uri.parse(randomUriString)
        loadNextTrack(uri)
    }

    private fun loadNextTrack(uri : Uri){
        viewBinding.buttonStop.isEnabled = false
        viewBinding.buttonPlay.isEnabled = false
        viewBinding.buttonPlay.setText(R.string.play)
        viewBinding.playerState.setText(R.string.press_play)
        mediaPlayer.reset()
        playerState = PlayerState.IDLE
        try {
            mediaPlayer.setDataSource(applicationContext,uri)
            mediaPlayer.prepareAsync()
        } catch (e : IOException){
            Toast.makeText(applicationContext,"Error while trying to load/prepare file $uri",Toast.LENGTH_SHORT).show()
            mediaPlayer.reset()
            playerState = PlayerState.IDLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}