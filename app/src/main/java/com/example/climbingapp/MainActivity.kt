package com.example.climbingapp

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.media.SoundPool
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.util.Log

class MainActivity : AppCompatActivity() {
    private var currentScore = 0
    private var currentHold = 0
    private var hasFallen = false
    private var highScore = 0
    private lateinit var soundPool: SoundPool
    private var soundClimb = 0
    private var soundFall = 0
    private var soundSuccess = 0
    private var soundReset = 0

    // Các hằng số
    companion object {
        private const val MAX_SCORE = 18
        private const val FALL_PENALTY = 3
        private const val BLUE_ZONE_START = 1
        private const val BLUE_ZONE_END = 3
        private const val GREEN_ZONE_START = 4
        private const val GREEN_ZONE_END = 6
        private const val RED_ZONE_START = 7
        private const val RED_ZONE_END = 9
        private const val TAG = "ClimbingScoreApp"
    }

    private lateinit var tvCurrentHold: TextView
    private lateinit var tvZoneInfo: TextView
    private lateinit var progressHolds: LinearProgressIndicator
    private lateinit var fabStats: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo views ngay sau setContentView
        tvCurrentHold = findViewById(R.id.tvCurrentHold)
        tvZoneInfo = findViewById(R.id.tvZoneInfo)
        progressHolds = findViewById(R.id.progressHolds)
        fabStats = findViewById(R.id.fabStats)

        setupToolbar()
        //setupSounds()
        loadHighScore()

        // Khôi phục trạng thái nếu có
        savedInstanceState?.let {
            currentScore = it.getInt("score", 0)
            currentHold = it.getInt("hold", 0)
            hasFallen = it.getBoolean("fallen", false)
            updateScoreDisplay()
        }

        setupButtons()
        setupProgress()
        setupFab()
        Log.d(TAG, "App khởi tạo thành công")
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    //private fun setupSounds() {
        //soundPool = SoundPool.Builder()
            //.setMaxStreams(3)
            //.build()

        //soundClimb = soundPool.load(this, R.raw.climb, 1)
        //soundFall = soundPool.load(this, R.raw.fall, 1)
        //soundSuccess = soundPool.load(this, R.raw.success, 1)
        //soundReset = soundPool.load(this, R.raw.reset, 1)
    //}

    private fun loadHighScore() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
        updateHighScore()
    }

    private fun updateHighScore() {
        if (currentScore > highScore) {
            highScore = currentScore
            saveHighScore()
        }
        findViewById<TextView>(R.id.tvHighScore).text =
            getString(R.string.high_score, highScore)
    }

    private fun saveHighScore() {
        getPreferences(Context.MODE_PRIVATE).edit {
            putInt("high_score", highScore)
        }
        // soundPool.play(soundSuccess, 1f, 1f, 1, 0, 1f)
        Log.d(TAG, "Đã lưu điểm cao mới: $highScore")
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.btnClimb).setOnClickListener {
            if (!hasFallen) {
                currentHold++
                updateScore()
                // soundPool.play(soundClimb, 1f, 1f, 1, 0, 1f)
                animateScore()
                Log.d(TAG, "Đã leo lên hold $currentHold")
            }
        }

        findViewById<MaterialButton>(R.id.btnFall).setOnClickListener {
            if (!hasFallen) {
                hasFallen = true
                currentScore = maxOf(0, currentScore - FALL_PENALTY)
                updateScoreDisplay()
                // soundPool.play(soundFall, 1f, 1f, 1, 0, 1f)
                updateHoldInfo()
                Log.d(TAG, "Đã ngã ở hold $currentHold")
            }
        }

        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            resetGame()
            // soundPool.play(soundReset, 1f, 1f, 1, 0, 1f)
            Log.d(TAG, "Game đã reset")
        }

        // Xóa phần khởi tạo views ở đây vì đã khởi tạo ở onCreate
        updateHoldInfo()
    }

    private fun updateScore() {
        val points = when (currentHold) {
            in BLUE_ZONE_START..BLUE_ZONE_END -> 1
            in GREEN_ZONE_START..GREEN_ZONE_END -> 2
            in RED_ZONE_START..RED_ZONE_END -> 3
            else -> 0
        }
        currentScore = minOf(MAX_SCORE, currentScore + points)
        updateScoreDisplay()
        updateProgress()
        updateHoldInfo()
    }

    private fun updateScoreDisplay() {
        val scoreText = findViewById<TextView>(R.id.tvScore)
        scoreText.text = currentScore.toString()

        // Cập nhật màu dựa theo vùng
        val color = when (currentHold) {
            in BLUE_ZONE_START..BLUE_ZONE_END -> Color.BLUE
            in GREEN_ZONE_START..GREEN_ZONE_END -> Color.GREEN
            in RED_ZONE_START..RED_ZONE_END -> Color.RED
            else -> Color.BLACK
        }
        scoreText.setTextColor(color)
    }

    private fun updateHoldInfo() {
        tvCurrentHold.text = getString(R.string.current_hold, currentHold)

        val zoneText = when (currentHold) {
            0 -> getString(R.string.not_started)
            in BLUE_ZONE_START..BLUE_ZONE_END -> getString(R.string.blue_zone)
            in GREEN_ZONE_START..GREEN_ZONE_END -> getString(R.string.green_zone)
            in RED_ZONE_START..RED_ZONE_END -> getString(R.string.red_zone)
            else -> ""
        }
        tvZoneInfo.text = if (hasFallen) getString(R.string.fallen) else zoneText
    }

    private fun resetGame() {
        currentScore = 0
        currentHold = 0
        hasFallen = false
        updateScoreDisplay()
        updateProgress()
        updateHoldInfo()
    }

    private fun setupProgress() {
        progressHolds.max = RED_ZONE_END
    }

    private fun setupFab() {
        fabStats.setOnClickListener {
            showStats()
        }
    }

    private fun updateProgress() {
        progressHolds.setProgressCompat(currentHold, true)
        val color = when (currentHold) {
            in BLUE_ZONE_START..BLUE_ZONE_END -> getColor(R.color.zone_blue)
            in GREEN_ZONE_START..GREEN_ZONE_END -> getColor(R.color.zone_green)
            in RED_ZONE_START..RED_ZONE_END -> getColor(R.color.zone_red)
            else -> getColor(R.color.text_secondary)
        }
        progressHolds.setIndicatorColor(color)
    }

    private fun animateScore() {
        val scoreText = findViewById<TextView>(R.id.tvScore)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f, 1f)

        ObjectAnimator.ofPropertyValuesHolder(scoreText, scaleX, scaleY).apply {
            duration = 300
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun showStats() {
        // Hiển thị bottom sheet với thống kê
        StatsBottomSheet().show(supportFragmentManager, "stats")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("score", currentScore)
        outState.putInt("hold", currentHold)
        outState.putBoolean("fallen", hasFallen)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}