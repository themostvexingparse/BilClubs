import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Upload tab — file picker + base64 upload via RequestManager.uploadFiles().
 * Endpoint: POST api/upload  action="upload"  (auth required)
 * Allowed extensions: png, jpg, jpeg, pdf, gif
 */
public class UploadTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public UploadTab(SessionState session, TabContext ctx) {
        super(new BorderLayout(6, 6));
        setBorder(new TitledBorder("Upload Files  (png, jpg, jpeg, pdf, gif)"));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {
        DefaultListModel<java.io.File> model = new DefaultListModel<>();
        JList<java.io.File> fileList = new JList<>(model);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                java.io.File f = (java.io.File) v;
                setText(f.getName() + "  (" + f.length() / 1024 + " KB)");
                return this;
            }
        });
        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setPreferredSize(new Dimension(500, 160));

        JButton addBtn    = new JButton("Add Files…");
        JButton removeBtn = new JButton("Remove Selected");
        JButton uploadBtn = new JButton("Upload");
        uploadBtn.setBackground(Color.decode("#1565c0"));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setFont(uploadBtn.getFont().deriveFont(Font.BOLD));
        uploadBtn.setOpaque(true);
        uploadBtn.setBorderPainted(false);

        addBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Allowed types (png, jpg, jpeg, pdf, gif)",
                    "png", "jpg", "jpeg", "pdf", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                for (java.io.File f : chooser.getSelectedFiles())
                    model.addElement(f);
        });

        removeBtn.addActionListener(e ->
                fileList.getSelectedValuesList().forEach(model::removeElement));

        uploadBtn.addActionListener(e -> {
            if (model.isEmpty()) {
                ctx.showError("No files selected.");
                return;
            }
            ArrayList<java.io.File> files = new ArrayList<>();
            for (int i = 0; i < model.size(); i++)
                files.add(model.get(i));

            JSONObject body = session.authAction("upload");
            ctx.log("» Uploading " + files.size() + " file(s) via RequestManager.uploadFiles() → api/upload");
            Response resp = RequestManager.uploadFiles(body, files);
            ctx.logResponse("api/upload", resp);
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.add(addBtn);
        btnRow.add(removeBtn);
        btnRow.add(Box.createHorizontalStrut(12));
        btnRow.add(uploadBtn);

        JLabel hintLabel = new JLabel(
                "  RequestManager encodes each file as base64 and submits them in one JSON request.");
        hintLabel.setForeground(Color.GRAY);

        add(hintLabel, BorderLayout.NORTH);
        add(scroll,    BorderLayout.CENTER);
        add(btnRow,    BorderLayout.SOUTH);
    }
}
