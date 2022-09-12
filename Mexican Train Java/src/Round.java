import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Vector;

public class Round {

	protected Vector<Tile> boneyard = new Vector<Tile>();
	private Vector<Tile> mexican_train = new Vector<Tile>();

	private Deck roundDeck = new Deck();
	private Tile engine = new Tile();

	private Human hum = new Human();
	private Computer comp = new Computer();

	private String turn = new String();
	private int roundNum;

	private boolean mexTrainOrphanDouble;
	private boolean gameEnd;
	private boolean isFirstRun;
	
	public Round()
	{
		this.turn = "";
		this.roundNum = 1;
		this.boneyard.clear();
		this.mexican_train.clear();
		this.mexTrainOrphanDouble = false;
		this.gameEnd = false;

		setUpRound();
		determineFirstTurn();
		gameplayLoop();
	}// End of Round()
	
	// Constructor to create round from an existing save game using the passed values to initialize the game
	public Round(String loadTurn, int numRounds, Human p1, Computer p2, Vector<Tile> load_mexican_train, Vector<Tile> load_boneyard)
	{

		if (loadTurn == "Human" || loadTurn == "Computer")
		{
			this.turn = loadTurn;
		}
		else
		{
			this.turn = "Error";
		}

		if (numRounds > 0)
		{
			this.roundNum = numRounds;
		}
		else
		{
			this.roundNum = 1;
		}

		this.hum = p1;
		this.comp = p2;

		this.boneyard.clear();
		this.boneyard = load_boneyard;

		this.mexican_train.clear();
		this.mexican_train = load_mexican_train;

		// Incremental integer to make sure no more than 1 orphan double is in play
		int orphanCheck = 0;

		// Check for orphan doubles on the trains last values
		if (mexican_train.size() >= 1 && mexican_train.get(mexican_train.size() - 1).isDouble() == true)
		{
			// Set orphan double on mexican train
			this.mexTrainOrphanDouble = true;
			orphanCheck++;
		}
		if (this.hum.getTrain().size() >= 2 && this.hum.getTrain().get(this.hum.getTrain().size() - 1).isDouble() == true)
		{
			// Set orphan double on humans train
			this.hum.setOrphan(true);
			orphanCheck++;
		}
		if (this.comp.getTrain().size() >= 2 && this.comp.getTrain().get(0).isDouble() == true)
		{
			// Note: for computer the end value is actually the first in the vector
			// Set orphan double on computers train
			this.comp.setOrphan(true);
			orphanCheck++;
		}
		if (orphanCheck > 1)
		{
			System.out.print("Error creating game: more than one orphan double in play");
			System.exit(1);
		}

		// The engineVal will always be the start of the human train on load
		// so we can use it to set our engine up
		int engineVal = hum.getTrain().get(0).getSideA();
		this.engine.setTile(engineVal, engineVal);

		gameplayLoop();
	}// End of Round(String loadTurn, int numRounds, Human p1, Computer p2, Vector<Tile> load_mexican_train, Vector<Tile> load_boneyard)

	/* *********************************************************************
	Function Name: setUpRound
	Purpose: set up a new round
	Parameters: none
	Return Value: void
	Algorithm: sets up a new round in order
				1) engine is determined based on the number of rounds played
				2) tiles are shuffled
				3) deal 16 tiles to human
				4) deal 16 tiles to computer
				5) place remaining tiles in boneyard
				6) set human and computer train to start with the current engine
	Assistance Received: none
	********************************************************************* */
	public void setUpRound()
	{
		// The engine for the round is determined from the rounds played
		Tile currentEngine = getEngine();	// !HERE 
		setEngine(currentEngine);
		removeEngineFromDeck(currentEngine);
		this.mexican_train.add(currentEngine);

		// The remaining tiles are shuffled
		this.roundDeck.shuffleDeck();
			
		// The human player is dealt 16 tiles 
		this.roundDeck.popNumTiles(this.hum.hand, 16);
			// this->hum.take16(this->roundDeck); !ALEC prob don't need this funciton

		// The computer player is dealt 16 tiles
		this.roundDeck.popNumTiles(this.comp.hand, 16);

		// The remaining tiles are placed in the boneyard
		this.roundDeck.popNumTiles(this.boneyard, roundDeck.double_nine_set.size());

		// Starts the human and computers personal trains off with the current engine
		this.hum.addTileTrain(currentEngine, this.hum.train);
		this.comp.addTileComputerTrain(currentEngine);
	}
	
