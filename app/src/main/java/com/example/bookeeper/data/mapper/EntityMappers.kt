package com.example.bookeeper.data.mapper

import com.example.bookeeper.data.local.entity.AccountEntity
import com.example.bookeeper.data.local.entity.CategoryEntity
import com.example.bookeeper.data.local.entity.TransactionEntity
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionRecord

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    transactionType = transactionType,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isBuiltIn = isBuiltIn,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    transactionType = transactionType,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isBuiltIn = isBuiltIn,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = type,
    initialBalance = Money(initialBalanceInCents),
    iconKey = iconKey,
    colorArgb = colorArgb,
    isBuiltIn = isBuiltIn,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type,
    initialBalanceInCents = initialBalance.cents,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isBuiltIn = isBuiltIn,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun TransactionEntity.toDomain(): TransactionRecord = TransactionRecord(
    id = id,
    type = type,
    amount = Money(amountInCents),
    categoryId = categoryId,
    accountId = accountId,
    note = note,
    occurredAtMillis = occurredAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun TransactionRecord.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type,
    amountInCents = amount.cents,
    categoryId = categoryId,
    accountId = accountId,
    note = note,
    occurredAtMillis = occurredAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)
