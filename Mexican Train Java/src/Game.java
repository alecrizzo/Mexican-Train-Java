import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

public class Game {
	
	public Game()
	{
		loadOrRound();
	}
	
	/* *********************************************************************
	Function Name: loadOrRound
	Purpose: to let the user load a save or start a new round
	Parameters: none
	Return Value: void
	Algorithm:
				1) ask user to load game or start round
				2) create round based on their input
	Assistance Received: none
	********************************************************************* */
	public void loadOrRound()
	{
		System.out.print("Would you like to load a save or start a new round? \n");
		String input = "";
		do
		{
			System.out.print("Enter (1) to load game (2) to create new round: ");
			input = ConsoleInput.readToWhiteSpace(true);
			//transform(input.iterator(), input.end(), input.iterator(), tolower);
			if (input.equals("1"))
			{
				break;
			}
			if (input.equals("2"))
			{
				break;
			}
		} while (true);
		// Had to do the while condition like this due to bool issues with string comparison
		if (input.equals("1"))
		{
			Round gameRound = readSave();
		}
		else if (input.equals("2"))
		{
			Round singleRound = new Round();
		}
	} // End of loadOrRound()

	/* *********************************************************************
	Function Name: readSaveInput
	Purpose: get the users input for the name of the save file to load
	Parameters: none
	Return Value: string for file name
	Algorithm:
				1) ask user the name of the file
				2) write the name of the file to file
				3) add .txt to the end of the string
	Assistance Received: none
	********************************************************************* */
	public String readSaveInput()
	{
		String file;
		do
		{
			System.out.print("Enter the name of the file you would like to read: ");
			file = ConsoleInput.readToWhiteSpace(true);
		} while (file.equals(""));
    
		file += ".txt";
		return file;
	} // End of readSaveInput()

