package gui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class AutocompleteJComboBox extends JComboBox<String>{

	private StringSearchable searchable;
	
	public void changeSearchable(ArrayList<String> arr){
		searchable = new StringSearchable(arr);
	}
	
	public boolean contains(String selectedItem){
		return searchable.contains(selectedItem);
	}
	
	public AutocompleteJComboBox(){
		this(new ArrayList<String>());
	}
	
	public void init(){
		Component c = getEditor().getEditorComponent();
		if ( c instanceof JTextComponent ){
			final JTextComponent tc = (JTextComponent)c;
			tc.getDocument().addDocumentListener(new DocumentListener(){
				@Override
				public void changedUpdate(DocumentEvent arg0) {
				}
				
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					update();
				}
				
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					update();
				}

				public void update(){
					SwingUtilities.invokeLater(new Runnable(){
						@Override
						public void run() {
							String text = tc.getText();
							ArrayList<String> founds = searchable.search(text);
							if (founds.size() == 0){
								tc.setText(text.substring(0, text.length() - 1));
								getToolkit().beep();
							} else{
								setEditable(false);
								removeAllItems();
								if (founds.size() > 1 || !text.equals(founds.get(0)))
									addItem(text);
								for (String s : founds) 
									addItem(s);
								setEditable(true);
								setPopupVisible(true);
								tc.requestFocus();
							}
						}
					});
				}
			});
			tc.addFocusListener(new FocusListener(){
				@Override
				public void focusGained(FocusEvent arg0) {
					if ( tc.getText().length() > 0 )
						setPopupVisible(true);
				}

				@Override
				public void focusLost(FocusEvent arg0) {
				}
			});
		}else
			throw new IllegalStateException("Editing component is not a JTextComponent!");
	}

	public AutocompleteJComboBox(ArrayList<String> terms){
		super();
		searchable = new StringSearchable(terms);
		setEditable(true);
	}
}