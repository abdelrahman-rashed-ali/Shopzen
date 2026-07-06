package shopzen.presentation.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import shopzen.presentation.onboarding.components.OnboardingBottomSection
import shopzen.presentation.onboarding.components.OnboardingPage
import shopzen.presentation.onboarding.components.OnboardingTopBar
import shopzen.presentation.onboarding.intent.OnboardingIntent
import shopzen.presentation.onboarding.viewmodel.OnboardingViewModel
import shopzen.presentation.R

data class OnboardingPageUi(
    val imageRes: Int,
    val title: String,
    val description: String
)

private val onboardingPages = listOf(
    OnboardingPageUi(
        title = "Discover the Extraordinary",
        description = "Curated collections of the world's finest timepieces and jewelry, tailored for the modern connoisseur.",
        imageRes = R.drawable.onboarding_screen1
    ),
    OnboardingPageUi(
        title = "Authenticity Guaranteed",
        description = "Every piece in our collection is meticulously inspected and certified for quality, ensuring you receive only the best.",
        imageRes = R.drawable.onboarding_screen2
    ),
    OnboardingPageUi(
        title = "Delivered with Care",
        description = "Experience premium, white-glove delivery service right to your doorstep, wherever you are in the world.",
        imageRes = R.drawable.onboarding_screen3
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        OnboardingTopBar(
            onSkipClick = { viewModel.processIntent(OnboardingIntent.CompleteOnboarding) }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction

            OnboardingPage(
                title = page.title,
                description = page.description,
                imageRes = page.imageRes,
                pageOffset = pageOffset
            )
        }

        OnboardingBottomSection(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage,
            onNextClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onGetStartedClick = {
                viewModel.processIntent(OnboardingIntent.CompleteOnboarding)
            }
        )
    }
}
