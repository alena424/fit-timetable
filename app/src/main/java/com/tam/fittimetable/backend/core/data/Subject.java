package com.tam.fittimetable.backend.core.data;

import com.tam.fittimetable.backend.core.extract.Downloader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Petr Kohout <xkohou14 at stud.fit.vutbr.cz>
 */
public final class Subject {

    enum Day {
        MONDAY("Monday", 1, "Mon"),
        TUESDAY("Tuesday", 2, "Tue"),
        WEDNESDAY("Wednesday", 3, "Wed"),
        THURSTDAY("Thursday", 4, "Thu"),
        FRIDAY("Friday", 5, "Fri");

        private final String display;
        private final int value;
        private final String shortCut;

        public String display() {
            return this.display;
        }

        public int value() {
            return this.value;
        }

        public String shortCut() {
            return this.shortCut;
        }

        private Day(String display, int value, String shortCut) {
            this.display = display;
            this.value = value;
            this.shortCut = shortCut;
        }
    }

    private String name;
    private String linkToSubject;
    private Room room;
    private int from; // hour of beggining: 8, 9, 10, ...
    private int to;
    private Day day;
    private String color;

    private boolean[] weeks = {false, false, false, false, false, false, false, false, false, false, false, false, false}; // weeks when event happens

    public Subject(String name) {
        this.name = name;
    }

    public Subject(String name, String link, String room, int from, int to, String day, String color) {
        this.name = name;
        this.linkToSubject = link;
        this.room = new Room(room.toUpperCase());
        this.from = from;
        this.to = to;
        setDay(day);
        this.color = color;
    }

    /**
     * Downloads file with subject card and extracts its mentoring weeks if
     * there is some special events listed in table with color #ffffdc which
     * means something really special like an exam, so we will check this events
     * already exists, if not, we will create it.
     * @throws java.text.ParseException
     */
    public void setWeeksOfMentoring() throws ParseException {
        try {
            Document doc = Jsoup.parse(Downloader.download(linkToSubject, Strings.SUBJECT_CARD_FILE), "ISO-8859-2");
            if (doc.select("table").isEmpty()) { // there is no table with info, so it is mentioned for all weeks
                for (int i = 0; i < 13; i++) {
                    weeks[i] = true;
                }
                return;
            }
            Elements rows = doc.selectFirst("table").selectFirst("tbody").select("tr");

            if(doc.select("table").size() > 1) { //there are other tables
                for (Element e : doc.select("table")) {
                    if(!e.select("th").isEmpty()) { // last often contains lectures timetable
                        rows = e.selectFirst("tbody").select("tr");
                    }
                }
            }

            for (Element e : rows) {
                if (e.select("th").isEmpty()) { // skip if there is no th, which significant day
                    continue;
                }
                if (e.selectFirst("th").text().equalsIgnoreCase(day.shortCut())
                        && // select day
                        Integer.parseInt(e.select("td").get(3).text().split(":")[0]) == from
                        && //select beginning time
                        Integer.parseInt(e.select("td").get(4).text().split(":")[0]) + 1 == to) {
                    // it is same subject as in table, so we extract the lectures weeks
                    String lectures = e.select("td").get(1).text(); // extract lectures
                    if (lectures.equals(Strings.ALL_SEMESTER_EDUCATION)) {
                        for (int i = 0; i < 13; i++) {
                            weeks[i] = true;
                        }
                    } else { // extract some weeks
                        String weeksOfMentioning = lectures.split(" of")[0]; // we can see: 13. of lectures (in tables)
                        String weekByWeek[] = {weeksOfMentioning, ""};
                        if (weeksOfMentioning.contains(",")) { // if it contains "," there is more then 1 week of lecture
                            weekByWeek = weeksOfMentioning.split(","); // so split the string
                            for (int i = 0; i < weekByWeek.length; i++) {
                                // iterate through but ignore the last one because there is nothing for us ;)
                                int numberOfWeek = Integer.parseInt(weekByWeek[i].trim().split("\\.")[0]); // from 13. we want just 13
                                weeks[numberOfWeek - 1] = true; // it is mentioned this week
                            }
                        } else {
                            if (weeksOfMentioning.contains("-")) { //it is simple date in format YYYY-MM-DD
                                SimpleDateFormat formatter = new SimpleDateFormat(Strings.DATE_FORMAT_YYYY_MM_DD);
                                Date date = formatter.parse(weekByWeek[0].trim());
                                int index = SubjectManager.get().getWeekOfSemester(date);
                                if(index > -1 && index < 13) {
                                    weeks[index] = true;
                                }
                            } else {
                                for (int i = 0; i + 1 < weekByWeek.length; i++) {
                                    // iterate through but ignore the last one because there is nothing for us ;)
                                    int numberOfWeek = Integer.parseInt(weekByWeek[i].trim().split("\\.")[0]); // from 13. we want just 13
                                    weeks[numberOfWeek - 1] = true; // it is mentioned this week
                                }
                            }
                        }
                    }
                } else { // it is not that subject, it can be special, so check it
                    if (e.attr("style").contains("#ffffdc")) { // it is special event
                        Subject specialSubject = new Subject(
                                this.name,
                                this.linkToSubject,
                                this.room.getName(),
                                Integer.parseInt(e.select("td").get(3).text().split(":")[0]),
                                Integer.parseInt(e.select("td").get(4).text().split(":")[0]) + 1,
                                e.selectFirst("th").text(),
                                "#ffffdc");
                        SubjectManager manager = SubjectManager.get();
                        if (!manager.contains(specialSubject)) {
                            manager.addSubject(specialSubject);
                            specialSubject.setWeeksOfMentoring();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Subject.class.getName()).log(Level.SEVERE, null, ex);
            // it will be mentored each week
        }
    }

    /**
     * returns string with semicoloned weeks
     * Only for toString()
     *
     * @return
     */
    public String semicolonedWeeks() {
        String list = "";
        for (int i = 0; i < 12; i++) {
            list += weeks[i] + ",";
        }
        list += weeks[12];
        return list;
    }

    /**
     *
     * @param week number of week (from 1 to 13)
     * @return
     */
    public boolean isMentioned(int week) {
        if(week < 1 || week > 13) {
            return false;
        }
        return weeks[week - 1];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLinkToSubject() {
        return linkToSubject;
    }

    public void setLinkToSubject(String linkToSubject) {
        this.linkToSubject = linkToSubject;
    }

    @Override
    public String toString() {
        return name + "|" + linkToSubject + "|" + room.getName() + "|" + from + "|" + to + "|" + day.shortCut() + "|" + color + "|" + semicolonedWeeks();
    }

    public void setDay(String day) {
        if (day.length() > 3) {
            day = day.toLowerCase().substring(0, 3);
        } else {
            day = day.toLowerCase();
        }
        switch (day) {
            case "mon":
            case "po":
                setDay(Day.MONDAY);
                break;
            case "tue":
            case "út":
                setDay(Day.TUESDAY);
                break;
            case "wed":
            case "st":
                setDay(Day.WEDNESDAY);
                break;
            case "thu":
            case "čt":
                setDay(Day.THURSTDAY);
                break;
            case "fri":
            case "pá":
                setDay(Day.FRIDAY);
                break;
            default:
                throw new IllegalArgumentException(day + " is not recognized as day. We support: Mon, Tue, Wed, Thu or Fri");
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + this.from;
        hash = 41 * hash + this.to;
        hash = 41 * hash + Objects.hashCode(this.day);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Subject other = (Subject) obj;
        if (this.from != other.from) {
            return false;
        }
        if (this.to != other.to) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.day != other.day) {
            return false;
        }
        return true;
    }

}
