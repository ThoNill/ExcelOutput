package org.thonill.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChooser extends JFileChooser {
	
	private static final long serialVersionUID = 1L;
	private SetMy<String> setMy;

	public FileChooser(SetMy<String> setMy, String pathString) {
		super();
		this.setMy = setMy;
		setDialogType(JFileChooser.OPEN_DIALOG);
		setFileFilter(new FileNameExtensionFilter("Excel Files", "xls"));
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		setMultiSelectionEnabled(false);
		setDialogTitle("Datei auswählen");
		setApproveButtonText("Auswählen");
		setApproveButtonToolTipText("Auswählen");
		setApproveButtonMnemonic('A');
		setFileHidingEnabled(false);
		String absolutePath = new File(pathString).getAbsolutePath();
		setCurrentDirectory(new File(absolutePath));
	}

	public void showDialog() {
		int returnVal = showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String steuerDatei = getSelectedFile().getAbsolutePath();
			setMy.setValue(steuerDatei);
		} 
	}

}
