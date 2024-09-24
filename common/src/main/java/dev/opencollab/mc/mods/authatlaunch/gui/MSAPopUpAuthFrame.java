package dev.opencollab.mc.mods.authatlaunch.gui;

import dev.opencollab.mc.mods.authatlaunch.auth.entra.DeviceCodeFlowStartResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

public class MSAPopUpAuthFrame extends JDialog {

    private JLabel instructionLabel;
    private JTextField codeField;
    private JButton openUrlButton;
    private JButton cancelButton;
    private NewMicrosoftAuthenticationProcess worker;
    private AuthAtLaunchFrame authFrame;
    private final String verificationUrl;
    private final String userCode;

    public MSAPopUpAuthFrame(AuthAtLaunchFrame owner, DeviceCodeFlowStartResponse response) {
        super(owner, "Microsoft Authentication", true);
        this.authFrame = owner;
        this.verificationUrl = response.getVerificationUri();
        this.userCode = response.getUserCode();
        instructionLabel = new JLabel();
        codeField = new JTextField();
        codeField.setEditable(false);
        openUrlButton = new JButton("Open URL and Copy Code");
        cancelButton = new JButton("Cancel");
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        instructionLabel.setText("<html>Please authenticate your Microsoft account.<br>"
                + "Visit the following URL and enter the code:</html>");
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        codeField.setFont(new Font("SansSerif", Font.BOLD, 16));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setText(userCode);

        openUrlButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(instructionLabel, BorderLayout.NORTH);
        centerPanel.add(codeField, BorderLayout.CENTER);
        centerPanel.add(openUrlButton, BorderLayout.SOUTH);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
        openUrlButton.addActionListener(e -> openUrlAndCopyCode());
        cancelButton.addActionListener(e -> cancelAuthentication());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelAuthentication();
            }
        });
        pack();
        setLocationRelativeTo(owner);
    }


    private void openUrlAndCopyCode() {
        // Copy code to clipboard
        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(codeField.getText()), null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to copy code to clipboard.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Open the URL
        try {
            Desktop.getDesktop().browse(new URI(this.verificationUrl));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to open browser. Please navigate to the URL manually:\n" + "worker.getVerificationUrl()", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelAuthentication() {
        authFrame.cancelledMSAuth();
        dispose();
    }


    public void setVerificationInfo(String verificationUrl, String userCode) {
        codeField.setText(userCode);
    }

    public void onAuthenticationSuccess(String account) {
        authFrame.onAuthenticationSuccess(account);
        dispose();
    }

    public void onAuthenticationFailure(String message) {
        authFrame.onAuthenticationFailure(message);
        dispose();
    }

}
