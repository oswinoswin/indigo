package indigo

class Deck {
    private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    private val suits = listOf("♦", "♥", "♠", "♣")
    private var deckBase :List<String>
    private var deck = mutableListOf<String>()
    init {
        for(suit in suits){
            for(rank in ranks){
                deck.add(rank+suit)
            }
        }
        deckBase = deck.toList()
    }
    fun reset(){
        deck = deckBase.toMutableList()
    }

    fun shuffle(){
        deck.shuffle()
    }

    fun get(amount: Int): MutableList<String> {
        if (amount == null || amount < 1 || amount > 52) {
//            println("Invalid number of cards.")
            throw Exception("Invalid number of cards")
        }
        if ( amount > deck.size) {
            throw Exception("The remaining cards are insufficient to meet the request.")
        }
        val hand = mutableListOf<String>()
        repeat(amount){
            hand.add(deck.removeFirst())
        }
//        println(hand.joinToString(" "))
        return hand
    }

    fun isEmpty(): Boolean {
        return deck.isEmpty()
    }

    fun cardsLeft(): Int {
        return deck.size
    }
}

open class Player {
    val hand: MutableList<String>
    val cardsWon = mutableListOf<String>()
    constructor(cards: MutableList<String>){
        this.hand = cards
    }

    open fun turn(){

    }

    fun printHand(){
        print("Cards in hand: ")
        hand.forEachIndexed { index, s ->  print("${index + 1})$s ") }
        print("\n")
    }
}

class Computer(cards: MutableList<String>) : Player(cards) {
    override fun turn(){
        println("Computer plays")
    }
}


class Game {
    val deck = Deck()
    val table = mutableListOf<String>()
    val player :Player
    val computer :Player
    var continueGame = true

    init {
        deck.shuffle()
        table.addAll(deck.get(4))
        computer = Computer(deck.get(6))
        player = Player(deck.get(6))
    }

    private fun topCard(): String {
        return table.last()
    }

    private fun cardsOnTable(): Int {
        return table.size
    }

    fun gameShouldEnd(): Boolean {
        if(cardsOnTable() < 8 || !continueGame) return false //ugly, but has to do for now
        else {
            continueGame = false
            println("52 cards on the table, and the top card is ${topCard()}")
            return true
        }
    }

    fun play(){
        println("Indigo Card Game")
        var firstAction = ::computerTurn
        var secondAction = ::playerTurn
        if (askForFirstPlayer()){
            firstAction = ::playerTurn
            secondAction = ::computerTurn
        }
        println("Initial cards on the table: ${table.joinToString(" ")}")
        while (continueGame){
            firstAction()
            if(! continueGame) break
            secondAction()
            if(! continueGame) break
            if(computer.hand.isEmpty() && player.hand.isEmpty()){
                if(deck.cardsLeft() < 12) break
                else{
                    computer.hand.addAll(deck.get(6))
                    player.hand.addAll(deck.get(6))
                }
            }
        }

        println("${cardsOnTable()} cards on the table, and the top card is ${topCard()}")
        println("Game Over")

    }

    fun askForFirstPlayer(): Boolean {
        println("Play first?")
        when(readln()) {
            "yes" -> return true
            "no" -> return false
            else -> return askForFirstPlayer()
        }
    }
    fun askForCardNumber(): Int? {
        println("Choose a card to play (1-${player.hand.size}):")
        val answer = readln()
        if (answer == "exit") {
            exit()
            return null
        }
        val num = answer.toIntOrNull()
        if (num == null || num < 1 || num > player.hand.size) return askForCardNumber()
        return num?.minus(1)
//        return num?.plus(1)
    }

    fun playerTurn(){
        println("${cardsOnTable()} cards on the table, and the top card is ${topCard()}")
        player.printHand()
        var cardIndex = askForCardNumber()
        if(cardIndex == null) return
        var playedCard = player.hand.removeAt(cardIndex!!)
        table.add(playedCard)
    }

    fun computerTurn(){
        println("${cardsOnTable()} cards on the table, and the top card is ${topCard()}")
        var playedCard = computer.hand.removeLast()
        println("Computer plays $playedCard\n")
        table.add(playedCard)

    }

    fun exit(){
        println("Game over")
        continueGame = false
    }
}


fun main() {
    val game = Game()
    game.play()
}