	/* *********************************************************************
	Function Name: getEngine
	Purpose: determines the engine based on round number
	Parameters: none
	Return Value: Tile
	Algorithm: use roundNum % 10 to get a number 0-9 for the engine
				return a tile with the number obtained on both sides
	Assistance Received: none
	********************************************************************* */
	// Determines the current engine from a double nine set
	private Tile getEngine()
	{
		int engineVal = -1;

		// Determine the last digit of the round number to get the current engine
		switch (this.roundNum % 10)
		{
		case 0: engineVal = 0;
			break;
		case 1: engineVal = 9;
			break;
		case 2: engineVal = 8;
			break;
		case 3: engineVal = 7;
			break;
		case 4: engineVal = 6;
			break;
		case 5: engineVal = 5;
			break;
		case 6: engineVal = 4;
			break;
		case 7: engineVal = 3;
			break;
		case 8: engineVal = 2;
			break;
		case 9: engineVal = 1;
			break;
		default: engineVal = -1;
			break;

		}

		// Set up the tile of the current engine engineVal-engineVal
		Tile engine = new Tile(engineVal, engineVal);
		return engine;
	}// End of getEngine()
	
	/* *********************************************************************
	Function Name: setEngine
	Purpose: set the member engine to the passed tile
	Parameters: eng, Tile to assign to engine
	Return Value: void
	Algorithm: check that eng is a double
				assign eng to engine
	Assistance Received: none
	********************************************************************* */
	// Fuction to change the current engine to a new value (to be used mainly with loading saves)
	private void setEngine(Tile eng)
	{
		// Check for equivalent sides on the engine, then set engine
		if (eng.getSideA() == eng.getSideB())
		{
			this.engine = eng;
		}
	}//End of setEngine()
	
	/* *********************************************************************
	Function Name: removeEngineFromDeck
	Purpose: remove the engine tile passed from the deck
	Parameters: eng, tile to search through the roundDeck and remove
	Return Value: void
	Algorithm: search through the roundDeck and remove the tile that matches
				eng in the deck
	Assistance Received: none
	********************************************************************* */
	// Fucntion to remove the current engine from the deck so it is not in play
	private void removeEngineFromDeck(Tile eng)
	{
		for (int i = 0; i < roundDeck.getDOUBLE_NINE_SIZE(); i++)
		{
			int tempSideA = this.roundDeck.double_nine_set.get(i).getSideA();
			int tempSideB = this.roundDeck.double_nine_set.get(i).getSideB();

			// If condition for matching engine tile (i.e. Tile eng 0-0 and tempA and tempB are 0-0)
			if (tempSideA == eng.getSideA() && tempSideB == eng.getSideB())
			{
				// Remove the engine tiles matching tile from the roundDeck
				this.roundDeck.double_nine_set.remove(i);
				break;
			}
		}
	}// End of removeEngineFromDeck()
	
