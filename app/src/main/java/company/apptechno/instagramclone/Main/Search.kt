package company.apptechno.instagramclone.Main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import company.apptechno.instagramclone.DestinationScreen
import company.apptechno.instagramclone.MainViewModel

@Composable
fun SearchScreen(navController: NavController, vm: MainViewModel) {
    val searchLoading = vm.searchedPostsProgress.value
    val serachedPosts = vm.searchedPosts.value
    var searchTerm by rememberSaveable { mutableStateOf("") }

    Column() {
        SearchBar(
            searchTerm = searchTerm,
            onSearchChange = { searchTerm = it },
            onSearch = { vm.searchPosts(searchTerm) }
        )
        PostList(
            isContextLoading = false,
            postsLoading = searchLoading,
            posts = serachedPosts,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) { post ->
            navigateTo(
                navController = navController,
                destinationScreen = DestinationScreen.SinglePostsScreen,
                NavParam("post", post)
            )
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController,
        )
    }


}


@Composable
fun SearchBar(searchTerm: String,onSearchChange: (String) -> Unit, onSearch: () -> Unit){

    val focusManager = LocalFocusManager.current

    TextField(value = searchTerm, onValueChange = onSearchChange, modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .border(1.dp, Color.LightGray, CircleShape),
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
       keyboardActions = KeyboardActions(
           onSearch = {
               onSearch()
               focusManager.clearFocus()
           }
       ),
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            textColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(onClick = {
                onSearch()
                focusManager.clearFocus()
            }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }

    )

}
