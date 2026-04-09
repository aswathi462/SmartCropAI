package com.example.myapplication

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import androidx.appcompat.app.AppCompatActivity

object LanguageSelectorHelper {

    data class LanguageOption(val code: String, val labelResId: Int)

    private val options = listOf(
        LanguageOption("en", R.string.language_english),
        LanguageOption("hi", R.string.language_hindi),
        LanguageOption("ml", R.string.language_malayalam)
    )

    fun bind(
        activity: AppCompatActivity,
        viewId: Int,
        onChanged: (() -> Unit)? = null
    ) {
        val dropdown = activity.findViewById<AutoCompleteTextView>(viewId)
        val labels = options.map { activity.getString(it.labelResId) }

        val adapter = object : ArrayAdapter<String>(
            activity,
            android.R.layout.simple_list_item_1,
            labels.toMutableList()
        ) {
            private val allItems = labels.toList()

            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val results = FilterResults()
                        results.values = allItems
                        results.count = allItems.size
                        return results
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        clear()
                        addAll(allItems)
                        notifyDataSetChanged()
                    }
                }
            }
        }

        dropdown.setAdapter(adapter)
        dropdown.setOnClickListener { dropdown.showDropDown() }
        dropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) dropdown.showDropDown()
        }

        val currentCode = LocaleManager.getSavedLanguage(activity)
        val selectedIndex = options.indexOfFirst { it.code == currentCode }.let { idx ->
            if (idx >= 0) idx else 0
        }
        dropdown.setText(labels[selectedIndex], false)

        dropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = options[position]
            val latestSavedCode = LocaleManager.getSavedLanguage(activity)
            if (selected.code != latestSavedCode) {
                LocaleManager.saveLanguage(activity, selected.code)
                dropdown.dismissDropDown()
                dropdown.clearFocus()
                dropdown.post {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        onChanged?.invoke() ?: activity.recreate()
                    }
                }
            }
        }
    }
}
