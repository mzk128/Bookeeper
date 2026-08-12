package com.example.bookeeper.domain.model

enum class AccountType(val storageValue: String) {
    CASH("cash"),
    BANK_CARD("bank_card"),
    ALIPAY("alipay"),
    WECHAT("wechat"),
    OTHER("other"),
    ;

    companion object {
        fun fromStorageValue(value: String): AccountType = entries.firstOrNull {
            it.storageValue == value
        } ?: throw IllegalArgumentException("Unknown account type: $value")
    }
}
