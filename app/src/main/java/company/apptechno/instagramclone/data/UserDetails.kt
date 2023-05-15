package company.apptechno.instagramclone.data

data class UserDetails(
    var userId :String?= null,
    var name : String?= null,
    var username : String?= null,
    var imageurl : String ?= null,
    var bio : String?= null,
    var following : List<String>?= null) {

    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "username" to username,
        "imageUrl" to imageurl,
        "bio" to bio,
        "following" to following
    )


}