	/* *********************************************************************
	Function Name: determineFirstTurn
	Purpose: determines which player gets to play first
	Parameters: none
	Return Value: void
	Algorithm: if human score or computer score is lower, they play first
				otherwise they call the coin flip for first turn
	Assistance Received: none
	********************************************************************* */
	private void determineFirstTurn()
	{
		if (this.comp.getScore() == this.hum.getScore())
		{
			// Flip a coin for first turn
				// !ANDROID adjust for android 
			boolean userCoin = getUsersCoin();
			boolean flippedCoin = flipCoin(userCoin);
			printCoin(flippedCoin);

			if ( userCoin == flippedCoin )
			{
				this.turn = "Human";
			}
			else
			{
				this.turn = "Computer";
			}
		}
		else if (this.comp.getScore() > this.hum.getScore())
		{
			// Human has lower score, goes first
			this.turn = "Human";
		}
		else if (this.comp.getScore() < this.hum.getScore())
		{
			// Computer has lower score, goes first
			this.turn = "Computer";
		}
		else
			
		{
			System.out.print("ERROR in determineFirstTurn(): could not determine turn\n");
			System.exit(1);
		}

		// !ANDROID 
		printFirstTurn();
	}// End of determineFirstTurn()
	
	/* *********************************************************************
	Function Name: getUsersCoin
	Purpose: get the users coin value
	Parameters: none
	Return Value: bool
	Algorithm: ask the user for a side of the coin, return 0 if they choose heads return
				1 if they choose tails
	Assistance Received: none
	********************************************************************* */
	// Get users choice for the coin toss
	private boolean getUsersCoin()
	{
		String input = "";
		do
		{
			System.out.print("Pick a side of the coin to flip, enter (H) for heads or (T) for tails: ");
			input = ConsoleInput.readToWhiteSpace(true);
			input.toLowerCase();
			if (input.equals("h"))
			{
				return false;
			}
			if (input.equals("t"))
			{
				return true;
			}
		} while (true);
		// Had to do the while condition like this due to bool issues with string comparison
    
	} // End of getUsersCoin()
	
	/* *********************************************************************
	Function Name: flipCoin
	Purpose: flips the coin returning 1 for tails or 0 for heads
	Parameters: none
	Return Value: void
	Algorithm: seed random number generator, call rand(), if random is 1 set
				value of our coin face to tails, else if its 2 set it to 0
	Assistance Received: https://www.w3schools.in/java-program/coin-toss/
	********************************************************************* */
	// Flip the coin randomly, return 0 for heads, 1 for tails
	private boolean flipCoin(boolean userVal)
	{
		boolean face;
		if (Math.random() < 0.5)
		{	
			// value is tails
			face = true; 
		}
		else
		{
			// value is heads
			face = false;
		}
		return face;
	}// End of flipCoin()

	/* *********************************************************************
	Function Name: printCoin
	Purpose: prints the value of a boolean we treat as a coin
	Parameters: none
	Return Value: void
	Algorithm: output heads for 0, tails for 1
	Assistance Received: none
	********************************************************************* */
	// Prints the boolean value of our "coin" as heads or tails
	private void printCoin(boolean coin)
	{
		if (coin == false)
		{
			System.out.println("Coin is heads");
		}
		else if (coin == true)
		{
			System.out.println("Coin is tails");
		}
	}// End of printCoin()
	
	/* *********************************************************************
	Function Name: printFirstTurn
	Purpose: prints the first turn based on the Round member turn
	Parameters: none
	Return Value: void
	Algorithm: output the first turn
	Assistance Received: none
	********************************************************************* */
	// Prints which player goes first based on local turn value of round
	private void printFirstTurn()
	{
		System.out.print(this.turn);
		System.out.print(" goes first\n");
	}// End of printFirstTurn()
	
