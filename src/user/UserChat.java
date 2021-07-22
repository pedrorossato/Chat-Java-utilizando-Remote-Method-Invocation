package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.ArrayList;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import room.IRoomChat;
import server.IServerChat;

/** Classe que representa um usuário do chat.
* @author Pedro Henrique Vestena Rossato
* @version 1.0
*/
public class UserChat extends UnicastRemoteObject implements IUserChat {
	
	private static final long serialVersionUID = 1L;
	private static JList<String> roomlist;
	private static JFrame homeFrame;
	private static JFrame messageFrame;
	private static JTextArea messageArea;
	private static JTextField textField;
	private String usrName;
	private static IRoomChat roomChat;
	private static UserChat user;
	private IServerChat serverChat;
	
	
	/** Construtor da classe que recupera a interface remota do servidor através do Naming.lookup .
	 * @throws RemoteException
	 */
	public UserChat() throws RemoteException {
		try {
			this.usrName = JOptionPane.showInputDialog("Digite seu nome: ");
			serverChat = (IServerChat) Naming.lookup("rmi://localhost:2020/Servidor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			user = new UserChat();
			showHomeGUI();
			getRooms();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 /**Método que atualiza as salas existentes, invocando o método remoto getRooms do serverChat.
	 * @return void
	 * @throws RemoteException
	 */
	private static void getRooms() throws RemoteException {
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		ArrayList<String> rooms = user.serverChat.getRooms();
		rooms.forEach((room)->listModel.addElement(room.replace("+", " ")));
		roomlist.setModel(listModel);
	}
	/**Método que permite o usuário criar uma sala, invocando o método remoto createRoom do serverChat.
	 * @return void
	 * @throws RemoteException
	 */
	private static void createRoom() throws RemoteException {
		String roomName = JOptionPane.showInputDialog("Digite o nome da sala:").replace(" ", "+");
		user.serverChat.createRoom(roomName);
		getRooms();
	}
	/**Método que permite ao usuário entrar em uma sala, invocando o método remoto joinRoom do roomChat.
	 * @return void
	 * @throws RemoteException
	 */
	private static void joinRoom() throws RemoteException {
		String selectedRoom = roomlist.getSelectedValue().replace(" ", "+");
		try {
			roomChat = (IRoomChat) Naming.lookup("rmi://localhost:2020/Servidor/" + selectedRoom);
			roomChat.joinRoom(user.usrName, user);
			showMessageGUI();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	/**Método que permite ao usuário deixar a sala, invocando o método remoto leaveRoom do roomChat.
	 * @return void
	 * @throws RemoteException
	 */
	private static void leaveRoom() throws RemoteException {
		roomChat.leaveRoom(user.usrName);
		messageFrame.setVisible(false);
		getRooms();
	}
	/**Método que manda uma mensagem digitada pelo usuário, invocando o método remoto sendMsg do roomChat.
	 * @return void
	 * @throws RemoteException
	 */
	private static void sendMsg() throws RemoteException {
		roomChat.sendMsg(user.usrName,textField.getText());
		textField.setText("");
	}
	
	 /**Método que permite o usuário receber uma mensagem remota.
	 * @param senderName String - Nome do remetente.
	 * @param msg String - Mensagem enviada pelo remetente.
	 * @return void
	 * @throws RemoteException
	 */
	@Override
	public void deliverMsg(String senderName, String msg) throws RemoteException {
		 messageArea.append(senderName + ": " + msg + "\n");
		 if(senderName.equals("SERVIDOR") && msg.equals("Sala fechada pelo servidor.")) {
			textField.setEditable(false);
			getRooms();
		 }
	}
	
	/**Método que desenha o GUI de menu e define seus action listeners.
	 * @return void
	 */
	private static void showHomeGUI() {
		homeFrame = new JFrame("Usuário " + user.usrName);
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
		panel.setLayout(new GridLayout(1, 2));
		
		roomlist = new JList<String>();
		roomlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roomlist.setLayoutOrientation(JList.VERTICAL);
		roomlist.setVisibleRowCount(-1);
		
		JScrollPane listScroller = new JScrollPane(roomlist);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JButton criarSalaButton = new JButton("Criar sala");
		criarSalaButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					createRoom();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JButton entrarSalaButton = new JButton("Entrar na sala");
		entrarSalaButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					joinRoom();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JButton atualizarSalasButton = new JButton("Atualizar lista");
		atualizarSalasButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					getRooms();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		panel.add(atualizarSalasButton);
		panel.add(criarSalaButton);
		panel.add(entrarSalaButton);
		
		homeFrame.add(listScroller,BorderLayout.CENTER);
		homeFrame.add(panel,BorderLayout.PAGE_END);
		
		homeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		homeFrame.pack();
		homeFrame.setSize(400, 500);
		homeFrame.setVisible(true);
	}
	
	/**Método que desenha o GUI de mensagem (chat) e define seus action listeners.
	 * @return void
	 */
	private static void showMessageGUI() throws RemoteException {
		messageFrame = new JFrame("Usuário " + user.usrName + " - Sala " + roomChat.getRoomName().replace("+", " "));
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
		panel.setLayout(new GridLayout(2, 1));
		
		textField = new JTextField(30);
		textField.setEditable(true);
		JButton sendButton = new JButton("Enviar");
		sendButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					sendMsg();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(textField);
		panel.add(sendButton);
		
		messageArea = new JTextArea(16, 30);
        messageArea.setEditable(false);
        JScrollPane scrollMessagePane = new JScrollPane(messageArea);
        scrollMessagePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        JButton leaveButton = new JButton("Sair da sala");
        leaveButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					leaveRoom();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
        messageFrame.add(leaveButton,BorderLayout.PAGE_START);
        messageFrame.add(scrollMessagePane,BorderLayout.CENTER);
        messageFrame.add(panel,BorderLayout.PAGE_END);
		messageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		messageFrame.pack();
		messageFrame.setSize(400, 500);
		messageFrame.setVisible(true);
	}
}