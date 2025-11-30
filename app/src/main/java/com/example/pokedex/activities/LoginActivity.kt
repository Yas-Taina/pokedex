package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.AuthResponse
import com.example.pokedex.models.LoginRequest
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var editLogin: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToRegister: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            goToHome()
        }
        editLogin = findViewById(R.id.editLogin)
        editPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val login = editLogin.text.toString().trim()
        val password = editPassword.text.toString().trim()

        if (login.isEmpty() || password.isEmpty()) {
            showAlert("Dados Incompletos", "Por favor, preencha o login e a senha.")
            return
        }

        val request = LoginRequest(login, password)

        RetrofitClient.apiService.login(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        sessionManager.saveUser(user.login, user.name)
                        goToHome()
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401, 404 -> "Usuário ou senha incorretos.\nTente novamente."
                        else -> "Erro no servidor (Código ${response.code()})"
                    }
                    showAlert("Acesso Negado", errorMsg)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                showAlert("Erro de Conexão", "Não foi possível conectar ao servidor.\nVerifique sua internet.")
            }
        })
    }

    private fun goToHome() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }
}