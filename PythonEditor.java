package Kursova;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.io.*;

public class PythonEditor extends JFrame {
    private final JTextPane textPane;
    private final JTextArea outputArea;
    private final UndoManager undoManager;

    public PythonEditor() {
        setTitle("Python Code Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        textPane = new JTextPane();
        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textPane);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, outputScrollPane);
        splitPane.setDividerLocation(400);
        add(splitPane);

        undoManager = new UndoManager();
        textPane.getDocument().addUndoableEditListener(undoManager);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem runItem = new JMenuItem("Run Code");
        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        JMenuItem pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");

        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        runItem.addActionListener(e -> runPythonCode());

        copyItem.setText("Copy");
        pasteItem.setText("Paste");
        cutItem.setText("Cut");

        undoItem.addActionListener(e -> undo());
        redoItem.addActionListener(e -> redo());

        menu.add(openItem);
        menu.add(saveItem);
        menu.add(runItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(cutItem);
        editMenu.add(undoItem);
        editMenu.add(redoItem);

        menuBar.add(menu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textPane.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    textPane.getDocument().insertString(textPane.getDocument().getLength(), line + "\n", null);
                }
            } catch (IOException | BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textPane.getText());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void runPythonCode() {
        String code = textPane.getText();
        PythonInterpreter interpreter = new PythonInterpreter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        try (printStream) {
            interpreter.setOut(printStream);
            interpreter.setErr(printStream);
            interpreter.exec(code);
            outputArea.setText(outputStream.toString());
        } catch (PyException ex) {
            outputArea.setText(ex.toString());
        }
    }

    private void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PythonEditor editor = new PythonEditor();
            editor.setVisible(true);
        });
    }
}
