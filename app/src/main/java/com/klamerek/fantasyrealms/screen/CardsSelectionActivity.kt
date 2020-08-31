package com.klamerek.fantasyrealms.screen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.klamerek.fantasyrealms.R
import kotlinx.android.synthetic.main.activity_cards_selection.*
import java.io.Serializable

class CardsSelectionActivity : AppCompatActivity() {

    private lateinit var input : CardsSelectionExchange

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards_selection)

        input = intent.getSerializableExtra(Constants.CARD_SELECTION_DATA_EXCHANGE_SESSION_ID) as CardsSelectionExchange

        updateMainLabel()
        checkSelectedCards()
        checkSelectedSuits()

        adaptCardListDisplay()
        adaptSuitListDisplay()
        updateMainButtonStatus()

        addCardsButton.setOnClickListener {
            val closingIntent = Intent()
            val answer = CardsSelectionExchange()
            answer.cardInitiator = input.cardInitiator
            answer.cardsSelected = cardChips().filter { chip -> chip.isChecked }
                .map { chip -> chip.tag }.mapNotNull { tag -> Integer.valueOf(tag.toString()) }.toMutableList()
            answer.suitsSelected = suitChips().filter { chip -> chip.isChecked }
                .mapNotNull { chip -> chip.tag.toString() }.toMutableList()
            closingIntent.putExtra(Constants.CARD_SELECTION_DATA_EXCHANGE_SESSION_ID, answer)
            setResult(Constants.RESULT_OK, closingIntent)
            finish()
        }

    }

    private fun checkSelectedSuits() {
        suitChips().forEach { chip -> chip.isChecked = input.suitsSelected.contains(chip.tag.toString()) }
    }

    private fun adaptSuitListDisplay() {
        val suitActivated = input.selectionMode == Constants.CARD_LIST_SELECTION_MODE_ONE_CARD_AND_SUIT
        suitChipGroup.visibility = if (suitActivated) View.VISIBLE else View.GONE
        divider.visibility = if (suitActivated) View.VISIBLE else View.GONE
        suitChips().forEach { chip -> chip.setOnClickListener(onlyOneSuitSelected()) }
    }

    private fun adaptCardListDisplay() {
        if (input.selectionMode != Constants.CARD_LIST_SELECTION_MODE_DEFAULT) {
            showOnlyPotentialCandidates()
            cardChips().forEach { chip -> chip.setOnClickListener(onlyOneCardSelected()) }
        }
    }

    private fun updateMainLabel() {
        selectionLabel.text = input.label.orEmpty()
    }

    private fun onlyOneSuitSelected(): View.OnClickListener? {
        return View.OnClickListener {
            suitChips().filter { chip -> chip != it }.forEach { chip -> chip.isChecked = false }
            updateMainButtonStatus()
        }
    }

    private fun onlyOneCardSelected(): View.OnClickListener {
        return View.OnClickListener {
            cardChips().filter { chip -> chip != it }.forEach { chip -> chip.isChecked = false }
            updateMainButtonStatus()
        }
    }

    private fun updateMainButtonStatus() {
        when (input.selectionMode) {
            Constants.CARD_LIST_SELECTION_MODE_DEFAULT -> addCardsButton.isEnabled = true
            Constants.CARD_LIST_SELECTION_MODE_ONE_CARD ->
                addCardsButton.isEnabled = cardChips().count { chip -> chip.isChecked } == 1
            Constants.CARD_LIST_SELECTION_MODE_ONE_CARD_AND_SUIT ->
                addCardsButton.isEnabled = cardChips().count { chip -> chip.isChecked } == 1 &&
                        suitChips().count { chip -> chip.isChecked } == 1

        }
    }

    private fun showOnlyPotentialCandidates() {
        cardChips().forEach { chip ->
            chip.visibility = if (input.cardsScope.contains(Integer.valueOf(chip.tag.toString()))) View.VISIBLE else View.GONE
        }
    }

    private fun checkSelectedCards() {
        cardChips().forEach { chip -> chip.isChecked = input.cardsSelected.contains(Integer.valueOf(chip.tag.toString())) }
    }

    private fun cardChips() = chipGroup.children.filter { child -> child is Chip }.map { child -> child as Chip }

    private fun suitChips() = suitChipGroup.children.filter { child -> child is Chip }.map { child -> child as Chip }
}

/**
 * POJO to transfer in and out data from cards selection activity
 *
 * @property selectionMode              can be many cards or one card or or card and a suit
 * @property label                      if set, display a label on the top
 * @property cardInitiator              card initiator of the selection (special rules), transferred when activity is finished
 * @property cardsSelected              indicates which cards must already displayed as selected
 * @property suitsSelected               indicates which suits must already displayed as selected
 * @property cardsScope                 indicates which cards must be accessible for selection
 */
class CardsSelectionExchange() : Serializable {
    var selectionMode: Int = Constants.CARD_LIST_SELECTION_MODE_DEFAULT
    var label: String? = null
    var cardInitiator: Int? = null
    var cardsSelected: MutableList<Int> = ArrayList()
    var suitsSelected: MutableList<String> = ArrayList()
    val cardsScope: MutableList<Int> = ArrayList()
}