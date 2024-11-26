package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static volatile UserSession instance; // This is to make sure that the changes are seen by all the threads
    private final Preferences userPreferences;

    private String userName;
    private String password;
    private String privileges;

    // This is to stop the class from being created directly
    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;

        // This is to save session information for later use
        userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    // This is to make sure only one instance is created
    public static UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(userName, password, privileges);
                }
            }
        }
        return instance;
    }

    public static UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    public synchronized String getUserName() {
        return userName;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized String getPrivileges() {
        return privileges;
    }

    public synchronized void updateUser(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;

        // This is to update preferences
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    public synchronized void cleanUserSession() {
        this.userName = "";
        this.password = "";
        this.privileges = "";

        // This is to clear preferences
        userPreferences.remove("USERNAME");
        userPreferences.remove("PASSWORD");
        userPreferences.remove("PRIVILEGES");
    }

    @Override
    public synchronized String toString() {
        return "UserSession{" +
                "userName='" + userName + '\'' +
                ", privileges='" + privileges + '\'' +
                '}';
    }

    // This is to retrieve preferences
    public static synchronized UserSession loadFromPreferences() {
        Preferences userPreferences = Preferences.userRoot();
        String userName = userPreferences.get("USERNAME", "");
        String password = userPreferences.get("PASSWORD", "");
        String privileges = userPreferences.get("PRIVILEGES", "NONE");

        if (userName.isEmpty() || password.isEmpty()) {
            return null; // No session found
        }

        return getInstance(userName, password, privileges);
    }
}
