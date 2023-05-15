package company.apptechno.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import company.apptechno.instagramclone.Main.*
import company.apptechno.instagramclone.auth.FeedScreen
import company.apptechno.instagramclone.auth.LogInScreen
import company.apptechno.instagramclone.auth.MyProfileScreen
import company.apptechno.instagramclone.auth.SignupScreen
import company.apptechno.instagramclone.data.PostDetails
import company.apptechno.instagramclone.ui.theme.InstagramCloneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstagramCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Feed : DestinationScreen("feed")
    object Search : DestinationScreen("search")
    object MyPosts : DestinationScreen("myposts")
    object EditProfile : DestinationScreen("profile")
    object SinglePostsScreen : DestinationScreen("singlepost")
    object NewPost : DestinationScreen("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }
    object CommentsScreen : DestinationScreen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
}

@Composable
fun InstagramApp() {
    FirebaseApp.initializeApp(LocalContext.current)
    val vm = hiltViewModel<MainViewModel>()
    val navController = rememberNavController()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {
        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Login.route) {
            LogInScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Search.route) {
            SearchScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.MyPosts.route) {
            MyPostsScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.EditProfile.route){
            MyProfileScreen(navController = navController, vm = vm )
        }


        composable(DestinationScreen.NewPost.route) { navBackStachEntry ->
            val imageUri = navBackStachEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, vm = vm, encodedUri = it)
            }
        }
        composable(DestinationScreen.SinglePostsScreen.route){
            val postData = navController
                .previousBackStackEntry
                ?.arguments
                ?.getParcelable<PostDetails>("post")
            postData?.let {
                SinglePostsScreen(
                    navController = navController,
                    vm = vm,
                    postDetails = postData
                )
            }
        }

        composable(DestinationScreen.CommentsScreen.route) { navBackStachEntry ->
            val comment = navBackStachEntry.arguments?.getString("postId")
            comment?.let {
                CommentsScreen(navController = navController, vm = vm, postId = it)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InstagramCloneTheme {
       InstagramApp()
    }
}