	/* *********************************************************************
	Function Name: gameplayLoop
	Purpose: display, menu and changing of turns
	Parameters: none
	Return Value: void
	Algorithm: 
				1) display the board
				2) display menu and get input
				3) change turn after menu
				4) determine if game is still in a playable state
				5) end game if its not, loop if it is
	Assistance Received: none
	********************************************************************* */
	private void gameplayLoop()
	{
		gameEnd = false;
		while (gameEnd == false)
		{
			displayBoard();
			menu();
			if (this.turn.equals("Computer"))
			{
				changeTurn();
			}
			else if (this.turn.equals("Human"))
			{
				changeTurn();
			}
			else
			{
				System.out.print("Error in gameplayLoop with determining turn");
				System.exit(1);
			}
    
			if (comp.getOrphan() == true && boneyard.size() == 0 && hum.playableTilesToComputerTrain(comp.getTrain()).size() == 0 
					&& comp.playableTilesToComputerTrain(comp.getTrain()).size() == 0)
			{
				System.out.print("No one can play a tile and the boneyard has emptied, the round must end\n");
				gameEnd = true;
			}
			else if (hum.getOrphan() == true && boneyard.size() == 0 && hum.playableTilesToTrain(hum.getTrain()).size() == 0 
					&& comp.playableTilesToTrain(hum.getTrain()).size() == 0)
			{
				System.out.print("No one can play a tile and the boneyard has emptied, the round must end\n");
				gameEnd = true;
			}
			else if (mexTrainOrphanDouble == true && boneyard.size() == 0 && hum.playableTilesToTrain(mexican_train).size() == 0 
					&& comp.playableTilesToTrain(mexican_train).size() == 0)
			{
				System.out.print("No one can play a tile and the boneyard has emptied, the round must end\n");
				gameEnd = true;
			}
			else if (hum.playableTiles(mexican_train, comp).size() == 0 && 
					comp.playableTiles(mexican_train, comp, mexTrainOrphanDouble).size() == 0 && boneyard.size() == 0)
			{
				System.out.print("No one can play a tile and the boneyard has emptied, the round must end\n");
				gameEnd = true;
			}
    
			if (gameEnd == true)
			{
    
				// !ANDROID change these for android
				
				hum.addScore(hum.calculateTotalPips());
				comp.addScore(comp.calculateTotalPips());
    
				System.out.print("=======================================================================================================================\n");
				System.out.print("\t\t\tThe round has ended\n");
				System.out.print("Computer has score of: ");
				System.out.print(comp.getScore());
				System.out.print("\n");
				System.out.print("Human has a score of: ");
				System.out.print(hum.getScore());
				System.out.print("\n");
				System.out.print("=======================================================================================================================\n");
    
				String temp = "";
				while (!(temp.equals("1") || temp.equals("2")))
				{
					System.out.print("Would you like to play another round? (1) yes (2) no: ");
					temp = new Scanner(System.in).nextLine();
				}
    
				if (temp.equals("1"))
				{
					// Create new round but increase the numRounds value and bring over the players scores
					resetRound();
				}
				else if (temp.equals("2"))
				{
					// Display the winner of the game based on score
					if (comp.getScore() == hum.getScore())
					{
						System.out.print("=======================================================================================================================\n");
						System.out.print("\t\t\tThe game is a draw\n");
						System.out.print("\tComputer score: ");
						System.out.print(comp.getScore());
						System.out.print("\n");
						System.out.print("\tHuman score: ");
						System.out.print(hum.getScore());
						System.out.print("\n");
						System.out.print("=======================================================================================================================\n");
					}
					else if (comp.getScore() > hum.getScore())
					{
						System.out.print("=======================================================================================================================\n");
						System.out.print("\t\t\tYou win!\n");
						System.out.print("\tComputer score: ");
						System.out.print(comp.getScore());
						System.out.print("\n");
						System.out.print("\tHuman score: ");
						System.out.print(hum.getScore());
						System.out.print("\n");
						System.out.print("=======================================================================================================================\n");
					}
					else if (comp.getScore() < hum.getScore())
					{
						System.out.print("=======================================================================================================================\n");
						System.out.print("\t\t\tComputer wins!\n");
						System.out.print("\tComputer score: ");
						System.out.print(comp.getScore());
						System.out.print("\n");
						System.out.print("\tHuman score: ");
						System.out.print(hum.getScore());
						System.out.print("\n");
						System.out.print("=======================================================================================================================\n");
					}
    
					System.exit(1);
				}
			}
		}		
	} // End of gameplayLoop()
	
