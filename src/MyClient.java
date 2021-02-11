import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

enum ContactOptions{
	
	NewContacts, ActualContacts;
	
	String newContactsTab [] = {};
	String actualContactsTab [] = {};
	
	@Override
	public String toString() {
		
		switch(this) {
		
			case NewContacts:
				
				return "Wyszukiwanie";
				
			case ActualContacts:
				
				return "Kontakty";
				
			default:
				
				return "";
		}
	}
	
	public String [] getTab() {
		
		switch(this) {
		
			case NewContacts:
				
				return newContactsTab;
				
			case ActualContacts:
				
				return actualContactsTab;
			
			default:
				
				return null;
		}
	}
	
	public void setTab(Object [] tab) {
		
		switch(this) {
		
			case NewContacts:
				
				newContactsTab = (String []) tab;
				
			case ActualContacts:
				
				actualContactsTab = (String []) tab;
			
			default:
			
		}
	}
}

public class MyClient extends JFrame implements ActionListener, Runnable{
	
	private static final long serialVersionUID = 1L;
    
    ClientStore clientStore = new ClientStore();
    
    //right panel
    String inputFieldPlaceholder = "Wpisz wiadomoœæ i naciœnij przycisk \"Wyœlij\"";
	private JTextArea inputField = new JTextArea(2, 24);
	
	private JButton inputFieldButton = new JButton("Wyœlij");
	
	private JLabel dialogAreaLabel = new JLabel("Czat");
	private JTextArea dialogArea    = new JTextArea(12, 26);
	
	//left panel
	
	private JComboBox <ContactOptions> contactsSelector = new JComboBox <ContactOptions> (ContactOptions.values());
	
	String searchFieldPlaceholder = "Wyszukaj";
	private JTextArea searchField = new JTextArea(1, 1);
	
	DefaultListModel <String> contactsListModel = new DefaultListModel<>();
	JList <String> contactsList = new JList <String>(contactsListModel);
	
	JMenuBar menuBar = new JMenuBar();
	
	JMenu menu = new JMenu("Menu");
	
	JMenuItem logoutItem = new JMenuItem("Wyloguj");
	
	String name;
	
	Socket socket;
	
	static ArrayList <String> messageToUpdate = new ArrayList <String>();
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	boolean doClose = false;
	static boolean doCloseAll = false;

	public MyClient(String name) {
	
		super("Ksi¹¿ka telefoniczna - aplikacja klienta");
		
		this.name = name;
		
		setJFrameParameters();
		
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		sp.setOneTouchExpandable(false);
		sp.setDividerSize(0);
		
		setMenu();
		
		setLeftPanelParameters(leftPanel);
		setRightPanelParameters(rightPanel);
		
		setMenu();
		
		setContentPane(sp);
		
		new Thread(this).start();
	}
	
	public void close() {
		
		doClose = true;
	}
	
	public static void closeAll() {
		
		doCloseAll = true;
	}
	
	public static void main(String[] args) {
		
		new MyClient("Adam");
	}
	
	public String getName() {
		
		return name;
	}
	
	private void setRightPanelParameters(JPanel rightPanel) {
		
		rightPanel.setLayout(new GridBagLayout());
		
		setTextAreaParameters();
		
		addRightPanel(rightPanel);
	}
	
	private void setLeftPanelParameters(JPanel leftPanel) {
		
		leftPanel.setLayout(new GridBagLayout());
		
		setSearchFieldParameters();
		
		setContactsParameters();
		
		addLeftPanel(leftPanel);
	}
	
	private void setSearchFieldParameters() {
		
		searchField.setFocusable(true);
		
		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				
				if(!searchField.getText().equals(searchFieldPlaceholder) && !searchField.getText().isBlank()) {
					
					sendMessage("searchLogins#" + searchField.getText());
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				
				if(!searchField.getText().equals(searchFieldPlaceholder) && !searchField.getText().isBlank()) {
					
					sendMessage("searchLogins#" + searchField.getText());
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				
				if(!searchField.getText().equals(searchFieldPlaceholder) && !searchField.getText().isBlank()) {
					
					sendMessage("searchLogins#" + searchField.getText());
				}
			}
		});
		
