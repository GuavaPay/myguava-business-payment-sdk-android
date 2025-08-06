package com.guavapay.paymentsdk.presentation.screens.phone._components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun PhoneSearch(searchQuery: String, onSearchQueryChange: (String) -> Unit, modifier: Modifier = Modifier, fieldModifier: Modifier = Modifier) {
  TextField(
    value = searchQuery,
    onValueChange = onSearchQueryChange,
    placeholder = stringResource(R.string.search_country_placeholder),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
    singleLine = true,
    leadingIcon = painterResource(R.drawable.ic_search),
    modifier = modifier,
    fieldModifier = fieldModifier,
  )
}

@PreviewLightDark @Composable private fun PhoneSearchPreview() {
  PreviewTheme {
    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }
    PhoneSearch(searchQuery = searchQuery, onSearchQueryChange = setSearchQuery)
  }
}