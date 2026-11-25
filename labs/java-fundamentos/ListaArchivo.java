import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class ListaArchivo extends JFrame {

    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);
    private final File archivo;

    public ListaArchivo(File archivo) {
        super("Archivo secuencial de texto");
        this.archivo = archivo;

        JButton llenar = new JButton("Llenar lista");
        JButton editar = new JButton("Editar archivo");

        llenar.addActionListener((ActionEvent e) -> llenarLista());
        editar.addActionListener((ActionEvent e) -> editarArchivo());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(llenar);
        botones.add(editar);

        add(new JScrollPane(lista), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(null);
    }

    private void llenarLista() {
        modelo.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                modelo.addElement(linea);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer el archivo:\n" + archivo.getAbsolutePath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarArchivo() {
        try {
            new ProcessBuilder("notepad.exe", archivo.getAbsolutePath()).start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir Notepad.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        final File archivo = args.length > 0 ? new File(args[0]) : new File("People.txt");
        SwingUtilities.invokeLater(() -> new ListaArchivo(archivo).setVisible(true));
    }
}
