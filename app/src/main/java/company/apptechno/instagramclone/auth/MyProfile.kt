package company.apptechno.instagramclone.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import company.apptechno.instagramclone.DestinationScreen
import company.apptechno.instagramclone.Main.CommonDivider
import company.apptechno.instagramclone.Main.CommonImage
import company.apptechno.instagramclone.Main.CommonProgressSpinner
import company.apptechno.instagramclone.Main.navigateTo
import company.apptechno.instagramclone.MainViewModel

@Composable
fun MyProfileScreen(navController: NavController, vm: MainViewModel){

    val isLoading = vm.inProgress.value
    if (isLoading)
        CommonProgressSpinner()
    else
    {
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username?:"") }
        var bio by rememberSaveable { mutableStateOf(userData?.bio?:"") }
        ProfileContent(
            vm = vm,
            name = name,
            username = username,
            bio = bio,
            onNameChange = { name = it },
            onUsernameChange = { username = it },
            onBioChange = { bio = it },
            onSave = { vm.updateProfileData(name, username, bio) },
            onBack = { navigateTo(navController = navController, DestinationScreen.MyPosts) },
            onLogOut = {
                vm.onLogOut()
                navigateTo(navController,DestinationScreen.Login)
            }
        )
    }

}



@Composable
fun ProfileContent(vm : MainViewModel,name : String, username :String, bio :String,
                    onNameChange: (String) -> Unit,
                    onUsernameChange : (String)-> Unit,
                    onBioChange : (String)-> Unit,
                    onSave:()-> Unit,
                    onBack:() -> Unit,
                    onLogOut :() -> Unit){

    val userData = vm.userData.value
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = "Back", modifier = Modifier.clickable{ onBack.invoke()})
            Text(text = "Save", modifier = Modifier.clickable{ onSave.invoke()})

        }
        CommonDivider()

        if(userData?.imageurl == null){
            userData?.imageurl = "https://www.istockphoto.com/photo/sick-asian-woman-sitting-on-sofa-in-living-room-at-home-and-talking-with-doctor-or-gm1409899470-460287749?utm_source=pixabay&utm_medium=affiliate&utm_campaign=SRP_image_sponsored&utm_content=http%3A%2F%2Fpixabay.com%2Fimages%2Fsearch%2Fuser%2F&utm_term=user"
        }

        ProfileImage(imageUrl = userData?.imageurl!!, vm = vm)
        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          
        ) {
           androidx.compose.material.Text(text = "Name", modifier = Modifier.width(100.dp))
           TextField(value = name, onValueChange = onNameChange,
           colors = TextFieldDefaults.textFieldColors(
               backgroundColor = Color.Transparent,
               textColor = Color.Black
           ))
            
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            androidx.compose.material.Text(text = "Username", modifier = Modifier.width(100.dp))
            TextField(value = username, onValueChange = onUsernameChange,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            androidx.compose.material.Text(text = "Bio", modifier = Modifier.width(100.dp))
            TextField(value = bio, onValueChange = onBioChange,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
            singleLine = false, modifier = Modifier.height(150.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Logout", modifier = Modifier.clickable {
//                onLogout.invoke() })
            })

        }

    }


}
@Composable
fun ProfileImage(imageUrl :String,vm :MainViewModel){

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()){
        it?.let {
            vm.uploadProfileImage(it)
        }
    }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { launcher.launch("image/*") },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl)
            }
            Text(text = "Change profile picture")
        }
    }

}