	/* *********************************************************************
	Function Name: displayBoard
	Purpose: output the board and game values to the user in a readable manner
	Parameters: none
	Return Value: void
	Algorithm: print all relevant values to the player through the output
	Assistance Received: none
	********************************************************************* */
	private void displayBoard()
	{
		// Round number
		System.out.print("Round: ");
		System.out.print(this.roundNum);
		System.out.print("\n");
		System.out.print("Turn: ");
		System.out.print(this.turn);
		System.out.print("\n");
		// Print Computer Score, hand
		System.out.print("Computer: ");
		if (comp.getOrphan() == true)
		{
			System.out.print(" Orphan Double!");
		}
		System.out.print("\n\tScore: ");
		System.out.print(this.comp.getScore());
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\tHand: ");
		this.comp.printHand();
		System.out.print("\n");
		System.out.print("\n");
    
				System.out.print("\t\t");
		// Print first number of engine
		String engineNum = "";
		for (int i = 0; i < getMenuWidth(); i++)
		{
			engineNum += " ";
		}
		// convert int to character with + '0'
		// account for displacement of engine by computer train marker
		if (comp.getMarker() == true)
		{
			engineNum += "  ";
		}
		// !ALEC this might not be necessary in Java
		engineNum += this.engine.getSideA();
		System.out.print(engineNum);
		System.out.print("\n");
    
    
		System.out.print("\t\t");
		// Print Computer Train on left, Engine in the middle, Human Train on Right
		this.comp.printM();
		this.comp.printTrain(this.engine);
		System.out.print("| ");
		this.hum.printTrain(this.engine);
		this.hum.printM();
		System.out.print("\n");
    
				System.out.print("\t\t");
		// Print second number of engine
		System.out.print(engineNum);
		System.out.print("\n");
		System.out.print("\n");
    
		// Print mexican train below the players personal trains
		System.out.print("Mexican Train: ");
		printMexicanTrain();
    
		if (mexTrainOrphanDouble == true)
		{
			System.out.print("\n\t\tOrphan Double!");
		}
    
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("Boneyard: ");
		this.printBoneyard();
    
		// Print Human Hand
		System.out.print("\nHuman: ");
		if (hum.getOrphan() == true)
		{
			System.out.print(" Orphan Double!");
		}
		System.out.print("\n\tScore: ");
		System.out.print(this.hum.getScore());
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\tHand: ");
		this.hum.printHand();
		System.out.print("\n");
		System.out.print("\n");
	} // End of displayBoard()

	/* *********************************************************************
	Function Name: getMenuWidth
	Purpose: get the width to push the engine values out to in display
	Parameters: none
	Return Value: int
	Algorithm: use train size * 4 to determine the distance the engine numbers need to
				be from the left side of the output
	Assistance Received: none
	********************************************************************* */
	// Get the width of the menu for lining up the engine in the output
	// Since tiles are 3 characters followed by a space this helps line up the engine
	private int getMenuWidth()
	{	
		return (this.comp.getTrain().size() * 4) - 4;	
	}// End of getMenuWidth()
	
	/* *********************************************************************
	Function Name: printMexicanTrain
	Purpose: prints the mexican train to output
	Parameters: none
	Return Value: void
	Algorithm: output all values of the mexican train except for the engine
	Assistance Received: none
	********************************************************************* */
	// Prints the values of the local mexican train in round one by one
	private void printMexicanTrain()
	{
		for (int i = 0; i < this.mexican_train.size(); i++)
		{
			// Skip displaying the engine in the mexican train
			if ((mexican_train.get(i).getSideA() == this.engine.getSideA())
				&& (mexican_train.get(i).getSideB() == this.engine.getSideB()))
			{
				continue;
			}
			this.mexican_train.get(i).printTile();
			System.out.print(" ");
		}
	}
	
	/* *********************************************************************
	Function Name: printBoneyard
	Purpose: prints the boneyard to output
	Parameters: none
	Return Value: void
	Algorithm: output all values of the boneyard
	Assistance Received: none
	********************************************************************* */
	// Prints the boneyard and the amount of tiles remaining if boneyard has values
	private void printBoneyard()
	{
		if (this.boneyard.size() > 0)
		{
			this.boneyard.get(0).printTile();
			System.out.print(" (");
			System.out.print(this.boneyard.size());
			System.out.print(" tiles remaining)\n");
		}
	}// End of printBoneyard()
	