		searchField.setText(searchFieldPlaceholder);
		
		searchField.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				
				if(searchField.getText().isBlank()) {
					
					searchField.setText(searchFieldPlaceholder);
				}
				else if(searchField.getText().equals(searchFieldPlaceholder)) {
					
					searchField.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				
				if(searchField.getText().isBlank())
					searchField.setText(searchFieldPlaceholder);
				
			}
		});
	}
	
	private void setTextAreaParameters() {

		dialogArea.setEditable(false);
		dialogArea.setLineWrap(true);
		dialogArea.setWrapStyleWord(true);
		
		inputFieldButton.addActionListener(this);
		
		inputField.setText(inputFieldPlaceholder);
		
		inputField.setFocusable(true);
		inputField.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				
				if(inputField.getText().isBlank()) {
					
					inputField.setText(inputFieldPlaceholder);
				}
				else if(inputField.getText().equals(inputFieldPlaceholder)) {
					
					inputField.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				
				if(inputField.getText().isBlank())
					inputField.setText(inputFieldPlaceholder);
				
			}
		});
	}
	
	private void setMenu() {
		
		menu.add(logoutItem);
		
		menuBar.add(menu);
		
		setJMenuBar(menuBar);
		
		logoutItem.addActionListener(this);
	}
	
	private void setContactsParameters() {
		
		contactsList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		
		contactsList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent event) {
				
				 if (!event.getValueIsAdjusting() && contactsList.getSelectedValue() != null) {
					 
					 dialogAreaLabel.setText("Czat z " + contactsList.getSelectedValue());
					 
					 dialogArea.setText("");
					 
					 sendMessage("getIdAboutLogin#" + contactsList.getSelectedValue());
		         }
			}
		});
		
		contactsSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				contactsListModel.removeAllElements();
				
				String tab[] = ((ContactOptions) contactsSelector.getSelectedItem()).getTab();
				
				for(int i=0; i < tab.length; i++)
					contactsListModel.add(i, tab[i]);
				
			}
		});
		
		contactsSelector.setSelectedItem(ContactOptions.ActualContacts);
	}
	
	private void setJFrameParameters() {
	
		setSize(540, 490);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent event) {
				
				doClose = true;
				
				sendMessage("logout#" + clientStore.id);
			}
			
			@Override
			public void windowClosed(WindowEvent event) {
				
				windowClosing(event);
			}
			
		});
	}
	
	private void addLeftPanel(JPanel panel) {
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		
		//
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets.left = 16;
		c.insets.top = 22;
		
		panel.add(contactsSelector, c);
		
		//
		
		c.gridx = 0;
		c.gridy = 1;
		c.insets.top = 17;
		
		panel.add(searchField, c);
		panel.add(new JScrollPane(searchField), c);
		
		//
		
		c.gridx = 0;
		c.gridy = 2;
		c.insets.top = 17;
		c.ipady = 146;
		
		panel.add(contactsList, c);
		panel.add(new JScrollPane(contactsList), c);
		
		c.weighty = 1;
		
		panel.add(new JLabel(" "), c);
	}
	
	private void addRightPanel(JPanel panel) {
	
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets.left = 8;
		
		c.insets.top = 8;
		
		panel.add(dialogAreaLabel, c);
		
		//
		
		c.gridx = 0;
		c.gridy = 1;
		c.insets.left = 8;
		
		c.gridwidth = 2;
		
		c.insets.top = 20;
		
		c.ipady = 100;
		
		panel.add(dialogArea, c);
		
		panel.add(new JScrollPane(dialogArea), c);
		
		//
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		
		c.ipady = 1;
		c.insets.top = 12;
		
		panel.add(inputField, c);
		panel.add(new JScrollPane(inputField), c);
		
		//
		
		c.gridx = 1;
		c.gridy = 3;
		
		c.insets.top = 12;
		c.ipady = 9;
		
		panel.add(inputFieldButton, c);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		Object source = event.getSource();
		
		if(source == inputFieldButton) {
			
			String dialog = inputField.getText();
			
			if(dialog.isEmpty() || dialog.equals(inputFieldPlaceholder) || clientStore.selectedUserId == clientStore.id)
				return;
			
			sendMessage("message#" + clientStore.id + "#" + clientStore.selectedUserId + "#" + new Date().getTime() + "#" + 
						dialog + "\n");
			
			repaint();
		}
		else if(source == logoutItem) {
			
			doClose = true;
			
			sendMessage("logout#" + clientStore.id);
			
			dispose();
		}
	}
	
	synchronized public void writeReceivedMessage(String message){
		
		try {
			
			dialogArea.setText(dialogArea.getText() + message + "\n");
		}
		catch(Exception e) {
			
			
		}
	}
	
	synchronized public void writeSentMessage(String message){
		
		try {
			
			dialogArea.setText(dialogArea.getText() + name + ": " + message);
		}
		catch(Exception e) {
			
			
		}
	}
	
	public void putText(String message) {
		
		dialogArea.setText(dialogArea.getText() + message);
	}
	
	public String toString() {
		
		return name;
	}
	
	public void sendMessage(String message) {
		
		try {
			
			output.writeObject(message);
		}
		catch(IOException e) {
			
			
		}
	}
	
	public void receivedMessageLogic(String message) throws Exception{
		
		String toCheck[] = message.split("#");
		
		switch(toCheck[1]) {
		
			case "logged":
	
				clientStore = ClientStore.importData(toCheck[2]);
				
				clientStore.selectedUserId = clientStore.id;
				
				JOptionPane.showMessageDialog(this, "Zalogowano siê pomyœlnie", "Informacja", JOptionPane.INFORMATION_MESSAGE);

				this.setTitle("U¿ytkownik " + clientStore.login);
				
				setVisible(true);
				
			break;
			
			case "registered":
				
				clientStore = ClientStore.importData(toCheck[2]);
				
				clientStore.selectedUserId = clientStore.id;
				
				JOptionPane.showMessageDialog(this, "Zarejestrowano siê pomyœlnie", "Informacja", JOptionPane.INFORMATION_MESSAGE);
				
				this.setTitle("U¿ytkownik " + clientStore.login);
				
				setVisible(true);
				
			break;
			
			case "notlogged":
				
				JOptionPane.showMessageDialog(this, "Podane login lub has³o s¹ niepoprawne", "B³¹d", JOptionPane.ERROR_MESSAGE);
				
				String tab[] = LoginDialog.makeLoginDialog(this);
		  		
		  		if(tab != null) {
		  			
		  			String toPut = "";
					
					for(String string : tab) {
						
						toPut += string;
					}
					
					sendMessage(toPut);
		  		}
		  		
		  	break;
		  		
			case "notregistered":
				
				JOptionPane.showMessageDialog(this, "Konto o podanym loginie ju¿ istnieje", "B³¹d", JOptionPane.ERROR_MESSAGE);
				
				String tab1[] = RegisterDialog.makeRegisterDialog(this);
		  		
		  		if(tab1 != null) {
		  			
		  			String toPut = "";
					
					for(String string : tab1) {
						
						toPut += string;
					}
					
					sendMessage(toPut);
		  		}
		  		
		  	break;
		  	
			case "foundLogins":
				
				ArrayList <String> searchedLogins = new ArrayList <String>();
				
				for(int i=2; i < toCheck.length; i++) {
					
					searchedLogins.add(toCheck[i]);
				}
				
				contactsListModel.removeAllElements();
				
				for(int i=0; i < searchedLogins.size(); i++)
					contactsListModel.add(i, searchedLogins.get(i));
				
				
				((ContactOptions) contactsSelector.getSelectedItem()).setTab(searchedLogins.toArray(new String[searchedLogins.size()]));
				
			break;
			
			case "messageSent":
				
				if(clientStore.selectedUserId == Integer.parseInt(toCheck[3]))
					dialogArea.setText(dialogArea.getText() + clientStore.login + ": " + toCheck[5]);
				
			break;
			
			case "IdAboutLogin":
				
				clientStore.selectedUserId = Integer.parseInt(toCheck[2]);
				
				sendMessage("loadChat#" + clientStore.id + "#" + clientStore.selectedUserId);
				
			break;
			
			case "loadedChat":
				
				for(int i=2; i < toCheck.length; i++) {
					
					if(i % 2 == 0)
						dialogArea.setText(dialogArea.getText() + toCheck[i] + ": ");
					else
						dialogArea.setText(dialogArea.getText() + toCheck[i]);
				}
				
			break;
			
			case "mustBeUpdated":
				
				dialogArea.setText(dialogArea.getText() + toCheck[2] + ": " + toCheck[4]);
			
			break;
		}
	}

	@Override
	public void run() {
		
		try {
			
			String message = "halo";
			
			socket = new Socket("localhost", 12312);
			
			input = new ObjectInputStream(socket.getInputStream());
			
			ObjectOutputStream testOutput = new ObjectOutputStream(socket.getOutputStream());
			
			output = testOutput;
	  		
	  		sendMessage(name);
	  		
	  		//
	  		
	  		String tab[] = LoginDialog.makeLoginDialog(this);
	  		
	  		if(tab != null) {
	  			
	  			String toPut = "";
				
				for(String string : tab) {
					
					toPut += string;
				}
				
				sendMessage(toPut);
	  		}
	  		
	  		while(!doClose) {
	  			
	  			try {
	  				message = (String) input.readObject();
	  			}
	  			catch(Exception e) {
	  				
	  				break;
	  			}
	  			
	  			receivedMessageLogic(message);
	  		}
	  		
			output.close();
			input.close();
			
			socket.close();

		 	dispose();
		}
		catch(Exception e) {

			e.printStackTrace();

		 	dispose();
		}
		
	}
}

