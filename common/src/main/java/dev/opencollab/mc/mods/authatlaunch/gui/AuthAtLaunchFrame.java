package dev.opencollab.mc.mods.authatlaunch.gui;

import dev.opencollab.mc.mods.authatlaunch.async.AuthAtLaunchExecutorService;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationDetails;
import dev.opencollab.mc.mods.authatlaunch.auth.CompletedMinecraftAuthentication;
import dev.opencollab.mc.mods.authatlaunch.config.AuthAtLaunchConfigManager;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class AuthAtLaunchFrame extends JFrame {

    private JComboBox<AccountDropdownOption> accountComboBox;
    private JButton primaryButton;
    private JLabel statusLabel;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> countdownFuture;
    private int countdownSeconds = 5;
    private AuthAtLaunchConfigManager configManager;
    private CompletableFuture<CompletedMinecraftAuthentication> result;


    public AuthAtLaunchFrame(AuthAtLaunchConfigManager configManager, AuthAtLaunchExecutorService scheduler, CompletableFuture<CompletedMinecraftAuthentication> completed) {
        super("Auth at Launch");
        this.result = completed;
        this.configManager = configManager;
        this.scheduler = scheduler.getExecutorService();
        initComponents();
        layoutComponents();
        attachListeners();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        if (configManager.autoLoginUser().isPresent()) {
            startCountdown();
        } else {
            updatePrimaryButton();
        }
    }

    private void initComponents() {
        accountComboBox = new JComboBox<>();
        primaryButton = new JButton("Continue");
        statusLabel = new JLabel("Please select an account to continue.");
        statusLabel.setForeground(Color.BLUE);

        populateAccountDropdown(true);
    }

    private void populateAccountDropdown(boolean selectAutoLogin) {
        // Add existing accounts
        List<AuthenticationDetails> accounts = configManager.availableUsers();

        Optional<String> autoLoginUser = configManager.autoLoginUser();
        AccountDropdownOption previouslySelectedItem = (AccountDropdownOption) accountComboBox.getSelectedItem();

        accountComboBox.removeAllItems();

        int autoLoginIdx = 0;
        for (int i = 0; i < accounts.size(); i++) {
            AuthenticationDetails account = accounts.get(i);
            AccountDropdownOption option = AccountDropdownOption.builder()
                    .displayName(account.getMcUsername())
                    .type(AccountDropdownOption.OptionAction.EXISTING_MSA)
                    .account(account)
                    .build();
            accountComboBox.addItem(option);
            if(autoLoginUser.isPresent()) {
                if(Objects.equals(autoLoginUser.get(), account.getMcUUID())) {
                    autoLoginIdx = i;
                }
            }
        }
        // Add special options
        accountComboBox.addItem(AccountDropdownOption.builder()
                        .displayName("Login to MSA")
                        .type(AccountDropdownOption.OptionAction.NEW_MSA)
                .build());
        accountComboBox.addItem(AccountDropdownOption.builder()
                        .displayName("Set Username (offline)")
                        .type(AccountDropdownOption.OptionAction.OFFLINE_AUTH)
                .build());

        // If no accounts, default to "Add New Microsoft Account"
        if (accounts.isEmpty()) {
            accountComboBox.setSelectedIndex(0); // Select "Add New Microsoft Account"
        }
        if(selectAutoLogin && autoLoginUser.isPresent()) {
            accountComboBox.setSelectedIndex(autoLoginIdx);
        }
    }

    private void layoutComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Center Panel with dropdown
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        JLabel selectAccountLabel = new JLabel("Select Account:");
        selectAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        accountComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        centerPanel.add(selectAccountLabel, BorderLayout.NORTH);
        centerPanel.add(accountComboBox, BorderLayout.CENTER);

        contentPanel.add(statusLabel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(primaryButton, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }

    private void attachListeners() {
        accountComboBox.addActionListener(e -> updatePrimaryButton());

        primaryButton.addActionListener(e -> handlePrimaryButtonClick());

        // Stop countdown on any user interaction
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                stopCountdown();
            }
        };
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                stopCountdown();
            }
        };
        addMouseListener(mouseAdapter);
        addKeyListener(keyAdapter);
    }

    private void updatePrimaryButton() {
        AccountDropdownOption selectedOption = (AccountDropdownOption) accountComboBox.getSelectedItem();
        if (selectedOption != null) {
            switch (selectedOption.getType()) {
                case EXISTING_MSA:
                    primaryButton.setText("Login");
                    break;
                case NEW_MSA:
                    primaryButton.setText("Start Authentication");
                    break;
                case OFFLINE_AUTH:
                    primaryButton.setText("Set Username");
                    break;
            }
        }
    }

    private void handlePrimaryButtonClick() {
        AccountDropdownOption selectedOption = (AccountDropdownOption) accountComboBox.getSelectedItem();
        if (selectedOption != null) {
            switch (selectedOption.getType()) {
                case EXISTING_MSA:
                    startSavedMSAAuth(selectedOption.getAccount());
                    break;
                case NEW_MSA:
                    addNewMicrosoftAccount();
                    break;
                case OFFLINE_AUTH:
                    offlineAuth();
                    break;
            }
        }
    }

    private void startCountdown() {
        updateCountdownButtonText();
        countdownFuture = scheduler.scheduleAtFixedRate(() -> {
            countdownSeconds--;
            if (countdownSeconds > 0) {
                SwingUtilities.invokeLater(this::updateCountdownButtonText);
            } else {
                stopCountdown();
                SwingUtilities.invokeLater(this::autoClick);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void updateCountdownButtonText() {
        primaryButton.setText("Auto-login in " + countdownSeconds + "s... (Click to cancel)");
    }

    private void stopCountdown() {
        if (countdownFuture != null && !countdownFuture.isCancelled()) {
            countdownFuture.cancel(true);
            countdownFuture = null;
            countdownSeconds = 5;
            SwingUtilities.invokeLater(() -> {
                updatePrimaryButton();
                infoMessage("Auto-login cancelled. Please choose an option.");
            });
        }
    }

    private void autoClick() {
        handlePrimaryButtonClick();
    }

    private void startSavedMSAAuth(AuthenticationDetails account) {
        stopCountdown();
        disableUIComponents();

        // Perform authentication in a background thread
        ExistingMicrosoftAuthenticationProcess authenticationProcess = new ExistingMicrosoftAuthenticationProcess(account, configManager, this);
        scheduler.execute(authenticationProcess::refreshAndAuthenticate);
    }

    public void infoMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.BLUE);
    }

    public void errorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }

    public void successMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(0, 128, 0));
    }

    private void addNewMicrosoftAccount() {
        stopCountdown();
        disableUIComponents();
        infoMessage("Starting Microsoft authentication...");

        // Start Microsoft authentication
        NewMicrosoftAuthenticationProcess newMicrosoftAuthenticationProcess = new NewMicrosoftAuthenticationProcess(configManager, this);
        scheduler.execute(newMicrosoftAuthenticationProcess::doMSA);
    }

    private void offlineAuth() {
        stopCountdown();
        disableUIComponents();
        String username = JOptionPane.showInputDialog(this, "Enter custom username", "Offline Login", JOptionPane.QUESTION_MESSAGE);
        if (username != null) {
            if(username.matches("^[a-zA-Z0-9_]{3,16}$")) {
                completeAuthentication(CompletedMinecraftAuthentication.builder()
                        .userType(null)
                        .username(username)
                        .accessToken("AuthAtLaunchUsername")
                        .build());
            } else {
                onAuthenticationFailure("Invalid Minecraft username... (Must match regex: ^[a-zA-Z0-9_]{3,16}$)");
            }
        } else {
            onAuthenticationFailure("Offline login cancelled or no username given.");
        }
    }
    

    public void onAuthenticationSuccess(Object auth) {
        successMessage("Authentication successful!");
        // Save last successful account
        log.info("Authentication successful...");
        // Proceed with application logic
    }

    public void onAuthenticationFailure(String message) {
        errorMessage(message);
        enableUIComponents();
    }

    private void disableUIComponents() {
        accountComboBox.setEnabled(false);
        primaryButton.setEnabled(false);
    }

    private void enableUIComponents() {
        accountComboBox.setEnabled(true);
        primaryButton.setEnabled(true);
    }

    public void cancelledMSAuth() {
        onAuthenticationFailure("MSA login cancelled.");
    }

    public void completeAuthentication(CompletedMinecraftAuthentication result) {
        this.result.complete(result);
    }

    public void completeExceptionally(Throwable t) {
        this.result.completeExceptionally(t);
    }
}
