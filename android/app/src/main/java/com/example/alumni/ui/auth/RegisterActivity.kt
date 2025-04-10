package com.example.alumni.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.alumni.MainActivity
import com.example.alumni.R
import com.example.alumni.data.model.UserRole
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private val registerViewModel: RegisterViewModel by viewModels()

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etGraduationYear: TextInputEditText
    private lateinit var etDepartment: TextInputEditText
    private lateinit var rgRole: RadioGroup
    private lateinit var rbStudent: RadioButton
    private lateinit var rbAlumni: RadioButton
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etGraduationYear = findViewById(R.id.etGraduationYear)
        etDepartment = findViewById(R.id.etDepartment)
        rgRole = findViewById(R.id.rgRole)
        rbStudent = findViewById(R.id.rbStudent)
        rbAlumni = findViewById(R.id.rbAlumni)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            register()
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        registerViewModel.registrationResult.observe(this) { result ->
            when (result) {
                is RegistrationResult.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is RegistrationResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerViewModel.isLoading.observe(this) { isLoading ->
            btnRegister.isEnabled = !isLoading
            // Show/hide loading indicator if needed
        }
    }

    private fun register() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val graduationYear = etGraduationYear.text.toString().trim()
        val department = etDepartment.text.toString().trim()
        val role = when (rgRole.checkedRadioButtonId) {
            R.id.rbStudent -> UserRole.STUDENT
            R.id.rbAlumni -> UserRole.ALUMNI
            else -> UserRole.STUDENT // Default value
        }

        registerViewModel.register(
            name = name,
            email = email,
            password = password,
            role = role,
            graduationYear = graduationYear,
            department = department
        )
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}
