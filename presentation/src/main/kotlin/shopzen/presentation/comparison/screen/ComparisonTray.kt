package shopzen.presentation.comparison.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shopzen.presentation.comparison.state.ComparisonState
import shopzen.presentation.comparison.state.ComparisonIntent

@Composable
fun ComparisonTray(
    state: ComparisonState,
    onIntent: (ComparisonIntent) -> Unit,
    onNavigateToComparison: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.selectedProducts.isEmpty()) return

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Compare (${state.selectedProducts.size}/2)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.selectedProducts.forEach { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(product.title, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onIntent(ComparisonIntent.RemoveProduct(product.productId)) }) {
                                Text("X")
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onIntent(ComparisonIntent.ClearComparison) }) {
                    Text("Clear")
                }
                Button(
                    onClick = { 
                        onIntent(ComparisonIntent.CompareWithAi)
                        onNavigateToComparison()
                    },
                    enabled = state.isCompareEnabled
                ) {
                    Text("Compare with AI")
                }
            }
        }
    }
}