	/* *********************************************************************
	Function Name: menu
	Purpose: determine the users actions based on int userChoice
	Parameters: none
	Return Value: void
	Algorithm: 
				1) output options to user
				2) get users input on the options
				3) act based on users choice
					1 is writeSave
					2 is playTurn
					3 is help
					4 is exit
	Assistance Received: none
	********************************************************************* */
	// Menu function which sets off different functionalities based on the number from getMenuInput()
	private void menu()
	{
    
		System.out.print("1: Save the game\n");
		// says continue if computers turn rather than letting user make move
		if (this.turn.equals("Human"))
		{
			System.out.print("2: Make a move\n");
		}
		else
		{
			System.out.print("2: Continue\n");
		}
		System.out.print("3: Ask for help\n");
		System.out.print("4: Quit the game\n");
    
		// !ANDROID get input in android
		int userChoice = getMenuInput();
    
		if (userChoice == 1)
		{
			// Save the game
			writeSave();
		}
		else if (userChoice == 2)
		{
			// Determine which players turn based on this->turn
			if (this.turn.equals("Human"))
			{
				// Human plays
				// !HERE
				// !HERE
				this.hum.playTurn(this.mexican_train, this.boneyard, this.comp, this.mexTrainOrphanDouble, this.gameEnd);
			}
			else if (this.turn.equals("Computer"))
			{
				// Computer plays
				// !HERE
				// !HERE
				this.comp.playTurn(this.mexican_train, this.boneyard, this.hum, this.mexTrainOrphanDouble, this.gameEnd);
			}
			else
			{
				System.out.print("Error in determining turn");
				System.exit(1);
			}
    
			// Pause to read what happened
			System.out.println("Press Any Key To Continue...");
	          new java.util.Scanner(System.in).nextLine();
		}
		else if (userChoice == 3)
		{
			// Help functionality
    
			if (this.turn.equals("Computer"))
			{
				System.out.print("Cannot help you, it is currently the computers turn :)\n");
				menu();
			}
			else
			{
				// !HERE
				// !HERE
				this.hum.help(mexican_train, comp, mexTrainOrphanDouble);
				System.out.print("\n");
				menu();
			}
		}
		else if (userChoice == 4)
		{
			// Exit the game
			System.exit(0);
		}
	} // End of menu()
	
	/* *********************************************************************
	Function Name: getMenuInput
	Purpose: get the users input for what menu value they want to select
	Parameters: none
	Return Value: int
	Algorithm: 
				1) loop to validate input for numbers 1 - 4
				2) return when a valid value is input
	Assistance Received: none
	********************************************************************* */
	private int getMenuInput()
	{
		String temp = " ";
    
		System.out.print("Please input a number for one of the given options: ");
		while (!(temp.equals("1") || temp.equals("2") || temp.equals("3") || temp.equals("4")))
		{
			temp = new Scanner(System.in).nextLine();
		}
    
		int input = Integer.parseInt(temp);
		return input;
	} // End of getMenuInput()
	
	/* *********************************************************************
	Function Name: changeTurn()
	Purpose: swap the users turn based on current turn
	Parameters: none
	Return Value: void
	Algorithm: changes turn to human if computer, computer if human
	Assistance Received: none
	********************************************************************* */
	private void changeTurn()
	{
		if (this.turn == "Human")
		{
			this.turn = "Computer";
		}
		else if (this.turn == "Computer")
		{
			this.turn = "Human";
		}
		else
		{
			System.out.print("Error changing turn");
			System.exit(1);
		}
	}// End of changeTurn()
	
