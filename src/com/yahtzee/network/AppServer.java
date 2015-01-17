package com.yahtzee.network;

import java.io.IOException;
import java.net.*;

import com.yahtzee.utils.Config;

public class AppServer implements Runnable {
	
	int clientCount = 0;
	private Thread thread = null;
	private ServerSocket server = null;
	private ServerThread clients[] = new ServerThread[Config.MAX_CLIENTS];
	
	public AppServer(int port) {
		try {
			System.out.println("Binding to port " + port + ", please wait ...");
			server = new ServerSocket(port);
			server.setReuseAddress(true);
			start();
		} catch(IOException e) {
			
		}
	}

	public void run() {
		while(thread != null) {
			try {
				System.out.println("Waiting for a client ...");
				addThread(server.accept());
			} catch(IOException e) {
				System.out.println("Server: Accepting Client Error");
//				Trace.exception(e);
			}
		}
	}
	
	public synchronized void handle(int ID, String input) {
		if(input == null)
			return;
		
		if(input.equals("quit")) {
			System.out.println("Removing Client: " + ID);
			int pos = findClient(ID);
			if(pos != -1) {
				clients[pos].send("quit" + "\n");
				remove(ID);
			}
		} else {
			for(int i = 0; i < clientCount; i++) {
				System.out.printf("Server: %6d Sending From: %6d Sending To: %6d\n",
					this.server.getLocalPort(), ID, clients[i].getID());
				clients[i].send(ID + ": " + input + "\n");
			}
		}
	}
	
	public synchronized void remove(int ID) {
		int pos = findClient(ID);
		if(pos >= 0) {
			ServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + ID + " at " + pos);
			if(pos < clientCount - 1) {
				for(int i = pos + 1; i < clientCount; i++) {
					clients[i - 1] = clients[i];
				}
			}
		}
	}
	
	public void start() {
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
			System.out.println("Server started: " + server + ": " + thread.getId());
		}
	}
	
	public void stop() {
		try {
			if(thread != null) {
				thread.join();
				thread = null;
			}
		} catch (InterruptedException e) {
//			Trace.exception(e);
		}
	}
	
	private int findClient(int ID) {
		for(int i = 0; i < clientCount; i++) {
			if(clients[i].getID() == ID) {
				return i;
			}
		}
		return -1;
	}
	
	private void addThread(Socket socket) {
		if (clientCount < clients.length) {
			System.out.println("Client accepted" + socket + "\n");
			clients[clientCount] = new ServerThread(this, socket);
			
			try {
				clients[clientCount].open();
				clients[clientCount].start();
				clientCount++;
			} catch (IOException e) {
				System.out.println("Error opening thread: ");
//				Trace.exception(e);
			}
		} else {
			System.out.println("Client refused maximum " + clients.length + " reached.");
		}
	}
	
	public static void main(String args[]) {
		AppServer server = new AppServer(Config.DEFAULT_PORT);
	}

}