package company.apptechno.instagramclone.data

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
    private set

    fun getContentorNull(): T?{
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

}