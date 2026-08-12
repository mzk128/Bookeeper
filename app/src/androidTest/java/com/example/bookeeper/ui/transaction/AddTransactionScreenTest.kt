package com.example.bookeeper.ui.transaction

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddTransactionScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun validationErrors_areDisplayed() {
        composeRule.setContent {
            MaterialTheme {
                AddTransactionContent(
                    uiState = testUiState(
                        form = testForm().copy(
                            amountError = AmountError.REQUIRED,
                            categoryError = true,
                            accountError = true,
                        ),
                    ),
                    onTypeSelected = {},
                    onAmountChanged = {},
                    onCategorySelected = {},
                    onAccountSelected = {},
                    onDateSelected = {},
                    onNoteChanged = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("请输入金额").assertIsDisplayed()
        composeRule.onNodeWithText("请选择分类").assertIsDisplayed()
        composeRule.onNodeWithText("请选择账户").assertIsDisplayed()
    }

    @Test
    fun amountInputAndSaveButton_forwardUserActions() {
        var amount = ""
        var saveClicks = 0
        composeRule.setContent {
            MaterialTheme {
                AddTransactionContent(
                    uiState = testUiState(),
                    onTypeSelected = {},
                    onAmountChanged = { amount = it },
                    onCategorySelected = {},
                    onAccountSelected = {},
                    onDateSelected = {},
                    onNoteChanged = {},
                    onSave = { saveClicks++ },
                )
            }
        }

        composeRule.onNodeWithText("金额（元）").performTextInput("25.68")
        composeRule.onNodeWithText("保存账单").performClick()

        assertEquals("25.68", amount)
        assertEquals(1, saveClicks)
    }

    private fun testUiState(
        form: TransactionFormState = testForm(),
    ) = AddTransactionUiState(
        form = form,
        categories = listOf(
            Category(
                id = 1L,
                name = "餐饮",
                transactionType = TransactionType.EXPENSE,
                iconKey = "test",
                colorArgb = 0L,
                isBuiltIn = true,
                isArchived = false,
                sortOrder = 0,
                createdAtMillis = 0L,
                updatedAtMillis = 0L,
            ),
        ),
        accounts = listOf(
            Account(
                id = 1L,
                name = "现金",
                type = AccountType.CASH,
                initialBalance = Money.Zero,
                iconKey = "test",
                colorArgb = 0L,
                isBuiltIn = true,
                isArchived = false,
                sortOrder = 0,
                createdAtMillis = 0L,
                updatedAtMillis = 0L,
            ),
        ),
    )

    private fun testForm() = TransactionFormState(
        occurredAtMillis = 1_700_000_000_000L,
        selectedCategoryId = 1L,
        selectedAccountId = 1L,
    )
}
