package com.kazemieh.home.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.home.component.CartItemCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navigateToCheckout: (Double) -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CartViewModel>()
    val cartState by viewModel.cartState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    ContentWithMessageBar(
        contentBackgroundColor = MaterialTheme.colorScheme.surface,
        messageBarState = messageBarState,
        errorContainerColor = MaterialTheme.colorScheme.error,
        errorContentColor = MaterialTheme.colorScheme.primaryContainer,
        successContainerColor = MaterialTheme.colorScheme.primary,
        successContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshCart() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = cartState) {
                is AppResult.Loading -> {
                    LoadingCard(modifier = Modifier.fillMaxWidth().height(200.dp))
                }

                is AppResult.Success -> {
                    val cart = state.data
                    if (cart.items.isEmpty()) {
                        InfoCard(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            image = Resources.Image.Cat,
                            title = stringResource(Resources.String.CartIsEmpty),
                            subtitle = stringResource(Resources.String.CartIsEmptySubtitle)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(
                                    items = cart.items,
                                    key = { it.id }
                                ) { item ->
                                    CartItemCard(
                                        cartItem = item,
                                        onPlusClick = {
                                            viewModel.adjustQuantity(item.variantId, 1, {}, { messageBarState.addError(it) })
                                        },
                                        onMinusClick = {
                                            viewModel.adjustQuantity(item.variantId, -1, {}, { messageBarState.addError(it) })
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteCartItem(item.id, {}, { messageBarState.addError(it) })
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Resources.String.TotalLabel),
                                    fontFamily = com.kazemieh.designsystem.AppFont(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(Resources.String.PriceFormat, cart.subtotal),
                                    fontFamily = com.kazemieh.designsystem.AppFont(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = com.kazemieh.designsystem.FontSize.LARGE,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryButton(
                                text = stringResource(Resources.String.Checkout),
                                onClick = { navigateToCheckout(cart.subtotal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                is AppResult.Error -> {
                    InfoCard(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        image = Resources.Image.Cat,
                        title = stringResource(Resources.String.Oops),
                        subtitle = state.message
                    )
                }
            }
        }
    }
}
