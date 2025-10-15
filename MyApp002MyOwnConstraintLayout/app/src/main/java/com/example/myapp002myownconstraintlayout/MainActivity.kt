package com.example.myapp002myownconstraintlayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputVelocity = findViewById<EditText>(R.id.inputVelocity)
        val inputYmm = findViewById<EditText>(R.id.inputYmm)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val resultText = findViewById<TextView>(R.id.resultText)

        // Air properties at 30 °C
        val rho = 1.164     // density [kg/m³]
        val nu = 1.568e-5   // kinematic viscosity [m²/s]

        btnCalculate.setOnClickListener {
            try {
                val U = inputVelocity.text.toString().toDouble()
                val y_mm = inputYmm.text.toString().toDouble()

                // Convert y from mm to meters
                val y = y_mm / 1000.0

                // Calculate Reynolds number (using characteristic length L = 1 m)
                val Re = U / nu

                // Skin friction coefficient (Blasius correlation)
                val Cf = 0.079 * Re.pow(-0.25)

                // Wall shear stress and friction velocity
                val tauW = 0.5 * Cf * rho * U * U
                val uTau = sqrt(tauW / rho)

                // y+ calculation
                val yPlus = uTau * y / nu

                // First cell height for y+ = 1
                val targetYPlus = 1.0
                val deltaY = targetYPlus * nu / uTau
                val deltaYmm = deltaY * 1000.0

                val result = """
                    --- Calculation results ---
                    
                    Flow conditions:
                    U = %.2f m/s
                    y = %.3f mm
                    Air (30 °C): ρ = %.3f kg/m³, ν = %.2e m²/s
                    
                    Results:
                    Estimated first cell height for y⁺ = 1:
                    Δy = %.3e m  (%.3f mm)
                    Re = %.2e
                    
                """.trimIndent().format(U, y_mm, rho, nu, deltaY, deltaYmm, Re, yPlus)

                resultText.text = result
            } catch (e: Exception) {
                Toast.makeText(this, "Please enter valid numeric values.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
