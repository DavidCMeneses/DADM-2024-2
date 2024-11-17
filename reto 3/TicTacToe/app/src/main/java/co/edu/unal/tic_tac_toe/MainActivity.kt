package co.edu.unal.tic_tac_toe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(Array(3) { Array(3) { "" } }) }
    var turn by remember { mutableStateOf("X") }
    var userWins by remember { mutableStateOf(0) }
    var draws by remember { mutableStateOf(0) }
    var machineWins by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("Tu turno") }

    fun checkWinner(): String? {
        // Verificar filas, columnas y diagonales
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != "") return board[i][0]
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != "") return board[0][i]
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != "") return board[0][0]
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != "") return board[0][2]
        return if (board.all { row -> row.all { it != "" } }) "Draw" else null
    }

    fun resetGame() {
        board = Array(3) { Array(3) { "" } }
        turn = "X"
        statusMessage = "Tu turno"
    }

    // Función para hacer el movimiento de la máquina
    fun machineMove() {
        // Primero intentamos ganar
        var moveMade = false
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    board[i][j] = "O"
                    if (checkWinner() == "O") {
                        moveMade = true
                        return
                    } else {
                        board[i][j] = ""
                    }
                }
            }
        }

        // Luego intentamos bloquear al jugador
        if (!moveMade) {
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == "") {
                        board[i][j] = "X"
                        if (checkWinner() == "X") {
                            board[i][j] = "O"
                            moveMade = true
                            return
                        } else {
                            board[i][j] = ""
                        }
                    }
                }
            }
        }

        // Si no hubo movimiento, hacemos un movimiento adyacente o aleatorio
        if (!moveMade) {
            val emptyCells = mutableListOf<Pair<Int, Int>>()
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == "") emptyCells.add(i to j)
                }
            }

            // Intentar mover cerca de un "O"
            var adjacents = mutableListOf<Pair<Int, Int>>()
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == "O") {
                        // Agregar celdas adyacentes
                        if (i > 0 && board[i - 1][j] == "") adjacents.add(Pair(i - 1, j))
                        if (i < 2 && board[i + 1][j] == "") adjacents.add(Pair(i + 1, j))
                        if (j > 0 && board[i][j - 1] == "") adjacents.add(Pair(i, j - 1))
                        if (j < 2 && board[i][j + 1] == "") adjacents.add(Pair(i, j + 1))
                    }
                }
            }

            // Si no encontramos celdas adyacentes, hacer un movimiento aleatorio
            if (adjacents.isNotEmpty()) {
                val (i, j) = adjacents.random()
                board[i][j] = "O"
            } else if (emptyCells.isNotEmpty()) {
                val (i, j) = emptyCells.random()
                board[i][j] = "O"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Marcador
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Usuario: $userWins", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Empates: $draws", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Máquina: $machineWins", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tablero
        for (i in 0..2) {
            Row {
                for (j in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.LightGray, CircleShape)
                            .clickable(enabled = board[i][j] == "" && turn == "X") {
                                board[i][j] = "X"
                                val winner = checkWinner()
                                if (winner != null) {
                                    when (winner) {
                                        "X" -> {
                                            userWins++
                                            statusMessage = "¡Ganaste!"
                                        }
                                        "O" -> {
                                            machineWins++
                                            statusMessage = "La máquina ganó"
                                        }
                                        "Draw" -> {
                                            draws++
                                            statusMessage = "¡Empate!"
                                        }
                                    }
                                } else {
                                    turn = "O"
                                    machineMove()
                                    val machineWinner = checkWinner()
                                    if (machineWinner != null) {
                                        when (machineWinner) {
                                            "X" -> {
                                                userWins++
                                                statusMessage = "¡Ganaste!"
                                            }
                                            "O" -> {
                                                machineWins++
                                                statusMessage = "La máquina ganó"
                                            }
                                            "Draw" -> {
                                                draws++
                                                statusMessage = "¡Empate!"
                                            }
                                        }
                                    } else {
                                        turn = "X"
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = board[i][j],
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (board[i][j] == "X") Color.Blue else Color.Red
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de estado
        Text(statusMessage, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de reinicio
        Button(onClick = { resetGame() }) {
            Text("Intentar de nuevo")
        }
    }
}
