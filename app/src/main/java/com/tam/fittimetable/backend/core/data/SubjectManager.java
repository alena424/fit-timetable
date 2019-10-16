package com.tam.fittimetable.backend.core.data;

import com.tam.fittimetable.backend.core.extract.DownloadException;
import com.tam.fittimetable.backend.core.extract.Downloader;
import com.tam.fittimetable.backend.core.extract.Extractor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Kohout <xkohou14 at stud.fit.vutbr.cz>
 */
public class SubjectManager {

    private static SubjectManager manager = null;
    private final List<Subject> subjects;
    private final List<Date> dates; // dates of semester , index 0: start of winter semester, 1 end of winter semester, 2/3 start/end

    public void addSubject(Subject s) {
        subjects.add(s);
    }

    public void addSubject(String name, String link, String room, int from, int to, String day, String color) {
        subjects.add(new Subject(name, link, room, from, to, day, color));
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    /**
     * Return sorted subjects from beggining of day (7) to end of the day (22)
     *
     * @param day
     * @return
     */
    public List<Subject> getSubjects(Subject.Day day) {
        List<Subject> daysubjects = new ArrayList<Subject>();
        for (Subject s : subjects) {
            if (s.getDay() == day) {
                daysubjects.add(s);
            }
        }

        Collections.sort(daysubjects, new TimeComparator());

        return daysubjects;
    }

    /**
     * return list of Subjects begging at <i>time</i> sorted by days (Monday,
     * Tuesday..)
     *
     * @param time
     * @return sorted list of Subjects
     */
    public List<Subject> getSubjects(int time) {
        List<Subject> timeSubjects = new ArrayList<Subject>();
        for (Subject s : subjects) {
            if (s.getFrom() == time) {
                timeSubjects.add(s);
            }
        }

        Collections.sort(timeSubjects, new DayComparator());

        return timeSubjects;
    }

    /**
     * Singleton construction
     *
     * @return instance of SubjectManager
     */
    public static SubjectManager get() throws ParseException, DownloadException {
        if (manager == null) {
            manager = new SubjectManager();

            Extractor extractor = new Extractor(Downloader.download(Strings.PRIVATE_TIMETABLE_LINK, Strings.PRIVATE_TIMETABLE_FILE));
            extractor.parse();
        }

        return manager;
    }

    /**
     * returns list of dates with dates of actual or next semester
     *
     * @return
     */
    public List<Date> actualSemester() throws ParseException {
        try {
            List<Date> semester = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat(Strings.DATE_FORMAT);
            Date actual = new Date();
            Date summerHolidays = formatter.parse(Strings.MIDDLE_OF_YEAR + actual.toString().trim().split(" ")[5]);
            Date winterHolidays = formatter.parse(Strings.END_OF_YEAR + actual.toString().trim().split(" ")[5]);

            Date tmp1, tmp2;
            if (actual.compareTo(summerHolidays) >= 0) { // winter semster
                for (Date d : dates) {
                    if (d.compareTo(summerHolidays) * d.compareTo(winterHolidays) <= 0) { // it is minus because 1 * -1
                        semester.add(d);
                    }
                }
            } else { // summer semester
                winterHolidays.setYear(0); //whatever it should be higher, it has to be ACTUAL_YEAR - 1
                for (Date d : dates) {
                    if (d.compareTo(winterHolidays) * d.compareTo(summerHolidays) <= 0) { // it is minus because 1 * -1
                        semester.add(d);
                    }
                }
            }

            return semester;
        } catch (ParseException ex) {
            Logger.getLogger(SubjectManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new ParseException("Dates of semester are not recognized.", 0);
        }
    }

    /**
     * Returns index of week for date. It does not depend on semester
     *
     * @param date
     * @return
     */
    public int getWeekOfSemester(Date date) {
        Date winterBegins = dates.get(0);
        Date winterEnds = dates.get(1);
        Date summerBegins = dates.get(2);
        Date summerEnds = dates.get(3);

        if (date.compareTo(winterBegins) * date.compareTo(winterEnds) <= 0) { //winter semester
            long diff = date.getTime() - winterBegins.getTime();
            long days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 7);
            return (int) days;
        } else {
            if (date.compareTo(summerBegins) * date.compareTo(summerEnds) <= 0) { //summer semester
                long diff = date.getTime() - summerBegins.getTime();
                long days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 7);
                return (int) days;
            }
        }

        return -1;
    }

    public boolean contains(Subject s) {
        for (Subject subject : subjects) {
            if (subject.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /*public boolean contains(String name, int from, int to, String day) {
        Subject s = new Subject(name, day, day, from, to, day, day)
        for(Subject subject : subjects) {
            if(subject.equals(s))
                return true;
        }
        return false;
    }*/
    public void addDate(String date) throws ParseException {
        dates.add(new SimpleDateFormat(Strings.DATE_FORMAT).parse(date));
    }

    private SubjectManager() throws ParseException, DownloadException {
        subjects = new ArrayList<Subject>();
        dates = Extractor.selectDatesOfSemesters();
    }

    private class TimeComparator implements Comparator<Subject> {

        @Override
        public int compare(Subject t, Subject t1) {
            return t.getFrom() - t1.getFrom();
        }

    }

    private class DayComparator implements Comparator<Subject> {

        @Override
        public int compare(Subject t, Subject t1) {
            return t.getDay().value() - t1.getDay().value();
        }

    }
}
