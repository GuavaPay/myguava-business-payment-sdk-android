package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.platform.compose.ComposeString
import java.io.Serializable
import androidx.compose.ui.res.stringResource as string

sealed class PaymentKind(open val text: ComposeString) : Serializable {
  data object Book : PaymentKind({ string(R.string.payment_kind_book) })
  data object Buy : PaymentKind({ string(R.string.payment_kind_buy) })
  data object Checkout : PaymentKind({ string(R.string.payment_kind_checkout) })
  data object Donate : PaymentKind({ string(R.string.payment_kind_donate) })
  data object Order : PaymentKind({ string(R.string.payment_kind_order) })
  data object Pay : PaymentKind({ string(R.string.payment_kind_pay) })
  data object Plain : PaymentKind({ string(R.string.payment_kind_plain) })
  data object Subscribe : PaymentKind({ string(R.string.payment_kind_subscribe) })
  data class Custom(override val text: ComposeString) : PaymentKind(text)
}