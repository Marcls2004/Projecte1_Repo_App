package com.example.projecte1_repo_app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartelleraAdapter(
    private var llista: List<Esdeveniment>
) : RecyclerView.Adapter<CartelleraAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tTitol: TextView = view.findViewById(R.id.textTitol)
        val tData: TextView = view.findViewById(R.id.textData)
        val layoutEntrades: LinearLayout =
            view.findViewById(R.id.layoutEntrades)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esdeveniment, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val esdeveniment = llista[position]

        holder.tTitol.text = esdeveniment.nom
        holder.tData.text = esdeveniment.data
        holder.layoutEntrades.removeAllViews()

        holder.layoutEntrades.visibility =
            if (esdeveniment.isExpanded) View.VISIBLE else View.GONE

        if (esdeveniment.isExpanded) {
            for (entrada in esdeveniment.tipusEntrades) {

                // Spec 13: de moment només es mostren les entrades GENERALS
                if (entrada.tipusZona != "general") {
                    continue
                }

                val textEntrada = TextView(holder.itemView.context)

                textEntrada.text =
                    "${entrada.nomTipus} - ${entrada.preuTipus} €"

                textEntrada.textSize = 16f
                textEntrada.setPadding(8, 12, 8, 12)

                textEntrada.setOnClickListener {
                    val intent = Intent(
                        holder.itemView.context,
                        DetallEsdevenimentActivity::class.java
                    )

                    intent.putExtra("idTipus", entrada.idTipus)
                    intent.putExtra("nomTipus", entrada.nomTipus)
                    intent.putExtra("preuTipus", entrada.preuTipus)
                    intent.putExtra("tipusZona", entrada.tipusZona)
                    intent.putExtra("idEsdeveniment", esdeveniment.id)

                    holder.itemView.context.startActivity(intent)
                }

                holder.layoutEntrades.addView(textEntrada)
            }

            // Si l'esdeveniment només té entrades numerades, ho indiquem
            if (holder.layoutEntrades.childCount == 0) {
                val textBuit = TextView(holder.itemView.context)
                textBuit.text = "No hi ha entrades generals disponibles"
                textBuit.textSize = 16f
                textBuit.setPadding(8, 12, 8, 12)
                holder.layoutEntrades.addView(textBuit)
            }
        }

        holder.itemView.setOnClickListener {
            esdeveniment.isExpanded = !esdeveniment.isExpanded
            notifyItemChanged(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int = llista.size

    fun updateData(novaLlista: List<Esdeveniment>) {
        llista = novaLlista
        notifyDataSetChanged()
    }
}