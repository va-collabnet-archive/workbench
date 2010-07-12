/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Panel comprising a read-only text field (to display date) and a button.
 * Pressing the button displays a calendar popup, from which a date can be
 * chosen.
 */
public class DatePicker extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private SimpleDateFormat format;
    protected CalendarPanel calendarPanel;
    private JTextField textField;
    private JButton popupButton;
    private Calendar originalDate;

    /**
     * Create a new DatePicker field, using the given date format.
     * 
     * @param format The format to display the selected date in.
     */
    public DatePicker(SimpleDateFormat format) {
        this(null, format);
    }

    /**
     * Create a new DatePicker field.
     * 
     * @param defaultDate The default "starting" date for the field. When
     *            opened, the calendar
     *            popup will default to this date.
     * @param format The format to display the selected date in.
     */
    public DatePicker(Calendar defaultDate, SimpleDateFormat format) {
        this(defaultDate, null, null, format);
    }

    /**
     * Create a new DatePicker field.
     * 
     * @param minimumDate The minimum selectable date.
     * @param maximumDate The maximum selectable date.
     * @param format
     */
    public DatePicker(Calendar minimumDate, Calendar maximumDate, SimpleDateFormat format) {
        this(Calendar.getInstance(), minimumDate, maximumDate, format);
    }

    /**
     * Create a new DatePicker field.
     * 
     * @param defaultDate The default "starting" date for the field. When
     *            opened, the calendar
     *            popup will default to this date.
     * @param minimumDate The minimum selectable date.
     * @param maximumDate The maximum selectable date.
     * @param format
     */
    public DatePicker(Calendar defaultDate, Calendar minimumDate, Calendar maximumDate, SimpleDateFormat format) {
        if (defaultDate != null) {
            originalDate = (Calendar) defaultDate.clone();
        } else {
            originalDate = Calendar.getInstance();
        }
        clearTime(originalDate);
        calendarPanel = new CalendarPanel(originalDate, minimumDate, maximumDate);
        this.format = format;
        init();
    }

    // setup fields
    private void init() {
        Box fieldBox = new Box(BoxLayout.X_AXIS);
        fieldBox.setBorder(BorderFactory.createEmptyBorder());

        textField = new JTextField(10);
        textField.setMaximumSize(new Dimension(100, 25));
        textField.setMinimumSize(new Dimension(100, 25));
        textField.setPreferredSize(new Dimension(100, 25));
        textField.setEditable(false);

        popupButton = new JButton("...");
        popupButton.setMargin(new Insets(0, 0, 0, 0));
        popupButton.addActionListener(new PopupListener());

        fieldBox.add(textField);
        fieldBox.add(Box.createRigidArea(new Dimension(5, 5)));
        fieldBox.add(popupButton);
        add(fieldBox);
    }

    // set all time components of date to 0
    private void clearTime(Calendar date) {
        if (date != null) {
            date.set(Calendar.HOUR, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
        }
    }

    // update text field display, based on selected date
    private void setText() {
        if (getSelectedDate() != null) {
            textField.setText(format.format(getSelectedDate().getTime()).toString());
        } else {
            textField.setText("");
        }
    }

    /**
     * Get the currently selected date.
     * 
     * @return The currently selected date, or null, if no date has been
     *         selected.
     */
    public Calendar getSelectedDate() {
        return calendarPanel.getSelectedDate();
    }

    /**
     * Sets the currently selected date.
     * 
     * @param selectedDate The date to be selected.
     */
    public void setSelectedDate(Calendar selectedDate) {
        calendarPanel.setSelectedDate(selectedDate);
        // update the text field display with the new date.
        this.setText();
    }

    /**
     * Sets the minimum selectable date.
     * 
     * @param minimumDate The minimum selectable date.
     */
    public void setMinimumDate(Calendar minimumDate) {
        calendarPanel.setMinimumDate(minimumDate);
    }

    /**
     * Sets the maximum selectable date.
     * 
     * @param maximumDate The maximum selectable date.
     */
    public void setMaximumDate(Calendar maximumDate) {
        calendarPanel.setMaximumDate(maximumDate);
    }

    /**
     * Private class to handle show the calendar popup when button is pressed.
     */
    private class PopupListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // set the current date, either to the currently selected date
            // or the original default date
            if (getSelectedDate() != null) {
                calendarPanel.setCurrentDate(getSelectedDate());
                calendarPanel.setHighlightedDate(getSelectedDate());
            } else {
                calendarPanel.setCurrentDate(originalDate);
                calendarPanel.setHighlightedDate(originalDate);
            }
            calendarPanel.refreshDays();
            calendarPanel.setLocationRelativeTo(DatePicker.this);
            calendarPanel.setVisible(true);
        }
    }

    /**
     * Private class representing the calendar popup dialog to select a date
     * from.
     */
    private class CalendarPanel extends JDialog {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        // strings to be used as label for current month
        private final String[] months = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP",
                                                      "OCT", "NOV", "DEC" };

        private Calendar selectedDate;
        private Calendar currentDate;
        private Calendar highlightedDate;
        private Calendar minimumDate;
        private Calendar maximumDate;

        private JLabel monthLabel;
        private JButton previousMonth;
        private JButton nextMonth;
        private JButton previousYear;
        private JButton nextYear;

        private JButton[][] daysOfMonth = new JButton[6][7];
        private JPanel dayPanel;

        private CalendarPanel(Calendar currentDate, Calendar minimumDate, Calendar maximumDate) {
            if (currentDate != null) {
                this.currentDate = (Calendar) currentDate.clone();
                this.highlightedDate = (Calendar) currentDate.clone();
            }
            clearTime(this.currentDate);
            clearTime(this.highlightedDate);

            if (minimumDate != null) {
                this.minimumDate = (Calendar) minimumDate.clone();
            }
            clearTime(this.minimumDate);

            if (maximumDate != null) {
                this.maximumDate = (Calendar) maximumDate.clone();
            }
            clearTime(this.maximumDate);
            init();
        }

        private void init() {
            setModal(true);
            setTitle("Select Date");

            previousYear = new JButton("<<");
            previousYear.setFocusable(false);
            previousYear.setMargin(new Insets(0, 0, 0, 0));
            previousMonth = new JButton("<");
            previousMonth.setFocusable(false);
            previousMonth.setMargin(new Insets(0, 0, 0, 0));
            nextMonth = new JButton(">");
            nextMonth.setFocusable(false);
            nextMonth.setMargin(new Insets(0, 0, 0, 0));
            nextYear = new JButton(">>");
            nextYear.setFocusable(false);
            nextYear.setMargin(new Insets(0, 0, 0, 0));
            previousYear.addActionListener(new NextPreviousListener(Calendar.YEAR, false));
            previousMonth.addActionListener(new NextPreviousListener(Calendar.MONTH, false));
            nextMonth.addActionListener(new NextPreviousListener(Calendar.MONTH, true));
            nextYear.addActionListener(new NextPreviousListener(Calendar.YEAR, true));

            dayPanel = new JPanel();
            GridLayout layout = new GridLayout(7, 7);
            layout.setHgap(0);
            layout.setVgap(0);
            dayPanel.setLayout(layout);

            monthLabel = new JLabel();
            monthLabel.setMaximumSize(new Dimension(75, 20));
            monthLabel.setMinimumSize(new Dimension(75, 20));
            monthLabel.setPreferredSize(new Dimension(75, 20));
            monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
            refreshDays();

            // layout components
            Box panel = new Box(BoxLayout.Y_AXIS);
            Box header = new Box(BoxLayout.X_AXIS);
            Box days = new Box(BoxLayout.X_AXIS);
            header.add(previousYear);
            header.add(Box.createRigidArea(new Dimension(5, 5)));
            header.add(previousMonth);
            header.add(Box.createHorizontalGlue());
            header.add(monthLabel);
            header.add(Box.createHorizontalGlue());
            header.add(nextMonth);
            header.add(Box.createRigidArea(new Dimension(5, 5)));
            header.add(nextYear);
            days.add(Box.createHorizontalGlue());
            days.add(dayPanel);
            days.add(Box.createHorizontalGlue());
            panel.add(header);
            panel.add(days);
            add(panel);
            pack();
            setResizable(false);
        }

        // returns the currently selected date
        private Calendar getSelectedDate() {
            return selectedDate;
        }

        // sets the selected date
        private void setCurrentDate(Calendar currentDate) {
            if (currentDate != null) {
                this.currentDate = (Calendar) currentDate.clone();
                clearTime(this.currentDate);
            } else {
                this.currentDate = null;
            }
        }

        // sets the date to highlight - this date will be displayed with a red
        // border
        private void setHighlightedDate(Calendar highlightedDate) {
            if (highlightedDate != null) {
                this.highlightedDate = (Calendar) highlightedDate.clone();
                clearTime(this.highlightedDate);
            } else {
                this.highlightedDate = null;
            }
        }

        // sets the minimum selectable date
        private void setSelectedDate(Calendar selectedDate) {
            if (selectedDate != null) {
                this.selectedDate = (Calendar) selectedDate.clone();
                clearTime(this.selectedDate);
            } else {
                this.selectedDate = null;
            }
        }

        // sets the maximum selectable date
        private void setMinimumDate(Calendar minimumDate) {
            if (minimumDate != null) {
                this.minimumDate = (Calendar) minimumDate.clone();
                clearTime(this.minimumDate);
            } else {
                this.minimumDate = null;
            }
        }

        // sets the maximum selectable date
        private void setMaximumDate(Calendar maximumDate) {
            if (maximumDate != null) {
                this.maximumDate = (Calendar) maximumDate.clone();
                clearTime(this.maximumDate);
            } else {
                this.maximumDate = null;
            }
        }

        // refresh the calendar display
        private void refreshDays() {
            // clear button array
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    daysOfMonth[i][j] = null;
                }
            }

            Calendar tempDate = Calendar.getInstance();
            tempDate.set(Calendar.DAY_OF_MONTH, 1);
            tempDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
            tempDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
            clearTime(tempDate);

            int firstDay = tempDate.get(Calendar.DAY_OF_WEEK);
            tempDate.add(Calendar.MONTH, 1);
            tempDate.add(Calendar.DAY_OF_MONTH, -1);
            int numDays = tempDate.get(Calendar.DAY_OF_MONTH);

            int week = 0;
            int weekday = firstDay;

            // set buttons for each day
            for (int i = 1; i <= numDays; i++) {
                Calendar date = Calendar.getInstance();
                date.set(Calendar.DAY_OF_MONTH, i);
                date.set(Calendar.MONTH, tempDate.get(Calendar.MONTH));
                date.set(Calendar.YEAR, tempDate.get(Calendar.YEAR));
                clearTime(date);

                JButton day = new JButton(Integer.toString(i));
                day.setFocusable(false);
                day.setMargin(new Insets(0, 0, 0, 0));
                // change colour of button for current date
                if (date.equals(highlightedDate)) {
                    day.setBorder(BorderFactory.createLineBorder(Color.RED));
                }

                // disable button if before the minimum date or after maximum
                // date
                if (date.before(minimumDate) || date.after(maximumDate)) {
                    day.setEnabled(false);
                }

                daysOfMonth[week][weekday - 1] = day;
                daysOfMonth[week][weekday - 1].addActionListener(new DayButtonListener(date));

                if (weekday == Calendar.SATURDAY) {
                    week++;
                }
                weekday = (weekday % 7) + 1;
            }

            dayPanel.removeAll();
            // add day label headings to panel
            addDayHeadings();
            // add buttons to panel
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    if (daysOfMonth[i][j] != null) {
                        dayPanel.add(daysOfMonth[i][j]);
                    } else {
                        // blank space for grid positions outside of current
                        // month
                        dayPanel.add(Box.createRigidArea(new Dimension(10, 10)));
                    }
                }
            }

            monthLabel.setText(months[currentDate.get(Calendar.MONTH)] + " " + currentDate.get(Calendar.YEAR));

            updateButtons();
            repaint();
        }

        // enable/disable the next and previous buttons depending on date range
        private void updateButtons() {
            Calendar tempDate = (Calendar) currentDate.clone();
            // set date to first day of month
            tempDate.set(Calendar.DAY_OF_MONTH, 1);
            clearTime(tempDate);

            if (maximumDate != null) {
                // add year to test whether "Next Year" button should be enabled
                tempDate.add(Calendar.YEAR, 1);
                nextYear.setEnabled(!tempDate.after(maximumDate) && !tempDate.before(minimumDate));
                tempDate.add(Calendar.YEAR, -1);

                // add month to test whether "Next Month" button should be
                // enabled
                tempDate.add(Calendar.MONTH, 1);
                nextMonth.setEnabled(!tempDate.after(maximumDate) && !tempDate.before(minimumDate));
                tempDate.add(Calendar.MONTH, -1);
            }

            if (minimumDate != null) {
                // take year, add month, take day - gives last day of current
                // month in previous year
                // to test whether "Previous Year" button should be enabled
                tempDate.add(Calendar.YEAR, -1);
                tempDate.add(Calendar.MONTH, 1);
                tempDate.add(Calendar.DAY_OF_MONTH, -1);
                previousYear.setEnabled(!tempDate.after(maximumDate) && !tempDate.before(minimumDate));
                tempDate.add(Calendar.DAY_OF_MONTH, 1);
                tempDate.add(Calendar.MONTH, -1);
                tempDate.add(Calendar.YEAR, 1);

                // take day to test whether "Previous Month" button should be
                // enabled
                tempDate.add(Calendar.DAY_OF_MONTH, -1);
                previousMonth.setEnabled(!tempDate.after(maximumDate) && !tempDate.before(minimumDate));
            }
        }

        // add column headings to panel
        private void addDayHeadings() {
            // add column heading for each day of week
            JLabel sunday = new JLabel("S");
            sunday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(sunday);
            JLabel monday = new JLabel("M");
            monday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(monday);
            JLabel tuesday = new JLabel("T");
            tuesday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(tuesday);
            JLabel wednesday = new JLabel("W");
            wednesday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(wednesday);
            JLabel thursday = new JLabel("T");
            thursday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(thursday);
            JLabel friday = new JLabel("F");
            friday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(friday);
            JLabel saturday = new JLabel("S");
            saturday.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(saturday);
        }

        // button listener to handle next/previous buttons
        private class NextPreviousListener implements ActionListener {

            // field corresponds to Calendar field (e.g. Calendar.YEAR,
            // Calendar.MONTH)
            private int field;
            private boolean isNext;

            public NextPreviousListener(int field, boolean isNext) {
                this.field = field;
                this.isNext = isNext;
            }

            public void actionPerformed(ActionEvent e) {
                currentDate.add(field, (isNext ? 1 : -1));
                refreshDays();
            }
        }

        // button listener for each day - sets selected date when pressed
        private class DayButtonListener implements ActionListener {

            private Calendar date;

            public DayButtonListener(Calendar date) {
                this.date = date;
            }

            public void actionPerformed(ActionEvent e) {
                if (selectedDate == null) {
                    selectedDate = Calendar.getInstance();
                    clearTime(selectedDate);
                }
                // set the selected date
                selectedDate.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
                selectedDate.set(Calendar.MONTH, date.get(Calendar.MONTH));
                selectedDate.set(Calendar.YEAR, date.get(Calendar.YEAR));

                // close the popup window
                dispose();
                setText();
            }
        }
    }
}
