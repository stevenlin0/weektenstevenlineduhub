package model;

// This is the list of some majors
public enum Major {
    CS("Computer Science"),
    CPIS("Computer Information Systems"),
    ENGLISH("English"),
    MATH("Mathematics"),
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    BIOLOGY("Biology"),
    BUSINESS("Business Administration");

    private final String displayName; // This is the display name for the major

    // This is to set the name for each major
    Major(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName; // This is to show the full name of the major
    }

    // This is to find the major that matches the given name
    public static Major fromString(String text) {
        for (Major major : Major.values()) {
            if (major.displayName.equalsIgnoreCase(text)) {
                return major;
            }
        }
        throw new IllegalArgumentException("No major with display name: " + text);
    }
}
