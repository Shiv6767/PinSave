package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PinItem
import com.example.data.PinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(private val repository: PinRepository) : ViewModel() {

    val historyPins: StateFlow<List<PinItem>> = repository.getHistoryPins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedPins: StateFlow<List<PinItem>> = repository.getDownloadedPins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePins: StateFlow<List<PinItem>> = repository.getActivePins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allFeedPins = MutableStateFlow<List<PinItem>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val feedPins: StateFlow<List<PinItem>> = combine(_allFeedPins, _searchQuery, _selectedFilter) { pins, query, filter ->
        pins.filter {
            val typeMatches = when (filter) {
                "Images" -> it.type == "image"
                "Videos" -> it.type == "video"
                else -> true
            }
            val queryMatches = query.isEmpty() || it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            typeMatches && queryMatches
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _isAccountLinked = MutableStateFlow(false)
    val isAccountLinked: StateFlow<Boolean> = _isAccountLinked.asStateFlow()

    private val _pinterestUsername = MutableStateFlow("")
    val pinterestUsername: StateFlow<String> = _pinterestUsername.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun linkAccount(input: String) {
        var cleanInput = input.trim()
        if (cleanInput.contains("pinterest.com/")) {
            cleanInput = cleanInput
                .substringAfter("pinterest.com/")
                .removeSuffix("/")
        } else if (cleanInput.startsWith("@")) {
            cleanInput = cleanInput.removePrefix("@")
        }
        _pinterestUsername.value = cleanInput.ifBlank { "user123" }
        _isAccountLinked.value = true
        fetchRecommendations()
    }

    fun unlinkAccount() {
        _isAccountLinked.value = false
        _pinterestUsername.value = ""
        _allFeedPins.value = getMockRecommendations()
    }

    fun refreshRecommendations() {
        if (_isAccountLinked.value) {
            fetchRecommendations()
        } else {
            _allFeedPins.value = getMockRecommendations()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    init {
        // Load initial mock feed (as seen in html)
        _allFeedPins.value = getMockRecommendations()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun parseUrl(url: String) {
        val pinRegex = Regex("^(https?://)?(www\\.)?(pinterest\\.com/pin/|pin\\.it/)[a-zA-Z0-9_-]+/?.*$")
        if (!pinRegex.matches(url)) {
            _errorState.value = "Invalid Pinterest URL format."
            return
        }

        _errorState.value = null
        _isLoading.value = true

        viewModelScope.launch {
            // Simulate network delay
            delay(1500)
            
            val isVideo = url.contains("video", ignoreCase = true) || url.length % 2 == 0
            val type = if (isVideo) "video" else "image"
            val domainTitle = if (url.contains("pin.it")) "UI Micro-interactions Showcase" else "Minimalist Workspace Inspiration"
            
            // Assign a high-quality mock URL
            val src = if (isVideo) {
                "https://lh3.googleusercontent.com/aida/AP1WRLsTrOqKbZ5ZLxsOsra4PYiEOLF73TBskwDi0PZBaE5AhxNqa_Ik-NY_pKr1pr-QRwKeZslki-w2UHkyE4sCuJINyqfJVwpOpMuAyPWgmYRMXk_rw2rJJR3_YerLzh2L1MuMWE_w4BXC0QEut08UZ62ZRmGFGsmPZG7O9DLP1jDaEh4Xkc94Iozky--nS-kj81qTbtFVTXtWyGm80lyxjP6jlgfAk0G98ixRFbo7Bcu3IiNMELBICj9KEMU"
            } else {
                "https://picsum.photos/seed/${kotlin.math.abs(url.hashCode())}/600/800"
            }

            // Record in history
            val historyItem = PinItem(
                id = UUID.randomUUID().toString(),
                src = src,
                title = domainTitle,
                description = "Extracted from: $url",
                domain = "pinterest.com",
                type = type,
                status = "history",
                timestamp = System.currentTimeMillis()
            )
            repository.insertPin(historyItem)

            // Start active download simulation automatically
            val activePin = historyItem.copy(id = UUID.randomUUID().toString(), status = "active")
            repository.insertPin(activePin)

            _isLoading.value = false
            
            // Run background simulation to complete the download
            launch {
                delay(2000)
                repository.deletePinById(activePin.id)
                val downloadedPin = activePin.copy(status = "downloaded")
                repository.insertPin(downloadedPin)
            }
        }
    }

    fun downloadPin(pin: PinItem) {
        viewModelScope.launch {
            // Create active download
            val activePin = pin.copy(id = UUID.randomUUID().toString(), status = "active")
            repository.insertPin(activePin)
            
            // Log history
            val historyItem = pin.copy(id = UUID.randomUUID().toString(), status = "history")
            repository.insertPin(historyItem)
            
            delay(2000) // Simulate download time
            
            // Move to downloaded
            repository.deletePinById(activePin.id)
            val downloadedPin = activePin.copy(status = "downloaded")
            repository.insertPin(downloadedPin)
        }
    }

    fun deletePin(id: String) {
        viewModelScope.launch {
            repository.deletePinById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
    
    fun clearDownloaded() {
        viewModelScope.launch {
            repository.clearDownloaded()
        }
    }
    
    fun clearError() {
        _errorState.value = null
    }

    private fun getMockRecommendations(): List<PinItem> {
        return listOf(
            // Images - diverse categories
            PinItem("rec_1", "https://picsum.photos/seed/aesthetic1/600/800", "Minimalist Architecture", "A stark, minimalist architectural interior featuring a smooth concrete staircase.", "archdaily.com", "image", "feed"),
            PinItem("rec_2", "https://picsum.photos/seed/fashion2/600/900", "Autumn Fashion Lookbook", "Cozy layered outfits featuring warm earth tones and textured fabrics.", "vogue.com", "video", "feed"),
            PinItem("rec_3", "https://picsum.photos/seed/tech3/600/750", "Tech Setup Goals", "An ultra-clean desk setup with RGB lighting, ultrawide monitor and mechanical keyboard.", "wired.com", "image", "feed"),
            PinItem("rec_4", "https://picsum.photos/seed/interior4/600/850", "Scandinavian Living Room", "Warm minimalist living room with natural wood tones and soft linen textures.", "dezeen.com", "image", "feed"),
            PinItem("rec_5", "https://picsum.photos/seed/food5/600/600", "Japanese Ramen Bowl", "Steaming tonkotsu ramen with chashu pork, soft-boiled egg and fresh scallions.", "bonappetit.com", "image", "feed"),
            PinItem("rec_6", "https://picsum.photos/seed/travel6/600/900", "Santorini Sunset", "Golden hour over the iconic blue-domed churches of Santorini, Greece.", "natgeo.com", "video", "feed"),
            PinItem("rec_7", "https://picsum.photos/seed/art7/600/800", "Abstract Digital Art", "Vibrant neon abstract art with flowing geometric shapes and gradients.", "behance.net", "image", "feed"),
            PinItem("rec_8", "https://picsum.photos/seed/fitness8/600/750", "Morning Yoga Flow", "A serene yoga pose at sunrise with mountain backdrop and misty valleys.", "self.com", "video", "feed"),
            PinItem("rec_9", "https://picsum.photos/seed/plants9/600/850", "Indoor Jungle Aesthetic", "A gorgeous indoor plant collection with monstera, pothos, and ferns.", "housebeautiful.com", "image", "feed"),
            PinItem("rec_10", "https://picsum.photos/seed/cars10/600/600", "Classic Porsche 911", "A perfectly restored vintage Porsche 911 in Gulf Blue livery.", "topgear.com", "image", "feed"),
            PinItem("rec_11", "https://picsum.photos/seed/coffee11/600/900", "Latte Art Mastery", "Intricate latte art rosetta pattern in a ceramic cup.", "barista.co", "image", "feed"),
            PinItem("rec_12", "https://picsum.photos/seed/space12/600/800", "Nebula Photography", "Stunning deep-space capture of the Orion Nebula with vivid cosmic colors.", "nasa.gov", "video", "feed"),
            PinItem("rec_13", "https://picsum.photos/seed/diy13/600/750", "DIY Macramé Wall Art", "Step-by-step tutorial for creating boho macramé wall hangings.", "etsy.com", "image", "feed"),
            PinItem("rec_14", "https://picsum.photos/seed/pets14/600/850", "Golden Retriever Puppy", "An adorable golden retriever puppy playing in a field of wildflowers.", "akc.org", "image", "feed"),
            PinItem("rec_15", "https://picsum.photos/seed/nature15/600/900", "Northern Lights", "Spectacular aurora borealis dancing across the Icelandic sky.", "natgeo.com", "video", "feed"),
            PinItem("rec_16", "https://picsum.photos/seed/cake16/600/600", "Matcha Layer Cake", "A stunning three-layer matcha cake with white chocolate ganache frosting.", "sallysbakingaddiction.com", "image", "feed"),
            PinItem("rec_17", "https://picsum.photos/seed/sneakers17/600/750", "Sneaker Collection", "Rare limited edition sneakers displayed on a floating shelf setup.", "hypebeast.com", "image", "feed"),
            PinItem("rec_18", "https://picsum.photos/seed/street18/600/800", "Tokyo Street Style", "Vibrant Harajuku fashion with bold colors and layered accessories.", "highsnobiety.com", "video", "feed"),
            PinItem("rec_19", "https://picsum.photos/seed/music19/600/850", "Vinyl Record Aesthetic", "A cozy vinyl record corner with warm lighting and vintage turntable.", "pitchfork.com", "image", "feed"),
            PinItem("rec_20", "https://picsum.photos/seed/sketch20/600/900", "Architectural Sketches", "Beautiful hand-drawn architectural sketches of modern buildings.", "archinect.com", "image", "feed"),
            PinItem("rec_21", "https://picsum.photos/seed/beach21/600/750", "Tropical Beach Vibes", "Crystal clear turquoise waters and pristine white sand beach.", "travelandleisure.com", "image", "feed"),
            PinItem("rec_22", "https://picsum.photos/seed/gaming22/600/600", "Gaming Setup RGB", "Immersive gaming battlestation with triple monitors and ambient lighting.", "pcgamer.com", "video", "feed"),
            PinItem("rec_23", "https://picsum.photos/seed/ceramics23/600/850", "Handmade Ceramics", "Beautiful hand-thrown pottery in organic earthy glazes.", "westelm.com", "image", "feed"),
            PinItem("rec_24", "https://picsum.photos/seed/sunset24/600/800", "Mountain Sunset", "Golden sunset behind jagged mountain peaks with alpine meadow.", "500px.com", "image", "feed")
        )
    }

    private val _accessToken = MutableStateFlow<String?>(null)

    fun setAccessToken(token: String) {
        if (token.startsWith("mock_token_for_")) {
            val username = token.removePrefix("mock_token_for_")
            _pinterestUsername.value = username
            _isAccountLinked.value = true
            _accessToken.value = null // clear token to fallback to public RSS recommendations
            fetchRecommendations()
        } else {
            _accessToken.value = token
            _isAccountLinked.value = true
            fetchRecommendations()
        }
    }

    private fun fetchRecommendations() {
        _isLoading.value = true
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val token = _accessToken.value
                if (token != null) {
                    fetchRealPinterestFeed(token)
                    return@launch
                }
                
                // Fallback to RSS strategies
                val allPins = mutableListOf<PinItem>()

                val client = okhttp3.OkHttpClient.Builder()
                    .followRedirects(true)
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                // STRATEGY 0: Public RSS Feed (No Login Required)
                val username = _pinterestUsername.value
                if (username.isNotEmpty()) {
                    val rssUrl = if (username.contains("/")) {
                        "https://www.pinterest.com/${username}.rss"
                    } else {
                        "https://www.pinterest.com/${username}/feed.rss"
                    }
                    try {
                        val request = okhttp3.Request.Builder()
                            .url(rssUrl)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                            .header("Accept", "application/xml, text/xml, */*")
                            .build()
                            
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val body = response.body?.string() ?: ""
                                val itemRegex = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
                                val titleRegex = Regex("<title>(.*?)</title>")
                                val linkRegex = Regex("<link>(.*?)</link>")
                                val descRegex = Regex("<description>(.*?)</description>", RegexOption.DOT_MATCHES_ALL)
                                val guidRegex = Regex("<guid>(.*?)</guid>")

                                val items = itemRegex.findAll(body)
                                var index = 0
                                for (itemMatch in items) {
                                    val itemContent = itemMatch.groupValues[1]
                                    val title = titleRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                                    val link = linkRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                                    val desc = descRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                                    val guid = guidRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""

                                    val imgUrlRegex = Regex("src=(?:\"|&quot;|')(https://i\\.pinimg\\.com/[^\"'&\\s>]+)(?:\"|&quot;|')")
                                    val imgUrlMatch = imgUrlRegex.find(desc)
                                    val src = imgUrlMatch?.groupValues?.get(1)?.replace("/236x/", "/736x/") ?: ""

                                    if (src.isNotEmpty()) {
                                        allPins.add(
                                            PinItem(
                                                id = "rss_${if (guid.isNotBlank()) guid else UUID.randomUUID().toString()}",
                                                src = src,
                                                title = if (title.isBlank()) "Pinterest Pin" else title,
                                                description = "From @${username.substringBefore("/")}'s board",
                                                domain = "Saved",
                                                type = "image",
                                                status = "feed"
                                            )
                                        )
                                        index++
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // STRATEGY 1: Dynamic Recommendations Scraped from Search
                val rssTopics = mutableListOf<String>()
                if (allPins.isNotEmpty()) {
                    val stopWords = setOf(
                        "and", "the", "for", "with", "from", "your", "that", "this", "these", "those", 
                        "how", "what", "why", "who", "whom", "pinterest", "pins", "saved", "board", 
                        "idea", "ideas", "aesthetic", "minimalist", "design", "illustration", "art", 
                        "photography", "decor", "inspiration", "diy", "tutorial", "style", "fashion"
                    )
                    
                    for (pin in allPins) {
                        val title = pin.title.trim()
                        if (title.isNotEmpty() && !title.equals("Pinterest Pin", ignoreCase = true) && !title.equals("Pin", ignoreCase = true)) {
                            val cleaned = title.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim()
                            val words = cleaned.split("\\s+".toRegex()).filter { it.isNotBlank() }
                            
                            if (words.size in 1..3) {
                                rssTopics.add(cleaned)
                            } else {
                                val filtered = words.filter { it.lowercase() !in stopWords }
                                if (filtered.isNotEmpty()) {
                                    rssTopics.add(filtered.take(3).joinToString(" "))
                                }
                            }
                        }
                    }
                }

                val topicsToSearch = rssTopics.distinct().filter { it.isNotBlank() }.take(4)
                val finalTopics = if (topicsToSearch.isEmpty()) {
                    listOf("aesthetic", "design", "photography", "travel", "food")
                } else {
                    topicsToSearch
                }

                val recommendations = mutableListOf<PinItem>()
                var recIndex = 0

                for (topic in finalTopics) {
                    try {
                        val requestBuilder = okhttp3.Request.Builder()
                            .url("https://www.pinterest.com/search/pins/?q=${java.net.URLEncoder.encode(topic, "UTF-8")}&rs=typed")
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36")
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "en-US,en;q=0.9")

                        client.newCall(requestBuilder.build()).execute().use { response ->
                            if (response.isSuccessful) {
                                val html = response.body?.string() ?: ""
                                val imgRegex = Regex("https://i\\.pinimg\\.com/(?:736x|564x|236x)/[a-f0-9]{2}/[a-f0-9]{2}/[a-f0-9]{2}/[a-f0-9]+\\.jpg")
                                val matches = imgRegex.findAll(html)
                                    .map { it.value.replace("/236x/", "/736x/").replace("/564x/", "/736x/") }
                                    .distinct()
                                    .toList()

                                val topicCap = topic.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                val isPersonalized = topicsToSearch.isNotEmpty()
                                val description = if (isPersonalized) {
                                    "Inspired by your save: \"$topicCap\""
                                } else {
                                    "Trending in $topicCap — curated for you."
                                }

                                matches.take(6).forEachIndexed { index, src ->
                                    recommendations.add(
                                        PinItem(
                                            id = "rec_${topic.hashCode()}_${recIndex++}",
                                            src = src,
                                            title = "$topicCap Idea #${index + 1}",
                                            description = description,
                                            domain = "Recommended",
                                            type = "image",
                                            status = "feed"
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val combined = mutableListOf<PinItem>()
                combined.addAll(allPins)
                combined.addAll(recommendations)

                if (combined.isNotEmpty()) {
                    _allFeedPins.value = combined.shuffled()
                } else {
                    _allFeedPins.value = getMockRecommendations()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _allFeedPins.value = getMockRecommendations()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchRealPinterestFeed(token: String) {
        val client = okhttp3.OkHttpClient()
        val allPins = mutableListOf<PinItem>()

        try {
            // 1. Fetch user's boards
            val boardsRequest = okhttp3.Request.Builder()
                .url("https://api.pinterest.com/v5/boards")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()

            val boardIds = mutableListOf<String>()
            client.newCall(boardsRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    try {
                        val jsonObject = org.json.JSONObject(responseStr)
                        if (jsonObject.has("items")) {
                            val items = jsonObject.getJSONArray("items")
                            for (i in 0 until minOf(5, items.length())) {
                                val item = items.getJSONObject(i)
                                if (item.has("id")) {
                                    boardIds.add(item.getString("id"))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 2. Fetch pins from these boards
            for (boardId in boardIds) {
                val pinsRequest = okhttp3.Request.Builder()
                    .url("https://api.pinterest.com/v5/boards/$boardId/pins")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .build()

                client.newCall(pinsRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseStr = response.body?.string() ?: ""
                        try {
                            val jsonObject = org.json.JSONObject(responseStr)
                            if (jsonObject.has("items")) {
                                val items = jsonObject.getJSONArray("items")
                                for (i in 0 until items.length()) {
                                    val obj = items.getJSONObject(i)
                                    val id = if (obj.has("id")) obj.getString("id") else UUID.randomUUID().toString()
                                    val title = if (obj.has("title")) obj.getString("title") else ""
                                    val desc = if (obj.has("description")) obj.getString("description") else ""
                                    var src = ""
                                    
                                    if (obj.has("media")) {
                                        val media = obj.getJSONObject("media")
                                        if (media.has("images")) {
                                            val images = media.getJSONObject("images")
                                            val highestResObj = if (images.has("1200x")) images.getJSONObject("1200x")
                                                              else if (images.has("600x")) images.getJSONObject("600x")
                                                              else if (images.has("400x")) images.getJSONObject("400x")
                                                              else null
                                            
                                            if (highestResObj != null && highestResObj.has("url")) {
                                                src = highestResObj.getString("url")
                                            }
                                        }
                                    }

                                    if (src.isNotEmpty()) {
                                        allPins.add(
                                            PinItem(
                                                id = "auth_$id",
                                                src = src,
                                                title = title,
                                                description = desc,
                                                domain = "My Board",
                                                type = "image",
                                                status = "feed"
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            if (allPins.isNotEmpty()) {
                _allFeedPins.value = allPins.shuffled()
            } else {
                _allFeedPins.value = getMockRecommendations()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _allFeedPins.value = getMockRecommendations()
        } finally {
            _isLoading.value = false
        }
    }
}
