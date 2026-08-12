package com.example.bookeeper.ui.transaction

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun delete_requiresConfirmationBeforeCallback() {
        var deleteCalls = 0
        composeRule.setContent {
            MaterialTheme {
                TransactionDetailContent(
                    uiState = TransactionDetailUiState(
                        isLoading = false,
                        transaction = TransactionRecord(
                            1L,
                            TransactionType.EXPENSE,
                            Money(1_280L),
                            1L,
                            1L,
                            "午餐",
                            1_000L,
                            1_000L,
                            1_000L,
                        ),
                        categoryName = "餐饮",
                        accountName = "现金",
                    ),
                    onEdit = {},
                    onDelete = { deleteCalls++ },
                )
            }
        }

        composeRule.onNodeWithText("删除账单").performClick()
        assertEquals(0, deleteCalls)
        composeRule.onNodeWithText("确认删除账单？").assertIsDisplayed()
        composeRule.onNodeWithText("删除").performClick()
        assertEquals(1, deleteCalls)
    }
}
