package multi_threaded;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Server {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private ArrayList<BufferedReader> ins = new ArrayList<>();
	private ArrayList<PrintWriter> outs = new ArrayList<>();
	private ArrayList<Socket> clientSockets = new ArrayList<Socket>();
	private int port;


	public Server(int port) {
		super();
		this.port = port;
		try {
			serverSocket = new ServerSocket(this.port);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ServerSocket getServerSocket() {
		return this.serverSocket;
	}

	public void start() throws IOException {
		int clientNumber = 0;

		Scanner in = new Scanner(System.in);

		System.out.println("Aloha, your server is smoothly running XD");

		while (true) {
			clientSocket = serverSocket.accept();
			if (clientSocket != null) {
				Thread th = new Thread(new RunnableServer(this.clientSockets, this.clientSocket, this.serverSocket,
						this.outs, this.ins, clientNumber++));
				th.start();
			}
		}

	}

	public void stop() {
		try {
			for (BufferedReader in : this.ins) {
				in.close();
			}
			for (PrintWriter out : this.outs) {
				out.close();
			}

			for (Socket s : this.clientSockets) {
				s.close();
			}
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {
		int port;

		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		else
			port = 9999;

		Server server = new Server(port);
		server.start();
	}
}

class RunnableServer implements Runnable {

	private PrintWriter out;
	private BufferedReader in;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private ArrayList<Socket> clientSockets;
	private ArrayList<PrintWriter> outs;
	private ArrayList<BufferedReader> ins;
	private int clientNumber;
    private int clientDice, serverDice;

	public RunnableServer(ArrayList<Socket> clientSockets, Socket clientSocket, ServerSocket serverSocket,
			ArrayList<PrintWriter> outs, ArrayList<BufferedReader> ins, int clientNumber) {
		this.clientSocket = clientSocket;
		this.serverSocket = serverSocket;
		this.clientSockets = clientSockets;
		this.outs = outs;
		this.ins = ins;
		this.clientNumber = clientNumber;
	}

	@Override
	public void run() {
		try {
			this.clientSockets.add(clientSocket);

			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.outs.add(out);
			this.ins.add(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String lineFromClient = "";

		while (true) {
			try {
				lineFromClient = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if ("c".contentEquals(lineFromClient)) {
				for (PrintWriter out : this.outs) {
					out.println("Look! The player from " + this.clientSocket.getInetAddress()
							+ " is cheating for a free win! That's too bad!");
				}
			} else if ("e".contentEquals(lineFromClient)) {
				break;
			} else if ("d".contentEquals(lineFromClient)){
				int result = play(lineFromClient);

				if (result == 0) {
					out.println("You rolled a " + this.clientDice + " and the server rolled a " + this.serverDice);
					out.println("It's a tie!");
				} else if (result == 1) {
					out.println("You rolled a " + this.clientDice + " and the server rolled a " + this.serverDice);
					out.println("You win!");
				} else if (result == -1) {
					out.println("You rolled a " + this.clientDice + " and the server rolled a " + this.serverDice);
					out.println("You lose!");
				}
			} else if ("r".contentEquals(lineFromClient) || "p".contentEquals(lineFromClient) || "s".contentEquals(lineFromClient)){
				int RPSresult = playRPS(lineFromClient);

				if (RPSresult == 0) {
					out.println("It's a tie");
				} else if (RPSresult == 1) {
					out.println("You win!");
				} else if (RPSresult == -1) {
					out.println("You lose!");
				}
			// incentive
			} else if ("D".contentEquals(lineFromClient)){
				int sDice, cDice;
				Random randomC = new Random();
				Random randomS = new Random();
				sDice = randomS.nextInt(6) + 1;
				cDice = randomC.nextInt(6) + 1;
				
				if (sDice < cDice){
					out.println("You rolled a " + cDice + " and the server rolled a " + sDice);
					out.println("You win!");
				} else {
					out.println("You rolled a " + cDice + " and the server rolled a " + cDice);
					out.println("It's a tie!");
				}
			} else {
				out.println("Dice game prompt> ");
				out.println("Roll a dice = d");
				out.println("Rock Paper Scissors game prompt> ");
				out.println("Rock = r, paper = p, scissors = s");
				out.println("Other prompt> ");
				out.println("exit = e, cheat = c");
				out.println("Now enter your choice and let\'s play! >>>");
			}

		}
	}

	public int play(String line) {

        Random randomC = new Random();
		Random randomS = new Random();
		serverDice = randomS.nextInt(6) + 1;
        clientDice = randomC.nextInt(6) + 1;
        int res = -2;

        if (serverDice - clientDice == 0){
            res = 0;
        } else if (serverDice - clientDice < 0){
            res = 1; //client wins
        } else 
            res = -1;

		return res;
	}

	public int playRPS(String clientRes) {
		String serverRes = "";
		Random random = new Random();
		int first = random.nextInt(3);

		if (first == 0) {
			serverRes = "r";
		} else if (first == 1) {
			serverRes = "p";
		} else if (first == 2) {
			serverRes = "s";
		}

		if (serverRes.equals(clientRes)) {
			return 0;
		} else if (clientRes.equals("r")) {
			if (serverRes.equals("p")) {
				return 1;
			} else if (serverRes.equals("p")) {
				return -1;
			}
		} else if (clientRes.equals("p")) {
			if (serverRes.equals("r")) {
				return 1;
			} else if (serverRes.equals("s")) {
				return -1;
			}
		} else if (clientRes.equals("s")) {
			if (serverRes.equals("r")) {
				return -1;
			} else if (serverRes.equals("p")) {
				return 1;
			}
		}

		return -2;
	}

}