class ClientStore implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	int id;
	String login;
	String password;
	String firstname;
	String surname;
	float age;
	
	int selectedUserId;
	
	boolean mustUpdate = false;
	
	ClientStore(String login, String password){
		
		this.login = login;
		this.password = password;
		this.firstname = null;
		this.surname = null;
		this.age = 20;
	}
	
	ClientStore(){
		
		login = "Ala1";
		password = "Ala123";
		this.firstname = "Adam";
		this.surname = "Nowak";
		this.age = 20;
	}
	
	public String toString() {
		
		return firstname + surname + age;
	}
	
	public static ClientStore importData(String source) throws ClassNotFoundException {
		
		try {
		
			byte [] data = Base64.getDecoder().decode(source);
			ObjectInputStream output = new ObjectInputStream(new ByteArrayInputStream(data));
	        
			Object obj  = output.readObject();
			output.close();
	        
			return (ClientStore) obj;
		}
		catch(IOException | ClassNotFoundException e) {
			
			return null;
		}
	}
	
	public static String returnData(ClientStore clientStore) {
		
		try {
		
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        
	        oos.writeObject(clientStore);
	        oos.close();
	        
	        return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
		catch(IOException e) {
			
			return "";
		}
	}
}

class LoginDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	Window parent;
	
	Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10);
	
	JLabel loginLabel = new JLabel("Login:  ");
	JTextField loginTextField = new JTextField(10);
	
	JLabel passwordLabel = new JLabel("Has³o:  ");
	JPasswordField passwordTextField = new JPasswordField(10);
	
	JButton acceptButton = new JButton("Zaloguj");
	JButton registerButton = new JButton("Zarejestruj siê");
	
	String message, login, password;
	
	LoginDialog(Window parent){
		
		super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
		
		this.parent = parent;
		message = "login#";
		
		setJFrameParameters(parent);
		
		setJButtonsParameters();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		
		addToPanel(panel);
		setContentPane(panel);
		
		setVisible(true);
	}
	
	private void setJFrameParameters(Window parent) {
		
		setTitle("Logowanie");
		setSize(240, 220);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
	}
	
	private void setJButtonsParameters(){
		
		acceptButton.addActionListener(this);
		registerButton.addActionListener(this);
	}
	
	private void addToPanel(JPanel panel) {
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//login
		
		c.gridx = 0;
		c.gridy = 1;
		c.insets.bottom = 12;
		
		panel.add(loginLabel, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 1;
		
		panel.add(loginTextField, c);
		
		//password
		
		c.gridx = 0;
		c.gridy = 2;
		
		panel.add(passwordLabel, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 2;
		
		panel.add(passwordTextField, c);
		
		//acceptButton
		
		c.gridx = 1;
		c.gridy = 3;
		
		panel.add(acceptButton, c);
		
		c.gridx = 1;
		c.gridy = 4;
		
		panel.add(registerButton, c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object eventSource = e.getSource();
		
		try {
			
			if(eventSource == acceptButton) {
				
				login = loginTextField.getText();
				password = String.valueOf(passwordTextField.getPassword());
				
				if(login.isBlank() || password.isBlank())
					JOptionPane.showMessageDialog(this, "Proszê uzupe³niæ dane", "Brak danych", JOptionPane.ERROR_MESSAGE);
				else
					dispose();
			}
			else if(eventSource == registerButton) {
				
				String [] tab = RegisterDialog.makeRegisterDialog(parent);
				
				if(tab == null)
					return;
				
				message = tab[0];
				login = tab[1];
				password = tab[2];
				
				dispose();
			}
		}
		catch(Exception exception) {
			
			exception.printStackTrace();
		}
	}
	
	public static String [] makeLoginDialog(Window parent) throws Exception{
		
		String [] tab = new String[3];
		
		LoginDialog loginDialog = new LoginDialog(parent);
		
		tab[0] = loginDialog.message;
		tab[1] = loginDialog.login;
		tab[2] = loginDialog.password;
		
		if(tab[1] == null || tab[2] == null)
			return null;
		
		if(tab[1].charAt(tab[1].length()-1) != '#')
			tab[1] += "#";
		
		return tab;
	}
}

class RegisterDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10);
	
	JLabel loginLabel = new JLabel("Login:  ");
	JTextField loginTextField = new JTextField(10);
	
	JLabel passwordLabel = new JLabel("Has³o:  ");
	JPasswordField passwordTextField = new JPasswordField(10);
	
	JButton registerButton = new JButton("Zarejestruj siê");
	
	String login, password;
	
	RegisterDialog(Window parent){
		
		super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
		
		setJFrameParameters(parent);
		
		setJButtonsParameters();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		
		addToPanel(panel);
		setContentPane(panel);
		
		setVisible(true);
	}
	
	private void setJFrameParameters(Window parent) {
		
		setTitle("Rejestracja");
		setSize(240, 220);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
	}
	
	private void setJButtonsParameters(){
		registerButton.addActionListener(this);
	}
	
	private void addToPanel(JPanel panel) {
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//login
		
		c.gridx = 0;
		c.gridy = 1;
		c.insets.bottom = 12;
		
		panel.add(loginLabel, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 1;
		
		panel.add(loginTextField, c);
		
		//password
		
		c.gridx = 0;
		c.gridy = 2;
		
		panel.add(passwordLabel, c);
		
		//
		
		c.gridx = 1;
		c.gridy = 2;
		
		panel.add(passwordTextField, c);
		
		//registerButton
		
		c.gridx = 1;
		c.gridy = 3;
		
		panel.add(registerButton, c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object eventSource = e.getSource();
		
		try {
			
			if(eventSource == registerButton) {
				
				login = loginTextField.getText();
				password = String.valueOf(passwordTextField.getPassword());
				
				if(login.isBlank() || password.isBlank())
					JOptionPane.showMessageDialog(this, "Proszê uzupe³niæ dane", "Brak danych", JOptionPane.ERROR_MESSAGE);
				else
					dispose();
			}
		}
		catch(Exception exception) {
			
			exception.printStackTrace();
		}
	}
	
	public static String [] makeRegisterDialog(Window parent) throws Exception{
		
		String [] tab = new String[3];
		
		RegisterDialog registerDialog = new RegisterDialog(parent);
		
		tab[0] = "register#";
		tab[1] = registerDialog.login + "#";
		tab[2] = registerDialog.password;
		
		if(tab[1] == null || tab[2] == null)
			return null;
		
		return tab;
	}
}



