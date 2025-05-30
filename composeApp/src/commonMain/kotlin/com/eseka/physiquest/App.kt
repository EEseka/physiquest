package com.eseka.physiquest

import androidx.compose.runtime.Composable
import com.eseka.physiquest.core.navigation.RootNavigation
import com.eseka.physiquest.ui.theme.PhysiQuestTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    PhysiQuestTheme {
        RootNavigation()
    }
}