package com.example.anidate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем твой splash_background как фон всего экрана
        window.setBackgroundDrawableResource(R.drawable.splash_background)

        // Таймер на 2000 миллисекунд (2 секунды)
        Handler(Looper.getMainLooper()).postDelayed({
            // Переходим на главный экран
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Закрываем заставку, чтобы нельзя было вернуться назад кнопкой
            finish()
        }, 2000)
    }
}