package com.example.pokedex.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.AuthResponse
import com.example.pokedex.models.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editLogin: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        editName = findViewById(R.id.editName)
        editLogin = findViewById(R.id.editLogin)
        editPassword = findViewById(R.id.editPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = editName.text.toString().trim()
        val login = editLogin.text.toString().trim()
        val password = editPassword.text.toString().trim()

        if (name.isEmpty() || login.isEmpty() || password.isEmpty()) {
            showAlert("Dados Incompletos", "Por favor, preencha todos os campos para continuar.")
            return
        }

        val request = RegisterRequest(name, login, password)

        RetrofitClient.apiService.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    showAlert("Bem-vindo!", "Cadastro realizado com sucesso! Agora você pode fazer login.", closeOnDismiss = true)
                } else {
                    val errorMsg = when (response.code()) {
                        409 -> "Este login já está em uso.\nPor favor, escolha outro nome de usuário."
                        else -> "Erro ao cadastrar usuário (Código ${response.code()})."
                    }
                    showAlert("Erro no Cadastro", errorMsg)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                showAlert("Erro de Conexão", "Não foi possível conectar ao servidor.\nVerifique sua internet.")
            }
        })
    }

    private fun showAlert(title: String, message: String, closeOnDismiss: Boolean = false) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            if (closeOnDismiss) {
                finish()
            }
        }

        builder.setCancelable(false)

        builder.show()
    }
}