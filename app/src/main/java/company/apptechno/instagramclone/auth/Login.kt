package company.apptechno.instagramclone.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import company.apptechno.instagramclone.DestinationScreen
import company.apptechno.instagramclone.Main.CheckSignedIn
import company.apptechno.instagramclone.Main.navigateTo
import company.apptechno.instagramclone.MainViewModel
import company.apptechno.instagramclone.R

@Composable
fun LogInScreen(navController: NavController, vm: MainViewModel) {
    CheckSignedIn(vm = vm, navController = navController)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passState = remember { mutableStateOf(TextFieldValue()) }

            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(10.dp)
                    .padding(top = 50.dp)

            )
            Text(
                text = "Log In",
                modifier = Modifier.padding(10.dp),
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif
            )

            OutlinedTextField(value = emailState.value,
                onValueChange = {emailState.value = it},
                modifier = Modifier.padding(10.dp),
                label = { Text(text = "Email") })

            OutlinedTextField(value = passState.value,
                onValueChange = {passState.value = it},
                modifier = Modifier.padding(10.dp),
                label = { Text(text = "Password") })

            Button( onClick = {
                vm.onLogin(emailState.value.text,passState.value.text)
            }, modifier = Modifier.padding(10.dp).width(200.dp).height(60.dp)
            ){
                Text(text = "LOG IN")

            }
            Text(text = "Already a user? Go to login ->", color = Color.Blue,
                modifier = Modifier.padding(8.dp).clickable {

                    navigateTo(navController, DestinationScreen.Signup)
                })

        }
    }
}