package com.example.eudayan.redesign

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.eudayan.R
import org.json.JSONArray
import org.json.JSONObject

fun samplePosts(): SnapshotStateList<FeedPost> = mutableStateListOf(
    FeedPost("I’ve felt overwhelmed with classes lately. Taking things one step at a time.", "Community example", R.drawable.flower_avatar, mutableStateListOf("I hear you. Small steps count, and you deserve rest too.")),
    FeedPost("Starting at a new campus has been hard. What helped you feel at home?", "Community example", R.drawable.flower_avatar, mutableStateListOf("Joining a club helped me meet people with similar interests.")),
    FeedPost("I tried a short walk between classes today. It helped me feel a little more present.", "Community example", R.drawable.flower_avatar, mutableStateListOf("That sounds lovely.", "I’m going to try this tomorrow.", "Small pauses help me too."))
)

fun loadPosts(prefs: SharedPreferences): SnapshotStateList<FeedPost> {
    val raw = prefs.getString("posts_json", null) ?: return samplePosts()
    return runCatching {
        val array = JSONArray(raw)
        mutableStateListOf<FeedPost>().apply {
            repeat(array.length()) { i ->
                val item = array.getJSONObject(i)
                val replies = mutableStateListOf<String>()
                val savedReplies = item.optJSONArray("replies") ?: JSONArray()
                repeat(savedReplies.length()) { j -> replies.add(savedReplies.optString(j)) }
                add(FeedPost(item.getString("text"), item.optString("age"), R.drawable.flower_avatar, replies, item.optBoolean("liked"), item.optString("author", "Anonymous")))
            }
        }
    }.getOrElse { samplePosts() }
}

fun savePosts(prefs: SharedPreferences, posts: List<FeedPost>) {
    val array = JSONArray()
    posts.forEach { post ->
        array.put(JSONObject().apply {
            put("text", post.text); put("age", post.age); put("author", post.author)
            put("liked", post.liked); put("replies", JSONArray(post.replies))
        })
    }
    prefs.edit().putString("posts_json", array.toString()).apply()
}

fun loadChat(prefs: SharedPreferences, name: String): SnapshotStateList<Pair<Boolean, String>> {
    val raw = prefs.getString("chat_json", null) ?: return mutableStateListOf(false to "Hi $name. What’s on your mind today?")
    return runCatching {
        val array = JSONArray(raw)
        mutableStateListOf<Pair<Boolean, String>>().apply {
            repeat(array.length()) { i -> val item = array.getJSONObject(i); add(item.optBoolean("mine") to item.getString("text")) }
        }
    }.getOrElse { mutableStateListOf(false to "Hi $name. What’s on your mind today?") }
}

fun saveChat(prefs: SharedPreferences, messages: List<Pair<Boolean, String>>) {
    val array = JSONArray()
    messages.forEach { (mine, message) -> array.put(JSONObject().put("mine", mine).put("text", message)) }
    prefs.edit().putString("chat_json", array.toString()).apply()
}
