package de.florianisme.wakeonlan.ssh;

public class SshCommandModel {

    private final String sshAddress;
    private final int sshPort;
    private final String username;
    private final String password;
    private final String command;

    public SshCommandModel(String sshAddress, int sshPort, String username, String password, String command) {
        this.sshAddress = sshAddress;
        this.sshPort = sshPort;
        this.username = username;
        this.password = password;
        this.command = command;
    }

    public String getSshAddress() {
        return sshAddress;
    }

    public int getSshPort() {
        return sshPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCommand() {
        return command;
    }
}
