package com.example.rainmusic.ui.screen.index.page

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import  androidx.compose.material.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.launch
import com.example.rainmusic.data.model.Playlists
import com.example.rainmusic.data.paging.TopPlaylistPagingSource
import com.example.rainmusic.data.retrofit.api.model.UserPlaylists
import com.example.rainmusic.data.retrofit.weapi.model.PlaylistCategory
import com.example.rainmusic.ui.component.shimmerPlaceholder
import com.example.rainmusic.ui.local.LocalNavController
import com.example.rainmusic.ui.local.LocalUserData
import com.example.rainmusic.ui.screen.Screen
import com.example.rainmusic.ui.screen.index.IndexViewModel
import com.example.rainmusic.ui.states.items
import com.example.rainmusic.util.DataState
import okhttp3.internal.filterList

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiscoverPage(indexViewModel: IndexViewModel) {
    val userData = LocalUserData.current
    var currentTab by remember { mutableIntStateOf(0) }
    val playlists by indexViewModel.userPlaylist.collectAsState()
    LaunchedEffect(userData) {

        if (playlists !is DataState.Success) {
            indexViewModel.refreshLibraryPage(userData.id)
            Log.d("LibraryPage", "LibraryPage")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(
                state = rememberPullRefreshState(playlists is DataState.Loading, onRefresh = {
                    indexViewModel.refreshLibraryPage(userData.id)
                })
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item{
                Profile()
            }

            item {
                Row(modifier = Modifier.padding(start = 8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 8.dp,
                        onClick = {
                            currentTab = 0
                        }
                    ) {
                        Text(text = "我创建的歌单", modifier = Modifier.padding(16.dp))
                    }

                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 8.dp,
                        onClick = {
                            currentTab = 1
                        }
                    ) {
                        Text(text = "我收藏的歌单", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            playlists.readSafely()?.playlist?.let{
                when (currentTab) {
                    0 -> itemsIndexed(it.filterList { creator.userId == userData.id }){i,v->
                            key (v.id){
                                PlayListItem(playlist = v)
                            }
                        }


                    1 -> itemsIndexed(it.filterList { creator.userId != userData.id }){i,v->
                        key (v.id){
                            PlayListItem(playlist = v)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun Profile() {
    val accountData = LocalUserData.current
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        if (!accountData.isVisitor) {
            Log.d("Profile", accountData.avatarUrl)
            AsyncImage(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .size(48.dp),
                model = ImageRequest.Builder(context)
                    .data(accountData.avatarUrl)
                    .build(),
                contentDescription = "avatar"
            )
        } else {
            Image(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp)),
                imageVector = Icons.Rounded.AccountCircle, contentDescription = null
            )
        }

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(text = "欢迎,")
            var nickname = "用户名"
            if (!accountData.isVisitor) nickname = accountData.nickname
            Text(text = nickname, style = MaterialTheme.typography.titleLarge)
        }

    }

}

@Composable
fun Playlists(items: List<UserPlaylists.Playlist>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),

    ) {
        items(items) {
            PlayListItem(it)
        }
    }
}

@Composable
private fun PlayListItem(playlist: UserPlaylists.Playlist) {
    val navController = LocalNavController.current
    val context= LocalContext.current
    val name by remember { mutableStateOf(playlist.name) }
    val info by remember { mutableStateOf("${playlist.trackCount} 首音乐 ${playlist.playCount} 次播放") }
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable {
                Screen.Playlist.navigate(navController) {
                    addPath(playlist.id.toString())
                }
            },
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(playlist.coverImgUrl)
                    .size(Size.ORIGINAL) // 根据需要调整大小
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(10))
                    .aspectRatio(1f)
                    .heightIn(min = 100.dp)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = info,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }

            IconButton(onClick = {
                // TODO: Playlist Actions
            }) {
                Icon(Icons.Rounded.Menu, null)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditor(
    categoryAll: DataState<PlaylistCategory>,
    selectedCategory: List<String>,
    onSave: (List<String>) -> Unit
) {
    var category by remember(selectedCategory) {
        mutableStateOf(selectedCategory)
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "自定义歌单类型",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = {
                    // 保存前重排序一遍
                    val list =
                        categoryAll.read().sub.filter { category.contains(it.name) }.map { it.name }
                            .toList()
                    onSave(list)
                }
            ) {
                Text(text = "保存")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            categoryAll.readSafely()?.categories?.entries?.forEach { (k, v) ->
                item {
                    Text(text = v)
                }

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryAll.read().sub.filter { it.category == k.toInt() }.forEach { sub ->
                            if (category.contains(sub.name)) {
                                OutlinedButton(onClick = {
                                    category = ArrayList(
                                        category.toMutableList().apply { remove(sub.name) })
                                }) {
                                    Text(text = sub.name)
                                }
                            } else {
                                TextButton(onClick = {
                                    category =
                                        ArrayList(category.toMutableList().apply { add(sub.name) })
                                }) {
                                    Text(text = sub.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun TopPlaylist(
    indexViewModel: IndexViewModel,
    category: String
) {
    if (category == "精品") {
        val highQualityPlaylist by indexViewModel.highQualityPlaylist.collectAsState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(110.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (highQualityPlaylist is DataState.Success) {
                items(highQualityPlaylist.read().playlists) { playlist ->
                    PlaylistItem(playlist)
                }
            }
        }
        return
    }

    val items = (indexViewModel.playlistCatPager[category] ?: run {
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 3
            ),
            pagingSourceFactory = {
                TopPlaylistPagingSource(
                    category = category,
                    musicRepo = indexViewModel.musicRepo
                )
            }
        ).flow.cachedIn(indexViewModel.viewModelScope).also {
            indexViewModel.playlistCatPager[category] = it
        }
    }).collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
    ) {
        items(items) { playlist ->
            PlaylistItem(playlist!!)
        }
    }
}

@Composable
private fun PlaylistItem(playlist: Playlists) {
    val navController = LocalNavController.current
    Column(
        modifier = Modifier
            .clickable {
                Screen.Playlist.navigate(navController) {
                    addPath(playlist.id.toString())
                }
            }
            .padding(8.dp)
            .width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val painter = rememberAsyncImagePainter(model = playlist.coverImgUrl)
        Image(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .shimmerPlaceholder(painter)
                .size(100.dp),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2
        )
    }
}