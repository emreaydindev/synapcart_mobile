package com.neilb.synapcart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.chatMarkdown

/** One chat message. User messages are teal and right-aligned; AI answers render markdown. */
@Composable
fun ChatBubble(text: String, isUser: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (isUser) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = SynapColors.OnPrimary,
                modifier = Modifier
                    .widthIn(max = Dimens.BubbleMaxWidth)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp))
                    .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM)
            )
        } else {
            AssistantAvatar()
            Spacer(Modifier.width(Dimens.SpaceS))
            val bold = SpanStyle(fontWeight = FontWeight.Bold, color = SynapColors.Text)
            val code = SpanStyle(fontFamily = FontFamily.Monospace, background = SynapColors.SurfaceHighest)
            val heading = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            val annotated = remember(text) { chatMarkdown(text, bold, code, heading) }
            SelectionContainer {
                Text(
                    annotated,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SynapColors.Text.copy(alpha = 0.92f),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp, 20.dp, 20.dp, 20.dp))
                        .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM)
                )
            }
        }
    }
}

/** Small gradient disc with the cart mark, marking assistant messages. */
@Composable
fun AssistantAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(30.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        BrandMark(width = 18.dp)
    }
}
