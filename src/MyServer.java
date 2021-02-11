import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MyServer extends JFrame implements ActionListener, Runnable{
	
	private static final long serialVersionUID = 1L;
	
	private JLabel inputFieldLabel = new JLabel("     Wiadomoœæ:  ");
	private JTextField inputField = new JTextField(20);
	private JButton inputFieldButton = new JButton("Wyœlij");
	
	private JLabel dialogAreaLabel = new JLabel("    Dialog:");
	private JTextArea  dialogArea    = new JTextArea(15, 5);
	
	static List <Integer> activeClients = Collections.synchronizedList(new ArrayList<Integer>());
	
	static Map <Integer, ClientHandling> clientHandlings = new ConcurrentHashMap <Integer, ClientHandling>();
	
	int putedClientId;
	
	boolean doClose = false;

	MyServer(){
		
		super("Ksi¹¿ka telefoniczna - serwer");
		
 		setJFrameParameters();
 		
 		JPanel panel = new JPanel();
 		
 		panel.setLayout(new GridBagLayout());
 		
 		setTextAreaParameters();
 		
 		addToPanel(panel);
 		
 		setContentPane(panel);
		
		setVisible(true);
		
		new Thread(this).start();
	}

	public static void main(String[] args) {

		new MyServer();
	}
	
	private void setTextAreaParameters() {
		
		dialogArea.setEditable(false);
		dialogArea.setLineWrap(true);
		dialogArea.setWrapStyleWord(true);
		
		inputFieldButton.addActionListener(this);
	}
	
	private void setJFrameParameters() {
		
		setSize(380, 500);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent event) {
				
				doClose = true;
			}
			
			@Override
			public void windowClosed(WindowEvent event) {
				
				windowClosing(event);
			}
			
		});
	}
	
	private void addToPanel(JPanel panel) {
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		
		//
		
		c.gridx = 0;
		c.gridy = 2;
		
		panel.add(inputFieldLabel, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 2;
		
		panel.add(inputField, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 3;
		
		c.insets.top = 10;
		
		panel.add(inputFieldButton, c);
		
		//
		
		c.gridx = 0;
		c.gridy = 4;
		
		c.insets.top = 25;
		c.insets.left = 0;
		
		panel.add(dialogAreaLabel, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 4;
		
		c.gridwidth = 2;
		
		c.insets.top = 20;
		
		c.ipady = 60;
		
		panel.add(dialogArea, c);
		
		panel.add(new JScrollPane(dialogArea), c);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		Object source = event.getSource();
		
		if(source == inputFieldButton) {
	
			
		}
	}
	
	synchronized public void writeReceivedMessage(ClientHandling client, String message){
		
		try {
			
			dialogArea.setText(dialogArea.getText() + client.returnId() + ": " + message + "\n");
		}
		catch(Exception e) {
			
			
		}
	}
	
	public void writeToDatabase(ClientStore client, String url, String user) {
		
		Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "INSERT INTO klienci (login, haslo, dane)  VALUES (?, ?, ?)";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, client.login);
			statement.setString(2, client.password);
			statement.setString(3, ClientStore.returnData(client));
			
			statement.executeUpdate();

	    }
	    catch(SQLException e) {
	    	
	    	System.out.println("SQL Exception threw");
	    	e.printStackTrace();
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
    }
	
	public boolean addMessageToDatabase(int idFrom, int idTo, long date, String message, String url, String user) {
		
		Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "INSERT INTO czat (id1, id2, data, wiadomosc)  VALUES (?, ?, ?, ?)";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, idFrom);
			statement.setInt(2, idTo);
			statement.setLong(3, date);
			statement.setString(4, message);
			
			statement.executeUpdate();
			
			return true;

	    }
	    catch(SQLException e) {
	    	
	    	System.out.println("SQL Exception threw");
	    	e.printStackTrace();
	    	
	    	return false;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
	}
	
	public ArrayList <String> readFromDatabaseAboutLogin(String login, String url, String user) {
    	
		Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "SELECT login FROM klienci WHERE login REGEXP ?";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, login);
			
			ResultSet result = null;
				
			result = statement.executeQuery();
			
			ArrayList <String> fitLogins = new ArrayList <String> ();

			while(result.next()) {
				
				fitLogins.add(result.getString(1));
			}
			
			if(fitLogins.size() > 0)
				return fitLogins;
			
			return null;
	    }
		catch(Exception e) {

	    	e.printStackTrace();
	    	
	    	return null;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
    }
    
    public ClientStore readFromDatabase(int id, String url, String user) {
    	
    	Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "SELECT dane FROM klienci WHERE id = " + id;
			
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			
			if(result.next()) {
				
				ClientStore clientStore = ClientStore.importData(result.getString(1));
				
				return clientStore;
			}
			
			return null;

	    }
	    catch(SQLException | ClassNotFoundException e) {
	    	
	    	e.printStackTrace();
	    	
	    	return null;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
    }
    
	public Integer readIdFromDatabase(String login, String url, String user) {
    	
    	Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "SELECT id FROM klienci WHERE login = ?";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, login);
			
			ResultSet result = null;
				
			result = statement.executeQuery();
			
			if(result.next()) {
				
				return result.getInt("id");
			}
			
			return null;

	    }
	    catch(SQLException e) {
	    	
	    	e.printStackTrace();
	    	
	    	return null;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
    }
	
	public ArrayList <String> loadChat(int id, int id1, String url, String user) {
		
		Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
    		
			String query1 = "SELECT login FROM klienci WHERE id = ? OR id = ?";
			
			PreparedStatement statement1 = connection.prepareStatement(query1);
			statement1.setInt(1, id);
			statement1.setInt(2, id1);
			
			ResultSet result1 = null;
				
			result1 = statement1.executeQuery();
			
			ArrayList <String> logins = new ArrayList <String>();
			
			while(result1.next()) {
				
				logins.add(result1.getString(1));
			}
			
			if(logins.size() == 0)
				return null;
			
			String query = "SELECT id1, id2, wiadomosc FROM `czat` WHERE (id1 = ? AND id2 = ?) OR (id1 = ? AND id2 = ?) ORDER BY data";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			statement.setInt(2, id1);
			statement.setInt(3, id1);
			statement.setInt(4, id);
			
			ResultSet result = null;
				
			result = statement.executeQuery();
			
			ArrayList <String> chat = new ArrayList <String>();
			
			while(result.next()) {
				
				if(result.getInt(1) == id)
					chat.add(logins.get(0) + "#" + result.getString(3));
				else
					chat.add(logins.get(1) + "#" + result.getString(3));
			}
			
			if(chat.size() == 0)
				return null;
			
			return chat;
	    }
		catch(Exception e) {

	    	e.printStackTrace();
	    	
	    	return null;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
	}
    
    public ClientStore checkPass(String login, String password, String url, String user) {
		
    	Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "SELECT id, dane FROM klienci WHERE login = ? AND haslo = ?";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, login);
			statement.setString(2, password);
			
			ResultSet result = null;
				
			result = statement.executeQuery();
			
			if(result.next()) {
				
				ClientStore clientStore = ClientStore.importData(result.getString(2));
				
				clientStore.id = result.getInt(1);
				
				putedClientId = clientStore.id;
				
				return clientStore;
			}
			
			return null;
	    }
		catch(Exception e) {

	    	e.printStackTrace();
	    	
	    	return null;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
	}
    
    public ClientStore checkRegister(String login, String password, String url, String user) {
		
    	Connection connection = null;
    	
    	try {
    		
    		connection = DriverManager.getConnection(url, user, null);
			
			String query = "SELECT dane FROM klienci WHERE login = ?";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, login);
			
			ResultSet result = null;
				
			result = statement.executeQuery();
			
			if(!result.next()) {
				
				ClientStore clientStore = new ClientStore(login, password);
				
				writeToDatabase(clientStore, url, user);
				
				//
				
				String query1 = "SELECT id FROM klienci WHERE login = ?";
				
				PreparedStatement statement1 = connection.prepareStatement(query1);
				statement1.setString(1, login);
				
				ResultSet result1 = null;
				
				result1 = statement1.executeQuery();
				
				if(result1.next()) {
				
					clientStore.id = result1.getInt(1);
				
					putedClientId = clientStore.id;
				}
				
				return clientStore;
			}
			
			return null;
	    }
		catch(Exception e) {

	    	e.printStackTrace();
	    	
	    	return null;
	    }
	    finally {
	    	
            if (connection != null) {
            	
                try {
                	connection.close();
                    
                } 
                catch (SQLException ex) {
                	
                    ex.printStackTrace();
                }
            }
	    }
	}
    
	@Override
	public void run() {
		
		ServerSocket serverSocket = null;
		
		try {
			
			serverSocket = new ServerSocket(12312);
			
			//String host = InetAddress.getLocalHost().getHostName();
			
			while(!doClose) {
				
				Socket socket = serverSocket.accept();
				
				if(socket != null) {
					
					new ClientHandling(this, socket);
				}
			}
			
			serverSocket.close();
			
			dispose();
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
	}

}

