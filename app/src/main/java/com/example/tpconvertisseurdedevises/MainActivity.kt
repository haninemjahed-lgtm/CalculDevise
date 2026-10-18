package com.example.tpconvertisseurdedevises

import android.graphics.Paint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tpconvertisseurdedevises.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    // Taux pour 1 EUR (EUR = devise pivot)
    private val taux = mapOf(
        "EUR" to 1.0, "USD" to 1.17, "TND" to 3.40,
        "GBP" to 0.87, "JPY" to 172.50, "CAD" to 1.62
    )

    private val longueurMax = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Marges des barres système (voir chapitre 5, section 12)
        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Soulignement du montant et du résultat
        b.tvMontant.paintFlags = b.tvMontant.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        b.tvResultat.paintFlags = b.tvResultat.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        configurerSpinners()
        configurerClavier()

        b.btnConvertir.setOnClickListener { lancerConversion() }
        b.btnInverser.setOnClickListener { inverserDevises() }
    }

    /** Remplit les deux Spinner depuis R.array.devises. USD par défaut en source, EUR en cible. */
    private fun configurerSpinners() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.devises, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        b.spDeviseSource.adapter = adapter
        b.spDeviseCible.adapter = adapter

        val liste = resources.getStringArray(R.array.devises).toList()
        b.spDeviseSource.setSelection(liste.indexOf("USD"))
        b.spDeviseCible.setSelection(liste.indexOf("EUR"))
    }

    /** Branche toutes les touches du clavier. */
    private fun configurerClavier() {
        val touches = listOf(
            b.btn0, b.btn1, b.btn2, b.btn3, b.btn4,
            b.btn5, b.btn6, b.btn7, b.btn8, b.btn9, b.btnPoint
        )
        touches.forEach { touche ->
            touche.setOnClickListener { ajouterCaractere(touche.text.toString()) }
        }
        b.btnC.setOnClickListener { reinitialiser() }
    }

    /** Ajoute un chiffre ou un point à la fin du montant, en respectant les règles de gestion. */
    private fun ajouterCaractere(c: String) {
        val actuel = b.tvMontant.text.toString()

        // Un seul point décimal
        if (c == "." && actuel.contains(".")) return

        val nouveau = if (actuel == "0" && c != ".") {
            c                       // le premier chiffre remplace le "0" initial
        } else {
            if (actuel.length >= longueurMax) return   // limite de 10 caractères
            actuel + c
        }
        b.tvMontant.text = nouveau
    }

    /** Touche C : montant remis à 0 et résultat vidé. */
    private fun reinitialiser() {
        b.tvMontant.text = getString(R.string.montant_initial)
        b.tvResultat.text = ""
    }

    /** Conversion via l'euro : montant / taux(source) * taux(cible). */
    private fun convertir(montant: Double, source: String, cible: String): Double {
        val tauxSource = taux.getValue(source)
        val tauxCible = taux.getValue(cible)
        val montantEnEuro = montant / tauxSource
        return montantEnEuro * tauxCible
    }

    /** Lit l'écran, vérifie le montant, convertit et affiche le résultat. */
    private fun lancerConversion() {
        val montant = b.tvMontant.text.toString().toDoubleOrNull() ?: 0.0
        if (montant == 0.0) {
            Toast.makeText(this, R.string.toast_montant, Toast.LENGTH_SHORT).show()
            return
        }
        val source = b.spDeviseSource.selectedItem.toString()
        val cible = b.spDeviseCible.selectedItem.toString()

        val resultat = convertir(montant, source, cible)
        // Locale.US pour garantir le point décimal
        b.tvResultat.text = String.format(Locale.US, "%.2f", resultat)
    }

    /** Échange les deux devises, puis relance la conversion si un résultat est affiché. */
    private fun inverserDevises() {
        val posSource = b.spDeviseSource.selectedItemPosition
        val posCible = b.spDeviseCible.selectedItemPosition
        b.spDeviseSource.setSelection(posCible)
        b.spDeviseCible.setSelection(posSource)

        if (b.tvResultat.text.isNotEmpty()) lancerConversion()
    }
}