package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import model.Person;

import java.util.Comparator;
import java.util.function.Predicate;

public class SearchService {
    private final FilteredList<Person> filteredData;
    private final SortedList<Person> sortedData;
    private String searchText = "";
    private SortField currentSortField = SortField.NONE;
    private boolean ascending = true;

    // This is lists of options for fields that can be sorted
    public enum SortField {
        NONE,
        FIRST_NAME,
        LAST_NAME,
        DEPARTMENT,
        MAJOR,
        EMAIL
    }

    // This is a setup method to start with empty lists for filtered and sorted data
    public SearchService(ObservableList<Person> data) {
        this.filteredData = new FilteredList<>(data, p -> true);
        this.sortedData = new SortedList<>(filteredData);
    }

    // This updates the filter to use the new search text
    public void updateSearchFilter(String searchText) {
        this.searchText = searchText.toLowerCase();
        filteredData.setPredicate(createPredicate());
    }

    // This sets the sort field
    public void setSortField(SortField field, boolean ascending) {
        this.currentSortField = field;
        this.ascending = ascending;
        updateSort();
    }

    // This updates the sorting comparator for the sorted list
    private void updateSort() {
        sortedData.setComparator(createComparator());
    }

    // This creates a rule to filter items based on the search text
    private Predicate<Person> createPredicate() {
        return person -> {
            if (searchText == null || searchText.isEmpty()) return true;

            // This checks if the search text matches any important information
            return person.getFirstName().toLowerCase().contains(searchText) ||
                   person.getLastName().toLowerCase().contains(searchText) ||
                   person.getDepartment().toLowerCase().contains(searchText) ||
                   person.getMajor().toLowerCase().contains(searchText) ||
                   person.getEmail().toLowerCase().contains(searchText);
        };
    }

    // This creates a comparator for sorting based on the selected field and order
    private Comparator<Person> createComparator() {
        if (currentSortField == SortField.NONE) return null;

        // This chooses how to sort based on the selected field
        Comparator<Person> comparator = switch (currentSortField) {
            case FIRST_NAME -> Comparator.comparing(Person::getFirstName);
            case LAST_NAME -> Comparator.comparing(Person::getLastName);
            case DEPARTMENT -> Comparator.comparing(Person::getDepartment);
            case MAJOR -> Comparator.comparing(Person::getMajor);
            case EMAIL -> Comparator.comparing(Person::getEmail);
            default -> null;
        };

        return ascending ? comparator : comparator.reversed();
    }

    // This returns the sorted data list
    public SortedList<Person> getSortedData() {
        return sortedData;
    }

    // This returns the filtered data list
    public FilteredList<Person> getFilteredData() {
        return filteredData;
    }

    // This is to clears all the filters and sorting
    public void clearFilters() {
        searchText = "";
        currentSortField = SortField.NONE;
        ascending = true;
        filteredData.setPredicate(p -> true);
        sortedData.setComparator(null);
    }

    // This is to give the filtered and sorted data that can't be changed
    public ObservableList<Person> getFilteredAndSortedData() {
        return FXCollections.unmodifiableObservableList(sortedData);
    }
}
