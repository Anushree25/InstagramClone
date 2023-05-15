package company.apptechno.instagramclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import company.apptechno.instagramclone.data.CommentDetails
import company.apptechno.instagramclone.data.Event
import company.apptechno.instagramclone.data.PostDetails
import company.apptechno.instagramclone.data.UserDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(val auth: FirebaseAuth,val db : FirebaseFirestore,val storage: FirebaseStorage) : ViewModel()
{
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserDetails?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val postsUploaded = mutableStateOf<List<PostDetails>>(listOf())

    val searchedPosts = mutableStateOf<List<PostDetails>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)

    val followers = mutableStateOf(0)

    val personalizedFeed = mutableStateOf<List<PostDetails>>(listOf())
    val personalizedFeedProgress = mutableStateOf(false)

    val commentList = mutableStateOf<List<CommentDetails>>(listOf())
    val commentsLoadingProgress = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
            refreshPosts()
        }

    }

    fun onSignup(username: String, email: String, pass:String){
        if (username.isEmpty() or email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection("users").whereEqualTo("username",username).get()
            .addOnSuccessListener { document->
                if(document.size()>0){
                    handleException(customMessage = "User already exists")
                    inProgress.value = false
                }else{
                    auth.createUserWithEmailAndPassword(email.trim(),pass)
                        .addOnSuccessListener {

                            signedIn.value = true
                            createOrUpdateProfile(username = username)
                            inProgress.value = false
                        }.addOnFailureListener {
                            inProgress.value = false
                            handleException(it,customMessage = it.message.toString())

                        }

                  }
            }
            .addOnFailureListener{
                handleException(it,customMessage = it.message.toString())
            }

    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserDetails(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageurl = imageUrl ?: userData.value?.imageurl,
            following = userData.value?.following
        )

        uid?.let { uid ->
            inProgress.value = true
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                                inProgress.value = false
                            }
                    } else {
                        db.collection("users").document(uid).set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create user")
                    inProgress.value = false
                }
        }
    }

    fun getUserData(uid: String){
        inProgress.value = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserDetails>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(uid)
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot retrieve user data")
                inProgress.value = false
            }
    }

    fun onLogin(email: String, pass:String){
        if (email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true

        auth.signInWithEmailAndPassword(email.trim(),pass)
            .addOnSuccessListener {
                signedIn.value = true
                auth.currentUser?.uid?.let { uid ->
                    getUserData(uid)
                }
                inProgress.value = false
            }.addOnFailureListener {
                inProgress.value = false
                handleException(it,customMessage = it.message.toString())
            }
            .addOnFailureListener{
                handleException(it,customMessage = it.message.toString())
            }

    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage"
        popupNotification.value = Event(message)

    }

    fun updateProfileData(name: String, username: String, bio: String) {
        createOrUpdateProfile(name, username, bio)
    }

     fun uploadProfileImage(uri:Uri) {
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString() )
            updatePostImageUserData(imageUrl = it.toString())
        }

    }

    private fun uploadImage(uri:Uri,onSuccess: (Uri) -> Unit){
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { exc ->
                handleException(exc)
                inProgress.value = false
            }

    }

    fun onLogOut(){
        auth.signOut()
        inProgress.value = false
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged Out")

    }

    fun onNewPost(uri: Uri,description :String,onPostSuccess: () -> Unit){
        uploadImage(uri){
            onCreatePost(it, description, onPostSuccess)
        }

    }

    private fun onCreatePost(imageUri: Uri,description :String,onPostSuccess: () -> Unit){
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageurl
        if(currentUid != null){
            val postUuid = UUID.randomUUID().toString()
            val fillerWords = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")
            val searchTerms = description
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }

            val post = PostDetails(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )

            db.collection("posts").document(postUuid).set(post)
                .addOnSuccessListener {
                    popupNotification.value = Event("Post successfully created")
                    inProgress.value = false
                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Unable to create post")
                    inProgress.value = false
                }

        } else {
            handleException(customMessage = "Error: username unavailable. Unable to create post")
            onLogOut()
            inProgress.value = false

        }

    }


    private fun refreshPosts(){
        val currentUid = auth?.currentUser?.uid
        if(currentUid != null){
            refreshPostsProgress.value = true
            db.collection("posts").whereEqualTo("userId",currentUid).get()
                .addOnSuccessListener {
                    convertPost(it,postsUploaded )
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener {
                    handleException(it, "Cannot fetch posts")
                    refreshPostsProgress.value = false
                }

        }else{
            handleException(customMessage = "Error: username unavailable. Unable to refresh posts")
            onLogOut()
        }

    }

    private fun convertPost(documents: QuerySnapshot, outState: MutableState<List<PostDetails>>){
        val newPosts = mutableListOf<PostDetails>()
        documents.forEach { doc ->
            val post = doc.toObject<PostDetails>()
            newPosts.add(post)

        }

       val sortedPosts = newPosts.sortedByDescending { it.time }
       outState.value = sortedPosts

    }


    private fun updatePostImageUserData(imageUrl: String){
        val currentUid = auth?.currentUser?.uid
        if(currentUid != null){
            db.collection("posts").get()
                .addOnSuccessListener {
                      val posts = mutableStateOf<List<PostDetails>>(arrayListOf())
                      convertPost(it,posts)
                      val refs = arrayListOf<DocumentReference>()
                      for(post in posts.value){
                          post.postId?.let{ id->
                              refs.add(db.collection("posts").document(id))

                          }
                      }
                     if(refs.isNotEmpty()){
                         db.runBatch {
                             for (ref in refs){
                                 it.update(ref, "userImage",imageUrl)
                             }
                         } .addOnSuccessListener {
                             refreshPosts()
                         }
                       }

                    }

                .addOnFailureListener{
                    handleException(it, "Cannot update post image")
                    refreshPostsProgress.value = false
                }

        }else{
            handleException(customMessage = "Error: username unavailable. Unable to refresh posts")
            onLogOut()
        }

    }

    fun searchPosts(searchTerm : String){
        if(searchTerm.isNotEmpty()){
            refreshPostsProgress.value = true
            db.collection("posts").whereArrayContains("searchTerms",searchTerm.trim().
                       lowercase()).get()
                .addOnSuccessListener {
                    convertPost(it,searchedPosts)
                    refreshPostsProgress.value = false
                  }
                .addOnFailureListener{
                    handleException(it, "Cannot update post image")
                    refreshPostsProgress.value = false
                }


        }else{
            handleException(customMessage = "Can not search posts")
            refreshPostsProgress.value = false
        }

    }

    fun onFollowClick(userId : String){
        auth.currentUser!!.uid?.let {
            val following = arrayListOf<String>()
            userData.value!!.following.let {
                if (it != null) {
                    following.addAll(it)
                }
            }
            if(following.contains(userId)){
                following.remove(userId)
            }else{
                following.add(userId)
            }

            db.collection("users").document(it).update("following", following)
                .addOnSuccessListener {
                    getUserData(auth.currentUser!!.uid)
                }
        }

    }

    fun getPersonalizedFeed(){
        val following = userData.value!!.following
        if(!following.isNullOrEmpty()){
            personalizedFeedProgress.value = true
            db.collection("posts").whereIn("userId",following).get()
                .addOnSuccessListener {
                    convertPost(documents = it, outState = personalizedFeed)
                    if (personalizedFeed.value.isEmpty()) {
                        generalFeed()
                    } else {
                        personalizedFeedProgress.value = false
                    }
                }
                .addOnFailureListener{
                    handleException(customMessage = "No Any posts are available")
                    personalizedFeedProgress.value = false
                }

        }else{
            generalFeed()
        }


    }

    private fun generalFeed(){
        personalizedFeedProgress.value = false
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 60 * 60 * 1000
        db.collection("posts").whereGreaterThan("time",currentTime - difference).get()
        .addOnSuccessListener {
            convertPost(documents = it, outState = personalizedFeed)
            personalizedFeedProgress.value = false
        }
        .addOnFailureListener {
            handleException(customMessage = "No Any posts are available")
            personalizedFeedProgress.value = false
        }

    }

    fun onLikePost(postDetails: PostDetails) {
        auth.currentUser!!.uid?.let { userId ->
            postDetails.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if(postDetails.likes!!.contains(userId)){
                    newLikes.addAll(likes.filter { userId != it })
                }else{
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }

                postDetails.postId?.let {
                    db.collection("posts").document(postDetails.postId).update("likes",newLikes)
                        .addOnSuccessListener {
                            postDetails.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, "Unable to like post")
                        }

                }
            }

        }
    }

    fun createComments(postId : String, text : String){
        val commentId = UUID.randomUUID().toString()
        userData.value?.username?.let { username ->
        if(postId.isNotEmpty() && text.isNotEmpty()) {
            val comment = CommentDetails(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                timeStamp = System.currentTimeMillis()
                )
            db.collection("comments").document(commentId).set(comment)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                    handleException(customMessage = "Can not create comment")
                }
        }

        }

    }

    fun getComments(postId: String){
        commentsLoadingProgress.value = true
        db.collection("comments").whereEqualTo("postId",postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentDetails>()
                documents.forEach {
                    val comment = it.toObject<CommentDetails>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending {it.timeStamp  }
                commentList.value = sortedComments
                commentsLoadingProgress.value = false
            }
            .addOnFailureListener {
                handleException(customMessage = "Can not get comment")
                commentsLoadingProgress.value = false
            }

    }

    private fun getFollowers(uid: String?){
        db.collection("users").whereArrayContains("following", uid?:"").get()
            .addOnSuccessListener { documents ->
                followers.value = documents.size()
            }
    }

}




