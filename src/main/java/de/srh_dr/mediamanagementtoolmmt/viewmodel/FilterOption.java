package de.srh_dr.mediamanagementtoolmmt.viewmodel;

//allows separation between localized display and internal string
public class FilterOption {
    private final String displayText;
    private final String filterText;

    public FilterOption(String displayText, String filterText) {
        this.displayText = displayText;
        this.filterText = filterText;
    }

    public String getInternalValue() {
        return filterText;
    }

    @Override
    public String toString() {
        return displayText;
    }
}
