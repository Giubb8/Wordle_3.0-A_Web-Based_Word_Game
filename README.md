# Wordle 3.0 - A Web-Based Word Game
🟩🟨⬜
### Overview 📝

This project involves the implementation of Wordle, a web-based word game that went viral at the end of 2021. In this section, we will describe the general rules of the game, followed by the specifics of the version of Wordle to be implemented.

Wordle challenges players to find an English word composed of 5 letters within a maximum of 6 attempts. Wordle has a vocabulary of 5-letter words from which it randomly selects a Secret Word (SW) that players must guess. Each day, a new SW is chosen, remaining unchanged until the following day, and it's offered to all users who connect to the system on that day. Thus, there's only one word for each day, fostering a social aspect of the game. Users propose a Guessed Word (GW), and the system initially checks if the word is in the vocabulary. If not, it prompts the user to enter another word. If the word is present, the system provides the user with three types of clues for each letter 'l' in GW:

- If 'l' is guessed and in the correct position compared to SW.
- If 'l' is guessed but in a different position in SW.
- If 'l' doesn't appear in SW.

In Wordle, these hints are presented to the user by coloring the letters in GW with different colors.

The user, using these hints, enters another word. The game ends when the user identifies the secret word or when the attempts are exhausted. The system tracks the following statistics for each user and displays them after each game:

- Number of games played.
- Percentage of games won.
- Length of the last continuous winning streak.
- Length of the longest continuous winning streak.
- Guess distribution: the distribution of attempts used to solve the game in each game won by the player.

### Features 💡

- Wordle is implemented using two main components that interact using different network communication protocols and paradigms: WordleClient and WordleServer.
- WordleClient manages user interaction via a Command Line Interface (CLI) and communicates with WordleServer to perform user-requested actions.
- WordleServer handles user registration and login, stores registered users, periodically selects a new secret word, interacts with different clients wanting to participate in the game, and manages user statistics.
- After successful login, each client joins a multicast group, which the server is also part of. The sharing of game outcomes is sent by the server to clients via UDP multicast messages.
- WordleServer maintains a ranked leaderboard of users based on their game performance. The score for each user is calculated based on the number of correctly guessed words and the average number of attempts required to solve the game.
- Upon the end of a game session (regardless of success or failure), the server provides the client with the Italian translation of the secret word by accessing a service available at the URL `https://mymemory.translated.net/doc/spec.php` via an HTTP GET request.
- WordleClient receives notifications when there are changes in the top three positions of the user ranking. The ranking is displayed on-demand to the client using the `showMeRanking()` command.

### Implementation Specifications ⚙️

The project utilize various technologies, including:

- Users interact with Wordle via a command-line interface (CLI), and graphical interfaces are optional.
- Registration Phase: Implemented via RMI (Remote Method Invocation).
- Login Phase: Must be done after successful registration. In each login session, each user can attempt to guess multiple SWs based on the session's duration. Each guessing attempt is considered completed when the user logs out.
  After login, the client registers with a notification service on the server to receive updates on the user leaderboard. The notification service must be implemented using RMI callback.
- Following successful login, the client interacts with the server using a client-server model (requests/responses) over the established persistent TCP connection.
- Each client, after login, joins a multicast group shared with the server. Sharing the game's outcome, requested by the client via the `share()` command, is sent by the server to all clients via UDP multicast messages. The client is always  listening for the server notifications and stores them in its data structure.
- The server is implemented using Java I/O and a thread pool. The server defines suitable data structures to store user information and persists the system's state to JSON files. When the server is restarted, this information is used to reconstruct the system's state. Data is stored on files in JSON format.
- The time period between publishing a word and publishing the next word is defined as a configuration parameter. A user can participate in multiple games (secret words) during the same login session. However, once they have attempted to guess a secret word (whether successfully or not), they must wait for the next secret word to be drawn before playing again.
- Displaying the server's hints on the CLI is done by associating a different letter with each color ( gray: 'X', green: '+', yellow: '?').

## Contact 📇

For questions or support, please feel free to contact me at gb8gb8**AT**gmail**DOT**com 

Feel free to explore the project, contribute, and adapt it to suit your data logging needs. Happy coding!
