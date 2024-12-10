package com.cognitivedevelopment.snake

import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.cognitivedevelopment.snake.ui.theme.DarkGreen
import com.cognitivedevelopment.snake.ui.theme.Shapes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val game = Game(lifecycleScope)

        setContent {
            Box(
                modifier = with (Modifier){
                    fillMaxSize()
                        .paint(
                            // Replace with your image id
                            painterResource(id = R.drawable.snake),
                            contentScale = ContentScale.FillBounds)

                })
            Snake(game)


        }
    }
}

data class State(val food: Pair<Int, Int>, val snake: List<Pair<Int, Int>>,)
data class Sign (val x:Int, val y:Int)


    val locationModels: List<Sign>
        get() = listOf(
            Sign(3,6),
            Sign(3,7),
            Sign(3,8),
            Sign(3,9),
            Sign(3,10),
            Sign(4,6),
            Sign(4,7),
            Sign(4,8),
            Sign(4,9),
            Sign(4,5),
            Sign(5,6),
            Sign(5,7),
            Sign(5,8),
            Sign(5,4),
            Sign(5,5),

            Sign(7,3),
            Sign(7,4),
            Sign(7,5),
            Sign(7,6),
            Sign(7,7),
            Sign(7,8),
            Sign(8,2),
            Sign(8,3),
            Sign(8,4),
            Sign(8,5),
            Sign(8,6),
            Sign(8,7),
            Sign(8,8),
            Sign(9,3),
            Sign(9,4),
            Sign(9,5),
            Sign(9,6),
            Sign(9,7),
            Sign(9,8),

            Sign(13,6),
            Sign(13,7),
            Sign(13,8),
            Sign(13,9),
            Sign(13,10),
            Sign(12,6),
            Sign(12,7),
            Sign(12,8),
            Sign(12,9),
            Sign(12,5),
            Sign(11,6),
            Sign(11,7),
            Sign(11,8),
            Sign(11,4),
            Sign(11,5),

            Sign(6,12),
            Sign(7,12),
            Sign(7,13),
            Sign(8,12),
            Sign(8,13),
            Sign(8,14),
            Sign(9,12),
            Sign(9,13),
            Sign(10,12),
            Sign(6,10),
            Sign(8,10),
            Sign(10,10),

        )


class Game(private val scope: CoroutineScope) {

    private val mutex = Mutex()
    private val mutableState =
        MutableStateFlow(State(food = Pair(5, 5), snake = listOf(Pair(7, 7))))
    val state: Flow<State> = mutableState

    var move = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

    init {
        scope.launch {
            var snakeLength = 4

            while (true) {
                delay(150)
                mutableState.update {
                    val newPosition = it.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }
                    }

                    if (newPosition == it.food) {
                        snakeLength++
                    }

                    if (it.snake.contains(newPosition)) {
                        snakeLength = 4
                    }

                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random().nextInt(BOARD_SIZE),
                            Random().nextInt(BOARD_SIZE)
                        ) else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLength - 1)
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 17
    }
}


@Composable
fun Snake(game: Game) {
    val state = game.state.collectAsState(initial = null)


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        state.value?.let {
            Board(it, locationModels)
        }
        Buttons {
            game.move = it
        }
    }

}

@Composable
fun Buttons(onDirectionChange: (Pair<Int, Int>) -> Unit) {
    val buttonSize = Modifier.size(64.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Button(onClick = { onDirectionChange(Pair(0, -1)) }, modifier = buttonSize) {
            Icon(painterResource(R.drawable.`var`), null)
        }
        Row {
            Button(onClick = { onDirectionChange(Pair(-1, 0)) }, modifier = buttonSize) {
                Icon(painterResource(R.drawable.`var`), null, modifier = Modifier.rotate(270f))
            }
            Spacer(modifier = buttonSize)
            Button(onClick = { onDirectionChange(Pair(1, 0)) }, modifier = buttonSize) {
                Icon(painterResource(R.drawable.`var`), null, modifier = Modifier.rotate(90f))
            }
        }
        Button(onClick = { onDirectionChange(Pair(0, 1)) }, modifier = buttonSize) {
            Icon(painterResource(R.drawable.`var`), null, modifier = Modifier.rotate(180f))
        }
    }
}

@Composable
fun Board(state: State,sign: List<Sign>) {
    BoxWithConstraints(Modifier.padding(16.dp)) {
        val tileSize = maxWidth / Game.BOARD_SIZE

        Box(
            Modifier
                .size(maxWidth)
                .border(2.dp, DarkGreen)
        )
        sign.forEach {
            Box(
                Modifier
                    .offset(x = tileSize*it.x, y = tileSize*it.y)
                    .size(tileSize)
                    .background(
                        Color.Red, Shapes.small
                    )
            )
        }

        Box(
            Modifier
                .offset(x = tileSize * state.food.first, y = tileSize * state.food.second)
                .size(tileSize)
                .background(
                    DarkGreen, CircleShape
                )
        )
        sign.forEach {
            Box(
                Modifier
                    .offset(x = tileSize*it.x, y = tileSize*it.y)
                    .size(tileSize)
                    .background(
                        Color.Red, Shapes.small
                    )
            )
        }



        state.snake.forEach {
            Box(
                modifier = Modifier
                    .offset(x = tileSize * it.first, y = tileSize * it.second)
                    .size(tileSize)
                    .background(
                        DarkGreen, Shapes.small
                    )
            )
        }
    }
}