class ClientHandling implements Runnable{
	
	private MyServer server;
	private Socket socket;
	
	String url = "jdbc:mysql://localhost:3306/czat";
    String user = "root";
	
	private ObjectOutputStream output = null;
	
	int id;
	int toId;
	String name;
	
	ClientHandling(MyServer server, Socket socket){
		
		this.server = server;
		this.socket = socket;
		
		new Thread (this).start();
	}
	
	public int returnId() {
		
		return id;
	}
	
	public void sendMessage(String message) {
		
		try {
			
			output.writeObject("Serwer: #" + message);
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
	}
	
	synchronized public void receivedMessageLogic(String message) {
		
		String toCheck[] = message.split("#");
		
		switch(toCheck[0]) {
		
			case "login":
				
				ClientStore client = server.checkPass(toCheck[1], toCheck[2], url, user);
	
				if(client != null) {
					
					id = server.putedClientId;
					
					MyServer.activeClients.add(id);
					
					MyServer.clientHandlings.put(id, this);
					
					sendMessage("logged#" + ClientStore.returnData(client));
				}
				else {
					
					sendMessage("notlogged#");
				}
				
			break;
			
			case "register":
				
				ClientStore client1 = server.checkRegister(toCheck[1], toCheck[2], url, user);
				
				if(client1 != null) {
					
					id = server.putedClientId;
					
					MyServer.activeClients.add(id);
					
					MyServer.clientHandlings.put(id, this);
					
					sendMessage("registered#" + ClientStore.returnData(client1));
				}
				else {
					
					sendMessage("notregistered#");
				}
				
			break;
			
			case "searchLogins":
				
				ArrayList <String> foundLogins = server.readFromDatabaseAboutLogin(toCheck[1], url, user);
				
				String toSend = "foundLogins";
				
				if(foundLogins != null) {
					
					for(String subString : foundLogins) {
						
						toSend += "#" + subString;
					}
				}
				else {
					
					toSend += "#";
				}
				
				sendMessage(toSend);
				
			break;
			
			case "message":
				
				
				if(server.addMessageToDatabase(Integer.parseInt(toCheck[1]), Integer.parseInt(toCheck[2]), 
						Long.parseLong(toCheck[3]), toCheck[4], url, user)) {
					
					sendMessage("messageSent#" + server.readFromDatabase(Integer.parseInt(toCheck[1]), url, user).login + "#" + 
								Integer.parseInt(toCheck[2]) + "#" + Long.parseLong(toCheck[3]) + "#" + toCheck[4]);
					
					if(MyServer.activeClients.contains(Integer.parseInt(toCheck[2]))) {
						
						String messageToSend1 = "mustBeUpdated#" + 
				 			     server.readFromDatabase(Integer.parseInt(toCheck[1]), url, user).login + "#" + 
				 			     Long.parseLong(toCheck[3]) + "#" + toCheck[4];
		
						MyServer.clientHandlings.get(Integer.parseInt(toCheck[2])).sendMessage(messageToSend1);
					}
				}
				
			break;
			
			case "getIdAboutLogin":
				
				Integer id = server.readIdFromDatabase(toCheck[1], url, user);
				
				if(id != null) {
					
					sendMessage("IdAboutLogin#" + id.intValue());
				}
				
				
			break;
			
			case "loadChat":
				
				ArrayList <String> chat = server.loadChat(Integer.parseInt(toCheck[1]), Integer.parseInt(toCheck[2]), url, user);
				
				if(chat != null) {
					
					String toSend1 = "loadedChat";
					
					for(String string : chat)
						toSend1 += "#" + string;
					
					sendMessage(toSend1);
				}
				
			break;
			
			case "logout":
				
				MyServer.activeClients.remove(Integer.parseInt(toCheck[1]));
				
			break;
		}
	}

	@Override
	public void run() {
		
		try {
			
			String message = "hello";
			
			ObjectOutputStream testOutput = new ObjectOutputStream(socket.getOutputStream());
			
			output = testOutput;
			
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			
			message = (String)input.readObject();
			
			name = message;
			
			while(true) {
				
				message = (String) input.readObject();
				
				receivedMessageLogic(message);
			}
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
		}
	}
}
