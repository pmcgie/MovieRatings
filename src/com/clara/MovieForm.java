package com.clara;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Calendar;


public class MovieForm extends JFrame implements WindowListener{
    private JTable movieDataTable;
    private JPanel rootPanel;
    private JTextField titleTextField;
    private JTextField yearTextField;
    private JButton addNewMovieButton;
    private JButton quitButton;
    private JButton deleteMovieButton;
    private JSpinner ratingSpinner;

    MovieForm(final MovieDataModel movieDataTableModel){

        setContentPane(rootPanel);
        pack();
        setTitle("Movie Database Application");
        addWindowListener(this);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Set up JTable
        movieDataTable.setGridColor(Color.BLACK);
        movieDataTable.setModel(movieDataTableModel);

        //Set up the rating spinner.
        //SpinnerNumberModel constructor arguments: spinner's initial value, min, max, step.
        ratingSpinner.setModel(new SpinnerNumberModel(1, MovieDatabase.MOVIE_MIN_RATING, MovieDatabase.MOVIE_MAX_RATING, 1));


        //Event handlers for add, delete and quit buttons
        addNewMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Get Movie title, make sure it's not blank
                String titleData = titleTextField.getText();

                if (titleData == null || titleData.trim().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a title for the new movie");
                    return;
                }

                //Get movie year. Check it's a number between 1900 and present year
                int yearData;

                try {
                    yearData = Integer.parseInt(yearTextField.getText());
                    if (yearData < 1900 || yearData > Calendar.getInstance().get(Calendar.YEAR)){
                        //Calendar.getInstance() returns a Calendar object representing right now.
                        //calendarobject.get(Calendar.MONTH) gets current month, calendarobject.get(Calendar.SECOND) gets current second
                        //Can get and set other time/date fields- check Java documentation for others
                        //http://docs.oracle.com/javase/7/docs/api/java/util/Calendar.html
                        throw new NumberFormatException("Year needs to be between 1900 and present year");
                    }
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(rootPane,
                            "Year needs to be a number between 1900 and now");
                    return;
                }

                //Using a spinner means we are guaranteed to get a number in the range we set, so no validation needed.
                int ratingData = (Integer)(ratingSpinner.getValue());

                System.out.println("Adding " + titleData + " " + yearData + " " + ratingData);
                boolean insertedRow = movieDataTableModel.insertRow(titleData, yearData, ratingData);
                if (insertedRow) {
                    //The table model made calls that updated the ResultSet, and which update the database, and the GUI. Cool!
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Error adding new movie");
                }
            }

        });


        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MovieDatabase.shutdown();
                System.exit(0);
            }
        });

        deleteMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentRow = movieDataTable.getSelectedRow();
                boolean deleted = movieDataTableModel.deleteRow(currentRow);
                if (deleted) {
                    MovieDatabase.loadAllMovies();
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Error deleting movie");
                }
            }
        });
    }

    //windowListener methods. Only need one of them, but are required to implement the others anyway
    //WindowClosing will call DB shutdown code, which is important, so the DB is in a consistent state however the application is closed.

    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("closing");
        MovieDatabase.shutdown();}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}