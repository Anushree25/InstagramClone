package company.apptechno.instagramclone.Main

import android.os.Parcelable
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import company.apptechno.instagramclone.DestinationScreen
import company.apptechno.instagramclone.MainViewModel
import company.apptechno.instagramclone.R

@Composable
fun NotificationMessage(vm : MainViewModel){
    val notifState = vm.popupNotification.value
    if(notifState != null) {
        val notifiMessage = notifState!!.getContentorNull()
        if (notifiMessage != null) {
            Toast.makeText(LocalContext.current, notifiMessage, Toast.LENGTH_LONG).show()
        }
    }
}
@Composable
fun CommonProgressSpinner() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) { }
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}


fun navigateTo(navController: NavController, destinationScreen: DestinationScreen,vararg params: NavParam){
    for (param in params) {
        navController.currentBackStackEntry?.arguments?.putParcelable(param.name, param.value)
    }
    navController.navigate(destinationScreen.route){
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }

}

fun navigateTo(navController: NavController, destinationScreen: DestinationScreen){
    navController.navigate(destinationScreen.route){
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }

}


@Composable
fun CheckSignedIn(vm: MainViewModel, navController: NavController) {
    val alreadyLoggedIn = remember { mutableStateOf(false) }
    val signedIn = vm.signedIn.value
    if (signedIn && !alreadyLoggedIn.value) {
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.Feed.route) {
            popUpTo(0)
        }
    }
}


@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {
    val painter = rememberImagePainter(data = data)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
    if (painter.state is ImagePainter.State.Loading) {
        CommonProgressSpinner()
    }
}

@Composable
fun UserImageCard(
    userImage: String?,
    modifier: Modifier = Modifier
        .padding(8.dp)
        .size(64.dp)
) {
    Card(shape = CircleShape, modifier = modifier) {
        if (userImage.isNullOrEmpty()) {
            Image(
                painter = painterResource(id = company.apptechno.instagramclone.R.drawable.ic_my_posts),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray)
            )
        } else {
            CommonImage(data = userImage)
        }
    }
}

@Composable
fun CommonDivider(){
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)

    )
}

data class NavParam(
    val name : String,
    val value : Parcelable
){}

private enum class LikeIconSize {
    SMALL,
    LARGE

}
@Composable
fun LikeAnimation(like:Boolean = true){
    var sizeState by remember { mutableStateOf(LikeIconSize.SMALL) }
    val transition = updateTransition(targetState = sizeState,label ="")
    val size by transition.animateDp (
        label = "", transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ){ state ->
        when(state){
            LikeIconSize.SMALL -> 0.dp
            LikeIconSize.LARGE -> 150.dp
        }

    }
    Image(painter = painterResource(id = if (like) R.drawable.ic_like else R.drawable.ic_dislike ), contentDescription = null,
          modifier = Modifier.size(size),
          colorFilter = ColorFilter.tint(if (like) Color.Red else Color.Gray)
    )
     sizeState = LikeIconSize.LARGE
}

