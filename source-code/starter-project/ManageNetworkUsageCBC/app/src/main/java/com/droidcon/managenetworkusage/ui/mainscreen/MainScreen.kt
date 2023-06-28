package com.droidcon.managenetworkusage.ui.mainscreen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droidcon.managenetworkusage.SettingsActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainScreenViewModel){

    val context = LocalContext.current

    // open the settings page
    val openSettingsButton = {
        val intent= Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }

    val homeScreenActions = remember { HomeScreenActions(onRefreshButtonClicked =viewModel::refresh,
        onSettingsButtonClicked =openSettingsButton)}

    val mainScreenState by viewModel.mainScreenState.collectAsState()

    LaunchedEffect(key1 = Unit, block = {
        viewModel.getJokeOfTheDay()
    })
    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = { MainScreenAppBar(homeScreenActions = homeScreenActions) }) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)){
                when(mainScreenState){
                    is Error -> ErrorPage(networkError = (mainScreenState as Error).errorString)
                    Loading -> CircularProgressIndicator(modifier = Modifier
                        .size(40.dp)
                        .align(
                            Alignment.Center
                        ))
                    is MainScreenData -> FeedPage(data = mainScreenState as MainScreenData)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAppBar(modifier: Modifier = Modifier,homeScreenActions: HomeScreenActions){
    TopAppBar(modifier = modifier,
        title = { Text(text = "Managy")}, actions = {
            Icon(imageVector = Icons.Filled.Refresh,
                modifier = Modifier.clickable {homeScreenActions.onRefreshButtonClicked()},
                contentDescription = "refresh feed")
            Spacer(modifier = Modifier.width(20.dp))
            Icon(imageVector = Icons.Filled.Settings,
                modifier = Modifier.clickable { homeScreenActions.onSettingsButtonClicked() },
                contentDescription = "navigate to settings page")
        })
}
@Composable
fun ErrorPage(modifier:Modifier = Modifier, networkError: String){
    val errorMessageToBeDisplayed by remember {
        mutableStateOf(networkError)
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
        Text(text = errorMessageToBeDisplayed,modifier=Modifier.padding(horizontal = 10.dp),
            textAlign = TextAlign.Justify, style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable
fun FeedPage(data: MainScreenData, modifier: Modifier = Modifier){
    val setUp by remember {
        mutableStateOf(data.joke?.setup)
    }
    val punchline by remember {
        mutableStateOf(data.joke?.punchline)
    }
    var expanded by remember {
        mutableStateOf(false)
    }
    Card(modifier = modifier
        .clickable { expanded = !expanded }
        .fillMaxWidth()
        .heightIn(min = 60.dp, max = 75.dp)
        .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(4.dp)
    ){
        Column(
            Modifier
                .padding(horizontal = 4.dp, vertical = 5.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally){
            Text(text = "$setUp",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,)
            Spacer(Modifier.height(5.dp))
            AnimatedVisibility(visible = expanded,
                enter = slideInVertically { -40} + fadeIn(initialAlpha = 0.3f), exit = slideOutVertically() + fadeOut()
            ) {
                Text(text= "$punchline", style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start)
            }
        }
    }
}
@Composable
@Preview(showSystemUi = true, showBackground = true)
fun FeedPagePreview(){
    var expanded by remember{mutableStateOf(false)}
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Card(modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp, max = 70.dp)
            .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(4.dp)
        ){
            Column(
                Modifier
                    .padding(horizontal = 4.dp, vertical = 5.dp)
                    .clickable { expanded = !expanded },
                horizontalAlignment = Alignment.CenterHorizontally){
                Text(text = "What did the late tomato say to the early tomato?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,)
                Spacer(Modifier.height(10.dp))
                AnimatedVisibility(visible = expanded,
                    enter = slideInVertically { -40} + fadeIn(initialAlpha = 0.3f), exit = slideOutVertically() + fadeOut()
                ) {
                    Text(text="I'll ketchup!", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}