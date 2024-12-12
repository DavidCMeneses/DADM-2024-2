package co.edu.unal.tic_tac_toe

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.foundation.Image
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import co.edu.unal.tic_tac_toe.ui.theme.TicTacToeTheme


class MainActivity : ComponentActivity() {

    companion object {
        var board = Array(3) { Array(3) { "" } }
        var turn = "X"
        var userWins = 0
        var draws = 0
        var machineWins = 0
        var statusMessage = "Tu turno"
        var difficulty = "Fácil"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ComposeView>(R.id.composeView).setContent {
            TicTacToeTheme {
                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> TicTacToeGameVertical(context = this@MainActivity)
                    Configuration.ORIENTATION_LANDSCAPE -> TicTacToeGameHorizontal(context = this@MainActivity)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)

        findViewById<ComposeView>(R.id.composeView).setContent {
            TicTacToeTheme {
                when (newConfig.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> TicTacToeGameVertical(context = this@MainActivity)
                    Configuration.ORIENTATION_LANDSCAPE -> TicTacToeGameHorizontal(context = this@MainActivity)
                }
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
fun TicTacToeGameVertical(context: Context) {
    var board by remember { mutableStateOf(MainActivity.board) }
    var turn by remember { mutableStateOf(MainActivity.turn) }
    var userWins by remember { mutableStateOf(MainActivity.userWins) }
    var draws by remember { mutableStateOf(MainActivity.draws) }
    var machineWins by remember { mutableStateOf(MainActivity.machineWins) }
    var statusMessage by remember { mutableStateOf(MainActivity.statusMessage) }
    var difficulty by remember { mutableStateOf(MainActivity.difficulty) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Cuando actualices las variables, también actualiza las globales
    // Ejemplo:
    MainActivity.board = board
    MainActivity.turn = turn
    MainActivity.userWins = userWins
    MainActivity.draws = draws
    MainActivity.machineWins = machineWins
    MainActivity.statusMessage = statusMessage
    MainActivity.difficulty = difficulty


    // Guardar el estado del tablero
    fun saveBoardState() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        board.forEachIndexed { i, row ->
            row.forEachIndexed { j, cell ->
                editor.putString("cell_$i$j", cell)
            }
        }
        editor.putString("turn", turn)
        editor.putInt("userWins", userWins)
        editor.putInt("draws", draws)
        editor.putInt("machineWins", machineWins)
        editor.putString("statusMessage", statusMessage)
        editor.putString("difficulty", difficulty)
        editor.apply()
    }

    // Cargar el estado del tablero
    fun loadBoardState() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        board = Array(3) { i ->
            Array(3) { j ->
                sharedPreferences.getString("cell_$i$j", "") ?: ""
            }
        }
        turn = sharedPreferences.getString("turn", "X") ?: "X"
        userWins = sharedPreferences.getInt("userWins", 0)
        draws = sharedPreferences.getInt("draws", 0)
        machineWins = sharedPreferences.getInt("machineWins", 0)
        statusMessage = sharedPreferences.getString("statusMessage", "Tu turno") ?: "Tu turno"
        difficulty = sharedPreferences.getString("difficulty", "Fácil") ?: "Fácil"
    }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Usuario: $userWins", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Empates: $draws", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            // Continuación de la función TicTacToeGame

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
                .fillMaxWidth() // Ocupa todo el ancho
                .wrapContentSize(Alignment.Center) // Ajusta el contenido y lo centra
        ) {
            Text(
                text = statusMessage,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(23.dp))

        // Cambiar Column por LazyColumn en la sección de botones
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(onClick = { resetGame() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Nuevo Juego")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Juego")
                }
            }

            item {
                Button(onClick = { saveBoardState() }) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Guardar Tablero")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Tablero")
                }
            }

            item {
                Button(onClick = { loadBoardState() }) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Cargar Tablero")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cargar Tablero")
                }
            }

            item {
                Button(onClick = { showDifficultyDialog = true }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Dificultad")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dificultad")
                }
            }

            item {
                Button(onClick = { showExitConfirmation = true }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salir")
                }
            }

            item {
                Button(onClick = { showInfoDialog = true }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Info")
                }
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

@Composable
fun TicTacToeGameHorizontal(context: Context) {
    var board by remember { mutableStateOf(MainActivity.board) }
    var turn by remember { mutableStateOf(MainActivity.turn) }
    var userWins by remember { mutableStateOf(MainActivity.userWins) }
    var draws by remember { mutableStateOf(MainActivity.draws) }
    var machineWins by remember { mutableStateOf(MainActivity.machineWins) }
    var statusMessage by remember { mutableStateOf(MainActivity.statusMessage) }
    var difficulty by remember { mutableStateOf(MainActivity.difficulty) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Cuando actualices las variables, también actualiza las globales
    // Ejemplo:
    MainActivity.board = board
    MainActivity.turn = turn
    MainActivity.userWins = userWins
    MainActivity.draws = draws
    MainActivity.machineWins = machineWins
    MainActivity.statusMessage = statusMessage
    MainActivity.difficulty = difficulty


    // Guardar el estado del tablero
    fun saveBoardState() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        board.forEachIndexed { i, row ->
            row.forEachIndexed { j, cell ->
                editor.putString("cell_$i$j", cell)
            }
        }
        editor.putString("turn", turn)
        editor.putInt("userWins", userWins)
        editor.putInt("draws", draws)
        editor.putInt("machineWins", machineWins)
        editor.putString("statusMessage", statusMessage)
        editor.putString("difficulty", difficulty)
        editor.apply()
    }

    // Cargar el estado del tablero
    fun loadBoardState() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        board = Array(3) { i ->
            Array(3) { j ->
                sharedPreferences.getString("cell_$i$j", "") ?: ""
            }
        }
        turn = sharedPreferences.getString("turn", "X") ?: "X"
        userWins = sharedPreferences.getInt("userWins", 0)
        draws = sharedPreferences.getInt("draws", 0)
        machineWins = sharedPreferences.getInt("machineWins", 0)
        statusMessage = sharedPreferences.getString("statusMessage", "Tu turno") ?: "Tu turno"
        difficulty = sharedPreferences.getString("difficulty", "Fácil") ?: "Fácil"
    }

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
                if (emptyCells.isNotEmpty()) {
                    val (i, j) = emptyCells.random()
                    board[i][j] = "O"
                }
            }
            "Difícil" -> {
                for (i in 0..2) {
                    for (j in 0..2) {
                        if (board[i][j] == "") {
                            board[i][j] = "O"
                            if (checkWinner() == "O") return
                            board[i][j] = ""

                            board[i][j] = "X"
                            if (checkWinner() == "X") {
                                board[i][j] = "O"
                                return
                            }
                            board[i][j] = ""
                        }
                    }
                }
                if (board[1][1] == "") {
                    board[1][1] = "O"
                    return
                }
                if (emptyCells.isNotEmpty()) {
                    val (i, j) = emptyCells.random()
                    board[i][j] = "O"
                }
            }
            "Experto" -> {
                val winningMove = findBestMove("O")
                if (winningMove != null) {
                    board[winningMove.first][winningMove.second] = "O"
                    return
                }

                val blockingMove = findBestMove("X")
                if (blockingMove != null) {
                    board[blockingMove.first][blockingMove.second] = "O"
                    return
                }

                val optimalMoves = listOf(1 to 1, 0 to 0, 0 to 2, 2 to 0, 2 to 2)
                for (move in optimalMoves) {
                    if (board[move.first][move.second] == "") {
                        board[move.first][move.second] = "O"
                        return
                    }
                }
                if (emptyCells.isNotEmpty()) {
                    val (i, j) = emptyCells.random()
                    board[i][j] = "O"
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Columna izquierda con el tablero, marcador y anunciador de movimientos
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Tablero de juego
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        TicTacToeBoardView(context).apply {
                            updateBoard(board)
                            onCellClick = { row, col ->
                                if (board[row][col] == "" && turn == "X") {
                                    board[row][col] = "X"
                                    playSound(context, R.raw.user_move)
                                    updateBoard(board)
                                    val winner = checkWinner()
                                    if (winner != null && turn != "D") {
                                        when (winner) {
                                            "X" -> {statusMessage = "¡Ganaste!"; userWins++; turn = "D"}
                                            "O" -> {statusMessage = "La máquina ganó."; machineWins++; turn = "D"}
                                            "Draw" -> {statusMessage = "Empate."; draws++; turn = "D"}
                                        }
                                    }
                                    if (turn != "D") {
                                        statusMessage = "Turno de la máquina"
                                        turn = "O"
                                        Handler(Looper.getMainLooper()).postDelayed(
                                            {
                                                machineMove()
                                                playSound(context, R.raw.cpu_move)
                                                updateBoard(board)
                                                val machineWinner = checkWinner()
                                                if (machineWinner != null) {
                                                    when (machineWinner) {
                                                        "X" -> {statusMessage = "¡Ganaste!"; userWins++; turn = "D"}
                                                        "O" -> {statusMessage = "La máquina ganó."; machineWins++; turn = "D"}
                                                        "Draw" -> {statusMessage = "Empate."; draws++; turn = "D"}
                                                    }
                                                }
                                                if (turn != "D") {
                                                    turn = "X"
                                                    statusMessage = "Tu turno"
                                                }
                                            }, 1000
                                        )
                                    }
                                }
                            }
                        }
                    },
                    update = { it.updateBoard(board) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anunciador de movimientos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = statusMessage,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }

        // Columna derecha con los botones
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(onClick = { resetGame() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Nuevo Juego")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Juego")
                }
            }

            item {
                Button(onClick = { saveBoardState() }) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Guardar Tablero")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Tablero")
                }
            }

            item {
                Button(onClick = { loadBoardState() }) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Cargar Tablero")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cargar Tablero")
                }
            }

            item {
                Button(onClick = { showDifficultyDialog = true }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Dificultad")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dificultad")
                }
            }

            item {
                Button(onClick = { showExitConfirmation = true }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salir")
                }
            }

            item {
                Button(onClick = { showInfoDialog = true }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Info")
                }
            }
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
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo del juego",
                        modifier = Modifier.size(100.dp)
                    )
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
