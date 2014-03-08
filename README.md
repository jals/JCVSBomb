Bomberman
========
By Vinayak Bansal, Jarred Linthorne, Sean Byron & Chris Siewecke

1. Starting up the game
-------------
###1.1 Server
To start up the server, call its main method with the following arguments:
* Mode - 1 starts the server in test mode, 0 starts the server in normal, game mode
* Port - Port to listed for clients on

###1.2 Client
To start up a client, call its main method with the following arguments:
* Server IP - IP address of the server
* Port - The port the server is listening on

Once the client is running, type you name on the command line and press enter.

2. Playing the game
-------------
Once a server is started, and one or more clients are connected, type START_GAME on the command line from your client to start up the game. Once the game has started, the GUI will be displayed.

To move around the board, type the following commands on the command line:
* MOVE_UP
* MOVE_DOWN
* MOVE_LEFT
* MOVE_RIGHT

To leave the game, type LEAVE_GAME

3. Test framework
-------------
For this milestone, the primary way of running the game is through the test framework.The TestDriver starts up a server instance, and one or two clients, and runs a series of commands. The TestDriver takes as input a test case, which is a text file. The test case defines which clients to create, and then a series of commands to run for each client. The TestDriver runs these commands, then checks the log file generated by the server to ensure that each player was added to the game, and that command was successfully received.

###3.1 Running a test case
To run a test case, call the main method of TestDriver, and pass the path to the test case as an argument. As the commands in the test case are executed, the TestDriver will print out to the console.

###3.2 Specifying a test case
A test case is a comma separated list of instructions for the TestDriver. There are two instructions:

* PLAYER,{player_name}- Creates a new client with the name player_name and starts the client
* COMMAND,{player_name},{operation} - Executes the giver operation (START_GAME, LEAVE_GAME, MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT)
	
The TestDriver starts the server in test mode, meaning that the default board is loaded, the players are always added to the board in the same place (1,1 for player 1, 10,10 for player 2), and the door is always in the same place. Using these pre-conditions, and a test case made up of a series of commands, it is possible to test a variety of scenarios.

###3.3 Test Cases
For the first milestone, we have created three test cases

* one_player_move_to_door.txt - Creates one player (at 1,1) and moves right until the player reaches the foor (at 1,4).
* two_players_test.txt - Creates two players, one at (1,1) the other at (10,10). Both players move around for a time, then leave the game.
* two_player_test_touch.txt - Creates two players, one at (1,1) the other at (10,10). Player one moves down and player two moves left until they touch in the bottom left corner.
* one_player_wall_error.txt - Creates one player, the player moves around, then tries to move into a wall, then leaves. Examining the log from the server should show an error when the player tries to move into the wall