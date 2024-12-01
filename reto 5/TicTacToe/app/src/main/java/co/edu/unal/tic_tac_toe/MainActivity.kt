package co.edu.unal.tic_tac_toe


import androidx.compose.foundation.Image
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import co.edu.unal.tic_tac_toe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                TicTacToeGame()
            }
        }
    }
}

fun playSound(context: Context, resourceId: Int) {
    val mediaPlayer = MediaPlayer.create(context, resourceId)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener {
        it.release() // Liberar recursos cuando el sonido termina
    }
}


@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(Array(3) { Array(3) { "" } }) }
    var turn by remember { mutableStateOf("X") }
    var userWins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }
    var machineWins by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf("Tu turno") }
    var difficulty by remember { mutableStateOf("Fácil") }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Reiniciar el juego
    fun resetGame() {
        board = Array(3) { Array(3) { "" } }
        turn = "X"
        statusMessage = "Tu turno"
    }

    // Verificar si hay un ganador
    fun checkWinner(): String? {
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != "") return board[i][0]
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != "") return board[0][i]
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != "") return board[0][0]
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != "") return board[0][2]
        return if (board.all { row -> row.all { it != "" } }) "Draw" else null
    }

    // Función auxiliar para buscar el mejor movimiento (ganador o bloqueador)
    fun findBestMove(player: String): Pair<Int, Int>? {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    board[i][j] = player
                    if (checkWinner() == player) {
                        board[i][j] = "" // Revertir el movimiento temporal
                        return i to j
                    }
                    board[i][j] = "" // Revertir el movimiento temporal
                }
            }
        }
        return null
    }


    // Lógica para que la máquina haga su movimiento
    fun machineMove() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") emptyCells.add(i to j)
            }
        }

        when (difficulty) {
            "Fácil" -> {
                // Mover aleatoriamente
                if (emptyCells.isNotEmpty()) {
                    val (i, j) = emptyCells.random()
                    board[i][j] = "O"
                }
            }
            "Difícil" -> {
                // Priorizar ganar o bloquear al jugador
                for (i in 0..2) {
                    for (j in 0..2) {
                        if (board[i][j] == "") {
                            // Intentar ganar
                            board[i][j] = "O"
                            if (checkWinner() == "O") return
                            board[i][j] = ""

                            // Bloquear al jugador si está cerca de ganar
                            board[i][j] = "X"
                            if (checkWinner() == "X") {
                                board[i][j] = "O"
                                return
                            }
                            board[i][j] = ""
                        }
                    }
                }

                // Jugar el movimiento más central o estratégico si está disponible
                if (board[1][1] == "") {
                    board[1][1] = "O"
                    return
                }

                // Elegir una celda aleatoria si no hay opciones ganadoras o bloqueos
                if (emptyCells.isNotEmpty()) {
                    val (i, j) = emptyCells.random()
                    board[i][j] = "O"
                }
            }
            "Experto" -> {
                // Realizar movimientos perfectos
                val winningMove = findBestMove("O") // Intentar ganar
                if (winningMove != null) {
                    board[winningMove.first][winningMove.second] = "O"
                    return
                }

                val blockingMove = findBestMove("X") // Bloquear al jugador
                if (blockingMove != null) {
                    board[blockingMove.first][blockingMove.second] = "O"
                    return
                }

                // Elegir la mejor posición inicial si está disponible
                val optimalMoves = listOf(1 to 1, 0 to 0, 0 to 2, 2 to 0, 2 to 2) // Centro y esquinas
                for (move in optimalMoves) {
                    if (board[move.first][move.second] == "") {
                        board[move.first][move.second] = "O"
                        return
                    }
                }

                // Si no hay opciones estratégicas, elegir una celda aleatoria
                if (emptyCells.isNotEmpty()) {
                    val (i, j) = emptyCells.random()
                    board[i][j] = "O"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Marcador
        // Marcador
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Usuario: $userWins", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Empates: $draws", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Máquina: $machineWins", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Integrar el CustomView
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            factory = { context ->
                TicTacToeBoardView(context).apply {
                    updateBoard(board)
                    onCellClick = { row, col ->
                        if (board[row][col] == "" && turn == "X") {
                            // El jugador X hace su movimiento
                            board[row][col] = "X"
                            playSound(context, R.raw.user_move)

                            updateBoard(board) // Actualizar tablero después de cada movimiento

                            // Verificar si hay un ganador
                            val winner = checkWinner()
                            if (winner != null && turn != "D") {
                                when (winner) {
                                    "X" -> {statusMessage = "¡Ganaste!"
                                        userWins++
                                        turn = "D"}
                                    "O" -> {
                                        statusMessage = "La máquina ganó."
                                        machineWins++
                                        turn = "D"}
                                    "Draw" -> {
                                        statusMessage = "Empate."
                                        draws++
                                        turn = "D"}
                                }
                            }
                            // Turno de la máquina
                            if (turn != "D") {
                                statusMessage = "Turno de la máquina"
                                turn = "O"
                                Handler(Looper.getMainLooper()).postDelayed(
                                    {
                                        machineMove()
                                        playSound(context, R.raw.cpu_move)
                                        updateBoard(board)

                                        // Verificar si la máquina ganó
                                        val machineWinner = checkWinner()
                                        if (machineWinner != null) {
                                            when (machineWinner) {
                                                "X" -> {
                                                    statusMessage = "¡Ganaste!"
                                                    userWins++
                                                    turn = "D"
                                                }

                                                "O" -> {
                                                    statusMessage = "La máquina ganó."
                                                    machineWins++
                                                    turn = "D"
                                                }

                                                "Draw" -> {
                                                    statusMessage = "Empate."
                                                    draws++
                                                    turn = "D"
                                                }
                                            }
                                        }

                                        if (turn != "D") {
                                            // Volver al turno del jugador
                                            turn = "X"
                                            statusMessage = "Tu turno"
                                        }
                                    }, 1000 // value in milliseconds
                                )
                            }
                        }
                    }
                }
            },
            update = { it.updateBoard(board) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth() // Occupy the full width of the parent
                .wrapContentSize(Alignment.Center) // Wrap content and center it
        ) {
            Text(
                text = statusMessage,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(23.dp))

        // Botones dispuestos verticalmente
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón "Nuevo Juego"
            Button(onClick = { resetGame() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Nuevo Juego")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Juego")
            }

            // Botón "Dificultad"
            Button(onClick = { showDifficultyDialog = true }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Dificultad")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dificultad")
            }

            // Botón "Salir"
            Button(onClick = { showExitConfirmation = true }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salir")
            }

            // Botón "Info"
            Button(onClick = { showInfoDialog = true }) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Info")
            }
        }

        // Diálogo de dificultad
        if (showDifficultyDialog) {
            AlertDialog(
                onDismissRequest = { showDifficultyDialog = false },
                title = { Text("Elija el nivel de dificultad:") },
                text = {
                    Column {
                        TextButton(onClick = {
                            difficulty = "Fácil"
                            showDifficultyDialog = false
                        }) { Text("Fácil") }
                        TextButton(onClick = {
                            difficulty = "Difícil"
                            showDifficultyDialog = false
                        }) { Text("Difícil") }
                        TextButton(onClick = {
                            difficulty = "Experto"
                            showDifficultyDialog = false
                        }) { Text("Experto") }
                    }
                },
                confirmButton = {}
            )
        }

        // Confirmación de salida
        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                title = { Text("¿Seguro que quiere salir?") },
                confirmButton = {
                    TextButton(onClick = { Process.killProcess(Process.myPid()) }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmation = false }) {
                        Text("No")
                    }
                }
            )
        }
        // Diálogo de información
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("Información") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Imagen del logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo del juego",
                            modifier = Modifier.size(100.dp)
                        )
                        // Texto de información
                        Text(
                            text = """
                        Tic-Tac-Toe
                        Creado por David Casallas
                        Elige una de tres dificultades
                        ¡Y no dejes que Android te gane!
                    """.trimIndent(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