	/* *********************************************************************
	Function Name: resetRound()
	Purpose: setup a new round carrying over score and round number from last round
	Parameters: none
	Return Value: void
	Algorithm: clear all values except for the users score and the round number
				start new gameplayLoop
	Assistance Received: none
	********************************************************************* */
	private void resetRound()
	{
		this.roundNum++;
		this.turn = "";
		this.boneyard.clear();
		this.mexican_train.clear();
		this.mexTrainOrphanDouble = false;
		this.gameEnd = false;

		this.hum.newRoundReset();
		this.comp.newRoundReset();

		setUpRound();
		determineFirstTurn();
		gameplayLoop();
	}
	
	/* *********************************************************************
	Function Name: writeSave
	Purpose: writes a save file to the format of readSave
	Parameters: none
	Return Value: void
	Algorithm: write all our values to sfile and save
	Assistance Received: none
	********************************************************************* */
	// Writes a current round as a save game then exits
	public void writeSave()
	{
		Vector<Tile> temp_tiles = new Vector<Tile>();
		
		try	{
			BufferedWriter bw = new BufferedWriter(
					new FileWriter("A:\\School\\OPL 2021\\Mexican Train Java\\src\\" + writeSaveInput()));
			
			bw.write("Round: ");
			bw.write(roundNum +"\n\n");

			bw.write("Computer: \n");
			bw.write("\tScore: " + this.comp.getScore() + "\n");

			bw.write("\tHand: ");

			
			temp_tiles.clear();
			temp_tiles = comp.getHand();
			for (int i = 0; i < temp_tiles.size(); i++)
			{
				bw.write(temp_tiles.get(i).stringTile() + " ");
			}
			
			bw.write("\n");
			
			bw.write("\tTrain: ");
			
			temp_tiles.clear();
			temp_tiles = comp.getTrain();
			// For computer marker goes on the end
			if (comp.getMarker() == true) {	bw.write("M "); } 
			for (int i = 0; i < temp_tiles.size(); i++)
			{
				bw.write( temp_tiles.get(i).stringTile() + " ");
			}
			bw.write("\n\n");
			
			bw.write("Human: \n");
			bw.write("\tScore: " + this.hum.getScore() + "\n");
			
			bw.write("\tHand: ");
			temp_tiles.clear();
			temp_tiles = hum.getHand();
			for (int i = 0; i < temp_tiles.size(); i++)
			{
				bw.write(temp_tiles.get(i).stringTile() + " ");
			}
			bw.write("\n");
			
			bw.write("\tTrain: ");
			temp_tiles.clear();
			temp_tiles = hum.getTrain();
			// For computer marker goes on the end
			for (int i = 0; i < temp_tiles.size(); i++)
			{
				bw.write( temp_tiles.get(i).stringTile() + " ");
			}
			if (hum.getMarker() == true) {	bw.write(" M"); } 
			bw.write("\n\n");
			
			bw.write("Mexican Train: ");
			temp_tiles.clear();
			temp_tiles = this.mexican_train;
			for (int i = 0; i < temp_tiles.size(); i++)
			{
				bw.write(temp_tiles.get(i).stringTile() + " ");
			}
			bw.write("\n\n");

			bw.write("Boneyard: ");
			temp_tiles.clear();
			temp_tiles = this.boneyard;
			for (int i = 0; i < temp_tiles.size(); i++)
			{
				bw.write(temp_tiles.get(i).stringTile() + " ");
			}
			bw.write("\n\n");

			bw.write("Next Player: " + this.turn);
			
			bw.close();
		}catch(Exception ex) {
			
		}

		System.exit(0);
	}// End of writeSave()

	/* *********************************************************************
	Function Name: writeSaveInput()
	Purpose: gets the users input for what to name the save file
	Parameters: none
	Return Value: string
	Algorithm: prompt user for input, validate that they entered something, then add .txt
	Assistance Received: none
	********************************************************************* */
	public String writeSaveInput()
	{
		Scanner user = new Scanner(System.in);
		String filename = "";
		while (filename == "")
		{
			System.out.print("Enter a name for the file, afterwards the program will close: ");
			filename = user.nextLine();
		}
		filename = filename + ".txt";

		return filename;
	}// End of writeSaveInput()
	
}
