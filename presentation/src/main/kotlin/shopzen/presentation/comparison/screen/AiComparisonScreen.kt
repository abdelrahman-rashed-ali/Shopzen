package shopzen.presentation.comparison.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.comparison.state.ComparisonState
import shopzen.presentation.comparison.state.ComparisonIntent
import shopzen.presentation.comparison.state.AiComparisonResultUiModel
import shopzen.presentation.comparison.viewmodel.ComparisonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiComparisonScreen(
    viewModel: ComparisonViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result by viewModel.resultState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (result != null) {
            ComparisonResultContent(result = result!!, padding = padding)
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(state.error ?: "No comparison data")
            }
        }
    }
}

@Composable
fun ComparisonResultContent(result: AiComparisonResultUiModel, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Quick Verdict", 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(result.quickVerdict)
        }
        item {
            Text("Side-by-Side", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            result.sideBySideSummary.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(row.label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(row.productAValue, modifier = Modifier.weight(1f))
                    Text(row.productBValue, modifier = Modifier.weight(1f))
                }
            }
        }
        item {
            Text("Key Differences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            result.keyDifferences.forEach { Text("• $it") }
        }
        item {
            Text("Recommendation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(result.finalRecommendation)
        }
    }
}
