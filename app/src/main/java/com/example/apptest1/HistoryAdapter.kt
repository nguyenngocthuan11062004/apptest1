package com.example.apptest1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptest1.model.HistoryItem
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private var list: List<HistoryItem>
) : RecyclerView.Adapter<HistoryAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.img)
        val result: TextView = v.findViewById(R.id.result)
        val text: TextView = v.findViewById(R.id.text)
        val time: TextView = v.findViewById(R.id.time)
    }

    fun update(newList: List<HistoryItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return Holder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: Holder, position: Int) {
        val item = list[position]

        // ===== PARSE RESULT =====
        val json = JSONObject(item.result)
        val prediction = json.optString("prediction")
        val confidence = json.optDouble("confidence", 0.0)
        val percent = String.format(Locale.US, "%.2f%%", confidence * 100)

        if (prediction.contains("phishing", true)) {
            h.img.setImageResource(R.drawable.phishing_history)
            h.result.text = "PHISHING · $percent"
            h.result.setTextColor(Color.parseColor("#D32F2F"))
        } else {
            h.img.setImageResource(R.drawable.legit_history)
            h.result.text = "LEGIT · $percent"
            h.result.setTextColor(Color.parseColor("#2E7D32"))
        }

        // ===== OCR TEXT =====
        h.text.text = item.text.take(150) + if (item.text.length > 150) "..." else ""

        // ===== TIME =====
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        h.time.text = sdf.format(Date(item.time))
    }
}
