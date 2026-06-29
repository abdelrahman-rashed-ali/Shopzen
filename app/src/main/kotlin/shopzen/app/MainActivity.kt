package shopzen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import shopzen.app.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import shopzen.presentation.theme.MyApplicationTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            MyApplicationTheme{
                AppNavHost(navController = navController)
            }
        }
    }
}
