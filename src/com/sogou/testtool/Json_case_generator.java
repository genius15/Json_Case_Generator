package com.sogou.testtool;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Button;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.Checkbox;

@SuppressWarnings("serial")
public class Json_case_generator extends JFrame {
	
	private JPanel contentPane;
	private TextField urltext = null;
	private TextArea postData = null;
	private Checkbox checkbox = null;
	private Button start = null;
	private Button pathSelector = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Json_case_generator frame = new Json_case_generator();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Json_case_generator() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 473, 318);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		pathSelector = new Button("Click to choose case path");
		pathSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(jfc.showDialog(Json_case_generator.this, "Ñ¡Ôñcase±£´æÂ·¾¶") == JFileChooser.APPROVE_OPTION){
					File selectedfile = jfc.getSelectedFile();
					case_generator.savePath = selectedfile.getAbsolutePath();
				}
			}
		});
		pathSelector.setActionCommand("action_path_selector");
		contentPane.add(pathSelector, BorderLayout.WEST);
		
		start = new Button("start generate");
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(urltext == null || postData == null || checkbox == null)
					return;
				urltext.setEnabled(false);
				postData.setEditable(false);
				checkbox.setEnabled(false);
				pathSelector.setEnabled(false);
				start.setEnabled(false);
				String urlString = urltext.getText();
				String postdString = null;
				if(checkbox.getState()){
					postdString = postData.getText();
				}
				
				try {
					case_generator.start_generator(urlString, postdString);
				} catch (Exception e2) {
					e2.printStackTrace();
					Toolkit.getDefaultToolkit().beep();
					
					StackTraceElement[] messageElements = e2.getStackTrace();
					String errString = "";
					for (int i = 0; i < messageElements.length; i++) {
						errString += messageElements[i].toString();
						errString += "\n";
					}
					JOptionPane.showMessageDialog(null, errString, "exception",JOptionPane.ERROR_MESSAGE);
				}
				urltext.setEnabled(true);
				postData.setEditable(true);
				checkbox.setEnabled(true);
				pathSelector.setEnabled(true);
				start.setEnabled(true);
				case_generator.savePath = null;
			}
		});
		start.setActionCommand("action_start");
		contentPane.add(start, BorderLayout.SOUTH);
		
		urltext = new TextField();
		contentPane.add(urltext, BorderLayout.NORTH);
		
		postData = new TextArea();
		postData.setRows(10);
		contentPane.add(postData, BorderLayout.CENTER);
		
		checkbox = new Checkbox("checked as a post");
		contentPane.add(checkbox, BorderLayout.EAST);
	}

}
