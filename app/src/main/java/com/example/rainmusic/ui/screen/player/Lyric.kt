package com.example.rainmusic.ui.screen.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rainmusic.data.retrofit.api.model.LyricLine

/**
 * 歌词 Item
 *
 * @param lyricsEntry 歌词 [LyricsEntry]
 * @param current 是否为当前播放
 * @param textSize 字体大小
 * @param textColor 字体颜色
 * @param centerAlign 是否居中对齐
 * @param showSubText 是否显示翻译
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.LyricsItem(
    lyricsEntry: LyricLine,
    current: Boolean = false,
    currentTextElementHeightPxState: MutableState<Int>,
    textSize: Int,
    centerAlign: Boolean = false,
    showSubText: Boolean = true,
    onClick: () -> Unit
) {
//    // 当前歌词，若不显示翻译则只显示主句
//    val mainLyrics = if (showSubText) lyricsEntry.translation else lyricsEntry.lyric
    // 当前正在播放的歌词高亮
    val textAlpha = animateFloatAsState(if (current) 1F else 0.32F, label = "").value
    // 歌词文本对齐方式，可选左 / 中
    val align = if (centerAlign) TextAlign.Center else TextAlign.Left
    val fontWeight=if (current) FontWeight.Bold else LocalTextStyle.current.fontWeight
    Card(

        modifier = Modifier
            .animateItemPlacement()
            .fillMaxWidth()
            .onSizeChanged {
                if (current) {
                    // 告知当前高亮歌词 Item 高度
                    currentTextElementHeightPxState.value = it.height
                }
            }
            .padding(0.dp, (textSize * 0.1F).dp)
        ,
        shape = RoundedCornerShape(8.dp),
        colors =  CardDefaults.cardColors(Color.Transparent),

        elevation =  CardDefaults.cardElevation()
    ) {
        val paddingY = (textSize * 0.3F).dp
        // 这里使用 Column 是为了若以后拓展具体显示
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(8.dp, paddingY),
            verticalArrangement = Arrangement.Center
        ) {
            val mainTextSize = textSize.textDp
            Text(
                modifier = Modifier
                    .alpha(textAlpha)
                    .fillMaxWidth()
                ,
                text = lyricsEntry.lyric,
                fontSize = mainTextSize,
                textAlign = align,
                fontWeight = fontWeight
            )


            if(showSubText && lyricsEntry.translation != null){
                Text(
                    modifier = Modifier
                        .alpha(textAlpha)
                        .fillMaxWidth()
                    ,
                    text = lyricsEntry.translation?:"",
                    fontSize = mainTextSize,
                    textAlign = align,
                    fontWeight = fontWeight
                )
            }

        }
    }
}