	/* *********************************************************************
	Function Name: readSave
	Purpose: lets the user load a savegame into a round
	Parameters: none
	Return Value: Round object
	Algorithm:
				1) call readSaveInput() to get the filename we need to load
				2) go through line by line and assign values to temporary vectors
				3) read through the lines until the semicolons while assigning the values
				4) at the end we initialize all the temporary values into our actual members and objects
					that we will use for a round
	Assistance Received: none
	********************************************************************* */
	public Round readSave()
	{
		int i = 0;
		int roundNum = 0;
		String temp = "";
		String temp2 = "";
		String line = "";
		String turn = "";
		Computer computer = new Computer();
		Human human = new Human();

		Vector<Tile> mexican_train = new Vector<Tile>();
		Vector<Tile> boneyard = new Vector<Tile>();
		Vector<Tile> computer_hand = new Vector<Tile>();
		Vector<Tile> computer_train = new Vector<Tile>();
		Vector<Tile> human_hand = new Vector<Tile>();
		Vector<Tile> human_train = new Vector<Tile>();

		// Default constructor value sets to 0-0
		Tile tempTile = new Tile();	

		// Prompt for user input on save name
		String filename = readSaveInput();

	    // pass the path to the file as a parameter
	    File file = new File("A:\\School\\OPL 2021\\Mexican Train Java\\src\\" + filename);
	    Scanner sc;
		try {
			sc = new Scanner(file);
			if (file.canRead())
			{
				while (sc.hasNextLine())
				{
					line = sc.nextLine();
					// Condition for if line is empty, don't increase i to keep
					// the switch logic in check for next fetched line
					if (line.equals(""))
					{
						continue;
					}
					else
					{
						switch (i)
						{
						// Round number
						case 0:	
							for (int z = line.length()-1; z > 5; z--)
							{
								// Reads if no space is with the first character read
								if (z == line.length()-1 && line.charAt(z) != ' ')
								{
									temp = String.valueOf(line.charAt(z));
									int r = 1;
									while (line.charAt(z-r) != ' ' && line.charAt(z-r) != ':')
									{
										temp = line.charAt(z-r) + temp;
										r++;
									}
									continue;
								}
								if (line.charAt(z) == ':') { break; }
								// Gathers string between 2 space characters
								if (line.charAt(z) == ' ')
								{
									int r = 1;
									while (line.charAt(z-r) != ' ' && line.charAt(z-r) != ':')
									{
										temp = line.charAt(z-r) + temp;
										r++;
									}
								}
							}
							// Set the round number to the integer value of temp string (convert string to int)
							roundNum = Integer.parseInt(temp);
							temp = "";
							break;
						// Computer
						case 1:	
							break;
						// C Score
						case 2:
							for (int z = line.length()-1; z > 6; z--)
							{
								// reads if no space is with the first character read
								if (z == line.length()-1 && line.charAt(z) != ' ')
								{
									temp = String.valueOf(line.charAt(z));
									int r = 1;
									while (line.charAt(z-r) != ' ' && line.charAt(z-r) != ':')
									{
										temp = line.charAt(z-r) + temp;
										r++;
									}
									continue;
								}
								if (line.charAt(z) == ':') { break; }
								// gathers string between 2 space characters
								if (line.charAt(z) == ' ')
								{
									int r = 1;
									while (line.charAt(z-r) != ' ' && line.charAt(z-r) != ':')
									{
										temp = line.charAt(z-r) + temp;
										r++;
									}
								}
							}
							// Write score to a computer player
							computer.setScore(Integer.parseInt(temp));
							temp = "";

							break;
						// C Hand
						case 3:
							// Loops until z is == to the start of the line, "Hand:"
							for (int z = line.length()-1; z > 5; z--)
							{
								// If we hit the : we know thats the end of the relevant data in the file
								if (line.charAt(z) == ':') { break; }

								// If we hit a '-' in the line, get the values on each side as a tile and push to hand
								if (line.charAt(z) == '-')
								{
									// Would have done this in 1 line but the compiler did not like that so I had to set
									// temp variables to use string to int here
									temp = String.valueOf(line.charAt(z-1));
									temp2 = String.valueOf(line.charAt(z+1));

									//tempTile.setTile(Integer.parseInt(temp), Integer.parseInt(temp2));
									computer_hand.add(new Tile(Integer.parseInt(temp), Integer.parseInt(temp2)));

									temp = "";
									temp2 = "";
								}
							}

							break;
						// C Train
						case 4:
							for (int z = line.length()-1; z > 6; z--)
							{
								if (line.charAt(z) == 'M')
								{
									computer.setMarker(true);
								}
								else
								{
									// If we hit the : we know thats the end of the relevant data in the file
									if (line.charAt(z) == ':') { break; }

									// If we hit a '-' in the line, get the values on each side as a tile and push to hand
									if (line.charAt(z) == '-')
									{
										// Would have done this in 1 line but the compiler did not like that so I had to set
										// temp variables to use string to int here
										temp = String.valueOf(line.charAt(z-1));
										temp2 = String.valueOf(line.charAt(z+1));

										//tempTile.setTile(Integer.parseInt(temp), Integer.parseInt(temp2));
										computer_train.add(new Tile(Integer.parseInt(temp), Integer.parseInt(temp2)));

										temp = "";
										temp2 = "";
									}
								}
							}
							break;
						// Human
						case 5:	
							break;
						// H Score
						case 6:
							for (int z = line.length()-1; z > 6; z--)
							{
								// reads if no space is with the first character read
								if (z == line.length()-1 && line.charAt(z) != ' ')
								{
									temp = String.valueOf(line.charAt(z));
									int r = 1;
									while (line.charAt(z-r) != ' ' && line.charAt(z-r) != ':')
									{
										temp = line.charAt(z-r) + temp;
										r++;
									}
									continue;
								}
								if (line.charAt(z) == ':') { break; }
								// gathers string between 2 space characters
								if (line.charAt(z) == ' ')
								{
									int r = 1;
									while (line.charAt(z-r) != ' ' && line.charAt(z-r) != ':')
									{
										temp = line.charAt(z-r) + temp;
										r++;
									}
								}
							}
							// Write score to a human player
							human.setScore(Integer.parseInt(temp));
							temp = "";

							break;
						// H Hand
			   			case 7:	
							// Loops until z is == to the start of the line, "Hand:"
							for (int z = line.length()-1; z > 5; z--)
							{
								// If we hit the : we know thats the end of the relevant data in the file
								if (line.charAt(z) == ':') { break; }

								// If we hit a '-' in the line, get the values on each side as a tile and push to hand
								if (line.charAt(z) == '-')
								{
									// Would have done this in 1 line but the compiler did not like that so I had to set
									// temp variables to use string to int here
									temp = String.valueOf(line.charAt(z-1));
									temp2 = String.valueOf(line.charAt(z+1));

									//tempTile.setTile(Integer.parseInt(temp), Integer.parseInt(temp2));
									human_hand.add(new Tile(Integer.parseInt(temp), Integer.parseInt(temp2)));

									temp = "";
									temp2 = "";
								}
							}
							break;
						// H Train
						case 8:	
							for (int z = line.length()-1; z > 6; z--)
							{
								if (line.charAt(z) == 'M')
								{
									human.setMarker(true);
								}
								else
								{
									// If we hit the : we know thats the end of the relevant data in the file
									if (line.charAt(z) == ':') { break; }

									// If we hit a '-' in the line, get the values on each side as a tile and push to hand
									if (line.charAt(z) == '-')
									{
										// Would have done this in 1 line but the compiler did not like that so I had to set
										// temp variables to use string to int here
										temp = String.valueOf(line.charAt(z-1));
										temp2 = String.valueOf(line.charAt(z+1));

										//tempTile.setTile(Integer.parseInt(temp), Integer.parseInt(temp2));
										human_train.add(new Tile(Integer.parseInt(temp), Integer.parseInt(temp2)));

										temp = "";
										temp2 = "";
									}
								}
							}
							break;
						// Mexican Train
						case 9:	
							for (int z = line.length()-1; z > 14; z--)
							{
								// If we hit the : we know thats the end of the relevant data in the file
								if (line.charAt(z) == ':') { break; }

								// If we hit a '-' in the line, get the values on each side as a tile and push to hand
								if (line.charAt(z) == '-')
								{
									// Would have done this in 1 line but the compiler did not like that so I had to set
									// temp variables to use string to int here
									temp = String.valueOf(line.charAt(z-1));
									temp2 = String.valueOf(line.charAt(z+1));

									//tempTile.setTile(Integer.parseInt(temp), Integer.parseInt(temp2));
									mexican_train.add(new Tile(Integer.parseInt(temp), Integer.parseInt(temp2)));

									temp = "";
									temp2 = "";
								}
							}
							break;
						// Boneyard
						case 10:
							for (int z = line.length()-1; z > 9; z--)
							{
								// If we hit the : we know thats the end of the relevant data in the file
								if (line.charAt(z) == ':') { break; }

								// If we hit a '-' in the line, get the values on each side as a tile and push to hand
								if (line.charAt(z) == '-')
								{
									// Would have done this in 1 line but the compiler did not like that so I had to set
									// temp variables to use string to int here
									temp = String.valueOf(line.charAt(z-1));
									temp2 = String.valueOf(line.charAt(z+1));

									//tempTile.setTile(Integer.parseInt(temp), Integer.parseInt(temp2));
									boneyard.add(new Tile(Integer.parseInt(temp), Integer.parseInt(temp2)));

									temp = "";
									temp2 = "";
								}
							}
							break;
						// Next player
						case 11:
							if (line.charAt(13) == 'C')
							{
								turn = "Computer";
							}
							else if (line.charAt(13) == 'H')
							{
								turn = "Human";
							}
							break;
						default:
							break;
						}
					}
					i++;
				}
			}
			else
			{
				System.out.print("File could not open");
				System.exit(0);
			}

			// Flip the values of all vectors to have beginning and end swapped 
			// (note: they are read from file in the wrong order)
			Collections.reverse(mexican_train);
			Collections.reverse(human_hand);
			Collections.reverse(computer_hand);
			Collections.reverse(human_train);
			Collections.reverse(computer_train);
			Collections.reverse(boneyard);
			human.setHand(human_hand);
			human.setTrain(human_train);
			computer.setHand(computer_hand);
			computer.setTrain(computer_train);	

			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Return a round created with the values we loaded			
		return new Round(turn, roundNum, human, computer, mexican_train, boneyard);

	}// End of readSave()
	// End of file Game.cpp

	
}
