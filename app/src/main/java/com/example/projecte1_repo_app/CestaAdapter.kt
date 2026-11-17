package com.example.projecte1_repo_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

// Una línea de la cistella: o bé entrada general (amb quantitat) o bé butaca numerada
sealed class LiniaCesta {
    data class General(val entrada: TipusEntrada, val quantitat: Int) : LiniaCesta()
    data class Butaca(val butaca: ButacaCesta) : LiniaCesta()
}

class CestaAdapter(
    private var linies: List<LiniaCesta>,
    private val onMes: (TipusEntrada) -> Unit,
    private val onMenys: (LiniaCesta.General) -> Unit,
    private val onEliminarButaca: (ButacaCesta) -> Unit
) : BaseAdapter() {

    // La Activity l'utilitza per redibuixar la llista amb dades noves
    fun actualitzar(noves: List<LiniaCesta>) {
        linies = noves
        notifyDataSetChanged()
    }

    override fun getCount() = linies.size
    override fun getItem(position: Int) = linies[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vista = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cesta_linia, parent, false)

        val txtNom = vista.findViewById<TextView>(R.id.txtNomLinia)
        val btnMenys = vista.findViewById<Button>(R.id.btnMenys)
        val txtQuantitat = vista.findViewById<TextView>(R.id.txtQuantitat)
        val btnMes = vista.findViewById<Button>(R.id.btnMes)
        val btnEliminar = vista.findViewById<Button>(R.id.btnEliminarButaca)

        when (val linia = linies[position]) {
            is LiniaCesta.General -> {
                val preu = linia.entrada.preuTipus * linia.quantitat
                txtNom.text = "${linia.entrada.nomTipus}  ->  $preu €"
                txtQuantitat.text = linia.quantitat.toString()

                btnMenys.visibility = View.VISIBLE
                txtQuantitat.visibility = View.VISIBLE
                btnMes.visibility = View.VISIBLE
                btnEliminar.visibility = View.GONE

                btnMes.setOnClickListener { onMes(linia.entrada) }
                btnMenys.setOnClickListener { onMenys(linia) }
            }
            is LiniaCesta.Butaca -> {
                val b = linia.butaca
                txtNom.text = "${b.nomTipus} - Fila ${b.fila}, butaca ${b.numero}  ->  ${b.preu} €"

                btnMenys.visibility = View.GONE
                txtQuantitat.visibility = View.GONE
                btnMes.visibility = View.GONE
                btnEliminar.visibility = View.VISIBLE

                btnEliminar.setOnClickListener { onEliminarButaca(b) }
            }
        }
        return vista
    }
}