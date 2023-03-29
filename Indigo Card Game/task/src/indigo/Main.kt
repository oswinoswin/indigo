package indigo

val WINNING_RANKS = listOf<String>("A", "10", "J", "Q", "K")
val RANKS = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
val SUITS = listOf("♦", "♥", "♠", "♣")
val DECK_SIZE = 12
data class Card(val rank :String, val suit :String){
    var points = 0
    init {
        points = if (rank in WINNING_RANKS) 1 else 0
    }
    fun matches(other :Card): Boolean {
        return this.rank == other.rank || this.suit == other.suit
    }
    override fun toString(): String {
        return "$rank$suit"
    }
}

class Deck {
    private var deckBase :List<Card>
    private var deck = mutableListOf<Card>()
    init {
        for(suit in SUITS){
            for(rank in RANKS){
                deck.add(Card(rank, suit))
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

    fun get(amount: Int): MutableList<Card> {
        if (amount == null || amount < 1 || amount > DECK_SIZE) {
            throw Exception("Invalid number of cards")
        }
        if ( amount > deck.size) {
            throw Exception("The remaining cards are insufficient to meet the request.")
        }
        val hand = mutableListOf<Card>()
        repeat(amount){
            hand.add(deck.removeFirst())
        }
        return hand
    }

    fun isEmpty(): Boolean {
        return deck.isEmpty()
    }

    fun cardsLeft(): Int {
        return deck.size
    }
}

open class PlayerSuperclass(val hand: MutableList<Card>) {
    private val cardsWon = mutableListOf<Card>()
    private var score = 0
    private var won = 0
    var ownsMostCards = false
    open fun playCard(topCard :Card?) :Card{
        return hand.last()
    }

    fun collectCard(card: Card ){
        cardsWon.add(card)
        won ++
        score += card.points
    }

    open fun wins(){
        println("")
    }

    fun collectedCards() = won
    fun score() = if (ownsMostCards) score + 3 else score
}

open class Player(cards: MutableList<Card>) :PlayerSuperclass(cards){
    private fun printHand(){
        print("Cards in hand: ")
        hand.forEachIndexed { index, s ->  print("${index + 1})$s ") }
        print("\n")
    }

    override fun wins() {
        println("Player wins cards")
    }

    override fun playCard(topCard: Card?): Card {
        printHand()
        val cardNum = askForCardNumber()
        return hand.removeAt(cardNum)
    }

    private fun askForCardNumber(): Int {
        println("Choose a card to play (1-${hand.size}):")
        val answer = readln()
        if (answer == "exit") {
             throw Exception("Exit")
        }
        val num = answer.toIntOrNull()
        if (num == null || num < 1 || num > hand.size) return askForCardNumber()
        return num.minus(1)
    }


}

class Computer(cards: MutableList<Card>) : PlayerSuperclass(cards) {

    private fun findSameSuit() : Card? {
        val cardsToThrow = mutableListOf<Card>()
        for(suit in SUITS){
            val tmp = hand.filter { card -> card.suit == suit }
            if(tmp.size >= 2) cardsToThrow.addAll(tmp)
        }
        return cardsToThrow.randomOrNull()
    }
    private fun findSameRank() :Card? {
        val cardsToThrow = mutableListOf<Card>()
        for(rank in RANKS){
            val tmp = hand.filter { card -> card.rank == rank }
            if(tmp.size >= 2) cardsToThrow.addAll(tmp)
        }
        return cardsToThrow.randomOrNull()
    }

    override fun playCard(topCard :Card?) :Card {
        println(hand.joinToString(" "))
        var cardToPlay :Card = hand.random()
        if (topCard == null){
            if (findSameSuit() != null) cardToPlay = findSameSuit()!!
            else if (findSameRank() != null) cardToPlay = findSameRank()!!
        }
        else {
            val candidateSuits = hand.filter { card -> card.suit == topCard.suit }
            val candidateRanks = hand.filter { card -> card.rank == topCard.rank }
            if(candidateSuits.isEmpty() && candidateRanks.isEmpty()) {
                if (findSameSuit() != null) cardToPlay = findSameSuit()!!
                else if (findSameRank() != null) cardToPlay = findSameRank()!!
            }
            else if (candidateSuits.size >= 2) {
                cardToPlay = candidateSuits.random()
            } else if(candidateSuits.size <2 && candidateRanks.size >= 2){
                cardToPlay = candidateRanks.random()
            }else{
                val candidates = candidateSuits.toMutableList()
                candidates.addAll(candidateRanks)
                cardToPlay = candidates.random()
            }

        }

        hand.remove(cardToPlay)
        println("Computer plays ${cardToPlay}\n")
        return cardToPlay
    }
    override fun wins() {
        println("Computer wins cards")
    }
}



class Game {
    private val deck = Deck()
    private val table = mutableListOf<Card>()
    private val player :PlayerSuperclass
    private val computer :PlayerSuperclass
    private var computerWonLast = true
    private var continueGame = true
    private var computerStarts = true

    init {
        deck.shuffle()
        table.addAll(deck.get(4))
        computer = Computer(deck.get(6))
        player = Player(deck.get(6))
    }

    private fun topCard(): Card {
        return table.last()
    }

    private fun cardsOnTable(): Int {
        return table.size
    }

    fun play(){
        println("Indigo Card Game")
        var firstPlayer = computer
        var secondPlayer = player
        if (askForFirstPlayer()){
            firstPlayer = player
            secondPlayer = computer
            computerStarts = false
            computerWonLast = false
        }
        println("Initial cards on the table: ${table.joinToString(" ")}")
        while (continueGame){
            turn(firstPlayer)
            if(! continueGame) break
            turn(secondPlayer)
            if(! continueGame) break
            if(computer.hand.isEmpty() && player.hand.isEmpty()){  //deal more cards if needed
                if(deck.cardsLeft() < 12) {
                    //normal end of game
                    countRemainingCards()
                    exitMessage()
                    break
                }
                else{
                    computer.hand.addAll(deck.get(6))
                    player.hand.addAll(deck.get(6))
                }
            }
        }

    }
    private fun countRemainingCards(){
        val lastWinner = if(computerWonLast) computer else player
        for(card in table){
            lastWinner.collectCard(card.copy())
        }
        if(computer.collectedCards() > player.collectedCards() || (computer.collectedCards() == player.collectedCards() && computerStarts)){
            computer.ownsMostCards = true
            player.ownsMostCards = false
        } else {
            computer.ownsMostCards = false
            player.ownsMostCards = true
        }
    }

    private fun printScore(){

        println("Score: Player ${player.score()} - Computer ${computer.score()}\n" +
                "Cards: Player ${player.collectedCards()} - Computer ${computer.collectedCards()}")
    }
    private fun turn(currentPlayer: PlayerSuperclass){
        println(if(!table.isEmpty())
            "${cardsOnTable()} cards on the table, and the top card is ${topCard()}" else "No cards on the table"
        )
        try {
            val playedCard = currentPlayer.playCard(table.lastOrNull())
            //check if player should get the card
            if (table.isNotEmpty() && playedCard.matches(topCard())) {
                currentPlayer.collectCard(playedCard)
                table.forEach { card -> currentPlayer.collectCard(card) }
                table.clear()
                currentPlayer.wins()
                printScore()
                computerWonLast = currentPlayer != player
            }
            else {
                table.add(playedCard)
            }
        }
        catch (e: Exception){
            println("Game Over")
            continueGame = false
        }
    }
    private fun askForFirstPlayer(): Boolean {
        println("Play first?")
        return when(readln()) {
            "yes" -> true
            "no" -> false
            else -> askForFirstPlayer()
        }
    }

    fun exitMessage(){
        println(if(!table.isEmpty())
            "${cardsOnTable()} cards on the table, and the top card is ${topCard()}" else "No cards on the table"
        )
        printScore()
        println("Game Over")
    }
}


fun main() {
    val game = Game()
    game.play()
}