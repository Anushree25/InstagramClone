package company.apptechno.instagramclone.Main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import company.apptechno.instagramclone.DestinationScreen
import company.apptechno.instagramclone.MainViewModel
import company.apptechno.instagramclone.data.PostDetails

@Composable
fun SinglePostsScreen(navController: NavController, vm :MainViewModel, postDetails : PostDetails){
    val comments = vm.commentList.value
    LaunchedEffect(key1 = Unit) {
        postDetails.postId?.let { vm.getComments(it) }
    }
    postDetails.userId?.let {
        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(8.dp)) {

            Text(text = "Back", modifier = Modifier.clickable() { navController.popBackStack() })

            CommonDivider()

            SinglePostDisplay(
                navController = navController,
                vm = vm,
                post = postDetails,
                nbComments = comments.size
            )

        }



    }

}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: MainViewModel,
    post: PostDetails,
    nbComments: Int
) {
    val userData = vm.userData.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Card(
                shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Image(
                    painter = rememberImagePainter(data = post.userImage),
                    contentDescription = null
                )
            }

            Text(text = post.username ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))

            if (userData?.userId == post.userId) {
                // Current user's post. Don't show anything
            } else if (userData?.following?.contains(post.userId) == true) {
                Text(
                    text = "Following",
                    color = androidx.compose.ui.graphics.Color.Gray,
                    modifier = Modifier.clickable { vm.onFollowClick(post.userId!!) })
            } else {
                Text(
                    text = "Follow",
                    color = androidx.compose.ui.graphics.Color.Blue,
                    modifier = Modifier.clickable { vm.onFollowClick(post.userId!!) })

            }
        }
    }

    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
        CommonImage(
            data = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }

    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = company.apptechno.instagramclone.R.drawable.ic_like),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.Red)
        )
        Text(text = " ${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "$nbComments comments",
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable {
                    post.postId?.let {
                        navController.navigate(DestinationScreen.CommentsScreen.createRoute(it))
                    }
                })
    }
}
